package org.powerassert;

public interface RecorderListener<T> {
	void valueRecorded(RecordedValue recordedValue);
	void expressionRecorded(RecordedExpression<T> recordedExpression);
	void recordingCompleted(Recording<T> recording);
}
