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

import static org.opennms.core.utils.InetAddressUtils.addr;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.opennms.core.test.snmp.annotations.JUnitMockSnmpStrategyAgents;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.LogUtils;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.config.SnmpAgentConfigFactory;
import org.opennms.netmgt.config.SnmpAgentConfigProxyMapper;
import org.opennms.netmgt.snmp.SnmpAgentAddress;
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

    private static final String STRATEGY_CLASS_PROPERTY = "org.opennms.snmp.strategyClass";

    private static final String STRATEGY_CLASS_KEY = "org.opennms.core.test-api.snmp.strategyClass";
    private static final String AGENT_KEY = "org.opennms.core.test-api.snmp.agentList";

    @Override
    public void beforeTestMethod(final TestContext testContext) throws Exception {
        final JUnitMockSnmpStrategyAgents agents = findAgentListAnnotation(testContext);
        testContext.setAttribute(STRATEGY_CLASS_KEY, System.getProperty(STRATEGY_CLASS_PROPERTY));
        testContext.setAttribute(AGENT_KEY, new ArrayList<MockSnmpAgent>());

        if (agents != null) {
            for (final JUnitSnmpAgent agent : agents.value()) {
                handleSnmpAgent(testContext, agent);
            }
        }

        handleSnmpAgent(testContext, findAgentAnnotation(testContext));
    }

    protected void handleSnmpAgent(final TestContext testContext, final JUnitSnmpAgent config) throws UnknownHostException, InterruptedException {
        if (config == null) return;

        String factoryClassName = "unknown";
        try {
	        final SnmpAgentConfigFactory factory = testContext.getApplicationContext().getBean("snmpPeerFactory", SnmpAgentConfigFactory.class);
	        factoryClassName = factory.getClass().getName();
        } catch (final Throwable t) {
        	// ignore
        }
		if (!factoryClassName.contains("ProxySnmpAgentConfigFactory")) {
        	LogUtils.warnf(this, "SNMP Peer Factory (%s) is not the ProxySnmpAgentConfigFactory -- did you forget to include applicationContext-proxy-snmp.xml?", factoryClassName);
        }

        final Boolean useMockSnmpStrategy = Boolean.getBoolean("org.opennms.core.test-api.snmp.useMockSnmpStrategy");
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
            host = str(InetAddress.getLocalHost());
            //host = "127.0.0.1";
        }
        
        final ResourceLoader loader = new DefaultResourceLoader();
        final Resource resource = loader.getResource(config.resource());

        // NOTE: The default value for config.port is specified inside {@link JUnitSnmpAgent}
    	final InetAddress hostAddress = addr(host);
        final int port = config.port();
		final SnmpAgentAddress agentAddress = new SnmpAgentAddress(hostAddress, port);
    	
    	final InetAddress localHost = InetAddress.getLocalHost();
    	final SnmpAgentConfigProxyMapper mapper = SnmpAgentConfigProxyMapper.getInstance();
    	SnmpAgentAddress listenAddress = null;

    	// try to find an unused port on localhost
    	int mappedPort = 1161;
    	do {
    		listenAddress = new SnmpAgentAddress(localHost, mappedPort++);
    	} while (mapper.contains(listenAddress));
    	mapper.addProxy(hostAddress, listenAddress);

		if (useMockSnmpStrategy) {
            System.setProperty(STRATEGY_CLASS_PROPERTY, MockSnmpStrategy.class.getName());
            try {
                MockSnmpStrategy.addHost(agentAddress, resource);
            } catch (final IOException e) {
                LogUtils.debugf(this, e, "Unable to add %s with resource %s to the MockSnmpStrategy!", agentAddress, config.resource());
                e.printStackTrace();
            }
        } else {
            LogUtils.debugf(this, "creating MockSnmpAgent on %s for 'real' address %s", listenAddress, agentAddress);
            final MockSnmpAgent agent = MockSnmpAgent.createAgentAndRun(resource, str(listenAddress.getAddress()) + "/" + listenAddress.getPort());

            LogUtils.debugf(this, "Set up agent %s, loaded content from %s", agent, config.resource());
            @SuppressWarnings("unchecked")
			final List<MockSnmpAgent> agents = (List<MockSnmpAgent>)testContext.getAttribute(AGENT_KEY);
            agents.add(agent);
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

    private JUnitMockSnmpStrategyAgents findAgentListAnnotation(final TestContext testContext) {
        final Method testMethod = testContext.getTestMethod();
        final JUnitMockSnmpStrategyAgents config = testMethod.getAnnotation(JUnitMockSnmpStrategyAgents.class);
        if (config != null) {
            return config;
        }

        final Class<?> testClass = testContext.getTestClass();
        return testClass.getAnnotation(JUnitMockSnmpStrategyAgents.class);

    }

    @Override
    public void afterTestMethod(final TestContext testContext) throws Exception {
        final String strategyClass = (String)testContext.getAttribute(STRATEGY_CLASS_KEY);

        @SuppressWarnings("unchecked")
		final List<MockSnmpAgent> agents = (List<MockSnmpAgent>)testContext.getAttribute(AGENT_KEY);

        for (final MockSnmpAgent agent : agents) {
            if (agent != null) {
                LogUtils.debugf(this, "Shutting down agent ", agent);
                agent.shutDownAndWait();
            }
        }
        
        if (strategyClass != null) {
            System.setProperty(STRATEGY_CLASS_PROPERTY, strategyClass);
        }
    }

}
