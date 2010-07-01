package org.opennms.netmgt.provision.adapters.link;


/**
 * <p>EndPointValidationExpression interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface EndPointValidationExpression {
   /**
    * <p>validate</p>
    *
    * @param endPoint a {@link org.opennms.netmgt.provision.adapters.link.EndPoint} object.
    * @throws org.opennms.netmgt.provision.adapters.link.EndPointStatusException if any.
    */
   public void validate(EndPoint endPoint) throws EndPointStatusException;
}
