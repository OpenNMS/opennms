/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
        @Override
	public AGateway[] getGateways() {
		return m_gateways;
	}
	
}
