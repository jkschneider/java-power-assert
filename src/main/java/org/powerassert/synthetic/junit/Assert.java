package org.powerassert.synthetic.junit;

import org.junit.internal.ArrayComparisonFailure;

@SuppressWarnings({"UnusedParameters", "WeakerAccess"})
public class Assert {
	public static boolean assertNull(Object expected) {
		return expected == null;
	}

	public static boolean assertNull(String message, Object expected) {
		return expected == null;
	}

	public static boolean assertNotNull(Object expected) {
		return expected != null;
	}

	public static boolean assertNotNull(String message, Object expected) {
		return expected != null;
	}

	public static boolean assertEquals(Object expected, Object actual) {
		return expected.equals(actual);
	}

	public static boolean assertEquals(String message, Object expected, Object actual) {
		return expected.equals(actual);
	}

	public static boolean assertEquals(long expected, long actual) {
		return expected == actual;
	}

	public static boolean assertEquals(String message, long expected, long actual) {
		return expected == actual;
	}

	public static boolean assertEquals(float expected, float actual, float delta) {
		try {
			org.junit.Assert.assertEquals(expected, actual, delta);
			return true;
		}
		catch(AssertionError ignored) {
			return false;
		}
	}

	public static boolean assertEquals(String message, float expected, float actual, float delta) {
		return assertEquals(expected, actual, delta);
	}

	@Deprecated
	public static boolean assertEquals(Object[] expected, Object[] actual) {
		return assertArrayEquals(expected, actual);
	}

	@Deprecated
	public static boolean assertEquals(String message, Object[] expected, Object[] actual) {
		return assertArrayEquals(expected, actual);
	}

	public static boolean assertEquals(double expected, double actual, double delta) {
		try {
			org.junit.Assert.assertEquals(expected, actual, delta);
			return true;
		}
		catch(AssertionError ignored) {
			return false;
		}
	}

	public static boolean assertEquals(String message, double expected, double actual, double delta) {
		return assertEquals(expected, actual, delta);
	}

	@Deprecated
	public static boolean assertEquals(double expected, double actual) {
		try {
			org.junit.Assert.assertEquals(expected, actual);
			return true;
		}
		catch(AssertionError ignored) {
			return false;
		}
	}

	@Deprecated
	public static boolean assertEquals(String message, double expected, double actual) {
		return assertEquals(expected, actual);
	}

	public static boolean assertNotEquals(Object expected, Object actual) {
		return !expected.equals(actual);
	}

	public static boolean assertNotEquals(String message, Object expected, Object actual) {
		return !expected.equals(actual);
	}

	public static boolean assertNotEquals(long expected, long actual) {
		return expected != actual;
	}

	public static boolean assertNotEquals(String message, long expected, long actual) {
		return expected != actual;
	}

	public static boolean assertNotEquals(float expected, float actual, float delta) {
		try {
			org.junit.Assert.assertNotEquals(expected, actual, delta);
			return true;
		}
		catch(AssertionError ignored) {
			return false;
		}
	}

	public static boolean assertNotEquals(String message, float expected, float actual, float delta) {
		return assertNotEquals(expected, actual, delta);
	}

	public static boolean assertTrue(boolean expected) {
		return expected;
	}

	public static boolean assertTrue(String message, boolean expected) {
		return expected;
	}

	public static boolean assertFalse(boolean expected) {
		return !expected;
	}

	public static boolean assertFalse(String message, boolean expected) {
		return !expected;
	}

	public static boolean assertSame(Object expected, Object actual) {
		return expected == actual;
	}

	public static boolean assertSame(String message, Object expected, Object actual) {
		return expected == actual;
	}

	public static boolean assertNotSame(Object expected, Object actual) {
		return expected != actual;
	}

	public static boolean assertArrayEquals(boolean[] expecteds, boolean[] actuals) {
		try {
			org.junit.Assert.assertArrayEquals(expecteds, actuals);
			return true;
		}
		catch(ArrayComparisonFailure ignored) {
			return false;
		}
	}

	public static boolean assertArrayEquals(String message, boolean[] expecteds, boolean[] actuals) {
		return assertArrayEquals(expecteds, actuals);
	}

	public static boolean assertArrayEquals(byte[] expecteds, byte[] actuals) {
		try {
			org.junit.Assert.assertArrayEquals(expecteds, actuals);
			return true;
		}
		catch(ArrayComparisonFailure ignored) {
			return false;
		}
	}

	public static boolean assertArrayEquals(String message, byte[] expecteds, byte[] actuals) {
		return assertArrayEquals(expecteds, actuals);
	}

	public static boolean assertArrayEquals(char[] expecteds, char[] actuals) {
		try {
			org.junit.Assert.assertArrayEquals(expecteds, actuals);
			return true;
		}
		catch(ArrayComparisonFailure ignored) {
			return false;
		}
	}

	public static boolean assertArrayEquals(String message, char[] expecteds, char[] actuals) {
		return assertArrayEquals(expecteds, actuals);
	}

	public static boolean assertArrayEquals(double[] expecteds, double[] actuals, double delta) {
		try {
			org.junit.Assert.assertArrayEquals(expecteds, actuals, delta);
			return true;
		}
		catch(ArrayComparisonFailure ignored) {
			return false;
		}
	}

	public static boolean assertArrayEquals(String message, double[] expecteds, double[] actuals, double delta) {
		return assertArrayEquals(expecteds, actuals, delta);
	}

	public static boolean assertArrayEquals(float[] expecteds, float[] actuals, float delta) {
		try {
			org.junit.Assert.assertArrayEquals(expecteds, actuals, delta);
			return true;
		}
		catch(ArrayComparisonFailure ignored) {
			return false;
		}
	}

	public static boolean assertArrayEquals(String message, float[] expecteds, float[] actuals, float delta) {
		return assertArrayEquals(expecteds, actuals, delta);
	}

	public static boolean assertArrayEquals(int[] expecteds, int[] actuals) {
		try {
			org.junit.Assert.assertArrayEquals(expecteds, actuals);
			return true;
		}
		catch(ArrayComparisonFailure ignored) {
			return false;
		}
	}

	public static boolean assertArrayEquals(String message, int[] expecteds, int[] actuals) {
		return assertArrayEquals(expecteds, actuals);
	}

	public static boolean assertArrayEquals(long[] expecteds, long[] actuals) {
		try {
			org.junit.Assert.assertArrayEquals(expecteds, actuals);
			return true;
		}
		catch(ArrayComparisonFailure ignored) {
			return false;
		}
	}

	public static boolean assertArrayEquals(String message, long[] expecteds, long[] actuals) {
		return assertArrayEquals(expecteds, actuals);
	}

	public static boolean assertArrayEquals(Object[] expecteds, Object[] actuals) {
		try {
			org.junit.Assert.assertArrayEquals(expecteds, actuals);
			return true;
		}
		catch(ArrayComparisonFailure ignored) {
			return false;
		}
	}

	public static boolean assertArrayEquals(String message, Object[] expecteds, Object[] actuals) {
		return assertArrayEquals(expecteds, actuals);
	}

	public static boolean assertArrayEquals(short[] expecteds, short[] actuals) {
		try {
			org.junit.Assert.assertArrayEquals(expecteds, actuals);
			return true;
		}
		catch(ArrayComparisonFailure ignored) {
			return false;
		}
	}

	public static boolean assertArrayEquals(String message, short[] expecteds, short[] actuals) {
		return assertArrayEquals(expecteds, actuals);
	}
}
