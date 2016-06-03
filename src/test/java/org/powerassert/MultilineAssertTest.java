package org.powerassert;

import org.junit.Test;

public class MultilineAssertTest extends AbstractAssertTest {

	@Test
	public void chainedMethodCallsOnMultipleLines() {
		java.compile(
				"public class A {\n" +
						"	@org.junit.Test public void test() {\n" +
						"      assert \"abc\"\n" +
						"			.substring(0)\n" +
						"			.contains(\"d\");\n" +
						"	}\n" +
						"}\n");

		testFailsWithMessage("A", "test",
				"\"abc\" .substring(0) .contains(\"d\")",
				"       |             |",
				"       abc           false");
	}
}
