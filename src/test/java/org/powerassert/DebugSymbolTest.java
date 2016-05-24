/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
