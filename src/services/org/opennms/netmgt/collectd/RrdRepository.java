package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.List;

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

    public String getCollectionName() {
        return m_collectionName;
    }

    List getRraList() {
        return DataCollectionConfigFactory.getInstance().getRRAList(getCollectionName());
    }

    int getStep() {
        return DataCollectionConfigFactory.getInstance().getStep(getCollectionName());
    }
    
    int getHeartBeat() {
        return (2 * DataCollectionConfigFactory.getInstance().getStep(getCollectionName()));
    }

}
