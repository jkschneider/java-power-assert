package org.powerassert;

import static org.assertj.core.api.Assertions.fail;

import org.junit.Test;
import org.powerassert.synthetic.PowerAssert;
import org.powerassert.synthetic.RecorderRuntime;

public class RecorderRuntimeTest {
	@Test
	public void recordingOfFalseExpressionThrowsAssertion() {
		PowerAssert pa = new PowerAssert();
		RecorderRuntime runtime = new RecorderRuntime(pa.getListener());

		try {
			runtime.recordExpression(
					"\"abc\".substring(0).contains(\"a\")",
					runtime.recordValue(runtime.recordValue("abc".substring(0), 6).contains("d"), 19),
					0);
			fail("Should have thrown assertion");
		} catch(AssertionError e) {
			System.out.println(e.getMessage());
		}
	}
}
