package org.powerassert;

import org.junit.Test;
import static org.junit.Assert.*;

public class AssertTest {
	@Test
	public void test() {
		assert 1+1 == 2;
		assert 2+2 == 3;
	}
}