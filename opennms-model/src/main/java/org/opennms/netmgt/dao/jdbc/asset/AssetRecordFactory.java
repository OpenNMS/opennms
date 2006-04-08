package org.opennms.netmgt.dao.jdbc.asset;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.Factory;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;

public class AssetRecordFactory extends Factory {
	
	public static void register(DataSource dataSource) {
        new AssetRecordFactory(dataSource);
	}

    public AssetRecordFactory() {
        super(OnmsAssetRecord.class);
    }
	
	public AssetRecordFactory(DataSource dataSource) {
        super(OnmsAssetRecord.class);
        setDataSource(dataSource);
        afterPropertiesSet();
	}
    
	protected void assignId(Object obj, Object id) {
		OnmsAssetRecord asset = (OnmsAssetRecord) obj;
		OnmsNode node = (OnmsNode)Cache.obtain(OnmsNode.class, id);
		asset.setNode(node);
		
	}

	protected Object create() {
		return new LazyAssetRecord(getDataSource());
	}




}
