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
