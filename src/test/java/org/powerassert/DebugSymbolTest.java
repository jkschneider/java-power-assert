package org.powerassert;

import org.junit.Test;

public class DebugSymbolTest {
	/**
	 * Baseline showing that chained method executions appear sequentially inside a single label with a single
	 * LINENUMBER instruction
	 */
	@Test
	public void debugSymbols() {
		compileAndTraceWithProcessors(new JavaCompilerHelper());
	}

	@Test
	public void debugSymbolsWithProcessor() {
		compileAndTraceWithProcessors(new JavaCompilerHelper(new PowerAssertProcessor()));
	}

	private void compileAndTraceWithProcessors(JavaCompilerHelper java) {
		java.compile(
				"public class A {\n" +
				"	@org.junit.Test public void mytest() {\n" +
				"      assert \"a\".contains(\"b\");\n" +
				"	}\n" +
				"}\n");

		java.traceMethod("A", "mytest");

		// TODO assert that all INVOKE* calls for the assert line are recorded on the same line number
	}
}
