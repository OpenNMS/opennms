package org.opennms.web.svclayer;

import java.io.InputStream;

import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface RrdGraphService {
    public InputStream getPrefabGraph(String resourceId,
            String report, long start, long end);
    
    public InputStream getAdhocGraph(String resourceId,
            String title, String[] dataSources, String[] aggregateFunctions,
            String[] colors, String[] dataSourceTitles, String[] styles,
            long start, long end);
}
