/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.registry.query;

import hu.bme.mit.ftsrg.hypernate.util.JSON;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

/** Executes translated rich queries against the Fabric stub and materializes typed results. */
public final class RichQueryExecutor {

  private final ChaincodeStub stub;

  public RichQueryExecutor(final ChaincodeStub stub) {
    this.stub = Objects.requireNonNull(stub, "stub must not be null");
  }

  public <T> List<T> execute(final String selector, final Class<T> entityType) {
    Objects.requireNonNull(selector, "selector must not be null");
    Objects.requireNonNull(entityType, "entityType must not be null");

    final QueryResultsIterator<KeyValue> results;
    try {
      results = stub.getQueryResult(selector);
    } catch (RuntimeException e) {
      throw new RichQueryExecutionException(
          "Failed to execute rich query for " + entityType.getName(), e);
    }

    RichQueryExecutionException failure = null;
    try {
      final List<T> entities = new ArrayList<>();
      for (final KeyValue result : results) {
        try {
          entities.add(
              JSON.deserialize(new String(result.getValue(), StandardCharsets.UTF_8), entityType));
        } catch (RuntimeException e) {
          throw failure =
              new RichQueryExecutionException(
                  "Failed to materialize rich query result for " + entityType.getName(), e);
        }
      }
      return entities;
    } catch (RichQueryExecutionException e) {
      failure = e;
      throw e;
    } catch (RuntimeException e) {
      throw failure =
          new RichQueryExecutionException(
              "Failed to execute rich query for " + entityType.getName(), e);
    } finally {
      try {
        results.close();
      } catch (Exception e) {
        if (failure != null) {
          failure.addSuppressed(e);
        } else {
          throw new RichQueryExecutionException(
              "Failed to close rich query results for " + entityType.getName(), e);
        }
      }
    }
  }
}
