package org.powerassert;

import org.junit.Test;

public class HamcrestAssertTest extends AbstractAssertTest {

	@Test
	public void hamcrestAssertBooleanCondition() {
		java.compile(
				"import static org.hamcrest.MatcherAssert.assertThat;\n" +
						"import static org.hamcrest.CoreMatchers.equalTo;\n" +
						"public class A {\n" +
						"	@org.junit.Test public void test() {\n" +
						"	   Boolean a = false;\n" +
						"      assertThat(\"my reason\", a);\n" +
						"	}\n" +
						"}\n");

		testFailsWithMessage("A", "test",
				"assertThat(\"my reason\", a)",
				"                        |",
				"                        false");
	}

	@Test
	public void hamcrestAssertThatWithMatcher() {
		java.compile(
				"import static org.hamcrest.MatcherAssert.assertThat;\n" +
				"import static org.hamcrest.CoreMatchers.equalTo;\n" +
				"public class A {\n" +
				"	@org.junit.Test public void test() {\n" +
				"	   Integer a = 2;\n" +
				"      assertThat(1, equalTo(a));\n" +
				"	}\n" +
				"}\n");

		testFailsWithMessage("A", "test",
				"assertThat(1, equalTo(a))",
				"               |      |",
				"               <2>    2");
	}

	@Test
	public void hamcrestAssertThatWithMatcherAndReason() {
		java.compile(
				"import static org.hamcrest.MatcherAssert.assertThat;\n" +
						"import static org.hamcrest.CoreMatchers.equalTo;\n" +
						"public class A {\n" +
						"	@org.junit.Test public void test() {\n" +
						"	   Integer a = 2;\n" +
						"      assertThat(\"my reason\", 1, equalTo(a));\n" +
						"	}\n" +
						"}\n");

		testFailsWithMessage("A", "test",
				"assertThat(\"my reason\", 1, equalTo(a))",
				"                            |      |",
				"                            <2>    2");
	}
}
