/*
 * Copyright (C) 2014 Google Inc.
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
import static org.junit.Assume.assumeTrue;

import com.google.gson.JsonElement;
import com.google.gson.Strictness;
import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.JsonTreeReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@SuppressWarnings("resource")
@RunWith(Parameterized.class)
public class JsonReaderPathTest {
  @Parameterized.Parameters(name = "{0}")
  public static List<Object[]> parameters() {
    return Arrays.asList(
        new Object[] {Factory.STRING_READER}, new Object[] {Factory.OBJECT_READER});
  }

  @Parameterized.Parameter public Factory factory;

  @Test
  public void path() throws IOException {
    JsonReader reader = factory.create("{\"a\":[2,true,false,null,\"b\",{\"c\":\"d\"},[3]]}");
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.");
    assertThat(reader.getPathFile()).isEqualTo("$.");
    String unused1 = reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPathFile()).isEqualTo("$.a");
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[0]");
    assertThat(reader.getPathFile()).isEqualTo("$.a[0]");
    int unused2 = reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[0]");
    assertThat(reader.getPathFile()).isEqualTo("$.a[1]");
    boolean unused3 = reader.nextBoolean();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[1]");
    assertThat(reader.getPathFile()).isEqualTo("$.a[2]");
    boolean unused4 = reader.nextBoolean();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[2]");
    assertThat(reader.getPathFile()).isEqualTo("$.a[3]");
    reader.nextNull();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[3]");
    assertThat(reader.getPathFile()).isEqualTo("$.a[4]");
    String unused5 = reader.nextString();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[4]");
    assertThat(reader.getPathFile()).isEqualTo("$.a[5]");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[5].");
    assertThat(reader.getPathFile()).isEqualTo("$.a[5].");
    String unused6 = reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[5].c");
    assertThat(reader.getPathFile()).isEqualTo("$.a[5].c");
    String unused7 = reader.nextString();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[5].c");
    assertThat(reader.getPathFile()).isEqualTo("$.a[5].c");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[5]");
    assertThat(reader.getPathFile()).isEqualTo("$.a[6]");
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[6][0]");
    assertThat(reader.getPathFile()).isEqualTo("$.a[6][0]");
    int unused8 = reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[6][0]");
    assertThat(reader.getPathFile()).isEqualTo("$.a[6][1]");
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[6]");
    assertThat(reader.getPathFile()).isEqualTo("$.a[7]");
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPathFile()).isEqualTo("$.a");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");
  }

  @Test
  public void objectPath() throws IOException {
    JsonReader reader = factory.create("{\"a\":1,\"b\":2}");
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");

    JsonToken unused1 = reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.");
    assertThat(reader.getPathFile()).isEqualTo("$.");

    JsonToken unused2 = reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$.");
    assertThat(reader.getPathFile()).isEqualTo("$.");
    String unused3 = reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPathFile()).isEqualTo("$.a");

    JsonToken unused4 = reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPathFile()).isEqualTo("$.a");
    int unused5 = reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPathFile()).isEqualTo("$.a");

    JsonToken unused6 = reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPathFile()).isEqualTo("$.a");
    String unused7 = reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b");
    assertThat(reader.getPathFile()).isEqualTo("$.b");

    JsonToken unused8 = reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b");
    assertThat(reader.getPathFile()).isEqualTo("$.b");
    int unused9 = reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b");
    assertThat(reader.getPathFile()).isEqualTo("$.b");

    JsonToken unused10 = reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b");
    assertThat(reader.getPathFile()).isEqualTo("$.b");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");

    JsonToken unused11 = reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");
    reader.close();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");
  }

  @Test
  public void arrayPath() throws IOException {
    JsonReader reader = factory.create("[1,2]");
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");

    JsonToken unused1 = reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPathFile()).isEqualTo("$[0]");

    JsonToken unused2 = reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPathFile()).isEqualTo("$[0]");
    int unused3 = reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPathFile()).isEqualTo("$[1]");

    JsonToken unused4 = reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPathFile()).isEqualTo("$[1]");
    int unused5 = reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$[1]");
    assertThat(reader.getPathFile()).isEqualTo("$[2]");

    JsonToken unused6 = reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$[1]");
    assertThat(reader.getPathFile()).isEqualTo("$[2]");
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");

    JsonToken unused7 = reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");
    reader.close();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");
  }

  @Test
  public void multipleTopLevelValuesInOneDocument() throws IOException {
    assumeTrue(factory == Factory.STRING_READER);

    JsonReader reader = factory.create("[][]");
    reader.setStrictness(Strictness.LENIENT);
    reader.beginArray();
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");
    reader.beginArray();
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");
  }

  @Test
  public void skipArrayElements() throws IOException {
    JsonReader reader = factory.create("[1,2,3]");
    reader.beginArray();
    reader.skipValue();
    reader.skipValue();
    assertThat(reader.getPreviousPath()).isEqualTo("$[1]");
    assertThat(reader.getPathFile()).isEqualTo("$[2]");
  }

  @Test
  public void skipArrayEnd() throws IOException {
    JsonReader reader = factory.create("[[],1]");
    reader.beginArray();
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0][0]");
    assertThat(reader.getPathFile()).isEqualTo("$[0][0]");
    reader.skipValue(); // skip end of array
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPathFile()).isEqualTo("$[1]");
  }

  @Test
  public void skipObjectNames() throws IOException {
    JsonReader reader = factory.create("{\"a\":[]}");
    reader.beginObject();
    reader.skipValue();
    assertThat(reader.getPreviousPath()).isEqualTo("$.<skipped>");
    assertThat(reader.getPathFile()).isEqualTo("$.<skipped>");

    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$.<skipped>[0]");
    assertThat(reader.getPathFile()).isEqualTo("$.<skipped>[0]");
  }

  @Test
  public void skipObjectValues() throws IOException {
    JsonReader reader = factory.create("{\"a\":1,\"b\":2}");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.");
    assertThat(reader.getPathFile()).isEqualTo("$.");
    String unused1 = reader.nextName();
    reader.skipValue();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPathFile()).isEqualTo("$.a");
    String unused2 = reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b");
    assertThat(reader.getPathFile()).isEqualTo("$.b");
  }

  @Test
  public void skipObjectEnd() throws IOException {
    JsonReader reader = factory.create("{\"a\":{},\"b\":2}");
    reader.beginObject();
    String unused = reader.nextName();
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a.");
    assertThat(reader.getPathFile()).isEqualTo("$.a.");
    reader.skipValue(); // skip end of object
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPathFile()).isEqualTo("$.a");
  }

  @Test
  public void skipNestedStructures() throws IOException {
    JsonReader reader = factory.create("[[1,2,3],4]");
    reader.beginArray();
    reader.skipValue();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPathFile()).isEqualTo("$[1]");
  }

  @Test
  public void skipEndOfDocument() throws IOException {
    JsonReader reader = factory.create("[]");
    reader.beginArray();
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");
    reader.skipValue();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");
    reader.skipValue();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");
  }

  @Test
  public void arrayOfObjects() throws IOException {
    JsonReader reader = factory.create("[{},{},{}]");
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPathFile()).isEqualTo("$[0]");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0].");
    assertThat(reader.getPathFile()).isEqualTo("$[0].");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPathFile()).isEqualTo("$[1]");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$[1].");
    assertThat(reader.getPathFile()).isEqualTo("$[1].");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$[1]");
    assertThat(reader.getPathFile()).isEqualTo("$[2]");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$[2].");
    assertThat(reader.getPathFile()).isEqualTo("$[2].");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$[2]");
    assertThat(reader.getPathFile()).isEqualTo("$[3]");
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");
  }

  @Test
  public void arrayOfArrays() throws IOException {
    JsonReader reader = factory.create("[[],[],[]]");
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPathFile()).isEqualTo("$[0]");
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0][0]");
    assertThat(reader.getPathFile()).isEqualTo("$[0][0]");
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPathFile()).isEqualTo("$[1]");
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[1][0]");
    assertThat(reader.getPathFile()).isEqualTo("$[1][0]");
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[1]");
    assertThat(reader.getPathFile()).isEqualTo("$[2]");
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[2][0]");
    assertThat(reader.getPathFile()).isEqualTo("$[2][0]");
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[2]");
    assertThat(reader.getPathFile()).isEqualTo("$[3]");
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");
  }

  @Test
  public void objectOfObjects() throws IOException {
    JsonReader reader = factory.create("{\"a\":{\"a1\":1,\"a2\":2},\"b\":{\"b1\":1}}");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.");
    assertThat(reader.getPathFile()).isEqualTo("$.");
    String unused1 = reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPathFile()).isEqualTo("$.a");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a.");
    assertThat(reader.getPathFile()).isEqualTo("$.a.");
    String unused2 = reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a.a1");
    assertThat(reader.getPathFile()).isEqualTo("$.a.a1");
    int unused3 = reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a.a1");
    assertThat(reader.getPathFile()).isEqualTo("$.a.a1");
    String unused4 = reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a.a2");
    assertThat(reader.getPathFile()).isEqualTo("$.a.a2");
    int unused5 = reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a.a2");
    assertThat(reader.getPathFile()).isEqualTo("$.a.a2");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPathFile()).isEqualTo("$.a");
    String unused6 = reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b");
    assertThat(reader.getPathFile()).isEqualTo("$.b");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b.");
    assertThat(reader.getPathFile()).isEqualTo("$.b.");
    String unused7 = reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b.b1");
    assertThat(reader.getPathFile()).isEqualTo("$.b.b1");
    int unused8 = reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b.b1");
    assertThat(reader.getPathFile()).isEqualTo("$.b.b1");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b");
    assertThat(reader.getPathFile()).isEqualTo("$.b");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPathFile()).isEqualTo("$");
  }

  public enum Factory {
    STRING_READER {
      @Override
      public JsonReader create(String data) {
        return new JsonReader(new StringReader(data));
      }
    },
    OBJECT_READER {
      @Override
      public JsonReader create(String data) {
        JsonElement element = Streams.parse(new JsonReader(new StringReader(data)));
        return new JsonTreeReader(element);
      }
    };

    abstract JsonReader create(String data);
  }
}
