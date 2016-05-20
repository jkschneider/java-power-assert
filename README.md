# Power Assertions for Java

Power assertions (a.k.a. diagrammed assertions) augment your assertion failures with information about values produced during the evaluation of a condition, and presents them in an easily digestible form.

For example, the assertion

```java
assert Character.isWhitespace("abc".charAt(0));
```

produces this diagram:

    Character.isWhitespace("abc".charAt(0))
              |                  |
              false              a
              
## Getting started

All you need to do is include java-power-assert as a dependency and turn on annotation processing in your IDE.

#### Gradle

```groovy
testCompile 'io.jschneider:java-power-assert:latest.release'
```

#### Maven

```xml
<dependency>
  <groupId>io.jschneider</groupId>
  <artifactId>java-power-assert</artifactId>
  <version>0.2</version>
</dependency>
```

## How does it work?

An annotation processor (`PowerAssertProcessor`) looks for assert statements and replaces the AST representing the assert statement with a few statements that record the values of fields, method invocations, etc produced during the evaluation of the expression. When the original assert condition would have produced a false value, java-power-assert throws an `AssertionError` with a diagram of expression values as the exception message.

## Examples

```java
public class PowerAssertExampleTest {
	@Test
	public void methodInvocation() {
		assert Character.isWhitespace("abc".charAt(0));

		/*
		Character.isWhitespace("abc".charAt(0))
				  |                  |
				  false              a
		 */
	}

	@Test
	public void chainedMethodInvocation() {
		assert "abc".substring(0).contains("d");

		/*
		"abc".substring(0).contains("d")
			  |            |
			  abc          false
		 */
	}

	@Test
	public void propertyRead() {
		Data d = new Data("abc");
		assert d.field.equals("def");

		/*
		d.field.equals("def")
		| |     |
		| abc   false
		Data[field=abc]
		 */
	}

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
	public void nullValues() {
		String a = null;
		assert "null".equals(a);

		/*
		"null".equals(a)
			   |      |
			   false  null
		 */
	}

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
```
