package org.opennms.sms.reflector.smsservice.internal;

import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.smslib.AGateway;
import org.smslib.test.*;

/**
 * <p>TestGatewayGroupImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class TestGatewayGroupImpl implements GatewayGroup {
	
	AGateway[] m_gateways;
	
	/**
	 * <p>Constructor for TestGatewayGroupImpl.</p>
	 */
	public TestGatewayGroupImpl(){
		m_gateways = new AGateway[1];
		m_gateways[0] = new TestGateway("modem.id");
	}
	
	/**
	 * <p>getGateways</p>
	 *
	 * @return an array of {@link org.smslib.AGateway} objects.
	 */
	public AGateway[] getGateways() {
		return m_gateways;
	}
	
}
