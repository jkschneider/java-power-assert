package org.powerassert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class PowerAssertProcessorTest {
	JavaCompilerHelper java;

	@Before
	public void setup() {
		java = new JavaCompilerHelper(new PowerAssertProcessor());
	}

	@Test
	public void identifiers() {
		java.compile(
				"public class A {" +
				"	@org.junit.Test public void test() {" +
				"      String a = \"abc\";" +
				"      assert a == \"def\";" +
				"	}" +
				"}");

		testFailsWithMessage("A", "test",
				"a == \"def\"",
				"|  |",
				"|  false",
				"abc");
	}

	@Test
	public void binaryExpression() {
		java.compile(
				"public class A {" +
				"	@org.junit.Test public void test() {" +
				"      assert 1+ 1 == 3;" +
				"	}" +
				"}");

		testFailsWithMessage("A", "test",
				"1+ 1 == 3", // notice how whitespace is preserved in the output
				" |    |",
				" 2    false");
	}

	@Test
	public void unaryExpression() {
		java.compile(
				"public class A {" +
				"	@org.junit.Test public void test() {" +
				"      assert !true;" +
				"	}" +
				"}");

		testFailsWithMessage("A", "test",
				"!true",
				"|",
				"false");
	}

	@Test
	public void methodInvocation() {
		java.compile(
				"public class A {" +
				"	@org.junit.Test public void test() {" +
				"      assert \"abc\".contains(\"d\");" +
				"	}" +
				"}");

		testFailsWithMessage("A", "test",
				"\"abc\".contains(\"d\")",
				"      |",
				"      false");
	}

	@Test
	public void methodInvocationInArguments() {
		java.compile(
				"public class A {" +
				"	@org.junit.Test public void test() {" +
				"      assert Character.isWhitespace(\"abc\".charAt(0));" +
				"	}" +
				"}");

		testFailsWithMessage("A", "test",
				"Character.isWhitespace(\"abc\".charAt(0))",
				"          |                  |",
				"          false              a");
	}

	@Test
	public void nullValues() {
		java.compile(
				"public class A {" +
						"	@org.junit.Test public void test() {" +
						"      String a = null;" +
						"      assert a == \"null\";" +
						"	}" +
						"}");

		testFailsWithMessage("A", "test",
				"a == \"null\"",
				"|  |",
				"|  false",
				"null");
	}

	@Test
	public void propertyRead() {
		java.compile(
			"public class Data {" +
			"   public String field = \"abc\";" +
			"}");

		java.compile(
				"public class A {" +
				"	@org.junit.Test public void test() {" +
				"      Data d = new Data();" +
				"      assert d.field == \"def\";" +
				"	}" +
				"}");

		testFailsWithMessage("A", "test",
				"d.field == \"def\"",
				"| |      |",
				"| abc    false",
				"Data[field=abc]");
	}

	@Test
	public void constructorCall() {
		java.compile(
				"public class Data {" +
				"   public String field;" +
				"   public Data(String field) { this.field = field; }" +
				"}");

		java.compile(
				"public class A {" +
				"	@org.junit.Test public void test() {" +
				"      String a = \"abc\";" +
				"      assert new Data(a).field == \"def\";" +
				"	}" +
				"}");

		testFailsWithMessage("A", "test",
				"new Data(a).field == \"def\"",
				"         |  |      |",
				"         |  abc    false",
				"         abc");
	}

	@Test
	public void arrayAccess() {
		java.compile(
				"public class A {" +
				"	@org.junit.Test public void test() {" +
				"      int n[] = new int[] { 0, 1, 2 };" +
				"      assert n[0] == 1;" +
				"	}" +
				"}");

		testFailsWithMessage("A", "test",
				"n[0] == 1",
				"||    |",
				"|0    false",
				"[0, 1, 2]");
	}

	@Test
	public void newArray() {
		java.compile(
				"public class A {" +
				"	@org.junit.Test public void test() {" +
				"      int i = 1;" +
				"      assert new int[] { i }[0] == 2;" + // extraordinarily contrived, I know...
				"	}" +
				"}");

		testFailsWithMessage("A", "test",
				"new int[] { i }[0] == 2",
				"            |  |    |",
				"            1  1    false");
	}

	@Ignore
	@Test
	public void ternary() {
		// TODO
	}

	@Test
	public void nestedCalls() {
		java.compile(
				"public class A {\n" +
				"	private <T> T ident(T t) {\n" +
				"		return t;\n" +
				"	}\n" +
				"	@org.junit.Test public void test() {\n" +
				"		String a = \"a\";\n" +
				"		assert ident(a.substring(0)) == \"bb\";\n" +
				"	}\n" +
				"}\n");

		testFailsWithMessage("A", "test",
				"ident(a.substring(0)) == \"bb\"",
				" |    | |              |",
				" a    a a              false");
	}

	@Test
	public void chainedMethodCalls() {
		java.compile(
				"public class A {" +
				"	@org.junit.Test public void test() {" +
				"      assert \"abc\".substring(0).contains(\"d\");" +
				"	}" +
				"}");

		testFailsWithMessage("A", "test",
				"\"abc\".substring(0).contains(\"d\")",
				"      |            |",
				"      abc          false");
	}

	private void testFailsWithMessage(String clazz, String test, String... messageLines) {
		try {
			Object inst = java.newInstance(clazz);
			inst.getClass().getMethod(test).invoke(inst);
			fail("Should have triggered assertion error");
		} catch(ReflectiveOperationException e) {
			String message = "\n\n";
			for(String line: messageLines) {
				message += line + "\n";
			}
			assertThat(e.getCause()).hasMessage(message);
		}
	}
}