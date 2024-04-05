package com.google.gson.stream;

import com.google.gson.internal.LazilyParsedNumber;

import java.io.IOException;


import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

public class JsonWriterErrorTest extends JsonWriterTest {

    @Test
    public void testInvalidTopLevelTypes() throws IOException {
        jsonWriter.beginObject(); // Commencez un objet
        try {
            jsonWriter.value("world"); // Essayez d'écrire une valeur sans écrire un nom au préalable
            jsonWriter.endObject(); // Terminez l'objet
            fail(); // Si aucune exception n'est levée, échouez le test
        } catch (IllegalStateException expected) {
            // Si une exception est levée, le test réussit
        }
    }
    @Override
    public void testTwoNames() throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("a");
        try {
            jsonWriter.name("a");
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    @Override
    public void testNameWithoutValue() throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("a");
        try {
            jsonWriter.endObject();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    @Override
    public void testValueWithoutName() throws IOException {
        jsonWriter.beginObject();
        try {
            jsonWriter.value(true);
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    @Override
    public void testMultipleTopLevelValues() throws IOException {
        jsonWriter.beginArray().endArray();
        try {
            jsonWriter.beginArray();
            fail();
        } catch (IllegalStateException expected) {
        }
    }



    @Override
    public void testBadNestingObject() throws IOException {
        jsonWriter.beginArray();
        jsonWriter.beginObject();
        try {
            jsonWriter.endArray();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    @Override
    public void testBadNestingArray() throws IOException {
        jsonWriter.beginArray();
        jsonWriter.beginArray();
        try {
            jsonWriter.endObject();
            fail();
        } catch (IllegalStateException expected) {
        }
    }



    @Override
    public void testNullName() throws IOException {
        jsonWriter.beginObject();
        try {
            jsonWriter.name(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    @Override
    public void testNonFiniteDoubles() throws IOException {
        jsonWriter.beginArray();
        try {
            jsonWriter.value(Double.NaN);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was NaN");
        }
        try {
            jsonWriter.value(Double.NEGATIVE_INFINITY);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was -Infinity");
        }
        try {
            jsonWriter.value(Double.POSITIVE_INFINITY);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was Infinity");
        }
    }

    @Override
    public void testNonFiniteNumbers() throws IOException {
        jsonWriter.beginArray();
        try {
            jsonWriter.value(Double.valueOf(Double.NaN));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was NaN");
        }
        try {
            jsonWriter.value(Double.valueOf(Double.NEGATIVE_INFINITY));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was -Infinity");
        }
        try {
            jsonWriter.value(Double.valueOf(Double.POSITIVE_INFINITY));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was Infinity");
        }
        try {
            jsonWriter.value(new LazilyParsedNumber("Infinity"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was Infinity");
        }
    }


    @Override
    public void testNonFiniteFloats() throws IOException {
        jsonWriter.beginArray();
        try {
            jsonWriter.value(Float.NaN);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was NaN");
        }
        try {
            jsonWriter.value(Float.NEGATIVE_INFINITY);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was -Infinity");
        }
        try {
            jsonWriter.value(Float.POSITIVE_INFINITY);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was Infinity");
        }
    }

    @Override
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
                jsonWriter.value(new LazilyParsedNumber(malformedNumber));
                fail("Should have failed writing malformed number: " + malformedNumber);
            } catch (IllegalArgumentException e) {
                assertThat(e.getMessage()).isEqualTo("String created by class com.google.gson.internal.LazilyParsedNumber is not a valid JSON number: " + malformedNumber);
            }
        }
    }
}
