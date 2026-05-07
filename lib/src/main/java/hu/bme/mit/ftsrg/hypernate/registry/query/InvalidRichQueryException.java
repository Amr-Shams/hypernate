/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.registry.query;

import hu.bme.mit.ftsrg.hypernate.registry.DataAccessException;
import lombok.experimental.StandardException;

/** Thrown when a rich query cannot be built from the provided entity metadata or inputs. */
@StandardException
public class InvalidRichQueryException extends DataAccessException {}
