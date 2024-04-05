/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.stream;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import com.google.gson.FormattingStyle;

import com.google.gson.Strictness;
import com.google.gson.internal.LazilyParsedNumber;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;


@SuppressWarnings("resource")
public class JsonWriterTest {
  protected StringWriter stringWriter;
  protected JsonWriter jsonWriter;
  @Before
  public void setUp(){
    this.stringWriter= new StringWriter();
    this.jsonWriter= new JsonWriter(this.stringWriter);
  }

  @Test
  public void testDefaultStrictness() throws IOException {
    assertThat(jsonWriter.getStrictness()).isEqualTo(Strictness.LEGACY_STRICT);
    jsonWriter.value(false);
    jsonWriter.close();
  }

  @SuppressWarnings("deprecation") // for JsonWriter.setLenient
  @Test
  public void testSetLenientTrue() throws IOException {
    jsonWriter.setLenient(true);
    assertThat(jsonWriter.getStrictness()).isEqualTo(Strictness.LENIENT);
    jsonWriter.value(false);
    jsonWriter.close();
  }

  @SuppressWarnings("deprecation") // for JsonWriter.setLenient
  @Test
  public void testSetLenientFalse() throws IOException {
    jsonWriter.setLenient(false);
    assertThat(jsonWriter.getStrictness()).isEqualTo(Strictness.LEGACY_STRICT);
    jsonWriter.value(false);
    jsonWriter.close();
  }

  @Test
  public void testSetStrictnessNull() throws IOException {
    assertThrows(NullPointerException.class, () -> jsonWriter.setStrictness(null));
    jsonWriter.value(false);
    jsonWriter.close();
  }

  @Test
  public void testTopLevelValueTypes() throws IOException {
    jsonWriter.value(true);
    jsonWriter.close();
    assertThat(stringWriter.toString()).isEqualTo("true");

    setUp();
    jsonWriter.nullValue();
    jsonWriter.close();
    assertThat(stringWriter.toString()).isEqualTo("null");

    setUp();
    jsonWriter.value(123);
    jsonWriter.close();
    assertThat(stringWriter.toString()).isEqualTo("123");

    setUp();
    jsonWriter.value(123.4);
    jsonWriter.close();
    assertThat(stringWriter.toString()).isEqualTo("123.4");

    setUp();
    jsonWriter.value("a");
    jsonWriter.close();
    assertThat(stringWriter.toString()).isEqualTo("\"a\"");
  }

  @Test
  public void testNameAsTopLevelValue() throws IOException {
    IllegalStateException e =
        assertThrows(IllegalStateException.class, () -> jsonWriter.name("hello"));
    assertThat(e).hasMessageThat().isEqualTo("Please begin an object before writing a name.");

    jsonWriter.value(12);
    jsonWriter.close();

    e = assertThrows(IllegalStateException.class, () -> jsonWriter.name("hello"));
    assertThat(e).hasMessageThat().isEqualTo("JsonWriter is closed.");
  }

  @Test
  public void testNameInArray() throws IOException {
    jsonWriter.beginArray();
    IllegalStateException e =
        assertThrows(IllegalStateException.class, () -> jsonWriter.name("hello"));
    assertThat(e).hasMessageThat().isEqualTo("Please begin an object before writing a name.");

    jsonWriter.value(12);
    e = assertThrows(IllegalStateException.class, () -> jsonWriter.name("hello"));
    assertThat(e).hasMessageThat().isEqualTo("Please begin an object before writing a name.");

    jsonWriter.endArray();
    jsonWriter.close();

    assertThat(stringWriter.toString()).isEqualTo("[12]");
  }

  @Test
  public void testTwoNames() throws IOException {
    jsonWriter.beginObject();
    jsonWriter.name("a");
    try {
      jsonWriter.name("a");
      fail();
    } catch (IllegalStateException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Already wrote a name, expecting a value.");
    }
  }

  @Test
  public void testNameWithoutValue() throws IOException {
    jsonWriter.beginObject();
    jsonWriter.name("a");
    try {
      jsonWriter.endObject();
      fail();
    } catch (IllegalStateException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Dangling name: a");
    }
  }

