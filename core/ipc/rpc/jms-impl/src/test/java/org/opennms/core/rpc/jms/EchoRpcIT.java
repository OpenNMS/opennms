/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.rpc.jms;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.opennms.core.rpc.camel.CamelRpcServerRouteManager;
import org.opennms.core.rpc.camel.MockMinionIdentity;
import org.opennms.core.rpc.jms.JmsRpcServerRouteManager;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.activemq.ActiveMQBroker;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-queuingservice-mq-vm.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-client-jms.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-echo.xml"
})
@JUnitConfigurationEnvironment
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@TestExecutionListeners({DirtiesContextTestExecutionListener.class})
public class EchoRpcIT extends org.opennms.core.rpc.camel.EchoRpcIT {

    @ClassRule
    public static ActiveMQBroker s_broker = new ActiveMQBroker();

    @Autowired
    private OnmsDistPoller identity;

    @Autowired
    @Qualifier("queuingservice")
    private ActiveMQComponent queuingservice;

    @Autowired
    @Qualifier("rpcClient")
    private CamelContext rpcClientContext;

    @Override
    public CamelContext getContext() {
        SimpleRegistry registry = new SimpleRegistry();
        CamelContext context = new DefaultCamelContext(registry);
        context.addComponent("queuingservice", queuingservice);
        return context;
    }

    @Override
    public CamelContext getClientContext() {
        return rpcClientContext;
    }

    @Override
    public CamelRpcServerRouteManager getRouteManager(CamelContext context) {
        return new JmsRpcServerRouteManager(context,
                new MockMinionIdentity(REMOTE_LOCATION_NAME));
    }
}
