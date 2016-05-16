package org.powerassert;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;

public class PowerAssertProcessorTest {
	@Test
	public void test() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		JavaCompilerHelper java = new JavaCompilerHelper(new PowerAssertProcessor());

		java.compile(
			"import org.junit.Test;" +
			"import java.util.concurrent.atomic.*;" +
			"public class A {" +
			"	@Test public void test() {" +
			"      assert 1 +1 == 3;" +
			"	}" +
			"}");

//		{
//			PowerAssert pa = new PowerAssert();
//			RecorderRuntime runtime = new RecorderRuntime(pa.getListener());
//			runtime.recordExpression("assert 1+1 == 2;",
//					runtime.recordValue(1, 8) + runtime.recordValue(1, 10) == runtime.recordValue(2, 15));
//			if(!runtime.completeRecording()) {
//				throw new AssertionError("print the tree");
//			}
//		}

		Object a = java.newInstance("A");
		a.getClass().getMethod("test").invoke(a);

		assertTrue(true);
	}
}