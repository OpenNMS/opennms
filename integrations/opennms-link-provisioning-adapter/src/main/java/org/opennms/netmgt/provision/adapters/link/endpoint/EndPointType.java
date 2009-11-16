/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link.endpoint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;
import org.opennms.netmgt.provision.adapters.link.EndPointValidationExpression;

@XmlRootElement(name="endpoint-type")
@XmlAccessorType(value=XmlAccessType.FIELD)
public class EndPointType {
    @XmlAttribute(name="name")
    private String m_name;
    
    @XmlElement(name="sysoid-mask")
    private String m_sysOid;
    
    @XmlElementRef
    private EndPointValidationExpressionImpl m_validator;

    public EndPointType() {
    }

    public EndPointType(String sysOid, EndPointValidationExpressionImpl validator) {
        setSysOid(sysOid);
        setValidator(validator);
    }

    public String getName() {
        return m_name;
    }
    
    public void setName(String name) {
        m_name = name;
    }

    public EndPointValidationExpression getValidator() {
        return m_validator;
    }


    public void setValidator(EndPointValidationExpressionImpl validator) {
        m_validator = validator;
    }


    public String getSysOid() {
        return m_sysOid;
    }


    public void setSysOid(String sysOid) {
        m_sysOid = sysOid;
    }


    public boolean matches(EndPoint ep) {
        if (ep == null) {
            LogUtils.debugf(this, "EndPoint is null!");
            return false;
        }
        if (ep.getSysOid() == null) {
            LogUtils.debugf(this, "sysObjectId for endpoint %s is null", ep);
            return false;
        }
        if (ep.getSysOid().startsWith(getSysOid())) {
            return true;
        }
        return false;
    }

    public void validate(EndPoint ep) throws EndPointStatusException {
        m_validator.validate(ep);
    }


}