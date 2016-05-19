package org.powerassert;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import com.sun.source.tree.AssertTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

@SupportedAnnotationTypes("*")
public class DebugProcessor extends AbstractProcessor {
	private Trees trees;
	private Context context;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		if(!isInitialized()) {
			super.init(processingEnv);
		}
		this.trees = Trees.instance(processingEnv);
		this.context = ((JavacProcessingEnvironment) processingEnv).getContext();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if(!roundEnv.processingOver()) {
			for(Element element: roundEnv.getRootElements()) {
				TreePath path = trees.getPath(element);
				new DebugScanner().scan(path, context);
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

class DebugScanner extends TreePathScanner<TreePath, Context> {
	TreeMaker treeMaker;
	JavacElements elements;

	@Override
	public TreePath scan(TreePath path, Context context) {
		treeMaker = TreeMaker.instance(context);
		elements = JavacElements.instance(context);
		return super.scan(path, context);
	}

	@Override
	public TreePath visitVariable(VariableTree node, Context context) {
		JCTree.JCVariableDecl decl = (JCTree.JCVariableDecl) node;

		JCTree.JCVariableDecl replacement = treeMaker.VarDef(
			decl.getModifiers(),
			elements.getName("a"), //decl.getName(),
			decl.vartype,
			decl.getInitializer()
		);
		replacement.setPos(decl.pos);

		JCTree.JCVariableDecl prepend = treeMaker.VarDef(
				decl.getModifiers(),
				elements.getName("b"), //decl.getName(),
				decl.vartype,
				decl.getInitializer()
		);

		JCTree.JCBlock block = (JCTree.JCBlock) getCurrentPath().getParentPath().getLeaf();
		block.stats = replaceStatement(block.stats, decl, replacement);
		block.stats = block.stats.prepend(prepend);

		return null;
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