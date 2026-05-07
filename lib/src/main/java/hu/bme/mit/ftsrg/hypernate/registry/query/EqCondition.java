/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.registry.query;

record EqCondition(String field, Object value) implements RichCondition {}
