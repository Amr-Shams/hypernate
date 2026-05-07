/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.util;

import hu.bme.mit.ftsrg.hypernate.annotations.DocType;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import lombok.experimental.UtilityClass;

/** Shared entity-type naming rules used by composite keys and rich-query scoping. */
@UtilityClass
public final class EntityTypes {

  /**
   * Resolve the Fabric object type used for composite key namespaces.
   *
   * @param clazz the entity class
   * @return fully qualified uppercase class name
   */
  public static String keyType(final Class<?> clazz) {
    Objects.requireNonNull(clazz, "clazz must not be null");
    return clazz.getName().toUpperCase(Locale.ROOT);
  }

  /**
   * Resolve the persisted rich-query discriminator for an entity.
   *
   * <p>If {@link DocType} is present it is treated as an explicit persisted discriminator.
   * Otherwise the composite-key type name is reused as the default so key and document typing stay
   * aligned.
   *
   * @param clazz the entity class
   * @return persisted document discriminator
   */
  public static String docType(final Class<?> clazz) {
    Objects.requireNonNull(clazz, "clazz must not be null");
    return Optional.ofNullable(clazz.getAnnotation(DocType.class))
        .map(DocType::value)
        .orElseGet(() -> keyType(clazz));
  }
}
