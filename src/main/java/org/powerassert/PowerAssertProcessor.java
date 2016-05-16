package org.powerassert;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
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

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.trees = Trees.instance(processingEnv);
		this.context = ((JavacProcessingEnvironment) processingEnv).getContext();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if(!roundEnv.processingOver()) {
			for(Element element: roundEnv.getRootElements()) {
				TreePath path = trees.getPath(element);
				new PowerAssertScanner().scan(path, context);
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

	@Override
	public TreePath scan(TreePath path, Context context) {
		treeMaker = TreeMaker.instance(context);
		elements = JavacElements.instance(context);
		return super.scan(path, context);
	}

	@Override
	public TreePath visitAssert(AssertTree node, Context context) {
		JCTree.JCAssert assertNode = (JCTree.JCAssert) node;

		JCTree.JCExpression powerAssertType = qualifiedName("org", "powerassert", "PowerAssert");
		JCTree.JCExpression recorderRuntimeType = qualifiedName("org", "powerassert", "RecorderRuntime");

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
							treeMaker.Literal(assertNode.getCondition().toString()),
							instrumented,
							treeMaker.Literal(assertNode.getCondition().getStartPosition())
						)
				)
		);

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
			return treeMaker.Binary(
					binary.getTag(),
					recordValue(binary.getLeftOperand()),
					recordValue(binary.getRightOperand())
			);
		}
		else if(expr instanceof JCTree.JCUnary) {
			// TODO implement me!
		}
		else if(expr instanceof JCTree.JCTypeApply) {
			// TODO implement me!
		}
		return expr;
	}

	private JCTree.JCExpression recordValue(JCTree.JCExpression expr) {
		return treeMaker.Apply(
			List.<JCTree.JCExpression>nil(),
			qualifiedName("$org_powerassert_recorderRuntime", "recordValue"),
			List.of(expr, treeMaker.Literal(anchor(expr)))
		);
	}

	private Integer anchor(JCTree.JCExpression expr) {
		if(expr instanceof JCTree.JCMethodInvocation) {
			return anchor(((JCTree.JCMethodInvocation) expr).getMethodSelect());
		}
		return expr.getStartPosition(); // this is an absolute position that will be later relativized
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
}