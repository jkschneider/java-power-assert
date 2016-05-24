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
	private RecorderListener<Boolean> listener;
	private List<RecordedExpression<Boolean>> recordedExprs = new ArrayList<>();
	private List<RecordedValue> recordedValues = new ArrayList<>();

	public RecorderRuntime(RecorderListener<Boolean> listener) {
		this.listener = listener;
	}

	public void resetValues() {
		recordedValues = new ArrayList<>();
	}

	public <U> U recordValue(U value, int anchor) {
		RecordedValue recordedValue = new RecordedValue(value, anchor);
		listener.valueRecorded(recordedValue);
		recordedValues.add(recordedValue);
		return value;
	}

	public void recordExpression(String text, Boolean value, int anchor) {
		for(RecordedValue recordedValue: recordedValues) {
			recordedValue.relativizeAnchor(anchor);
		}

		RecordedExpression<Boolean> recordedExpr = new RecordedExpression<>(text, value, recordedValues);
		listener.expressionRecorded(recordedExpr);
		recordedExprs.add(recordedExpr);
	}

	public Boolean completeRecording() {
		RecordedExpression<Boolean> lastRecorded = recordedExprs.get(0);
		Recording<Boolean> recording = new Recording<>(lastRecorded.getValue(), recordedExprs);
		listener.recordingCompleted(recording);
		return recording.getValue();
	}
}
