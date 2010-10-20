package org.opennms.netmgt.config;

import java.net.InetAddress;
import java.util.concurrent.locks.Lock;

import org.opennms.netmgt.config.snmpAsset.adapter.AssetField;

/**
 */
public interface SnmpAssetAdapterConfig {

	public AssetField[] getAssetFieldsForAddress(InetAddress address, String sysoid);

	/**
	 */
	void update() throws Exception;

    public Lock getReadLock();

    public Lock getWriteLock();
}
