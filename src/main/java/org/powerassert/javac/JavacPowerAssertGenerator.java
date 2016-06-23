package org.powerassert.javac;

import com.sun.source.tree.AssertTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import org.powerassert.PowerAssertGenerator;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Arrays;

public class JavacPowerAssertGenerator extends TreePathScanner<TreePath, Context> implements PowerAssertGenerator {
	// name that likely won't collide with any other
	private static final String RECORDER_RUNTIME = "$rr";
	private static final Integer MAX_JAVA8_DEPTH = 3;

	private TreeMaker treeMaker;
	private JavacElements elements;
	private CharSequence rawSource;

	private Trees trees;
	private Context context;
	private Messager messager;

	private boolean limitDepthOnJava8;

	public JavacPowerAssertGenerator(boolean limitDepthOnJava8) {
		this.limitDepthOnJava8 = limitDepthOnJava8;
	}

	@Override
	public void init(ProcessingEnvironment env) {
		this.trees = Trees.instance(env);
		this.context = ((JavacProcessingEnvironment) env).getContext();
		this.messager = env.getMessager();
	}

	@Override
	public boolean scan(Element element) {
		try {
			this.rawSource = ((Symbol.ClassSymbol) element).sourcefile.getCharContent(true);
			TreePath path = trees.getPath(element);

			treeMaker = TreeMaker.instance(context);
			elements = JavacElements.instance(context);

			scan(path, context);

			return true;
		} catch(IOException e) {
			messager.printMessage(Diagnostic.Kind.WARNING,
					"Unable to find raw source for element " + element.getSimpleName().toString());
		} catch(NoClassDefFoundError ignored) {
			messager.printMessage(Diagnostic.Kind.WARNING,
					"Unable to generate power assertions because javac is not compiling the code");
		}
		return false;
	}

	private static java.util.List<String> junitAsserts = Arrays.asList("assertEquals", "assertNotEquals",
			"assertArrayEquals", "assertTrue", "assertFalse", "assertSame", "assertNotSame", "assertNull",
			"assertNotNull");

	@Override
	public TreePath visitMethodInvocation(MethodInvocationTree node, Context context) {
		JCTree.JCMethodInvocation meth = (JCTree.JCMethodInvocation) node;
		Tree parent = getCurrentPath().getParentPath().getLeaf();

		if(parent instanceof JCTree.JCStatement) {
			JCTree.JCStatement statement = (JCTree.JCStatement) parent;
			String methodName = methodName(meth);

			if(junitAsserts.contains(methodName)) {
				JavacExpressionRecorder expressionRecorder = new JavacExpressionRecorder();

				JCTree.JCExpression recorded = treeMaker.Apply(
						List.<JCTree.JCExpression>nil(),
						qualifiedName("org", "powerassert", "synthetic", "junit", "Assert", methodName),
						expressionRecorder.record(meth.getArguments())
				);

				JCTree.JCExpressionStatement instrumented = treeMaker.Exec(
						treeMaker.Apply(
								List.<JCTree.JCExpression>nil(),
								qualifiedName(RECORDER_RUNTIME, "powerAssert"),
								List.of(
										treeMaker.Literal(source(meth)),
										recorded,
										treeMaker.Literal(meth.getStartPosition())
								)
						)
				);

				// so that we don't disrupt IDE debugging, give the instrumented expression the same position as the original
				instrumented.setPos(statement.pos);

				return replaceWithInstrumented(statement, instrumented);
			}
			else if("assertThat".equals(methodName)) {
				JavacExpressionRecorder expressionRecorder = new JavacExpressionRecorder();

				JCTree.JCExpression recorded = treeMaker.Apply(
						List.<JCTree.JCExpression>nil(),
						qualifiedName("org", "powerassert", "synthetic", "hamcrest", "MatcherAssert", methodName),
						expressionRecorder.record(meth.getArguments())
				);

				JCTree.JCExpressionStatement instrumented = treeMaker.Exec(
						treeMaker.Apply(
								List.<JCTree.JCExpression>nil(),
								qualifiedName(RECORDER_RUNTIME, "powerAssert"),
								List.of(
										treeMaker.Literal(source(meth)),
										recorded,
										treeMaker.Literal(meth.getStartPosition())
								)
						)
				);

				// so that we don't disrupt IDE debugging, give the instrumented expression the same position as the original
				instrumented.setPos(statement.pos);

				return replaceWithInstrumented(statement, instrumented);
			}
		}

		return super.visitMethodInvocation(node, context);
	}

