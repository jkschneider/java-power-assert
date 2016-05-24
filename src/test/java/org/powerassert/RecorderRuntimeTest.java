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
