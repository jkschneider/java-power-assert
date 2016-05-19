package org.powerassert;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.lang.reflect.Array;
import java.util.*;

public class ExpressionRenderer {
	boolean showTypes;

	public ExpressionRenderer(boolean showTypes) {
		this.showTypes = showTypes;
	}

	public String render(RecordedExpression<?> recordedExpr) {
		int offset = 0;
		for(char c: recordedExpr.getText().toCharArray()) {
			if(Character.isWhitespace(c))
				offset++;
			else break;
		}

		String intro = recordedExpr.getText().trim().replaceAll(";$", ""); // strip trailing semicolons
		List<String> lines = new ArrayList<>();

		for(RecordedValue recordedValue: filterAndSortByAnchor(recordedExpr.getValues())) {
			placeValue(lines, recordedValue.getValue(), recordedValue.getAnchor() - offset);
		}

		lines.add(0, intro);

		String rendered = "";
		for(String line: lines) {
			rendered += line + "\n";
		}
		return rendered;
	}

	private Iterable<RecordedValue> filterAndSortByAnchor(List<RecordedValue> recordedValues) {
		Map<Integer, RecordedValue> map = new TreeMap<>(new Comparator<Integer>() {
			@Override
			public int compare(Integer n1, Integer n2) {
				return n2.compareTo(n1);
			}
		});

		for(RecordedValue value: recordedValues) {
			if(!map.containsKey(value.getAnchor()))
				map.put(value.getAnchor(), value);
		}

		return map.values();
	}

	private void placeValue(List<String> lines, Object value, int col) {
		String str = renderValue(value);

		if(lines.isEmpty())
			lines.add("");

		// ensure that there is at least one row of pipes between the expression and values
		lines.set(0, placeString(lines.get(0), "|", col));

		for (int i = 1; i < lines.size(); i++) {
			String line = lines.get(i);
			if(fits(line, str, col)) {
				lines.set(i, placeString(line, str, col));
				return;
			}
			lines.set(i, placeString(line, "|", col));
		}

		String newLine = placeString("", str, col);
		lines.add(newLine);
	}

	private String renderValue(Object value) {
		String str;
		if(value == null)
			str = "null";
		else if(value.getClass().isArray()) {
			// recursive string join
			str = "[";
			for(Object o: toArray(value)) {
				str += renderValue(o) + ", ";
			}
			str = (str.length() > 1 ? str.substring(0, str.length()-2) : "") + "]";
		}
		else {
			try {
				if(value.getClass().getMethod("toString").getDeclaringClass() == Object.class) {
					str = ToStringBuilder.reflectionToString(value, ToStringStyle.SHORT_PREFIX_STYLE);
				}
				else str = value.toString();
			} catch (NoSuchMethodException e) {
				str = value.toString();
			}
		}

		return showTypes && value != null ? str + " (" + value.getClass().getName() + ")" : str;
	}

	private String placeString(String line, String str, int anchor) {
		int diff = anchor - line.length();
		for(int i = 0; i < diff; i++) {
			line += ' ';
		}
		String prefix = line.substring(0, anchor);
		String suffix = anchor + str.length() > line.length() ? "" : line.substring(anchor + str.length());
		return prefix + str + suffix;
	}

	private Boolean fits(String line, String str, int anchor) {
		if(str.length() > line.length())
			return false;

		for(char c: line.substring(anchor, anchor + str.length() + 1).toCharArray()) {
			if(!Character.isWhitespace(c))
				return false;
		}
		return true;
	}

	private Object[] toArray(Object array) {
		if (array.getClass().getComponentType().isPrimitive()) {
			List<Object> list = new ArrayList<>();
			for (int i = 0; i < Array.getLength(array); i++) {
				list.add(Array.get(array, i));
			}
			return list.toArray();
		}
		else {
			return (Object[]) array;
		}
	}
}
