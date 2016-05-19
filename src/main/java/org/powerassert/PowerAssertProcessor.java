package org.powerassert;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import com.sun.source.tree.AssertTree;
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

@SupportedAnnotationTypes("*")
public class PowerAssertProcessor extends AbstractProcessor {
	private Trees trees;
	private Context context;
	private Messager messager;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		if(!isInitialized()) {
			super.init(processingEnv);
		}
		this.trees = Trees.instance(processingEnv);
		this.context = ((JavacProcessingEnvironment) processingEnv).getContext();
		this.messager = processingEnv.getMessager();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if(!roundEnv.processingOver()) {
			for(Element element: roundEnv.getRootElements()) {
				try {
					CharSequence source = ((Symbol.ClassSymbol) element).sourcefile.getCharContent(true);
					TreePath path = trees.getPath(element);
					new PowerAssertScanner(source, messager).scan(path, context);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return false;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		// the latest version of whatever JDK we run on
		return SourceVersion.values()[SourceVersion.values().length - 1];
	}
}

class PowerAssertScanner extends TreePathScanner<TreePath, Context> {
	TreeMaker treeMaker;
	JavacElements elements;
	Messager messager;
	CharSequence rawSource;

	public PowerAssertScanner(CharSequence rawSource, Messager messager) {
		this.rawSource = rawSource;
		this.messager = messager;
	}

	@Override
	public TreePath scan(TreePath path, Context context) {
		treeMaker = TreeMaker.instance(context);
		elements = JavacElements.instance(context);
		return super.scan(path, context);
	}

	@Override
	public TreePath visitAssert(AssertTree node, Context context) {
		JCTree.JCAssert assertNode = (JCTree.JCAssert) node;

		JCTree.JCExpression powerAssertType = qualifiedName("org", "powerassert", "synthetic", "PowerAssert");
		JCTree.JCExpression recorderRuntimeType = qualifiedName("org", "powerassert", "synthetic", "RecorderRuntime");

		JCTree.JCVariableDecl powerAssert = treeMaker.VarDef(
				treeMaker.Modifiers(Flags.FINAL),
				name("$org_powerassert_powerAssert"), // name that likely won't collide with any other
				powerAssertType,
				treeMaker.NewClass(null, List.<JCTree.JCExpression>nil(), powerAssertType,
						List.<JCTree.JCExpression>nil(), null)
		);

		JCTree.JCVariableDecl recorderRuntime = treeMaker.VarDef(
				treeMaker.Modifiers(Flags.FINAL),
				name("$org_powerassert_recorderRuntime"), // name that likely won't collide with any other
				recorderRuntimeType,
				treeMaker.NewClass(null, List.<JCTree.JCExpression>nil(), recorderRuntimeType,
						List.<JCTree.JCExpression>of(
							treeMaker.Apply(List.<JCTree.JCExpression>nil(),
									qualifiedName("$org_powerassert_powerAssert", "getListener"),
									List.<JCTree.JCExpression>nil()
							)
						),
						null)
		);

		JCTree.JCExpression instrumented = recordAllValues(assertNode.getCondition());

		JCTree.JCExpressionStatement recordExpr = treeMaker.Exec(
				treeMaker.Apply(
						List.<JCTree.JCExpression>nil(),
						qualifiedName("$org_powerassert_recorderRuntime", "recordExpression"),
						List.of(
							treeMaker.Literal(source(assertNode.getCondition())),
							instrumented,
							treeMaker.Literal(assertNode.getCondition().getStartPosition())
						)
				)
		);
		recordExpr.setPos(assertNode.getCondition().pos);

		JCTree.JCBlock powerAssertBlock = treeMaker.Block(0, List.of(
				powerAssert,
				recorderRuntime,
				recordExpr,
				completeRecording(),
				(JCTree.JCStatement) node)
		);

		JCTree.JCBlock parent = (JCTree.JCBlock) getCurrentPath().getParentPath().getLeaf();
		parent.stats = replaceStatement(parent.getStatements(), (JCTree.JCAssert) node, powerAssertBlock);

		return null; // no need to call super because we are visiting the assert condition manually
	}

	public JCTree.JCExpression recordAllValues(JCTree.JCExpression expr) {
		if(expr instanceof JCTree.JCBinary) {
			JCTree.JCBinary binary = (JCTree.JCBinary) expr;
			return recordValue(
					treeMaker.Binary(
							binary.getTag(),
							recordAllValues(binary.getLeftOperand()),
							recordAllValues(binary.getRightOperand())
					).setPos(binary.pos),
					binary.getRightOperand().pos - 2
			);
		}
		else if(expr instanceof JCTree.JCUnary) {
			JCTree.JCUnary unary = (JCTree.JCUnary) expr;
			return recordValue(
					treeMaker.Unary(
						unary.getTag(),
						recordAllValues(unary.getExpression())
					).setPos(unary.pos),
					unary.getExpression().pos - 1
			);
		}
		else if(expr instanceof JCTree.JCMethodInvocation) {
			JCTree.JCMethodInvocation method = (JCTree.JCMethodInvocation) expr;
			return recordValue(
					// oddly, methodSelect is expressed as a JCFieldAccess, and if we recurse through this expression,
					// we will attempt to record the method name as a field access, which it is not... so do
					// not recurse on method select
					treeMaker.Apply(
							method.typeargs,
							method.getMethodSelect(),
							recordArgs(method.args)
					).setPos(method.pos),
					method.getMethodSelect().pos + 1
			);
		}
		else if(expr instanceof JCTree.JCIdent) {
			String name = ((JCTree.JCIdent) expr).getName().toString();

			// differentiate between class name identifiers and variable identifiers
			if(elements.getTypeElement(name) == null && elements.getTypeElement("java.lang." + name) == null) {
				return recordValue(expr, expr.pos);
			}
			return expr;
		}
		else if(expr instanceof JCTree.JCFieldAccess) {
			JCTree.JCFieldAccess field = (JCTree.JCFieldAccess) expr;
			if(!(field.selected instanceof JCTree.JCLiteral)) {
				return recordValue(
						treeMaker.Select(
								recordAllValues(field.getExpression()),
								field.name
						).setPos(field.pos),
						expr.pos + 1);
			}
			return expr;
		}
		else if(expr instanceof JCTree.JCNewClass) {
			JCTree.JCNewClass newClass = (JCTree.JCNewClass) expr;
			return treeMaker.NewClass(
					recordAllValues(newClass.encl),
					newClass.typeargs,
					newClass.clazz,
					recordArgs(newClass.args),
					newClass.def
			).setPos(newClass.pos);
		}
		else if(expr instanceof JCTree.JCArrayAccess) {
			JCTree.JCArrayAccess arrayAccess = (JCTree.JCArrayAccess) expr;
			return recordValue(
					treeMaker.Indexed(
						recordAllValues(arrayAccess.getExpression()),
						recordAllValues(arrayAccess.getIndex())
					).setPos(arrayAccess.pos),
					expr.pos
			);
		}
		else if(expr instanceof JCTree.JCNewArray) {
			JCTree.JCNewArray newArray = (JCTree.JCNewArray) expr;
			return treeMaker.NewArray(
					recordAllValues(newArray.getType()),
					recordArgs(newArray.getDimensions()),
					recordArgs(newArray.getInitializers())
			).setPos(newArray.pos);
		}
		return expr;
	}

	private JCTree.JCExpression recordValue(JCTree.JCExpression expr, int anchor) {
		return treeMaker.Apply(
			List.<JCTree.JCExpression>nil(),
			qualifiedName("$org_powerassert_recorderRuntime", "recordValue"),
			List.of(expr, treeMaker.Literal(anchor))
		);
	}

	private JCTree.JCExpressionStatement completeRecording() {
		return treeMaker.Exec(
				treeMaker.Apply(
					List.<JCTree.JCExpression>nil(),
					qualifiedName("$org_powerassert_recorderRuntime", "completeRecording"),
					List.<JCTree.JCExpression>nil()
				)
		);
	}

	private JCTree.JCExpression qualifiedName(String... name) {
		JCTree.JCExpression prior = treeMaker.Ident(elements.getName(name[0]));
		for(int i = 1; i < name.length; i++) {
			prior = treeMaker.Select(prior, elements.getName(name[i]));
		}
		return prior;
	}

	private Name name(String name) {
		return elements.getName(name);
	}

	/**
	 * For debugging purposes only
	 */
	@SuppressWarnings("unused")
	private JCTree.JCStatement debugPrint(JCTree.JCExpression expr) {
		return treeMaker.Exec(
				treeMaker.Apply(List.<JCTree.JCExpression>nil(),
					qualifiedName("System", "out", "println"),
					List.of(expr)
				)
		);
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

	private List<JCTree.JCExpression> recordArgs(List<JCTree.JCExpression> args) {
		JCTree.JCExpression[] recordedArgs = new JCTree.JCExpression[args.length()];
		for(int i = 0; i < args.length(); i++) {
			recordedArgs[i] = recordAllValues(args.get(i));
		}
		return List.from(recordedArgs);
	}
}