	private static String methodName(JCTree.JCMethodInvocation meth) {
		JCTree.JCExpression methodSelect = meth.getMethodSelect();
		if(methodSelect instanceof JCTree.JCIdent) {
			return ((JCTree.JCIdent) methodSelect).name.toString();
		}
		else if(methodSelect instanceof JCTree.JCFieldAccess) {
			return ((JCTree.JCFieldAccess) methodSelect).name.toString();
		}
		return null;
	}

	@Override
	public TreePath visitAssert(AssertTree node, Context context) {
		JavacExpressionRecorder expressionRecorder = new JavacExpressionRecorder();

		JCTree.JCAssert assertStatement = (JCTree.JCAssert) node;
		JCTree.JCExpression assertCondition = assertStatement.getCondition();

		JCTree.JCExpressionStatement instrumented = treeMaker.Exec(
				treeMaker.Apply(
						List.<JCTree.JCExpression>nil(),
						qualifiedName(RECORDER_RUNTIME, "powerAssert"),
						List.of(
								treeMaker.Literal(source(assertCondition)),
								expressionRecorder.record(assertCondition),
								treeMaker.Literal(assertCondition.getStartPosition())
						)
				)
		);

		// so that we don't disrupt IDE debugging, give the instrumented expression the same position as the original
		instrumented.setPos(assertCondition.pos);

		return replaceWithInstrumented(assertStatement, instrumented);
	}

	private TreePath replaceWithInstrumented(JCTree.JCStatement statement, JCTree.JCExpressionStatement instrumented) {
		TreePath parent = getCurrentPath().getParentPath();
		while(parent != null && !(parent.getLeaf() instanceof JCTree.JCBlock)) {
			parent = parent.getParentPath();
		}

		if(parent == null) {
			// TODO is this case possible?
			return null;
		}

		JCTree.JCExpression recorderRuntimeType = qualifiedName("org", "powerassert", "synthetic", "RecorderRuntime");
		JCTree.JCVariableDecl recorderRuntime = treeMaker.VarDef(
				treeMaker.Modifiers(Flags.FINAL),
				elements.getName(RECORDER_RUNTIME),
				recorderRuntimeType,
				treeMaker.NewClass(null, List.<JCTree.JCExpression>nil(), recorderRuntimeType, List.<JCTree.JCExpression>nil(), null)
		);

		JCTree.JCBlock containingBlock = (JCTree.JCBlock) parent.getLeaf();
		JCTree.JCBlock powerAssertBlock = treeMaker.Block(0, List.of(recorderRuntime, instrumented));
		containingBlock.stats = replaceStatement(containingBlock.getStatements(), statement, powerAssertBlock);

		return null;
	}

	private JCTree.JCExpression qualifiedName(String... name) {
		JCTree.JCExpression prior = treeMaker.Ident(elements.getName(name[0]));
		for(int i = 1; i < name.length; i++) {
			prior = treeMaker.Select(prior, elements.getName(name[i]));
		}
		return prior;
	}

	private List<JCTree.JCStatement> replaceStatement(List<JCTree.JCStatement> list, JCTree.JCStatement replace, JCTree.JCStatement with) {
		JCTree.JCStatement[] stats = list.toArray(new JCTree.JCStatement[list.size()]);
		for(int i = 0; i < stats.length; i++) {
			if(stats[i] == replace) {
				stats[i] = with;
				break;
			}
		}
		return List.from(stats);
	}

