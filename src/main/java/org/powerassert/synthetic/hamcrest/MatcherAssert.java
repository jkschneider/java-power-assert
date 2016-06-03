package org.powerassert.synthetic.hamcrest;

import org.hamcrest.Matcher;

public class MatcherAssert {
	public static <T> boolean assertThat(T actual, Matcher<? super T> matcher) {
		return matcher.matches(actual);
	}

	public static <T> boolean assertThat(String reason, T actual, Matcher<? super T> matcher) {
		return matcher.matches(actual);
	}

	public static boolean assertThat(String reason, boolean assertion) {
		return assertion;
	}
}
