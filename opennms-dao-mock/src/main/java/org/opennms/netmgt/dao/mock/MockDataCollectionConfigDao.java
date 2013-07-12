package org.opennms.netmgt.dao.mock;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.model.RrdRepository;

public class MockDataCollectionConfigDao implements DataCollectionConfigDao {

    @Override
    public String getSnmpStorageFlag(final String collectionName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<MibObject> getMibObjectList(final String cName, final String aSysoid, final String anAddress, final int ifType) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Map<String, ResourceType> getConfiguredResourceTypes() {
        return Collections.emptyMap();
    }

    @Override
    public RrdRepository getRrdRepository(final String collectionName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int getStep(final String collectionName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<String> getRRAList(final String collectionName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public String getRrdPath() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public DatacollectionConfig getRootDataCollection() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<String> getAvailableDataCollectionGroups() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<String> getAvailableSystemDefs() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<String> getAvailableMibGroups() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