  @Test
  public void testValueWithoutName() throws IOException {
    jsonWriter.beginObject();
    try {
      jsonWriter.value(true);
      fail();
    } catch (IllegalStateException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Nesting problem.");
    }
  }

  @Test
  public void testMultipleTopLevelValues() throws IOException {
    jsonWriter.beginArray().endArray();

    IllegalStateException expected =
        assertThrows(IllegalStateException.class, jsonWriter::beginArray);
    assertThat(expected).hasMessageThat().isEqualTo("JSON must have only one top-level value.");
  }

  @Test
  public void testMultipleTopLevelValuesStrict() throws IOException {
    jsonWriter.setStrictness(Strictness.STRICT);
    jsonWriter.beginArray().endArray();

    IllegalStateException expected =
        assertThrows(IllegalStateException.class, jsonWriter::beginArray);
    assertThat(expected).hasMessageThat().isEqualTo("JSON must have only one top-level value.");
  }

  @Test
  public void testMultipleTopLevelValuesLenient() throws IOException {
    jsonWriter.setStrictness(Strictness.LENIENT);
    jsonWriter.beginArray();
    jsonWriter.endArray();
    jsonWriter.beginArray();
    jsonWriter.endArray();
    jsonWriter.close();
    assertThat(stringWriter.toString()).isEqualTo("[][]");
  }

