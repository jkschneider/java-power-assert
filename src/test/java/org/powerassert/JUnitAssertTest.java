package org.powerassert;

import org.junit.Before;
import org.junit.Test;

public class JUnitAssertTest extends AbstractAssertTest {
	@Before
	public void setup() {
		super.setup();
		java.compile("public class Data { int n = 1; }");
	}

	@Test
	public void junitAssertEquals() {
		java.compile(
				"import static org.junit.Assert.*;\n" +
				"public class A {\n" +
				"	@org.junit.Test public void test() {\n" +
				"      assertEquals(3, 1+ 1);\n" +
				"	}\n" +
				"}\n");

		testFailsWithMessage("A", "test",
				"assertEquals(3, 1+ 1)",
				"                 |",
				"                 2");
	}

	@Test
	public void junitAssertNotEquals() {
		java.compile(
				"import static org.junit.Assert.*;\n" +
				"public class A {\n" +
				"	@org.junit.Test public void test() {\n" +
				"      assertNotEquals(2, 1+ 1);\n" +
				"	}\n" +
				"}\n");

		testFailsWithMessage("A", "test",
				"assertNotEquals(2, 1+ 1)",
				"                    |",
				"                    2");
	}

	@Test
	public void junitAssertNull() {
		java.compile(
				"import static org.junit.Assert.*;\n" +
				"public class A {\n" +
				"	@org.junit.Test public void test() {\n" +
				"	   Integer a = 1;\n" +
				"      assertNull(a);\n" +
				"	}\n" +
				"}\n");

		testFailsWithMessage("A", "test",
				"assertNull(a)",
				"           |",
				"           1");
	}

	@Test
	public void junitAssertNotNull() {
		java.compile(
				"import static org.junit.Assert.*;\n" +
				"public class A {\n" +
				"	@org.junit.Test public void test() {\n" +
				"	   Integer a = null;\n" +
				"      assertNotNull(a);\n" +
				"	}\n" +
				"}\n");

		testFailsWithMessage("A", "test",
				"assertNotNull(a)",
				"              |",
				"              null");
	}

	@Test
	public void junitAssertTrue() {
		java.compile(
				"import static org.junit.Assert.*;\n" +
				"public class A {\n" +
				"	@org.junit.Test public void test() {\n" +
				"	   boolean a = false;\n" +
				"      assertTrue(a);\n" +
				"	}\n" +
				"}\n");

		testFailsWithMessage("A", "test",
				"assertTrue(a)",
				"           |",
				"           false");
	}

	@Test
	public void junitAssertFalse() {
		java.compile(
				"import static org.junit.Assert.*;\n" +
				"public class A {\n" +
				"	@org.junit.Test public void test() {\n" +
				"	   boolean a = true;\n" +
				"      assertFalse(a);\n" +
				"	}\n" +
				"}\n");

		testFailsWithMessage("A", "test",
				"assertFalse(a)",
				"            |",
				"            true");
	}

	@Test
	public void junitAssertArrayEquals() {
		java.compile(
				"import static org.junit.Assert.*;\n" +
				"public class A {\n" +
				"	@org.junit.Test public void test() {\n" +
				"	   int[] a = new int[] { 1, 2, 3 };\n" +
				"      int[] b = new int[] { 1, 2, 4 };\n" +
				"      assertArrayEquals(a, b);\n" +
				"	}\n" +
				"}\n");

		testFailsWithMessage("A", "test",
				"assertArrayEquals(a, b)",
				"                  |  |",
				"                  |  [1, 2, 4]",
				"                  [1, 2, 3]");

		java.compile(
				"import static org.junit.Assert.*;\n" +
				"public class B {\n" +
				"	@org.junit.Test public void test() {\n" +
				"	   double[] a = new double[] { 0.05, 2, 3 };\n" +
				"      double[] b = new double[] { 0.04, 2, 4 };\n" +
				"      assertArrayEquals(a, b, 0.1);\n" +
				"	}\n" +
				"}\n");

		testFailsWithMessage("B", "test",
				"assertArrayEquals(a, b, 0.1)",
				"                  |  |",
				"                  |  [0.04, 2.0, 4.0]",
				"                  [0.05, 2.0, 3.0]");
	}

	@Test
	public void junitAssertSame() {
		java.compile(
				"import static org.junit.Assert.*;\n" +
				"public class A {\n" +
				"	@org.junit.Test public void test() {\n" +
				"	   Data d1 = new Data();\n" +
				"	   Data d2 = new Data();\n" +
				"      assertSame(d1, d2);\n" +
				"	}\n" +
				"}\n");

		testFailsWithMessage("A", "test",
				"assertSame(d1, d2)",
				"           |   |",
				"           |   Data[n=1]",
				"           Data[n=1]");
	}

	@Test
	public void junitAssertNotSame() {
		java.compile(
				"import static org.junit.Assert.*;\n" +
				"public class A {\n" +
				"	@org.junit.Test public void test() {\n" +
				"	   Data d1 = new Data();\n" +
				"	   Data d2 = d1;\n" +
				"      assertNotSame(d1, d2);\n" +
				"	}\n" +
				"}\n");

		testFailsWithMessage("A", "test",
				"assertNotSame(d1, d2)",
				"              |   |",
				"              |   Data[n=1]",
				"              Data[n=1]");
	}

	@Test
	public void junitFailureMessageIsDiagrammed() {
		java.compile(
				"import static org.junit.Assert.*;\n" +
				"public class A {\n" +
				"	@org.junit.Test public void test() {\n" +
				"      assertEquals(\"my message\", 3, 1+ 1);\n" +
				"	}\n" +
				"}\n");

		testFailsWithMessage("A", "test",
				"assertEquals(\"my message\", 3, 1+ 1)",
				"                               |",
				"                               2");
	}
}