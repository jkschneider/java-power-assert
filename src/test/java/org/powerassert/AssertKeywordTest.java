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

public class AssertKeywordTest extends AbstractAssertTest {
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

	/**
	 * This will take several seconds to run on Oracle JDK 1.8.0_60. It is faster on JDK 7 and
	 * JDK 1.8.0_92. Gradle and IntelliJ builds can still appear to hang on 1.8.0_92.
	 */
	@Test
	public void longMethodChain() {
		java.compile(
				"public class Data {" +
				"   public Data ident() { return this; }" +
				"}");

		java.compile(
				"public class A {" +
				"	@org.junit.Test public void test() {" +
				"      Data d = new Data();" +
				"      assert d.ident().ident().ident().ident().ident().ident() != null;" +
				"	}" +
				"}");
	}

	@Test
	public void staticFieldReference() {
		java.compile("public class Constants { public static int CONST = 1; }");
		java.compile(
				"public class A {\n" +
				"   public boolean compare(int a, int b) { return a == b; }\n" +
				"	\n" +
				"	@org.junit.Test public void test() {\n" +
				"      assert compare(Constants.CONST, 2);\n" +
				"	}\n" +
				"}");

		testFailsWithMessage("A", "test",
				"compare(Constants.CONST, 2)",
				" |                |",
				" false            1");
	}

	@Test
	public void typeReferenceInAnotherPackageWithImport() {
		java.compile(
				"package mypack;\n" +
				"public enum Constants { CONST }");

		// both classes will fail compilation with "cannot find symbol: variable Constants" if type identification
		// does not work correctly

		java.compile(
				"import mypack.*;\n" + // wildcard import
				"public class A {\n" +
				"	@org.junit.Test public void test() {\n" +
				"      assert Constants.CONST == Constants.CONST;\n" +
				"	}\n" +
				"}");

		java.compile(
				"import mypack.Constants;\n" + // specific import
				"public class B {\n" +
				"	@org.junit.Test public void test() {\n" +
				"      assert Constants.CONST == Constants.CONST;\n" +
				"	}\n" +
				"}");

		java.compile(
				"public class C {\n" +
				"	@org.junit.Test public void test() {\n" +
				"      assert Integer.valueOf(0).intValue() == 0;\n" +
				"	}\n" +
				"}");
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

	@Test
	public void ternary() {
		java.compile(
				"public class A {" +
						"	@org.junit.Test public void test() {" +
						"      boolean b = false;" +
						"      boolean a = true;" +
						"      assert \"a\".contains(\"b\") ? a : b;" + // extraordinarily contrived, I know...
						"	}" +
						"}");

		testFailsWithMessage("A", "test",
				"\"a\".contains(\"b\") ? a : b",
				"    |             |     |",
				"    false         false false");
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
}