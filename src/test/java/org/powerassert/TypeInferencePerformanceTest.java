package org.powerassert;

import org.junit.Test;

public class TypeInferencePerformanceTest extends AbstractAssertTest {
	/**
	 * Otherwise, it will take several seconds to run on JDK 1.8. It is faster on JDK 7.
	 * Type inferencing for nested generic method calls is known to be super slow on JDK 8, but this
	 * has been fixed in JDK 9. See https://bugs.openjdk.java.net/browse/JDK-8067767
	 */
	@Test
	public void onJava8LimitDepthOfRecordValueCalls() {
		java.compile(
				"public class Data {" +
				"   public Data ident() { return this; }" +
				"}");

		java.compile(
				"public class A {" +
				"	@org.junit.Test public void test() {" +
				"      Data d = new Data();" +
				"      assert d.ident().ident().ident().ident() == null;" +
				"	}" +
				"}");

		if(System.getProperty("java.version").startsWith("1.8")) {
			testFailsWithMessage("A", "test",
					"d.ident().ident().ident().ident() == null",
					"                  |       |        |",
					"                  Data[]  Data[]   false");
		} else {
			testFailsWithMessage("A", "test",
					"d.ident().ident().ident().ident() == null",
					"| |       |       |       |        |",
					"| Data[]  Data[]  Data[]  Data[]   false",
					"Data[]");
		}
	}
}