/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.registry.query;

/** Fluent step for attaching a condition to a previously selected field. */
public final class ConditionStep<T> {

  private final RichQueryBuilder<T> builder;
  private final String field;

  ConditionStep(final RichQueryBuilder<T> builder, final String field) {
    this.builder = builder;
    this.field = field;
  }

  /**
   * Append an equality condition for the currently selected field.
   *
   * @param value the required field value
   * @return the parent builder for continued chaining
   */
  public RichQueryBuilder<T> is(final Object value) {
    return builder.addEqCondition(field, value);
  }
}
