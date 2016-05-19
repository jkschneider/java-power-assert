package org.powerassert.synthetic;

import java.util.List;

public class RecordedExpression<T> {
	private String text;
	private T value;
	private List<RecordedValue> values;

	public RecordedExpression(String text, T value, List<RecordedValue> values) {
		this.text = text;
		this.value = value;
		this.values = values;
	}

	public String getText() {
		return text;
	}

	public T getValue() {
		return value;
	}

	public List<RecordedValue> getValues() {
		return values;
	}
}
