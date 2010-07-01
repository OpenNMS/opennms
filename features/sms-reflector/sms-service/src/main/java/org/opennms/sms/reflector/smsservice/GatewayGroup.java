package org.opennms.sms.reflector.smsservice;

import org.smslib.AGateway;

/**
 * <p>GatewayGroup interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface GatewayGroup {
	/**
	 * <p>getGateways</p>
	 *
	 * @return an array of {@link org.smslib.AGateway} objects.
	 */
	AGateway[] getGateways();
}
