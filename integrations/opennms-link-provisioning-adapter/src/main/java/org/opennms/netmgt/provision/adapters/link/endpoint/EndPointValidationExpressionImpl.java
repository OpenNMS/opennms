package org.opennms.netmgt.provision.adapters.link.endpoint;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;
import org.opennms.netmgt.provision.adapters.link.EndPointValidationExpression;

/**
 * <p>Abstract EndPointValidationExpressionImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="validator")
public abstract class EndPointValidationExpressionImpl implements EndPointValidationExpression {
    protected String m_value;

    /**
     * <p>getValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlValue
    public String getValue() {
        return m_value;
    }
    
    /**
     * <p>setValue</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setValue(String value) {
        m_value = value;
    }
    
    /** {@inheritDoc} */
    public abstract void validate(EndPoint endPoint) throws EndPointStatusException;

}
