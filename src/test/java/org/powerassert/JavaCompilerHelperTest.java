package org.powerassert;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class JavaCompilerHelperTest {

	@Test
	public void compile() throws Exception {
		JavaCompilerHelper java = new JavaCompilerHelper();
		java.compile("public class A {}");
		java.compile("public class B { A a = new A(); }");
	}

	@Test
	public void fullyQualifiedName() throws Exception {
		assertThat(JavaCompilerHelper.fullyQualifiedName("package myorg.a; public class A {}"))
				.isEqualTo("myorg.a.A");

		assertThat(JavaCompilerHelper.fullyQualifiedName("package myorg.a; public interface A {}"))
				.isEqualTo("myorg.a.A");
	}
}