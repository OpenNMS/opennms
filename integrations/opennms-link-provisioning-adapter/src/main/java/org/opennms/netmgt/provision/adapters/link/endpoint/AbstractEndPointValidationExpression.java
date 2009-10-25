package org.opennms.netmgt.provision.adapters.link.endpoint;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;
import org.opennms.netmgt.provision.adapters.link.EndPointValidationExpression;

@XmlRootElement(name="validator")
public abstract class AbstractEndPointValidationExpression implements EndPointValidationExpression {

    public abstract void validate(EndPoint endPoint) throws EndPointStatusException;

}
