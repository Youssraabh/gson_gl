package com.google.gson.internal.bind;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;

public abstract class ReadTypeAdaptersNumber {

    public Number read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return readNumber(in);
    }

    protected abstract Number readNumber(JsonReader in) throws IOException;
}
