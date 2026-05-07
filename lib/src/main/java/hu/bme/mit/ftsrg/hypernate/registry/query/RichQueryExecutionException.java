/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.registry.query;

import hu.bme.mit.ftsrg.hypernate.registry.DataAccessException;
import lombok.experimental.StandardException;

/** Thrown when a rich query fails during execution or result materialization. */
@StandardException
public class RichQueryExecutionException extends DataAccessException {}
