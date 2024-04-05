/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gson.metrics;

import com.google.caliper.BeforeExperiment;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;



public class BagOfPrimitivesDeserializationBenchmark extends AbstractDeserializationBenchmark {

  @BeforeExperiment
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  public void timeBagOfPrimitivesDefault(int reps) {
    for (int i = 0; i < reps; ++i) {
      gson.fromJson(json, BagOfPrimitives.class);
    }
  }

  @Override
  public void timeBagOfPrimitivesStreaming(int reps) throws IOException {
    for (int i = 0; i < reps; ++i) {
      JsonReader jr = new JsonReader(new StringReader(json));
      deserializeJsonReader(jr);
    }
  }

  @Override
public void timeBagOfPrimitivesReflectionStreaming(int reps) throws Exception {
    for (int i = 0; i < reps; ++i) {
        JsonReader jr = new JsonReader(new StringReader(json));
        // La méthode deserializeJsonReader est appelée, mais l'objet bag n'est pas utilisé.
        // Si l'utilisation de 'bag' n'est pas nécessaire pour le benchmark, retirez la variable.
        deserializeJsonReader(jr);
    }
}


  public static void main(String[] args) {
    CaliperRunnerBenchmark.run(BagOfPrimitivesDeserializationBenchmark.class, args);
  }
}