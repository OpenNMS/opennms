package org.opennms.netmgt.measurements.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.MapContext;
import org.opennms.netmgt.measurements.api.ExpressionEngine;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.api.exceptions.ExpressionException;
import org.opennms.netmgt.measurements.model.Expression;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.utils.Utils;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An expression engine implemented using JEXL.
 *
 * @author jwhite
 */
@Component("expressionEngine")
public class JEXLExpressionEngine implements ExpressionEngine {

    private static final Logger LOG = LoggerFactory.getLogger(JEXLExpressionEngine.class);

    /**
     * Use a single instance of the JEXL engine, which is thread-safe.
     */
    private final JexlEngine jexl = new JexlEngine();

    public JEXLExpressionEngine() {
        // Add additional functions to the engine
        Map<String, Object> functions = Maps.newHashMap();
        functions.put("math", Math.class);
        functions.put("strictmath", StrictMath.class);
        jexl.setFunctions(functions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyExpressions(final QueryRequest request, final FetchResults results) throws ExpressionException {
        Preconditions.checkNotNull(request, "request argument");
        Preconditions.checkNotNull(results, "results argument");

        final int numExpressions = request.getExpressions().size();

        // Don't do anything if there are no expressions
        if (numExpressions < 1) {
            return;
        }

        // Use to keep track of transient expression so that we don't
        // allocate memory to store their results
        int numNonTransientExpression = 0;
        boolean transientFlags[] = new boolean[numExpressions];

        // Compile the expressions
        int j, k = 0;
        final LinkedHashMap<String, org.apache.commons.jexl2.Expression> expressions = Maps.newLinkedHashMap();
        for (final Expression e : request.getExpressions()) {

            // Populate the transientFlags array
            transientFlags[k] = e.getTransient();
            if (!transientFlags[k]) {
                numNonTransientExpression++;
            }
            k++;

            try {
                expressions.put(e.getLabel(), jexl.createExpression(e.getExpression()));
            } catch (JexlException ex) {
                throw new ExpressionException(ex, "Failed to parse expression label '{}'.", e.getLabel());
            }
        }

        // Prepare the JEXL context
        final Map<String, Object> jexlValues = Maps.newHashMap();
        final JexlContext context = new MapContext(jexlValues);

        // Add constants (i.e. values from strings.properties) retrieved by the fetch operation
        jexlValues.putAll(results.getConstants());
        LOG.debug("JEXL context constants: {}", jexlValues);

        // Add some additional constants for ease of use
        jexlValues.put("__inf", Double.POSITIVE_INFINITY);
        jexlValues.put("__neg_inf", Double.NEGATIVE_INFINITY);
        jexlValues.put("NaN", Double.NaN);

        final long timestamps[] = results.getTimestamps();
        final Map<String, double[]> columns = results.getColumns();
        final int numRows = timestamps.length;

        // Calculate the time span
        jexlValues.put("__diff_time", numRows < 1 ? 0d : timestamps[numRows-1] - timestamps[0]);

        final double expressionValues[][] = new double[numNonTransientExpression][numRows];

        // Iterate through all of the rows, apply the expressions
        for (int i = 0; i < numRows; i++) {
            // Evaluate every expression, in the same order as which they appeared in the query
            j = k = 0;
            for (final Map.Entry<String, org.apache.commons.jexl2.Expression> expressionEntry : expressions.entrySet()) {
                // Update the timestamp
                jexlValues.put("timestamp", timestamps[i]);

                // Add all of the values from the row to the context
                // overwriting values from the last loop
                for (final String sourceLabel : columns.keySet()) {
                    jexlValues.put(sourceLabel, columns.get(sourceLabel)[i]);
                }

                // Evaluate the expression
                try {
                    Object derived = expressionEntry.getValue().evaluate(context);
                    double derivedAsDouble = Utils.toDouble(derived);

                    // Only store the values for non-transient expressions
                    if (!transientFlags[j++]) {
                        expressionValues[k++][i] = derivedAsDouble;
                    }

                    // Store the result back in the context, so that it can be referenced
                    // by subsequent expression in the row
                    jexlValues.put(expressionEntry.getKey(), derivedAsDouble);
                } catch (NullPointerException|NumberFormatException e) {
                    throw new ExpressionException(e, "The return value from expression with label '" +
                            expressionEntry.getKey() + "' could not be cast to a Double.");
                } catch (JexlException e) {
                    throw new ExpressionException(e, "Failed to evaluate expression with label '" +
                            expressionEntry.getKey() + "'.");
                }
            }
        }

        // Store the results
        j = k = 0;
        for (final String expressionLabel : expressions.keySet()) {
            if (!transientFlags[j++]) {
                columns.put(expressionLabel, expressionValues[k++]);
            }
        }
    }
}
