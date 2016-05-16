package org.powerassert;

public class RecordedValue {
	private Object value;
	private int anchor;

	public RecordedValue(Object value, int anchor) {
		this.value = value;
		this.anchor = anchor;
	}

	public Object getValue() {
		return value;
	}

	public int getAnchor() {
		return anchor;
	}

	public void relativizeAnchor(int relativeTo) {
		this.anchor -= relativeTo;
	}
}
