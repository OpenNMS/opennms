package org.opennms.web.svclayer;

import java.io.InputStream;

public interface RrdGraphService {
    public InputStream getPrefabGraph(String parentResourceType,
            String parentResource, String resourceType, String resource,
            String report, long start, long end);
    
    public InputStream getAdhocGraph(String parentResourceType,
            String parentResource, String resourceType, String resource,
            String title, String[] dataSources, String[] aggregateFunctions,
            String[] colors, String[] dataSourceTitles, String[] styles,
            long start, long end);
}
