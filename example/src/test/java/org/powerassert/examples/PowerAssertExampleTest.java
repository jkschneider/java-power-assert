package org.powerassert.examples;

import org.junit.Test;

public class PowerAssertExampleTest {
	@Test
	public void identifiers() {
		String a = "abc";
		assert a == "def";

		/*
		a == "def"
		|  |
		|  false
		abc
		 */
	}

	@Test
	public void binaryExpression() {
		// notice how whitespace is preserved in the output
		assert 1+ 1 == 3;

		/*
		1+ 1 == 3
		 |    |
		 2    false
		 */
	}

	@Test
	public void unaryExpression() {
		assert !true;

		/*
		!true
		|
		false
		 */
	}

	@Test
	public void methodInvocation() {
		assert "abc".contains("d");

		/*
		"abc".contains("d")
			  |
			  false
		 */
	}

	@Test
	public void methodInvocationInArguments() {
		assert Character.isWhitespace("abc".charAt(0));

		/*
		Character.isWhitespace("abc".charAt(0))
				  |                  |
				  false              a
		 */
	}

	@Test
	public void nullValues() {
		String a = null;
		assert "null".equals(a);

		/*
		"null".equals(a)
			   |      |
			   false  null
		 */
	}

//	@Test
//	public void propertyRead() {
//		Data d = new Data("abc");
//		assert d.field.equals("def");
//	}

	@Test
	public void arrayAccess() {
		int n[] = new int[] { 0, 1, 2 };
		assert n[0] == 1;

		/*
		n[0] == 1
		||    |
		|0    false
		[0, 1, 2]
		 */
	}

	@Test
	public void newArray() {
		int i = 1;
		// extraordinarily contrived, I know...
		assert new int[] { i }[0] == 2;

		/*
		new int[] { i }[0] == 2
					|  |    |
					1  1    false
		 */
	}
}

class Data {
	public String field;

	public Data(String field) {
		this.field = field;
	}
}