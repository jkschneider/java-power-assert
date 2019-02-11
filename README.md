# Power Assertions for Java

[![Build Status](https://travis-ci.org/jkschneider/java-power-assert.svg?branch=master)](https://travis-ci.org/jkschneider/java-power-assert)
[![Apache 2.0](https://img.shields.io/github/license/jkschneider/java-power-assert.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Power assertions (a.k.a. diagrammed assertions) augment your assertion failures with information about values produced during the evaluation of a condition, and presents them in an easily digestible form.
Power assertions are a popular feature of [Spock](https://github.com/spockframework/spock) (and later the whole [Groovy](https://github.com/apache/groovy) language independently of Spock),
[ScalaTest](http://www.scalatest.org/), and [Expecty](https://github.com/pniederw/expecty).

#### Java `assert` keyword

The plain java assert keyword is replaced with a diagrammed assertion. For example, the assertion

```java
assert Character.isWhitespace("abc".charAt(0));
```

produces this diagram:

    Character.isWhitespace("abc".charAt(0))
              |                  |
              false              a

#### JUnit

JUnit `assertXXX` invocations are also diagrammed. The assertion

```java
int[] a = new int[] { 1, 2, 3 };
int[] b = new int[] { 1, 2, 4 };
assertArrayEquals(a, b);
```

produces this diagram:

    assertArrayEquals(a, b)
                      |  |
                      |  [1, 2, 4]
                      [1, 2, 3]

Regular JUnit assert output is suppressed in favor of the more descriptive diagram.

#### Hamcrest

But wait, there's more! Hamcrest `assertThat` invocations are also diagrammed. The assertion

```java
Integer a = 2;
assertThat(1, equalTo(a));
```

produces this diagram:

    assertThat(1, equalTo(a))
                   |      |
                   <2>    2

Regular Hamcrest assert output is suppressed in favor of the more descriptive diagram, however, the `Matcher` itself
is diagrammed, so sophisticated descriptions will still be in the output.


#### Non-Goals

java-power-assert does not change the semantics of equality in Java. Every power-asserted statement will yield the same result as if it were left unchanged. In other words, if you turn off annotation processing during your build process or in your IDE, you can expect your tests to pass or fail just as they would if java-power-assert's annotation processing was doing its magic, you just would not see diagrammed output.

## Limitations

Currently, java-power-assert only works on code compiled with javac. This means it works in IntelliJ IDEA, gradle, etc.

Notably, it does *not* work in Eclipse which uses the Eclipse Compiler for Java (ECJ). There is
 a known solution for ECJ that involves running Eclipse with a Java agent that intercepts the ECJ generated AST prior to bytecode generation (and indeed this is what
 [Lombok](https://github.com/rzwitserloot/lombok) does). It is currently unknown whether it is possible to access the ECJ AST from a regular annotation processor.
 
Also, in the same way we diagram JUnit and Hamcrest assertions, we could trivially expand to assertj `assertThat` style chains. Contributions welcome if you beat me to it!

## A special note about Java 8

Java 8 saw a significant regression in type inference performance ([details here](https://bugs.openjdk.java.net/browse/JDK-8067767)).
In short, the time it takes to infer types for nested generic method calls without explicit type arguments grows exponentially in JDK 8.
This was *not* a problem in JDK 7 and has already been fixed in JDK 9, but Oracle does not intend to fix this in Java 8.
Because the method we use to record values is generic itself, we have no choice but to arbitrarily limit the depth at which we can record values on Java 8. So a statement like:

    @org.junit.Test public void test() {
        Data d = new Data();
        assert d.ident().ident().ident().ident() == null
    }

Yields a diagram like this:

    d.ident().ident().ident().ident() == null,
                     |       |        |
                     Data[]  Data[]   false

## Getting started

All you need to do is include `java-power-assert` as a dependency and turn on annotation processing in your IDE.
Currently it released only in [BinTray](https://bintray.com/jkschneider/maven/java-power-assert) so you need to add this repository first.

#### Gradle
Add the repository:
```groovy
repositories {
  maven {
    url  "https://dl.bintray.com/jkschneider/maven" 
  }
}
```

And add the dependency:
```groovy
testCompile 'io.jschneider:java-power-assert:latest.release'
```

#### Maven
Add the repository into your `pom.xml`:
```xml
<repositories>
  <repository>
    <id>bintray-jkschneider-maven</id>
    <name>bintray-jkschneider-maven</name>
    <url>https://dl.bintray.com/jkschneider/maven</url>
  </repository>
</repositories>
```

And add the dependency:
```xml
<dependency>
  <groupId>io.jschneider</groupId>
  <artifactId>java-power-assert</artifactId>
  <version>0.9.1</version>
  <scope>test</scope>
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

## License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
