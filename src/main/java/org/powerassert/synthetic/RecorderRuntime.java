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

package org.powerassert.synthetic;

import java.util.ArrayList;
import java.util.List;

public class RecorderRuntime {
	private List<RecordedValue> recordedValues = new ArrayList<>();

	public boolean recordValue(boolean value, int anchor) {
		RecordedValue recordedValue = new RecordedValue(value, anchor);
		recordedValues.add(recordedValue);
		return value;
	}

	public byte recordValue(byte value, int anchor) {
		RecordedValue recordedValue = new RecordedValue(value, anchor);
		recordedValues.add(recordedValue);
		return value;
	}

	public char recordValue(char value, int anchor) {
		RecordedValue recordedValue = new RecordedValue(value, anchor);
		recordedValues.add(recordedValue);
		return value;
	}

	public double recordValue(double value, int anchor) {
		RecordedValue recordedValue = new RecordedValue(value, anchor);
		recordedValues.add(recordedValue);
		return value;
	}

	public double recordValue(float value, int anchor) {
		RecordedValue recordedValue = new RecordedValue(value, anchor);
		recordedValues.add(recordedValue);
		return value;
	}

	public int recordValue(int value, int anchor) {
		RecordedValue recordedValue = new RecordedValue(value, anchor);
		recordedValues.add(recordedValue);
		return value;
	}

	public long recordValue(long value, int anchor) {
		RecordedValue recordedValue = new RecordedValue(value, anchor);
		recordedValues.add(recordedValue);
		return value;
	}

	public <U> U recordValue(U value, int anchor) {
		RecordedValue recordedValue = new RecordedValue(value, anchor);
		recordedValues.add(recordedValue);
		return value;
	}

	public short recordValue(short value, int anchor) {
		RecordedValue recordedValue = new RecordedValue(value, anchor);
		recordedValues.add(recordedValue);
		return value;
	}

	public void recordExpression(String text, Boolean value, int anchor) {
		for(RecordedValue recordedValue: recordedValues) {
			recordedValue.relativizeAnchor(anchor);
		}

		RecordedExpression<Boolean> recordedExpr = new RecordedExpression<>(text, value, recordedValues);

		if(!recordedExpr.getValue()) {
			throw new AssertionError("\n\n" + new ExpressionRenderer(false).render(recordedExpr));
		}
	}
}
