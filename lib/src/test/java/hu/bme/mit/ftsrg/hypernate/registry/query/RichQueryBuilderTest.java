/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.registry.query;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.bme.mit.ftsrg.hypernate.annotations.AttributeInfo;
import hu.bme.mit.ftsrg.hypernate.annotations.DocType;
import hu.bme.mit.ftsrg.hypernate.annotations.PrimaryKey;
import hu.bme.mit.ftsrg.hypernate.registry.Registry;
import hu.bme.mit.ftsrg.hypernate.util.EntityTypes;
import hu.bme.mit.ftsrg.hypernate.util.JSON;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.experimental.FieldNameConstants;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

@DisplayNameGeneration(ReplaceUnderscores.class)
class RichQueryBuilderTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private final ChaincodeStub stub = Mockito.mock(ChaincodeStub.class);
  private final Registry registry = new Registry(stub);

  @FieldNameConstants
  @PrimaryKey(@AttributeInfo(name = Asset.Fields.assetId))
  @DocType("ASSET")
  private record Asset(String assetId, String color, int size) {}

  @FieldNameConstants
  @PrimaryKey(@AttributeInfo(name = PlainAsset.Fields.assetId))
  private record PlainAsset(String assetId, String color) {}

  @Test
  void rich_query_builds_selector_with_doc_type_and_eq_condition() throws Exception {
    final String selector =
        registry.richQuery(Asset.class).where("color").is("blue").buildSelector();

    assertEquals(
        objectMapper.readTree(
            """
                    {
                      "selector": {
                        "docType": "ASSET",
                        "color": {
                          "$eq": "blue"
                        }
                      }
                    }
                    """),
        objectMapper.readTree(selector));
  }

  @Test
  void rich_query_supports_multiple_equality_conditions_through_and() throws Exception {
    final String selector =
        registry
            .richQuery(Asset.class)
            .where("color")
            .is("blue")
            .and("size")
            .is(10)
            .buildSelector();

    assertEquals(
        objectMapper.readTree(
            """
                    {
                      "selector": {
                        "docType": "ASSET",
                        "color": {
                          "$eq": "blue"
                        },
                        "size": {
                          "$eq": 10
                        }
                      }
                    }
                    """),
        objectMapper.readTree(selector));
  }

  @Test
  void rich_query_rejects_unknown_fields_early() {
    final InvalidRichQueryException exception =
        assertThrows(
            InvalidRichQueryException.class, () -> registry.richQuery(Asset.class).where("owner"));

    assertTrue(exception.getMessage().contains("does not declare a field named 'owner'"));
  }

  @Test
  void rich_query_rejects_duplicate_conditions_for_the_same_field() {
    final InvalidRichQueryException exception =
        assertThrows(
            InvalidRichQueryException.class,
            () -> registry.richQuery(Asset.class).where("color").is("blue").and("color").is("red"));

    assertTrue(exception.getMessage().contains("already has a condition"));
  }

  @Test
  void rich_query_uses_fully_qualified_uppercase_doc_type_as_default_fallback() throws Exception {
    final String selector =
        registry.richQuery(PlainAsset.class).where("color").is("blue").buildSelector();

    assertEquals(
        objectMapper.readTree(
            """
            {
              "selector": {
                "docType": "%s",
                "color": {
                  "$eq": "blue"
                }
              }
            }
            """
                .formatted(EntityTypes.docType(PlainAsset.class))),
        objectMapper.readTree(selector));
  }

  @Test
  void rich_query_execute_runs_selector_and_materializes_entities() throws Exception {
    final Asset asset = new Asset("asset-1", "blue", 10);
    final String persistedJson =
        JSON.serializeEntityDocument(EntityTypes.docType(Asset.class), asset);

    Mockito.when(stub.getQueryResult(Mockito.anyString()))
        .thenReturn(
            new QueryResultsIterator<>() {
              private boolean done = false;

              @Override
              public void close() {}

              @Override
              public @Nonnull Iterator<KeyValue> iterator() {
                return new Iterator<>() {
                  @Override
                  public boolean hasNext() {
                    return !done;
                  }

                  @Override
                  public KeyValue next() {
                    if (done) {
                      throw new UnsupportedOperationException();
                    }

                    done = true;

                    return new KeyValue() {
                      @Override
                      public String getKey() {
                        return "asset-1";
                      }

                      @Override
                      public byte[] getValue() {
                        return persistedJson.getBytes(StandardCharsets.UTF_8);
                      }

                      @Override
                      public String getStringValue() {
                        return persistedJson;
                      }
                    };
                  }
                };
              }
            });

    final List<Asset> results = registry.richQuery(Asset.class).where("color").is("blue").execute();

    assertEquals(List.of(asset), results);
    final ArgumentCaptor<String> selectorCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(stub).getQueryResult(selectorCaptor.capture());
    assertEquals(
        objectMapper.readTree(
            """
            {
              "selector": {
                "docType": "ASSET",
                "color": {
                  "$eq": "blue"
                }
              }
            }
            """),
        objectMapper.readTree(selectorCaptor.getValue()));
  }
}
