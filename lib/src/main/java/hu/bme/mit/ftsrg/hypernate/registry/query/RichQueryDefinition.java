/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.registry.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Accumulated rich-query state before translation or execution. */
final class RichQueryDefinition<T> {

  private final Class<T> entityType;
  private final List<RichCondition> conditions = new ArrayList<>();

  RichQueryDefinition(final Class<T> entityType) {
    this.entityType = entityType;
  }

  Class<T> entityType() {
    return entityType;
  }

  List<RichCondition> conditions() {
    return Collections.unmodifiableList(conditions);
  }

  void addCondition(final RichCondition condition) {
    conditions.add(condition);
  }

  boolean hasConditionFor(final String field) {
    return conditions.stream().anyMatch(condition -> condition.field().equals(field));
  }
}
