/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.util;

import static org.assertj.core.api.Assertions.*;

import hu.bme.mit.ftsrg.hypernate.registry.SerializationException;
import org.junit.jupiter.api.Test;

public class JSONTest {

  @Test
  public void givenObject_whenSerialize_thenReturnJSONString() throws SerializationException {
    /* --- given --- */
    var obj = new Foo("abc");

    /* --- when --- */
    String json = JSON.serialize(obj);

    /* --- then --- */
    assertThat(json).isEqualToIgnoringWhitespace("{\"string\": \"abc\"}");
  }

  @Test
  public void givenObject_whenSerialize_thenReturnJSONStringWithAlphabeticallyOrderedKeys()
      throws SerializationException {
    /* --- given --- */
    var obj = new Bar("abc", 100);

    /* --- when --- */
    String json = JSON.serialize(obj);

    /* --- then --- */
    assertThat(json).isEqualToIgnoringWhitespace("{\"number\": 100, \"string\": \"abc\"}");
  }

  @Test
  public void given_json_with_unknown_doc_type_when_deserialize_then_ignore_extra_field()
      throws SerializationException {
    /* --- given --- */
    String json = "{\"docType\":\"TEST\",\"string\":\"abc\"}";

    /* --- when --- */
    Foo obj = JSON.deserialize(json, Foo.class);

    /* --- then --- */
    assertThat(obj).isEqualTo(new Foo("abc"));
  }

  private record Foo(String string) {}

  private record Bar(String string, int number) {}
}
