package org.opennms.web.rest.measurements;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.MapContext;
import org.opennms.web.rest.measurements.fetch.FetchResults;
import org.opennms.web.rest.measurements.model.Expression;
import org.opennms.web.rest.measurements.model.Measurement;
import org.opennms.web.rest.measurements.model.QueryRequest;
import org.opennms.web.rest.measurements.model.Source;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * An expression engine implemented using JEXL.
 *
 * @author jwhite
 */
public class JEXLExpressionEngine implements ExpressionEngine {

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

        // Compile the expressions
        final LinkedHashMap<String, org.apache.commons.jexl2.Expression> expressions = Maps.newLinkedHashMap();
        for (final Expression e : request.getExpressions()) {
            try {
                expressions.put(e.getLabel(), jexl.createExpression(e.getExpression()));
            } catch (JexlException ex) {
                throw new ExpressionException("Failed to parse expression label '" +
                        e.getLabel() + "'.", ex);
            }
        }

        // Prepare the JEXL context
        final Map<String, Object> jexlValues = Maps.newHashMap();
        final JexlContext context = new MapContext(jexlValues);

        // Add constants (i.e. values from strings.properties)
        // retrieved by the fetch operation
        jexlValues.putAll(results.getConstants());

        // Add some additional constants for ease of use
        jexlValues.put("__inf", Double.POSITIVE_INFINITY);
        jexlValues.put("__neg_inf", Double.NEGATIVE_INFINITY);

        // Iterate through all of the rows, apply the expressions
        // and remove any transient values
        for (final Measurement measurement : results.getMeasurements()) {
            final Map<String, Double> values = measurement.getValues();

            // Evaluate every expression, in the same order as which they appeared in the query
            // and store the results back in the row, allowing expressions to use previous results.
            for (final Map.Entry<String, org.apache.commons.jexl2.Expression> expressionEntry : expressions.entrySet()) {
                // Update the timestamp
                jexlValues.put("timestamp", measurement.getTimestamp());

                // Add all of the values from the row to the context
                // overwriting values from the last loop
                jexlValues.putAll(values);

                // Evaluate the expression
                try {
                    Object derived = expressionEntry.getValue().evaluate(context);
                    values.put(expressionEntry.getKey(), Utils.toDouble(derived));
                } catch (NullPointerException|NumberFormatException e) {
                    throw new ExpressionException("The return value from expression with label '" +
                            expressionEntry.getKey() + "' could not be cast to a Double.", e);
                } catch (JexlException e) {
                    throw new ExpressionException("Failed to evaluate expression with label '" +
                            expressionEntry.getKey() + "'.", e);
                }
            }

            // Remove any transient values belonging to sources
            for (final Source source : request.getSources()) {
                if (source.getTransient()) {
                    values.remove(source.getLabel());
                }
            }

            // Remove any transient values belonging to expressions
            for (final Expression e : request.getExpressions()) {
                if (e.getTransient()) {
                    values.remove(e.getLabel());
                }
            }
        }
    }
}
