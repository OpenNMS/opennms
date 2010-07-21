package org.opennms.netmgt.config;

import java.net.InetAddress;

import org.opennms.netmgt.config.snmpAsset.adapter.AssetField;

/**
 */
public interface SnmpAssetAdapterConfig {

	public AssetField[] getAssetFieldsForAddress(InetAddress address, String sysoid);

	/**
	 */
	void update() throws Exception;
}
