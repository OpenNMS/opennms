package org.opennms.netmgt.dao.mock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.model.OnmsAssetRecord;


public class MockAssetRecordDao extends AbstractMockDao<OnmsAssetRecord, Integer> implements AssetRecordDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsAssetRecord asset) {
        asset.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsAssetRecord asset) {
        return asset.getId();
    }

    @Override
    public OnmsAssetRecord findByNodeId(final Integer id) {
        for (final OnmsAssetRecord asset : findAll()) {
            if (asset.getNode().getId() == id) {
                return asset;
            }
        }
        return null;
    }

    @Override
    public Map<String, Integer> findImportedAssetNumbersToNodeIds(final String foreignSource) {
        final Map<String,Integer> ret = new HashMap<String,Integer>();
        for (final OnmsAssetRecord asset : findAll()) {
            ret.put(asset.getAssetNumber(), asset.getNode().getId());
        }
        return ret;
    }

    @Override
    public List<OnmsAssetRecord> getDistinctProperties() {
        return findAll();
    }

}
