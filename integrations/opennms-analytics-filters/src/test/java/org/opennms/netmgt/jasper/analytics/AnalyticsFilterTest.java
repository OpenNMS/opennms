package org.opennms.netmgt.jasper.analytics;

import org.junit.BeforeClass;
import org.opennms.netmgt.jasper.analytics.helper.DataSourceFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public abstract class AnalyticsFilterTest {
    private static List<FilterFactory> filterFactories = new ArrayList<FilterFactory>();

    private static DataSourceFilter dataSourceFilter;

    @BeforeClass
    public static void beforeClass() {
        ServiceLoader<FilterFactory> load = ServiceLoader.load(FilterFactory.class);
        for (FilterFactory eachFactory : load) {
            filterFactories.add(eachFactory);
        }
        dataSourceFilter = new DataSourceFilter(filterFactories);
    }

    protected static List<FilterFactory> getFilterFactories() {
        return filterFactories;
    }

    protected static DataSourceFilter getDataSourceFilter() {
        return dataSourceFilter;
    }
}
