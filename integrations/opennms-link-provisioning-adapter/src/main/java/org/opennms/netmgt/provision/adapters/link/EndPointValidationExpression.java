package org.opennms.netmgt.provision.adapters.link;


public interface EndPointValidationExpression {
   public void validate(EndPoint endPoint) throws EndPointStatusException;
}