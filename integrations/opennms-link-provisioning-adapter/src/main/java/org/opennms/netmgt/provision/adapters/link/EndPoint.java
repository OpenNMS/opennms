/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpValue;

public interface EndPoint {
    public SnmpValue get(String oid);
    public String getSysOid();
    public InetAddress getAddress();
    public boolean ping() throws EndPointStatusException;
}