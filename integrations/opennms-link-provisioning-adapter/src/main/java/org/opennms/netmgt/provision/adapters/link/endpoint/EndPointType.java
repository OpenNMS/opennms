
/**
 * <p>EndPointType class.</p>
 *
 * @author ranger
 * @version $Id: $
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

    /**
     * <p>Constructor for EndPointType.</p>
     */
    public EndPointType() {
    }

    /**
     * <p>Constructor for EndPointType.</p>
     *
     * @param sysOid a {@link java.lang.String} object.
     * @param validator a {@link org.opennms.netmgt.provision.adapters.link.endpoint.EndPointValidationExpressionImpl} object.
     */
    public EndPointType(String sysOid, EndPointValidationExpressionImpl validator) {
        setSysOid(sysOid);
        setValidator(validator);
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * <p>getValidator</p>
     *
     * @return a {@link org.opennms.netmgt.provision.adapters.link.EndPointValidationExpression} object.
     */
    public EndPointValidationExpression getValidator() {
        return m_validator;
    }


    /**
     * <p>setValidator</p>
     *
     * @param validator a {@link org.opennms.netmgt.provision.adapters.link.endpoint.EndPointValidationExpressionImpl} object.
     */
    public void setValidator(EndPointValidationExpressionImpl validator) {
        m_validator = validator;
    }


    /**
     * <p>getSysOid</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSysOid() {
        return m_sysOid;
    }


    /**
     * <p>setSysOid</p>
     *
     * @param sysOid a {@link java.lang.String} object.
     */
    public void setSysOid(String sysOid) {
        m_sysOid = sysOid;
    }


    /**
     * <p>matches</p>
     *
     * @param ep a {@link org.opennms.netmgt.provision.adapters.link.EndPoint} object.
     * @return a boolean.
     */
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

    /**
     * <p>validate</p>
     *
     * @param ep a {@link org.opennms.netmgt.provision.adapters.link.EndPoint} object.
     * @throws org.opennms.netmgt.provision.adapters.link.EndPointStatusException if any.
     */
    public void validate(EndPoint ep) throws EndPointStatusException {
        m_validator.validate(ep);
    }


}
