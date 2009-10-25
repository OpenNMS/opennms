/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link.endpoint;

import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;
import org.opennms.netmgt.provision.adapters.link.EndPointValidationExpression;

public class EndPointType {
    
    private String m_sysOid;
    private EndPointValidationExpression m_validator;
    
    public EndPointType(String sysOid, EndPointValidationExpression validator) {
        setSysOid(sysOid);
        setValidator(validator);
    }

    public EndPointValidationExpression getValidator() {
        return m_validator;
    }


    public void setValidator(EndPointValidationExpression validator) {
        m_validator = validator;
    }


    public String getSysOid() {
        return m_sysOid;
    }


    public void setSysOid(String sysOid) {
        m_sysOid = sysOid;
    }


    public boolean matches(EndPoint ep) {
        if (ep.getSysOid().equals(getSysOid())) {
            return true;
        }
        return false;
    }

    public void validate(EndPoint ep) throws EndPointStatusException {
        m_validator.validate(ep);
    }


}