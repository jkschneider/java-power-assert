package org.powerassert.examples;

import org.junit.Test;

public class AssertTest {
	@Test
	public void test() {
		assert new Data().bar().contains("b");
	}

	private String foo() {
		return "a";
	}
}

class Data {
	public Data() {
		System.out.println("hit the constructor");
	}

	public String bar() {
		return "a";
	}
}