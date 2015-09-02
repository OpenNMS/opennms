package org.opennms.netmgt.jasper.analytics.helper;

import com.google.common.base.Preconditions;
import com.google.common.collect.RowSortedTable;
import org.opennms.netmgt.jasper.analytics.AnalyticsCommand;
import org.opennms.netmgt.jasper.analytics.Filter;
import org.opennms.netmgt.jasper.analytics.FilterFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Allows an RRD data source to be modified by analytics modules.
 *
 * The list of modules to run, and their options are set with
 * additional commands in the query string. These commands are
 * removed from the query string before being passed to the actual
 * data source.
 *
 * The commands are of the form:
 *   ANALYTICS:moduleName=columnNameOrPrefix(:otherOptions)
 * where:
 *   - moduleName is a unique name for the module
 *   - columnNameOrPrefix identifies the name of the column, or the prefix of the
 *   column name if there are multiple where the additional values will be stored
 *   - otherOptions are optional and specific to the module in question
 *
 * Once the data source has been loaded, the modules are invoked
 * in the same order as they appear in the query string.
 *
 * The module factories are loaded at run-time using the
 * ServiceLoader paradigm.
 *
 * @author jwhite
 */
// TODO MVR re-use javadoc?
// TODO MVR rewrite javadoc
public class DataSourceFilter {

    /**
     * A list of {@link FilterFactory} services that can be used to fetch analytics
     * filters to filter the measurements that are returned.
     */
    private final List<FilterFactory> filterFactories;

    public DataSourceFilter(List<FilterFactory> filterFactories) {
        Preconditions.checkArgument(filterFactories != null, "The filterFactories must not be null.");
        this.filterFactories = filterFactories;
    }

    public void filter(AnalyticsCommand command, RowSortedTable<Integer, String, Double> table) throws Exception {
        Preconditions.checkArgument(command != null, "command must not be null");
        Preconditions.checkArgument(table != null, "table must not be null");
        filter(Arrays.asList(new AnalyticsCommand[]{command}), table);
    }

    /**
     * Filters the given data source by successively applying
     * all of the analytics commands.
     */
    public void filter(final List<AnalyticsCommand> analyticsCommandList,
                       final RowSortedTable<Integer, String, Double> dsAsTable) throws Exception {
        for (AnalyticsCommand command : analyticsCommandList) {
            Filter filter = getFilter(command);
            if (filter == null) {
                throw new Exception("No analytics module found for " + command.getModule());
            }
            filter.filter(dsAsTable);
        }
    }

    /**
     * Retrieves an Enricher that supports the given analytics command
     *
     * @return null if no suitable Enricher was found
     * @throws Exception
     */
    private Filter getFilter(AnalyticsCommand command) throws Exception {
        for (FilterFactory module : filterFactories) {
            Filter filter = module.getFilter(command);
            if (filter != null) {
                return filter;
            }
        }
        return null;
    }
}

