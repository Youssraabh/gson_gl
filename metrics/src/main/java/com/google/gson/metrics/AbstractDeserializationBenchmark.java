package com.google.gson.metrics;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.lang.reflect.Field;

public abstract class AbstractDeserializationBenchmark {

    protected Gson gson;
    protected String json;
  
    protected void setUp() throws Exception {
      this.gson = new Gson();
      BagOfPrimitives bag = new BagOfPrimitives(10L, 1, false, "foo");
      this.json = gson.toJson(bag);
    }
  
    protected BagOfPrimitives deserializeJsonReader(JsonReader jr) throws IOException {
      jr.beginObject();
      BagOfPrimitives bag = new BagOfPrimitives();
      while (jr.hasNext()) {
        String name = jr.nextName();
        setBagOfPrimitivesField(bag, name, jr);
      }
      jr.endObject();
      return bag;
    }
  
    protected void setBagOfPrimitivesField(BagOfPrimitives bag, String fieldName, JsonReader jr) throws IOException {
      try {
        Field field = BagOfPrimitives.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        Class<?> fieldType = field.getType();
        if (fieldType.equals(long.class)) {
          field.setLong(bag, jr.nextLong());
        } else if (fieldType.equals(int.class)) {
          field.setInt(bag, jr.nextInt());
        } else if (fieldType.equals(boolean.class)) {
          field.setBoolean(bag, jr.nextBoolean());
        } else if (fieldType.equals(String.class)) {
          field.set(bag, jr.nextString());
        } else {
          throw new IOException("Unexpected field type: " + fieldType);
        }
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new IOException("Error setting field: " + fieldName, e);
      }
    }
    
    // Abstract methods to be implemented by subclasses for different benchmarks
    public abstract void timeBagOfPrimitivesDefault(int reps);
    public abstract void timeBagOfPrimitivesStreaming(int reps) throws IOException;
    public abstract void timeBagOfPrimitivesReflectionStreaming(int reps) throws Exception;
  }
