/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.registry.query;

import hu.bme.mit.ftsrg.hypernate.annotations.AttributeInfo;
import hu.bme.mit.ftsrg.hypernate.annotations.QueryIndex;
import hu.bme.mit.ftsrg.hypernate.util.EntityTypes;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/** Cached reflection-backed metadata for entity validation in query builders. */
final class EntityMetadata {

  private static final ConcurrentMap<Class<?>, EntityMetadata> CACHE = new ConcurrentHashMap<>();

  private final Set<String> fields;
  private final Set<String> indexedFields;
  private final String docType;

  private EntityMetadata(final Class<?> entityClass) {
    final Set<String> discoveredFields =
        Arrays.stream(entityClass.getDeclaredFields())
            .filter(field -> !field.isSynthetic())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .map(Field::getName)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    this.fields = Collections.unmodifiableSet(discoveredFields);

    final Set<String> discoveredIndexedFields =
        Arrays.stream(entityClass.getAnnotationsByType(QueryIndex.class))
            .flatMap(queryIndex -> Arrays.stream(queryIndex.attributes()))
            .map(AttributeInfo::name)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    this.indexedFields = Collections.unmodifiableSet(discoveredIndexedFields);

    this.docType = EntityTypes.docType(entityClass);
  }

  static EntityMetadata forClass(final Class<?> entityClass) {
    Objects.requireNonNull(entityClass, "entityClass must not be null");
    return CACHE.computeIfAbsent(entityClass, EntityMetadata::new);
  }

  String docType() {
    return docType;
  }

  boolean hasField(final String field) {
    return fields.contains(field);
  }

  boolean hasIndexedField(final String field) {
    return indexedFields.contains(field);
  }
}
