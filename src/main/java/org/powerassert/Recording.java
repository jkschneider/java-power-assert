package org.powerassert;

import java.util.List;

public class Recording<T> {
	private T value;
	private List<RecordedExpression<T>> recordedExprs;

	public Recording(T value, List<RecordedExpression<T>> recordedExprs) {
		this.value = value;
		this.recordedExprs = recordedExprs;
	}

	public T getValue() {
		return value;
	}

	public List<RecordedExpression<T>> getRecordedExprs() {
		return recordedExprs;
	}
}