  @Test
  public void testBadNestingObject() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.beginObject();
    try {
      jsonWriter.endArray();
      fail();
    } catch (IllegalStateException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Nesting problem.");
    }
  }

  @Test
  public void testBadNestingArray() throws IOException {
    jsonWriter.beginArray();
    jsonWriter.beginArray();
    try {
      jsonWriter.endObject();
      fail();
    } catch (IllegalStateException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Nesting problem.");
    }
  }

  @Test
  public void testNullName() throws IOException {
    jsonWriter.beginObject();
    try {
      jsonWriter.name(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testNullStringValue() throws IOException {
    jsonWriter.beginObject();
    jsonWriter.name("a");
    jsonWriter.value((String) null);
    jsonWriter.endObject();
    assertThat(stringWriter.toString()).isEqualTo("{\"a\":null}");
  }

  @Test
  public void testJsonValue() throws IOException {
    jsonWriter.beginObject();
    jsonWriter.name("a");
    jsonWriter.jsonValue("{\"b\":true}");
    jsonWriter.name("c");
    jsonWriter.value(1);
    jsonWriter.endObject();
    assertThat(stringWriter.toString()).isEqualTo("{\"a\":{\"b\":true},\"c\":1}");
  }

  private static void assertNonFiniteFloatsExceptions(JsonWriter jsonWriter) throws IOException {
    jsonWriter.beginArray();

    IllegalArgumentException expected =
        assertThrows(IllegalArgumentException.class, () -> jsonWriter.value(Float.NaN));
    assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was NaN");

    expected =
        assertThrows(
            IllegalArgumentException.class, () -> jsonWriter.value(Float.NEGATIVE_INFINITY));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Numeric values must be finite, but was -Infinity");

    expected =
        assertThrows(
            IllegalArgumentException.class, () -> jsonWriter.value(Float.POSITIVE_INFINITY));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Numeric values must be finite, but was Infinity");
  }

  @Test
  public void testNonFiniteFloats() throws IOException {
    assertNonFiniteFloatsExceptions(this.jsonWriter);
  }

  @Test
  public void testNonFiniteFloatsWhenStrict() throws IOException {
    this.jsonWriter.setStrictness(Strictness.STRICT);
    assertNonFiniteFloatsExceptions(this.jsonWriter);
  }

  private static void assertNonFiniteDoublesExceptions(JsonWriter jsonWriter) throws IOException {
    jsonWriter.beginArray();

    IllegalArgumentException expected =
        assertThrows(IllegalArgumentException.class, () -> jsonWriter.value(Double.NaN));
    assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was NaN");

    expected =
        assertThrows(
            IllegalArgumentException.class, () -> jsonWriter.value(Double.NEGATIVE_INFINITY));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Numeric values must be finite, but was -Infinity");

    expected =
        assertThrows(
            IllegalArgumentException.class, () -> jsonWriter.value(Double.POSITIVE_INFINITY));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Numeric values must be finite, but was Infinity");
  }

  @Test
  public void testNonFiniteDoubles() throws IOException {
    assertNonFiniteDoublesExceptions(this.jsonWriter);
  }

  @Test
  public void testNonFiniteDoublesWhenStrict() throws IOException {
    this.jsonWriter.setStrictness(Strictness.STRICT);
    assertNonFiniteDoublesExceptions(this.jsonWriter);
  }

  private static void assertNonFiniteNumbersExceptions(JsonWriter jsonWriter) throws IOException {
    jsonWriter.beginArray();

    IllegalArgumentException expected =
        assertThrows(
            IllegalArgumentException.class, () -> jsonWriter.value(Double.valueOf(Double.NaN)));
    assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was NaN");

    expected =
        assertThrows(
            IllegalArgumentException.class,
            () -> jsonWriter.value(Double.valueOf(Double.NEGATIVE_INFINITY)));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Numeric values must be finite, but was -Infinity");

    expected =
        assertThrows(
            IllegalArgumentException.class,
            () -> jsonWriter.value(Double.valueOf(Double.POSITIVE_INFINITY)));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Numeric values must be finite, but was Infinity");

    expected =
        assertThrows(
            IllegalArgumentException.class,
            () -> jsonWriter.value(new LazilyParsedNumber("Infinity")));
    assertThat(expected)
        .hasMessageThat()
        .isEqualTo("Numeric values must be finite, but was Infinity");
  }

  @Test
  public void testNonFiniteNumbers() throws IOException {
    assertNonFiniteNumbersExceptions(this.jsonWriter);
  }

  @Test
  public void testNonFiniteNumbersWhenStrict() throws IOException {
    this.jsonWriter.setStrictness(Strictness.STRICT);
    assertNonFiniteNumbersExceptions(this.jsonWriter);
  }

  @Test
  public void testNonFiniteFloatsWhenLenient() throws IOException {
    this.jsonWriter.setStrictness(Strictness.LENIENT);
    this.jsonWriter.beginArray();
    this.jsonWriter.value(Float.NaN);
    this.jsonWriter.value(Float.NEGATIVE_INFINITY);
    this.jsonWriter.value(Float.POSITIVE_INFINITY);
    this.jsonWriter.endArray();
    assertThat(this.stringWriter.toString()).isEqualTo("[NaN,-Infinity,Infinity]");
  }

  @Test
  public void testNonFiniteDoublesWhenLenient() throws IOException {
    this.jsonWriter.setStrictness(Strictness.LENIENT);
    this.jsonWriter.beginArray();
    this.jsonWriter.value(Double.NaN);
    this.jsonWriter.value(Double.NEGATIVE_INFINITY);
    this.jsonWriter.value(Double.POSITIVE_INFINITY);
    this.jsonWriter.endArray();
    assertThat(this.stringWriter.toString()).isEqualTo("[NaN,-Infinity,Infinity]");
  }

  @Test
  public void testNonFiniteNumbersWhenLenient() throws IOException {
    this.jsonWriter.setStrictness(Strictness.LENIENT);
    this.jsonWriter.beginArray();
    this.jsonWriter.value(Double.valueOf(Double.NaN));
    this.jsonWriter.value(Double.valueOf(Double.NEGATIVE_INFINITY));
    this.jsonWriter.value(Double.valueOf(Double.POSITIVE_INFINITY));
    this.jsonWriter.value(new LazilyParsedNumber("Infinity"));
    this.jsonWriter.endArray();
    assertThat(this.stringWriter.toString()).isEqualTo("[NaN,-Infinity,Infinity,Infinity]");
  }

  @Test
  public void testFloats() throws IOException {
    this.jsonWriter.beginArray();
    this.jsonWriter.value(-0.0f);
    this.jsonWriter.value(1.0f);
    this.jsonWriter.value(Float.MAX_VALUE);
    this.jsonWriter.value(Float.MIN_VALUE);
    this.jsonWriter.value(0.0f);
    this.jsonWriter.value(-0.5f);
    this.jsonWriter.value(2.2250739E-38f);
    this.jsonWriter.value(3.723379f);
    this.jsonWriter.value((float) Math.PI);
    this.jsonWriter.value((float) Math.E);
    this.jsonWriter.endArray();
    this.jsonWriter.close();
    assertThat(this.stringWriter.toString())
        .isEqualTo(
            "[-0.0,"
                + "1.0,"
                + "3.4028235E38,"
                + "1.4E-45,"
                + "0.0,"
                + "-0.5,"
                + "2.2250739E-38,"
                + "3.723379,"
                + "3.1415927,"
                + "2.7182817]");
  }

  @Test
  public void testDoubles() throws IOException {
    this.jsonWriter.beginArray();
    this.jsonWriter.value(-0.0);
    this.jsonWriter.value(1.0);
    this.jsonWriter.value(Double.MAX_VALUE);
    this.jsonWriter.value(Double.MIN_VALUE);
    this.jsonWriter.value(0.0);
    this.jsonWriter.value(-0.5);
    this.jsonWriter.value(2.2250738585072014E-308);
    this.jsonWriter.value(Math.PI);
    this.jsonWriter.value(Math.E);
    this.jsonWriter.endArray();
    this.jsonWriter.close();
    assertThat(this.stringWriter.toString())
        .isEqualTo(
            "[-0.0,"
                + "1.0,"
                + "1.7976931348623157E308,"
                + "4.9E-324,"
                + "0.0,"
                + "-0.5,"
                + "2.2250738585072014E-308,"
                + "3.141592653589793,"
                + "2.718281828459045]");
  }

  @Test
  public void testLongs() throws IOException {
    this.jsonWriter.beginArray();
    this.jsonWriter.value(0);
    this.jsonWriter.value(1);
    this.jsonWriter.value(-1);
    this.jsonWriter.value(Long.MIN_VALUE);
    this.jsonWriter.value(Long.MAX_VALUE);
    this.jsonWriter.endArray();
    this.jsonWriter.close();
    assertThat(this.stringWriter.toString())
        .isEqualTo("[0," + "1," + "-1," + "-9223372036854775808," + "9223372036854775807]");
  }

  @Test
  public void testNumbers() throws IOException {
    this.jsonWriter.beginArray();
    this.jsonWriter.value(new BigInteger("0"));
    this.jsonWriter.value(new BigInteger("9223372036854775808"));
    this.jsonWriter.value(new BigInteger("-9223372036854775809"));
    this.jsonWriter.value(new BigDecimal("3.141592653589793238462643383"));
    this.jsonWriter.endArray();
    this.jsonWriter.close();
    assertThat(this.stringWriter.toString())
        .isEqualTo(
            "[0,"
                + "9223372036854775808,"
                + "-9223372036854775809,"
                + "3.141592653589793238462643383]");
  }

  @Test
public void testNumbersCustomClass() throws IOException {
  String[] validNumbers = {
    "-0.0",
    "1.0",
    "1.7976931348623157E308",
    "4.9E-324",
    "0.0",
    "0.00",
    "-0.5",
    "2.2250738585072014E-308",
    "3.141592653589793",
    "2.718281828459045",
    "0",
    "0.01",
    "0e0",
    "1e+0",
    "1e-0",
    "1e0000", // leading 0 is allowed for exponent
    "1e00001",
    "1e+1",
  };

  this.jsonWriter.beginArray(); // Start the JSON array

  for (String validNumber : validNumbers) {
    this.jsonWriter.value(new LazilyParsedNumber(validNumber));
  }

  this.jsonWriter.endArray(); // End the JSON array
  this.jsonWriter.close();

  // Check the result after all numbers have been written
  String expected = "[" + String.join(",", validNumbers) + "]";
  assertThat(this.stringWriter.toString()).isEqualTo(expected);
}

  @Test
  public void testMalformedNumbers() throws IOException {
    String[] malformedNumbers = {
      "some text",
      "",
      ".",
      "00",
      "01",
      "-00",
      "-",
      "--1",
      "+1", // plus sign is not allowed for integer part
      "+",
      "1,0",
      "1,000",
      "0.", // decimal digit is required
      ".1", // integer part is required
      "e1",
      ".e1",
      ".1e1",
      "1e-",
      "1e+",
      "1e--1",
      "1e+-1",
      "1e1e1",
      "1+e1",
      "1e1.0",
    };

    for (String malformedNumber : malformedNumbers) {
      try {
        this.jsonWriter.value(new LazilyParsedNumber(malformedNumber));
        fail("Should have failed writing malformed number: " + malformedNumber);
      } catch (IllegalArgumentException e) {
        assertThat(e)
            .hasMessageThat()
            .isEqualTo(
                "String created by class com.google.gson.internal.LazilyParsedNumber is not a valid"
                    + " JSON number: "
                    + malformedNumber);
      }
    }
  }

  @Test
  public void testBooleans() throws IOException {
    this.jsonWriter.beginArray();
    this.jsonWriter.value(true);
    this.jsonWriter.value(false);
    this.jsonWriter.endArray();
    assertThat(this.stringWriter.toString()).isEqualTo("[true,false]");
  }

  @Test
  public void testBoxedBooleans() throws IOException {
      jsonWriter.beginArray();
      jsonWriter.value((Boolean) true);
      jsonWriter.value((Boolean) false);
      jsonWriter.value((Boolean) null);
      jsonWriter.endArray();
      assertThat(stringWriter.toString()).isEqualTo("[true,false,null]");
  }

  @Test
  public void testNulls() throws IOException {
      jsonWriter.beginArray();
      jsonWriter.nullValue();
      jsonWriter.endArray();
      assertThat(stringWriter.toString()).isEqualTo("[null]");
  }

  @Test
  public void testStrings() throws IOException {
      jsonWriter.beginArray();
      jsonWriter.value("a");
      jsonWriter.value("a\"");
      jsonWriter.value("\"");
      jsonWriter.value(":");
      jsonWriter.value(",");
      jsonWriter.value("\b");
      jsonWriter.value("\f");
      jsonWriter.value("\n");
      jsonWriter.value("\r");
      jsonWriter.value("\t");
      jsonWriter.value(" ");
      jsonWriter.value("\\");
      jsonWriter.value("{");
      jsonWriter.value("}");
      jsonWriter.value("[");
      jsonWriter.value("]");
      jsonWriter.value("\0");
      jsonWriter.value("\u0019");
      jsonWriter.endArray();
      assertThat(stringWriter.toString())
          .isEqualTo(
              "[\"a\","
                  + "\"a\\\"\","
                  + "\"\\\"\","
                  + "\":\","
                  + "\",\","
                  + "\"\\b\","
                  + "\"\\f\","
                  + "\"\\n\","
                  + "\"\\r\","
                  + "\"\\t\","
                  + "\" \","
                  + "\"\\\\\","
                  + "\"{\","
                  + "\"}\","
                  + "\"[\","
                  + "\"]\","
                  + "\"\\u0000\","
                  + "\"\\u0019\"]");
  }

  @Test
  public void testUnicodeLineBreaksEscaped() throws IOException {
      jsonWriter.beginArray();
      jsonWriter.value("\u2028 \u2029");
      jsonWriter.endArray();
      // JSON specification does not require that they are escaped, but Gson escapes them for
      // compatibility with JavaScript where they are considered line breaks
      assertThat(stringWriter.toString()).isEqualTo("[\"\\u2028 \\u2029\"]");
  }

  @Test
  public void testEmptyArray() throws IOException {
      jsonWriter.beginArray();
      jsonWriter.endArray();
      assertThat(stringWriter.toString()).isEqualTo("[]");
  }

  @Test
  public void testEmptyObject() throws IOException {
      jsonWriter.beginObject();
      jsonWriter.endObject();
      assertThat(stringWriter.toString()).isEqualTo("{}");
  }

  @Test
  public void testObjectsInArrays() throws IOException {
      jsonWriter.beginArray();
      jsonWriter.beginObject();
      jsonWriter.name("a").value(5);
      jsonWriter.name("b").value(false);
      jsonWriter.endObject();
      jsonWriter.beginObject();
      jsonWriter.name("c").value(6);
      jsonWriter.name("d").value(true);
      jsonWriter.endObject();
      jsonWriter.endArray();
      assertThat(stringWriter.toString())
          .isEqualTo("[{\"a\":5,\"b\":false}," + "{\"c\":6,\"d\":true}]");
  }

  @Test
  public void testArraysInObjects() throws IOException {
      jsonWriter.beginObject();
      jsonWriter.name("a");
      jsonWriter.beginArray();
      jsonWriter.value(5);
      jsonWriter.value(false);
      jsonWriter.endArray();
      jsonWriter.name("b");
      jsonWriter.beginArray();
      jsonWriter.value(6);
      jsonWriter.value(true);
      jsonWriter.endArray();
      jsonWriter.endObject();
      assertThat(stringWriter.toString()).isEqualTo("{\"a\":[5,false]," + "\"b\":[6,true]}");
  }

  @Test
  public void testDeepNestingArrays() throws IOException {
      for (int i = 0; i < 20; i++) {
        jsonWriter.beginArray();
      }
      for (int i = 0; i < 20; i++) {
        jsonWriter.endArray();
      }
      assertThat(stringWriter.toString()).isEqualTo("[[[[[[[[[[[[[[[[[[[[]]]]]]]]]]]]]]]]]]]]");
  }

  @Test
  public void testDeepNestingObjects() throws IOException {
      jsonWriter.beginObject();
      for (int i = 0; i < 20; i++) {
        jsonWriter.name("a");
        jsonWriter.beginObject();
      }
      for (int i = 0; i < 20; i++) {
        jsonWriter.endObject();
      }
      jsonWriter.endObject();
      assertThat(stringWriter.toString())
          .isEqualTo(
              "{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":"
                  + "{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{"
                  + "}}}}}}}}}}}}}}}}}}}}}");
  }
  @Test
  public void testRepeatedName() throws IOException {
      jsonWriter.beginObject();
      jsonWriter.name("a").value(true);
      jsonWriter.name("a").value(false);
      jsonWriter.endObject();
      // JsonWriter doesn't attempt to detect duplicate names
      assertThat(stringWriter.toString()).isEqualTo("{\"a\":true,\"a\":false}");
  }

  @Test
  public void testPrettyPrintObject() throws IOException {
      jsonWriter.setIndent("   ");

      jsonWriter.beginObject();
      jsonWriter.name("a").value(true);
      jsonWriter.name("b").value(false);
      jsonWriter.name("c").value(5.0);
      jsonWriter.name("e").nullValue();
      jsonWriter.name("f").beginArray();
      jsonWriter.value(6.0);
      jsonWriter.value(7.0);
      jsonWriter.endArray();
      jsonWriter.name("g").beginObject();
      jsonWriter.name("h").value(8.0);
      jsonWriter.name("i").value(9.0);
      jsonWriter.endObject();
      jsonWriter.endObject();

      String expected =
          "{\n"
              + "   \"a\": true,\n"
              + "   \"b\": false,\n"
              + "   \"c\": 5.0,\n"
              + "   \"e\": null,\n"
              + "   \"f\": [\n"
              + "      6.0,\n"
              + "      7.0\n"
              + "   ],\n"
              + "   \"g\": {\n"
              + "      \"h\": 8.0,\n"
              + "      \"i\": 9.0\n"
              + "   }\n"
              + "}";
      assertThat(stringWriter.toString()).isEqualTo(expected);
  }

  @Test
  public void testPrettyPrintArray() throws IOException {
      jsonWriter.setIndent("   ");

      jsonWriter.beginArray();
      jsonWriter.value(true);
      jsonWriter.value(false);
      jsonWriter.value(5.0);
      jsonWriter.nullValue();
      jsonWriter.beginObject();
      jsonWriter.name("a").value(6.0);
      jsonWriter.name("b").value(7.0);
      jsonWriter.endObject();
      jsonWriter.beginArray();
      jsonWriter.value(8.0);
      jsonWriter.value(9.0);
      jsonWriter.endArray();
      jsonWriter.endArray();

      String expected =
          "[\n"
              + "   true,\n"
              + "   false,\n"
              + "   5.0,\n"
              + "   null,\n"
              + "   {\n"
              + "      \"a\": 6.0,\n"
              + "      \"b\": 7.0\n"
              + "   },\n"
              + "   [\n"
              + "      8.0,\n"
              + "      9.0\n"
              + "   ]\n"
              + "]";
      assertThat(stringWriter.toString()).isEqualTo(expected);
  }

  @Test
  public void testClosedWriterThrowsOnStructure() throws IOException {
      jsonWriter.beginArray();
      jsonWriter.endArray();
      jsonWriter.close();
      try {
        jsonWriter.beginArray();
        fail();
      } catch (IllegalStateException expected) {
      }
      try {
        jsonWriter.endArray();
        fail();
      } catch (IllegalStateException expected) {
      }
      try {
        jsonWriter.beginObject();
        fail();
      } catch (IllegalStateException expected) {
      }
      try {
        jsonWriter.endObject();
        fail();
      } catch (IllegalStateException expected) {
      }
  }

  @Test
  public void testClosedWriterThrowsOnName() throws IOException {
      jsonWriter.beginArray();
      jsonWriter.endArray();
      jsonWriter.close();
      try {
        jsonWriter.name("a");
        fail();
      } catch (IllegalStateException expected) {
      }
  }
  @Test
  public void testClosedWriterThrowsOnValue() throws IOException {
      jsonWriter.beginArray();
      jsonWriter.endArray();
      jsonWriter.close();
      try {
        jsonWriter.value("a");
        fail();
      } catch (IllegalStateException expected) {
      }
  }

  @Test
  public void testClosedWriterThrowsOnFlush() throws IOException {
      jsonWriter.beginArray();
      jsonWriter.endArray();
      jsonWriter.close();
      try {
        jsonWriter.flush();
        fail();
      } catch (IllegalStateException expected) {
      }
  }

  @Test
  public void testWriterCloseIsIdempotent() throws IOException {
      jsonWriter.beginArray();
      jsonWriter.endArray();
      jsonWriter.close();
      jsonWriter.close();
  }

  @Test
  public void testSetGetFormattingStyle() throws IOException {
      String lineSeparator = "\r\n";

      // Default should be FormattingStyle.COMPACT
      assertThat(jsonWriter.getFormattingStyle()).isSameInstanceAs(FormattingStyle.COMPACT);
      jsonWriter.setFormattingStyle(
          FormattingStyle.PRETTY.withIndent(" \t ").withNewline(lineSeparator));

      jsonWriter.beginArray();
      jsonWriter.value(true);
      jsonWriter.value("text");
      jsonWriter.value(5.0);
      jsonWriter.nullValue();
      jsonWriter.endArray();

      String expected =
          "[\r\n" //
              + " \t true,\r\n" //
              + " \t \"text\",\r\n" //
              + " \t 5.0,\r\n" //
              + " \t null\r\n" //
              + "]";
      assertThat(stringWriter.toString()).isEqualTo(expected);

      assertThat(jsonWriter.getFormattingStyle().getNewline()).isEqualTo(lineSeparator);
  }
  @Test
  public void testIndentOverwritesFormattingStyle() throws IOException {
      jsonWriter.setFormattingStyle(FormattingStyle.COMPACT);
      // Should overwrite formatting style
      jsonWriter.setIndent("  ");

      jsonWriter.beginObject();
      jsonWriter.name("a");
      jsonWriter.beginArray();
      jsonWriter.value(1);
      jsonWriter.value(2);
      jsonWriter.endArray();
      jsonWriter.endObject();

      String expected =
          "{\n" //
              + "  \"a\": [\n" //
              + "    1,\n" //
              + "    2\n" //
              + "  ]\n" //
              + "}";
      assertThat(stringWriter.toString()).isEqualTo(expected);
  }
}
