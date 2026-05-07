/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.registry.query;

import hu.bme.mit.ftsrg.hypernate.util.JSON;
import java.util.LinkedHashMap;
import java.util.Map;

/** Translates a rich-query definition into CouchDB selector JSON. */
final class RichQueryTranslator {

  String toSelector(final RichQueryDefinition<?> definition) {
    final EntityMetadata metadata = EntityMetadata.forClass(definition.entityType());
    final Map<String, Object> selector = new LinkedHashMap<>();
    selector.put("docType", metadata.docType());

    for (final RichCondition condition : definition.conditions()) {
      if (condition instanceof EqCondition eqCondition) {
        selector.put(eqCondition.field(), Map.of("$eq", eqCondition.value()));
      } else {
        throw new InvalidRichQueryException(
            "Unsupported rich query condition type: " + condition.getClass().getName());
      }
    }

    return JSON.serialize(Map.of("selector", selector));
  }
}
