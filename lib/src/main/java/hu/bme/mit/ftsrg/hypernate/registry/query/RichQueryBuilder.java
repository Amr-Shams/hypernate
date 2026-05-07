/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.registry.query;

import java.util.List;
import java.util.Objects;

/** Lazy builder for a type-scoped CouchDB rich query selector. */
public final class RichQueryBuilder<T> {

  private final RichQueryDefinition<T> definition;
  private final EntityMetadata metadata;
  private final RichQueryTranslator translator;
  private final RichQueryExecutor executor;

  public RichQueryBuilder(final Class<T> entityType) {
    this(entityType, new RichQueryTranslator(), null);
  }

  public RichQueryBuilder(final Class<T> entityType, final RichQueryExecutor executor) {
    this(entityType, new RichQueryTranslator(), executor);
  }

  RichQueryBuilder(
      final Class<T> entityType,
      final RichQueryTranslator translator,
      final RichQueryExecutor executor) {
    Objects.requireNonNull(entityType, "entityType must not be null");
    this.translator = Objects.requireNonNull(translator, "translator must not be null");
    this.definition = new RichQueryDefinition<>(entityType);
    this.metadata = EntityMetadata.forClass(entityType);
    this.executor = executor;
  }

  /**
   * Select a field for the next condition.
   *
   * @param field entity field name
   * @return a condition step bound to the selected field
   */
  public ConditionStep<T> where(final String field) {
    validateField(field);
    return new ConditionStep<>(this, field);
  }

  /**
   * Alias for {@link #where(String)} used for chaining multiple conditions.
   *
   * @param field entity field name
   * @return a condition step bound to the selected field
   */
  public ConditionStep<T> and(final String field) {
    return where(field);
  }

  /**
   * Translate the accumulated definition to a CouchDB selector JSON string.
   *
   * @return serialized selector JSON
   */
  public String buildSelector() {
    return translator.toSelector(definition);
  }

  /**
   * Execute the accumulated selector against CouchDB and materialize typed results.
   *
   * <p>This builder is normally created through {@link
   * hu.bme.mit.ftsrg.hypernate.registry.Registry#richQuery(Class)} which wires the execution
   * backend automatically.
   *
   * @return query results deserialized to the requested entity type
   */
  public List<T> execute() {
    if (executor == null) {
      throw new InvalidRichQueryException(
          "This rich query builder was created without an execution backend");
    }

    return executor.execute(buildSelector(), definition.entityType());
  }

  RichQueryBuilder<T> addEqCondition(final String field, final Object value) {
    if (value == null) {
      throw new InvalidRichQueryException("Rich query equality conditions do not support null");
    }
    if (definition.hasConditionFor(field)) {
      throw new InvalidRichQueryException(
          "Rich query field '" + field + "' already has a condition in this builder");
    }

    definition.addCondition(new EqCondition(field, value));
    return this;
  }

  private void validateField(final String field) {
    if (field == null || field.isBlank()) {
      throw new InvalidRichQueryException("Rich query field name must not be blank");
    }
    if (!metadata.hasField(field)) {
      throw new InvalidRichQueryException(
          "Entity type "
              + definition.entityType().getName()
              + " does not declare a field named '"
              + field
              + "'");
    }
  }
}
