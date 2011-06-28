/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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



package org.opennms.core.test.snmp;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.mock.snmp.MockSnmpAgentAware;
import org.opennms.netmgt.snmp.mock.MockSnmpStrategy;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * This {@link TestExecutionListener} looks for the {@link JUnitSnmpAgent} annotation
 * and uses attributes on it to launch a mock SNMP agent for use during unit testing.
 *
 * @author brozow
 * @version $Id: $
 */
public class JUnitSnmpAgentExecutionListener extends AbstractTestExecutionListener {

    private static final String MOCK_SNMP_AGENT = MockSnmpAgent.class.getName();

    @Override
    public void beforeTestMethod(final TestContext testContext) throws Exception {
        final JUnitSnmpAgents agents = findAgentListAnnotation(testContext);
        Boolean useMockSnmpStrategy = null;
        testContext.setAttribute("strategyClass", System.getProperty("org.opennms.snmp.strategyClass"));
        
        if (agents != null) {
            useMockSnmpStrategy = agents.useMockSnmpStrategy();
            for (final JUnitSnmpAgent agent : agents.value()) {
                handleSnmpAgent(testContext, agent, useMockSnmpStrategy);
            }
        }

        handleSnmpAgent(testContext, findAgentAnnotation(testContext), useMockSnmpStrategy);
    }

    protected void handleSnmpAgent(final TestContext testContext, final JUnitSnmpAgent config, final Boolean agentsUseMockSnmpStrategy) throws UnknownHostException, InterruptedException {
        if (config == null) return;

        boolean useMockSnmpStrategy = config.useMockSnmpStrategy();
        // if the JUnitSnmpAgents object has set a property, use it globally instead
        if (agentsUseMockSnmpStrategy != null) {
            useMockSnmpStrategy = agentsUseMockSnmpStrategy;
        }
        LogUtils.debugf(this, "handleSnmpAgent(%s, %s, %s)", testContext, config, useMockSnmpStrategy);
        
        String host = config.host();
        if (host == null || "".equals(host)) {
            /*
             * NOTE: This call produces different results on different platforms so make
             * sure your client code is aware of this. If you use the {@link ProxySnmpAgentConfigFactory}
             * by including the <code>classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml</code>
             * Spring context, you probably won't need to deal with this. It will override the
             * SnmpPeerFactory with the correct values.
             * 
             * Linux: 127.0.0.1
             * Mac OS: primary external interface
             */
            host = InetAddressUtils.str(InetAddress.getLocalHost());
            //host = "127.0.0.1";
        }
        
        final ResourceLoader loader = new DefaultResourceLoader();
        final Resource resource = loader.getResource(config.resource());

        // NOTE: The default value for config.port is specified inside {@link JUnitSnmpAgent}
        if (useMockSnmpStrategy) {
            System.setProperty("org.opennms.snmp.strategyClass", MockSnmpStrategy.class.getName());
            try {
                MockSnmpStrategy.addHost(InetAddressUtils.addr(host), config.port(), resource);
            } catch (final IOException e) {
                LogUtils.debugf(this, e, "Unable to add %s:%d with resource %s to the MockSnmpStrategy!", host, config.port(), config.resource());
                e.printStackTrace();
            }
        } else {
            final String bindAddress = host +"/"+ config.port();
            LogUtils.debugf(this, "creating MockSnmpAgent on %s", bindAddress);
            final MockSnmpAgent agent = MockSnmpAgent.createAgentAndRun(resource, bindAddress);
            
            LogUtils.debugf(this, "Set up agent %s, loaded content from %s", agent, config.resource());
            testContext.setAttribute(MOCK_SNMP_AGENT, agent);
            
            // FIXME: Is there a better way to inject the MockSnmpAgent into the test class?  Seems that spring doesn't have appropriate hooks
            if (testContext.getTestInstance() instanceof MockSnmpAgentAware) {
                LogUtils.debugf(this, "injecting agent into MockSnmpAgentAware test: %s", testContext.getTestInstance());
                ((MockSnmpAgentAware)testContext.getTestInstance()).setMockSnmpAgent(agent);
            }
        }
    }

    private JUnitSnmpAgent findAgentAnnotation(TestContext testContext) {
        final Method testMethod = testContext.getTestMethod();
        final JUnitSnmpAgent config = testMethod.getAnnotation(JUnitSnmpAgent.class);
        if (config != null) {
            return config;
        }

        final Class<?> testClass = testContext.getTestClass();
        return testClass.getAnnotation(JUnitSnmpAgent.class);

    }

    private JUnitSnmpAgents findAgentListAnnotation(TestContext testContext) {
        final Method testMethod = testContext.getTestMethod();
        final JUnitSnmpAgents config = testMethod.getAnnotation(JUnitSnmpAgents.class);
        if (config != null) {
            return config;
        }

        final Class<?> testClass = testContext.getTestClass();
        return testClass.getAnnotation(JUnitSnmpAgents.class);

    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        final MockSnmpAgent agent = (MockSnmpAgent)testContext.getAttribute(MOCK_SNMP_AGENT);
        final String strategyClass = (String)testContext.getAttribute("strategyClass");
        
        if (agent != null) {
            LogUtils.debugf(this, "Shutting down agent ", agent);
            agent.shutDownAndWait();
        }
        
        if (strategyClass != null) {
            System.setProperty("org.opennms.snmp.strategyClass", strategyClass);
        }
    }

}