	/**
	 * @return the raw source of expr, extracted from the raw source itself since JCExpression's toString()
	 * normalizes whitespace but positions still refer to the position in source prior to this normalization
	 */
	private String source(JCTree.JCExpression expr) {
		String exprStr = expr.toString();
		int sourcePos = expr.getStartPosition();

		for(int exprPos = 0; exprPos < exprStr.length();) {
			char exprChar = exprStr.charAt(exprPos);
			char sourceChar = rawSource.charAt(sourcePos);

			if(Character.isWhitespace(exprChar)) {
				exprPos++;
				continue;
			}
			if(Character.isWhitespace(sourceChar)) {
				sourcePos++;
				continue;
			}
			exprPos++;
			sourcePos++;
		}

		return rawSource.subSequence(expr.getStartPosition(), sourcePos).toString();
	}

	private class JavacExpressionRecorder {
		JCTree.JCExpression record(JCTree.JCExpression expr) {
			return record(expr, null, 0);
		}

		List<JCTree.JCExpression> record(List<JCTree.JCExpression> args) {
			return record(args, null, 0);
		}

		private JCTree.JCExpression record(JCTree.JCExpression expr, JCTree.JCExpression parent, int recordingDepth) {
			if (expr == null) {
				return null;
			} else if (expr instanceof JCTree.JCBinary) {
				JCTree.JCBinary binary = (JCTree.JCBinary) expr;
				return injectRecordValue(
						treeMaker.Binary(
								binary.getTag(),
								record(binary.getLeftOperand(), expr, recordingDepth + 1),
								record(binary.getRightOperand(), expr, recordingDepth + 1)
						).setPos(binary.pos),
						binary.getRightOperand().pos - 2,
						recordingDepth
				);
			} else if (expr instanceof JCTree.JCUnary) {
				JCTree.JCUnary unary = (JCTree.JCUnary) expr;
				return injectRecordValue(
						treeMaker.Unary(
								unary.getTag(),
								record(unary.getExpression(), expr, recordingDepth + 1)
						).setPos(unary.pos),
						unary.getExpression().pos - 1,
						recordingDepth
				);
			} else if (expr instanceof JCTree.JCMethodInvocation) {
				JCTree.JCMethodInvocation method = (JCTree.JCMethodInvocation) expr;
				return injectRecordValue(
						treeMaker.Apply(
								method.typeargs,
								record(method.getMethodSelect(), expr, recordingDepth + 1),
								record(method.args, expr, recordingDepth + 1)
						).setPos(method.pos),
						method.getMethodSelect().pos + 1,
						recordingDepth
				);
			} else if (expr instanceof JCTree.JCIdent) {
				Name name = ((JCTree.JCIdent) expr).getName();
				if (!isType(name) && !isPartOfMethodName(expr, parent)) {
					return injectRecordValue(expr, expr.pos, recordingDepth);
				}
				return expr;
			} else if (expr instanceof JCTree.JCFieldAccess) {
				JCTree.JCFieldAccess field = (JCTree.JCFieldAccess) expr;
				if (!(field.selected instanceof JCTree.JCLiteral)) {
					JCTree.JCExpression recordedField = treeMaker.Select(
							record(field.getExpression(), expr, recordingDepth + 1),
							field.name
					).setPos(field.pos);

					if (isPartOfMethodName(expr, parent)) {
						// when the parent is a method invocation, this is not a true "field access", so don't attempt
						// to record the value... instead the recording of the result of the method invocation will capture
						// its output
						return recordedField;
					} else {
						return injectRecordValue(
								recordedField,
								expr.pos + 1,
								recordingDepth);
					}
				}
				return expr;
			} else if (expr instanceof JCTree.JCNewClass) {
				JCTree.JCNewClass newClass = (JCTree.JCNewClass) expr;
				return treeMaker.NewClass(
						record(newClass.encl, expr, recordingDepth + 1),
						newClass.typeargs,
						newClass.clazz,
						record(newClass.args, expr, recordingDepth + 1),
						newClass.def
				).setPos(newClass.pos);
			} else if (expr instanceof JCTree.JCArrayAccess) {
				JCTree.JCArrayAccess arrayAccess = (JCTree.JCArrayAccess) expr;
				return injectRecordValue(
						treeMaker.Indexed(
								record(arrayAccess.getExpression(), expr, recordingDepth + 1),
								record(arrayAccess.getIndex(), expr, recordingDepth + 1)
						).setPos(arrayAccess.pos),
						expr.pos,
						recordingDepth
				);
			} else if (expr instanceof JCTree.JCNewArray) {
				JCTree.JCNewArray newArray = (JCTree.JCNewArray) expr;
				return treeMaker.NewArray(
						record(newArray.getType(), expr, recordingDepth + 1),
						record(newArray.getDimensions(), expr, recordingDepth + 1),
						record(newArray.getInitializers(), expr, recordingDepth + 1)
				).setPos(newArray.pos);
			} else if (expr instanceof JCTree.JCConditional) {
				JCTree.JCConditional conditional = (JCTree.JCConditional) expr;
				return injectRecordValue(
						treeMaker.Conditional(
								record(conditional.getCondition(), expr, recordingDepth + 1),
								record(conditional.getTrueExpression(), expr, recordingDepth + 1),
								record(conditional.getFalseExpression(), expr, recordingDepth + 1)
						).setPos(conditional.pos),
						expr.pos,
						recordingDepth
				);
			}
			return expr;
		}

