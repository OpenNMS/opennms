
/**
 * <p>EndPoint interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.provision.adapters.link;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpValue;
public interface EndPoint {
    /**
     * <p>get</p>
     *
     * @param oid a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public SnmpValue get(String oid);
    /**
     * <p>getSysOid</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSysOid();
    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress();
    /**
     * <p>ping</p>
     *
     * @return a boolean.
     * @throws org.opennms.netmgt.provision.adapters.link.EndPointStatusException if any.
     */
    public boolean ping() throws EndPointStatusException;
}
