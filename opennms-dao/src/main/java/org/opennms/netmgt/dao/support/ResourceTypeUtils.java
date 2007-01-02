package org.opennms.netmgt.dao.support;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ResourceTypeUtils {
    // This class has only static methods
    private ResourceTypeUtils() {
    }

    public static List<String> getDataSourcesInDirectory(File directory) {
        int suffixLength = RrdFileConstants.getRrdSuffix().length();
    
        // get the interface data sources
        File[] files =
            directory.listFiles(RrdFileConstants.RRD_FILENAME_FILTER);
    
        ArrayList<String> dataSources = new ArrayList<String>(files.length);
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            String dsName =
                fileName.substring(0, fileName.length() - suffixLength);
    
            dataSources.add(dsName);
        }
    
        return dataSources;
    }
}
