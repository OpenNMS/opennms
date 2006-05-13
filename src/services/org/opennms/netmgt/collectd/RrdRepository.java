package org.opennms.netmgt.collectd;

import java.io.File;

import org.opennms.netmgt.config.DataCollectionConfigFactory;

public class RrdRepository {
    
    String m_collectionName;

    public RrdRepository(String collectionName) {
        m_collectionName = collectionName;
    }

    File getRrdBaseDir() {
        String rrdPath = DataCollectionConfigFactory.getInstance().getRrdPath();
        File rrdBaseDir = new File(rrdPath);
        return rrdBaseDir;
    }

}
