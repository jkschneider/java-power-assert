package org.powerassert;

import org.junit.Before;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public abstract class AbstractAssertTest {
	JavaCompilerHelper java;

	@Before
	public void setup() {
		java = new JavaCompilerHelper(new PowerAssertProcessor(false));
	}

	void testFailsWithMessage(String clazz, String test, String... messageLines) {
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
