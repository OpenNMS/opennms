package org.opennms.netmgt.alarmd.api.support;


import org.opennms.netmgt.alarmd.api.Preservable;

public interface StatusFactory<T extends Preservable> {
	
    public T createSyncLostMessage();

}
