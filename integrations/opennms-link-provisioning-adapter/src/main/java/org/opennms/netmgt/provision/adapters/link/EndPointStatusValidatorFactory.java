/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link;


public interface EndPointStatusValidatorFactory{
    public EndPointStatusValidator getEndPointStatusValidatorFor(String sysOid);
}