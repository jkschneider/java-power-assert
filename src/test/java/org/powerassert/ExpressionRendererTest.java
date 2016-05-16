package org.powerassert;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class ExpressionRendererTest {
	@Test
	public void literals() {

	}

	private Boolean outputs() {
		return true;
	}
}

//		def outputs(rendering: String)(expectation: => Boolean) {
//		def normalize(s: String) = s.trim().lines.mkString
//
//		try {
//		expectation
//		fail("Expectation should have failed but didn't")
//		}
//		catch  {
//		case e: AssertionError => {
//		val expected = normalize(rendering)
//		val actual = normalize(e.getMessage).replaceAll("@[0-9a-f]*", "@\\.\\.\\.")
//		if (actual != expected) {
//		throw new ComparisonFailure("Expectation output doesn't match", expected, actual)
//		}
//		}
//		}
//		}

//class ExpectyRenderingSpec {
//	val expect = new Expecty(printAsts = true)
//
//	@Test
//	def literals() {
//		outputs("""
//				"abc".length() == 2
//						|        |
//				3        false
//		""") {
//		expect {
//			"abc".length() == 2
//		}
//	}
//}
//
//	@Test
//	def object_apply() {
//		outputs("""
//				List() == List(1, 2)
//						|  |
//		|  List(1, 2)
//		false
//		""") {
//		expect {
//			List() == List(1, 2)
//		}
//	}
//}
//
//@Test
//def object_apply_2() {
//		outputs("""
//		List(1, 2) == List()
//		|          |
//		List(1, 2) false
//		""") {
//		expect {
//		List(1, 2) == List()
//		}
//		}
//		}
//
//@Test
//def infix_operators() {
//		val str = "abc"
//
//		outputs("""
//		str + "def" == "other"
//		|   |       |
//		abc abcdef  false
//		""") {
//		expect {
//		str + "def" == "other"
//		}
//		}
//		}
//
//@Test
//def null_value() {
//		val x = null
//
//		outputs("""
//		x == "null"
//		| |
//		| false
//		null
//		""") {
//		expect {
//		x == "null"
//		}
//		}
//		}
//
//@Test
//def value_with_type_hint() {
//		val expect = new Expecty(showTypes = true)
//		val x = "123"
//
//		outputs("""
//		x == 123
//		| |
//		| false (java.lang.Boolean)
//		123 (java.lang.String)
//		""") {
//		expect {
//		x == 123
//		}
//		}
//		}
//
//@Test
//def arithmetic_expressions() {
//		val one = 1
//
//		outputs("""
//		one + 2 == 4
//		|   |   |
//		1   3   false
//		""") {
//		expect {
//		one + 2 == 4
//		}
//		}
//		}
//
//@Test
//def property_read() {
//		val person = Person()
//
//		outputs("""
//		person.age == 43
//		|      |   |
//		|      42  false
//		Person(Fred,42)
//		""") {
//		expect {
//		person.age == 43
//		}
//		}
//		}
//
//@Test
//def method_call_zero_args() {
//		val person = Person()
//
//		outputs("""
//		person.doIt() == "pending"
//		|      |      |
//		|      done   false
//		Person(Fred,42)
//		""") {
//		expect {
//		person.doIt() == "pending"
//		}
//		}
//		}
//
//@Test
//def method_call_one_arg() {
//		val person = Person()
//		val word = "hey"
//
//		outputs("""
//		person.sayTwice(word) == "hoho"
//		|      |        |     |
//		|      heyhey   hey   false
//		Person(Fred,42)
//		""") {
//		expect {
//		person.sayTwice(word) == "hoho"
//		}
//		}
//		}
//
//@Test
//def method_call_multiple_args() {
//		val person = Person()
//		val word1 = "hey"
//		val word2 = "ho"
//
//		outputs("""
//		person.sayTwo(word1, word2) == "hoho"
//		|      |      |      |      |
//		|      heyho  hey    ho     false
//		Person(Fred,42)
//		""") {
//		expect {
//		person.sayTwo(word1, word2) == "hoho"
//		}
//		}
//		}
//
//@Test
//def method_call_var_args() {
//		val person = Person()
//		val word1 = "foo"
//		val word2 = "bar"
//		val word3 = "baz"
//
//		outputs("""
//		person.sayAll(word1, word2, word3) == "hoho"
//		|      |      |      |      |      |
//		|      |      foo    bar    baz    false
//		|      foobarbaz
//		Person(Fred,42)
//		""") {
//		expect {
//		person.sayAll(word1, word2, word3) == "hoho"
//		}
//		}
//		}
//
//@Test
//def nested_property_reads_and_method_calls() {
//		val person = Person()
//
//		outputs("""
//		person.sayTwo(person.sayTwice(person.name), "bar") == "hoho"
//		|      |      |      |        |      |             |
//		|      |      |      FredFred |      Fred          false
//		|      |      Person(Fred,42) Person(Fred,42)
//		|      FredFredbar
//		Person(Fred,42)
//		""") {
//		expect {
//		person.sayTwo(person.sayTwice(person.name), "bar") == "hoho"
//		}
//		}
//		}
//
//@Test
//def constructor_call() {
//		val brand = "BMW"
//		val model = "M5"
//
//		outputs("""
//		new Car(brand, model).brand == "Audi"
//		|       |      |      |     |
//		BMW M5  BMW    M5     BMW   false
//		""") {
//		expect {
//		new Car(brand, model).brand == "Audi"
//		}
//		}
//		}
//
//@Test
//def higher_order_methods() {
//		outputs("""
//		a.map(_ * 2) == b
//		| |  |  |    |  |
//		| |  |  |    |  List(2, 4, 7)
//		| |  |  |    false
//		| |  |  <function1>
//| |  scala.collection.generic.GenTraversableFactory$ReusableCBF@...
//		| List(2, 4, 6)
//		List(1, 2, 3)
//		""") {
//		val a = List(1, 2, 3)
//		val b = List(2, 4, 7)
//		expect {
//		a.map(_ * 2) == b
//		}
//		}
//		}
//
//@Test
//def tuple() {
//		outputs("""
//		(1, 2)._1 == 3
//		|      |  |
//		(1,2)  1  false
//		""") {
//		expect {
//		(1, 2)._1 == 3
//		}
//		}
//		}
//
//@Test
//def case_class() {
//		outputs("""
//		Some(1).map(_ + 1) == Some(3)
//		|       |     |    |  |
//		Some(1) |     |    |  Some(3)
//		|     |    false
//		|     <function1>
//Some(2)
//		""") {
//		expect {
//		Some(1).map(_ + 1) == Some(3)
//		}
//		}
//		}
//
//@Test
//def class_with_package() {
//		outputs("""
//		collection.mutable.Map(1->"a").get(1) == "b"
//		|   ||      |      |
//		|   |(1,a)  |      false
//		|   |       Some(a)
//		|   scala.Predef$ArrowAssoc@...
//		Map(1 -> a)
//		""") {
//		expect {
//		collection.mutable.Map(1->"a").get(1) == "b"
//		}
//		}
//		}
//
//@Test
//def java_static_method() {
//		outputs("""
//		java.util.Collections.emptyList() == null
//		|           |
//		[]          false
//		""") {
//		expect {
//		java.util.Collections.emptyList() == null
//		}
//		}
//		}
//
//@Test
//def implicit_conversion() {
//		outputs("""
//		"fred".slice(1, 2) == "frog"
//		|      |           |
//		fred   r           false
//		""") {
//		expect {
//		"fred".slice(1, 2) == "frog"
//		}
//		}
//		}
//
//@Test
//def option_type() {
//		outputs(
//		"""
//		Some(23) == Some(22)
//		|        |  |
//		Some(23) |  Some(22)
//		false
//		""") {
//		expect {
//		Some(23) == Some(22)
//		}
//		}
//		}

class Person {
	private String name;
	private Integer age;

	public Person(String name, Integer age) {
		this.name = name;
		this.age = age;
	}

	public String doIt() {
		return "done";
	}

	public String sayTwice(String word) {
		return word + word;
	}

	public String sayTwo(String word1, String word2) {
		return word1 + word2;
	}

	public String sayAll(String... words) {
		String all = "";
		for(String word: words)
			all += word;
		return all;
	}

	public String getName() {
		return name;
	}

	public Integer getAge() {
		return age;
	}
}

class Car {
	private String brand;
	private String model;

	public Car(String brand, String model) {
		this.brand = brand;
		this.model = model;
	}

	@Override
	public String toString() {
		return brand + " " + model;
	}

	public String getBrand() {
		return brand;
	}

	public String getModel() {
		return model;
	}
}