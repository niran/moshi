/*
 * Copyright (C) 2010 Google Inc.
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
package com.squareup.moshi;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public final class TypesTest {

  @Test public void newParameterizedType() throws Exception {
    // List<A>. List is a top-level class.
    Type type = Types.newParameterizedType(List.class, A.class);
    assertThat(getFirstTypeArgument(type)).isEqualTo(A.class);

    // A<B>. A is a static inner class.
    type = Types.newParameterizedType(A.class, B.class);
    assertThat(getFirstTypeArgument(type)).isEqualTo(B.class);

    final class D {
    }
    try {
      // D<A> is not allowed since D is not a static inner class.
      Types.newParameterizedType(D.class, A.class);
      fail();
    } catch (IllegalArgumentException expected) {
    }

    // A<D> is allowed.
    type = Types.newParameterizedType(A.class, D.class);
    assertThat(getFirstTypeArgument(type)).isEqualTo(D.class);
  }

  @Test public void getFirstTypeArgument() throws Exception {
    assertNull(getFirstTypeArgument(A.class));

    Type type = Types.newParameterizedType(A.class, B.class, C.class);
    assertThat(getFirstTypeArgument(type)).isEqualTo(B.class);
  }

  @Test public void newParameterizedTypeObjectMethods() throws Exception {
    Type mapOfStringIntegerType = TypesTest.class.getDeclaredField(
        "mapOfStringInteger").getGenericType();
    ParameterizedType newMapType = Types.newParameterizedType(Map.class, String.class, Integer.class);
    assertThat(newMapType).isEqualTo(mapOfStringIntegerType);
    assertThat(newMapType.hashCode()).isEqualTo(mapOfStringIntegerType.hashCode());
    assertThat(newMapType.toString()).isEqualTo(mapOfStringIntegerType.toString());

    Type arrayListOfMapOfStringIntegerType = TypesTest.class.getDeclaredField(
        "arrayListOfMapOfStringInteger").getGenericType();
    ParameterizedType newListType = Types.newParameterizedType(ArrayList.class, newMapType);
    assertThat(newListType).isEqualTo(arrayListOfMapOfStringIntegerType);
    assertThat(newListType.hashCode()).isEqualTo(arrayListOfMapOfStringIntegerType.hashCode());
    assertThat(newListType.toString()).isEqualTo(arrayListOfMapOfStringIntegerType.toString());
  }

  private static final class A {
  }

  private static final class B {
  }

  private static final class C {
  }

  /**
   * Given a parameterized type {@code A<B, C>}, returns B. If the specified type is not a generic
   * type, returns null.
   */
  public static Type getFirstTypeArgument(Type type) throws Exception {
    if (!(type instanceof ParameterizedType)) return null;
    ParameterizedType ptype = (ParameterizedType) type;
    Type[] actualTypeArguments = ptype.getActualTypeArguments();
    if (actualTypeArguments.length == 0) return null;
    return Types.canonicalize(actualTypeArguments[0]);
  }

  Map<String, Integer> mapOfStringInteger;
  Map<String, Integer>[] arrayOfMapOfStringInteger;
  ArrayList<Map<String, Integer>> arrayListOfMapOfStringInteger;
  interface StringIntegerMap extends Map<String, Integer> {
  }

  @Test public void arrayComponentType() throws Exception {
    assertThat(Types.arrayComponentType(String[][].class)).isEqualTo(String[].class);
    assertThat(Types.arrayComponentType(String[].class)).isEqualTo(String.class);

    Type arrayOfMapOfStringIntegerType = TypesTest.class.getDeclaredField(
        "arrayOfMapOfStringInteger").getGenericType();
    Type mapOfStringIntegerType = TypesTest.class.getDeclaredField(
        "mapOfStringInteger").getGenericType();
    assertThat(Types.arrayComponentType(arrayOfMapOfStringIntegerType))
        .isEqualTo(mapOfStringIntegerType);
  }

  @Test public void collectionElementType() throws Exception {
    Type arrayListOfMapOfStringIntegerType = TypesTest.class.getDeclaredField(
        "arrayListOfMapOfStringInteger").getGenericType();
    Type mapOfStringIntegerType = TypesTest.class.getDeclaredField(
        "mapOfStringInteger").getGenericType();
    assertThat(Types.collectionElementType(arrayListOfMapOfStringIntegerType, List.class))
        .isEqualTo(mapOfStringIntegerType);
  }

  @Test public void mapKeyAndValueTypes() throws Exception {
    Type mapOfStringIntegerType = TypesTest.class.getDeclaredField(
        "mapOfStringInteger").getGenericType();
    assertThat(Types.mapKeyAndValueTypes(mapOfStringIntegerType, Map.class))
        .containsExactly(String.class, Integer.class);
  }

  @Test public void propertiesTypes() throws Exception {
    assertThat(Types.mapKeyAndValueTypes(Properties.class, Properties.class))
        .containsExactly(String.class, String.class);
  }

  @Test public void fixedVariablesTypes() throws Exception {
    assertThat(Types.mapKeyAndValueTypes(StringIntegerMap.class, StringIntegerMap.class))
        .containsExactly(String.class, Integer.class);
  }
}
