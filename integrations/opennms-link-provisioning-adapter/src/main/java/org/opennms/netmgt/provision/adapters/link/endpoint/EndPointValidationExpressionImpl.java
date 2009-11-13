package org.opennms.netmgt.provision.adapters.link.endpoint;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;
import org.opennms.netmgt.provision.adapters.link.EndPointValidationExpression;

@XmlRootElement(name="validator")
public abstract class EndPointValidationExpressionImpl implements EndPointValidationExpression {
    protected String m_value;

    @XmlValue
    public String getValue() {
        return m_value;
    }
    
    public void setValue(String value) {
        m_value = value;
    }
    
    public abstract void validate(EndPoint endPoint) throws EndPointStatusException;

}