		private boolean isType(Name name) {
			JCTree.JCCompilationUnit cu = (JCTree.JCCompilationUnit) getCurrentPath().getCompilationUnit();
			return cu.starImportScope.getElementsByName(name).iterator().hasNext() ||
					cu.namedImportScope.getElementsByName(name).iterator().hasNext() ||
					elements.getTypeElement(name.toString()) != null ||
					(cu.getPackageName() != null && elements.getTypeElement(cu.getPackageName().toString() + "." + name.toString()) != null);
		}

		/**
		 * Wrap the original expression in a call to RecorderRuntime.recordValue
		 * @param expr - the expression to record
		 * @param anchor - the starting position of this expression
		 * @return a wrapped expression
		 */
		private JCTree.JCExpression injectRecordValue(JCTree.JCExpression expr, int anchor, int recordingDepth) {
			if(limitDepthOnJava8 && System.getProperty("java.version").startsWith("1.8") && recordingDepth > MAX_JAVA8_DEPTH) {
				return expr;
			}

			return treeMaker.Apply(
					List.<JCTree.JCExpression>nil(),
					qualifiedName(RECORDER_RUNTIME, "recordValue"),
					List.of(expr, treeMaker.Literal(anchor))
			);
		}

		private boolean isPartOfMethodName(JCTree.JCExpression expr, JCTree.JCExpression parent) {
			boolean isPartOfMethodName = parent instanceof JCTree.JCMethodInvocation;
			if(isPartOfMethodName) {
				for (JCTree.JCExpression arg : ((JCTree.JCMethodInvocation) parent).args) {
					if(expr == arg)
						return false;
				}
			}
			return isPartOfMethodName;
		}

		private List<JCTree.JCExpression> record(List<JCTree.JCExpression> args, JCTree.JCExpression parent, int recordingDepth) {
			JCTree.JCExpression[] recordedArgs = new JCTree.JCExpression[args.length()];
			for(int i = 0; i < args.length(); i++) {
				recordedArgs[i] = record(args.get(i), parent, recordingDepth + 1);
			}
			return List.from(recordedArgs);
		}
	}
}