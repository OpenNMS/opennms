/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.test.snmp;

import static org.opennms.core.utils.InetAddressUtils.addr;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.snmp.SnmpAgentAddress;
import org.opennms.netmgt.snmp.mock.MockSnmpStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * This {@link TestExecutionListener} looks for the {@link JUnitSnmpAgent} annotation
 * and uses attributes on it to launch a mock SNMP agent for use during unit testing.
 */
public class JUnitSnmpAgentExecutionListener extends AbstractTestExecutionListener {
    private static final Logger LOG = LoggerFactory.getLogger(JUnitSnmpAgentExecutionListener.class);
    private static final Boolean s_useMockSnmpStrategyDefault = false;

    private static final String USE_STRATEGY_PROPERTY = "org.opennms.core.test-api.snmp.useMockSnmpStrategy";
    private static final String STRATEGY_CLASS_PROPERTY = "org.opennms.snmp.strategyClass";

    private static final String STRATEGY_CLASS_KEY = "org.opennms.core.test-api.snmp.strategyClass";
    private static final String PROVIDER_KEY = "org.opennms.core.test-api.snmp.dataProvider";
    public static final String IPADDRESS_KEY = "org.opennms.core.test-api.snmp.ipAddress";
    public static final String PORT_KEY = "org.opennms.core.test-api.snmp.port";

    @Override
    public void beforeTestMethod(final TestContext testContext) throws Exception {
        super.beforeTestClass(testContext);

        final JUnitSnmpAgents agents = findAgentListAnnotation(testContext);
        final JUnitSnmpAgent agent = findAgentAnnotation(testContext);

        // save the existing strategy property
        String strategy = System.getProperty(STRATEGY_CLASS_PROPERTY);
        testContext.setAttribute(STRATEGY_CLASS_KEY, strategy);

        // if no annotations exists then do nothing
        if (agents == null && agent == null) {
            // no annotations found
            return;
        }

        // determine if we should use the mock strategry
        boolean useMockSnmpStrategy = shouldUseMockStrategy(agents, agent);
        
        // override the configured strategy to use the mock strategy if necessary
        if (useMockSnmpStrategy) {
            strategy = MockSnmpStrategy.class.getName();
            System.setProperty(STRATEGY_CLASS_PROPERTY, MockSnmpStrategy.class.getName());
            LOG.info("Forcing JUnit SNMP Agent to use mock strategy");
        }

        LOG.debug("Initializing JUnit SNMP Agent with strategy: {}", strategy == null ? "default" : strategy);

        final MockSnmpDataProvider provider = useMockSnmpStrategy 
                ? new MockSnmpStrategyDataProvider() 
                : new MockSnmpAgentDataProvider();
                
        testContext.setAttribute(PROVIDER_KEY, provider);

        if (agents != null) {
            for (final JUnitSnmpAgent a : agents.value()) {
                handleSnmpAgent(testContext, a, useMockSnmpStrategy, provider);
            }
        }

        handleSnmpAgent(testContext, agent, useMockSnmpStrategy, provider);

        if (testContext.getTestInstance() instanceof MockSnmpDataProviderAware) {
            LOG.debug("injecting data provider into MockSnmpDataProviderAware test: {}", testContext.getTestInstance());
            ((MockSnmpDataProviderAware)testContext.getTestInstance()).setMockSnmpDataProvider(provider);
        }
    }

    private boolean shouldUseMockStrategy(JUnitSnmpAgents agents, JUnitSnmpAgent agent) {
        if (agents != null && agents.forceMockStrategy()) {
            return true;
        }
        if (agent != null && agent.forceMockStrategy()) {
            return true;
        }
        final String useMockSnmpStrategyString = System.getProperty(USE_STRATEGY_PROPERTY, s_useMockSnmpStrategyDefault.toString());
        return Boolean.parseBoolean(useMockSnmpStrategyString);
    }

    @Override
    public void afterTestMethod(final TestContext testContext) throws Exception {
        super.afterTestMethod(testContext);

        final MockSnmpDataProvider provider = (MockSnmpDataProvider)testContext.getAttribute(PROVIDER_KEY);
        if (provider != null) {
            LOG.debug("Tearing down JUnit SNMP Agent provider: {}", provider);
            provider.resetData();
        }

        // Put the strategy class property back the way it was before the tests.
        final String strategyClass = (String)testContext.getAttribute(STRATEGY_CLASS_KEY);
        if (strategyClass == null) {
            System.clearProperty(STRATEGY_CLASS_PROPERTY);
        } else {
            System.setProperty(STRATEGY_CLASS_PROPERTY, strategyClass);
        }
    }

    private void handleSnmpAgent(final TestContext testContext, final JUnitSnmpAgent config, boolean useMockSnmpStrategy, MockSnmpDataProvider provider) throws IOException, UnknownHostException, InterruptedException {
        if (config == null) return;

        String factoryClassName = "unknown";
        try {
            final SnmpAgentConfigFactory factory = testContext.getApplicationContext().getBean("snmpPeerFactory", SnmpAgentConfigFactory.class);
            factoryClassName = factory.getClass().getName();
        } catch (final Exception e) {
            // ignore
        }
        if (!factoryClassName.contains("ProxySnmpAgentConfigFactory")) {
            LOG.warn("SNMP Peer Factory ({}) is not the ProxySnmpAgentConfigFactory -- did you forget to include applicationContext-proxy-snmp.xml?", factoryClassName);
        }

        LOG.debug("handleSnmpAgent(testContext, {}, {})", config, useMockSnmpStrategy);

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
            host = InetAddressUtils.getLocalHostAddressAsString();
            //host = "127.0.0.1";
        }

        final ResourceLoader loader = new DefaultResourceLoader();
        final Resource resource = loader.getResource(config.resource());

        // NOTE: The default value for config.port is specified inside {@link JUnitSnmpAgent}
        final InetAddress hostAddress = addr(host);
        final int port = config.port();
        final SnmpAgentAddress agentAddress = new SnmpAgentAddress(hostAddress, port);

        final SnmpAgentConfigProxyMapper mapper = SnmpAgentConfigProxyMapper.getInstance();

        if (useMockSnmpStrategy) {
            // since it's all virtual, the "mapped" port just points to the real agent address
            mapper.addProxy(hostAddress, agentAddress);
        } else {
            MockSnmpAgent agent = null;
            try {
                agent = MockSnmpAgent.createAgentAndRun(resource.getURL(), str(InetAddress.getLocalHost()) + "/0");
            } catch (Throwable e) {
                agent = MockSnmpAgent.createAgentAndRun(resource.getURL(), str(InetAddressUtils.ONE_TWENTY_SEVEN) + "/0");
            }
            SnmpAgentAddress listenAddress = new SnmpAgentAddress(agent.getInetAddress(), agent.getPort());

            mapper.addProxy(hostAddress, listenAddress);
            testContext.setAttribute(IPADDRESS_KEY, listenAddress.getAddress());
            testContext.setAttribute(PORT_KEY, listenAddress.getPort());

            LOG.debug("using MockSnmpAgent on {} for 'real' address {}", listenAddress, agentAddress);

            provider.addAgent(agentAddress, agent);
        }

        provider.setDataForAddress(agentAddress, resource);
    }

    private JUnitSnmpAgent findAgentAnnotation(final TestContext testContext) {
        final Method testMethod = testContext.getTestMethod();
        final JUnitSnmpAgent config = testMethod.getAnnotation(JUnitSnmpAgent.class);
        if (config != null) {
            return config;
        }

        final Class<?> testClass = testContext.getTestClass();
        return testClass.getAnnotation(JUnitSnmpAgent.class);

    }

    private JUnitSnmpAgents findAgentListAnnotation(final TestContext testContext) {
        final Method testMethod = testContext.getTestMethod();
        final JUnitSnmpAgents config = testMethod.getAnnotation(JUnitSnmpAgents.class);
        if (config != null) {
            return config;
        }

        final Class<?> testClass = testContext.getTestClass();
        return testClass.getAnnotation(JUnitSnmpAgents.class);

    }

    private static final class MockSnmpStrategyDataProvider implements MockSnmpDataProvider {
        @Override
        public void addAgent(SnmpAgentAddress address, MockSnmpAgent agent) {
            // Don't do anything, we don't need to track agent instances
        }

        @Override
        public void setDataForAddress(final SnmpAgentAddress address, final Resource resource) {
            try {
                MockSnmpStrategy.setDataForAddress(address, resource);
            } catch (final Exception e) {
                LOG.warn("Unable to set mock SNMP data for {}", address, e);
            }
        }

        @Override
        public void resetData() {
            MockSnmpStrategy.resetData();
        }

        @Override
        public String toString() {
            return "MockSnmpStrategyDataProvider[]";
        }

        @Override
        public void updateIntValue(SnmpAgentAddress address, String oid, int val) {
            MockSnmpStrategy.updateIntValue(address, oid, val);
        }

        @Override
        public void updateStringValue(SnmpAgentAddress address, String oid, String val) {
            MockSnmpStrategy.updateStringValue(address, oid, val);
        }

        @Override
        public void updateCounter32Value(SnmpAgentAddress address, String oid, int val) {
            MockSnmpStrategy.updateCounter32Value(address, oid, (long)val);
        }

        @Override
        public void updateCounter64Value(SnmpAgentAddress address, String oid, long val) {
            MockSnmpStrategy.updateCounter64Value(address, oid, BigInteger.valueOf(val));
        }
    }

    private static final class MockSnmpAgentDataProvider implements MockSnmpDataProvider {
        private final Map<SnmpAgentAddress, MockSnmpAgent> m_agents = new ConcurrentHashMap<SnmpAgentAddress, MockSnmpAgent>();

        @Override
        public void addAgent(SnmpAgentAddress address, MockSnmpAgent agent) {
            m_agents.put(address, agent);
        }

        @Override
        public void setDataForAddress(final SnmpAgentAddress address, final Resource resource) throws IOException {
            final MockSnmpAgent agent = m_agents.get(address);
            if (agent == null) {
                LOG.warn("Unable to set mock SNMP data for {}: no such agent", address);
                return;
            }
            agent.updateValuesFromResource(resource.getURL());
        }

        @Override
        public void resetData() {
            for (final MockSnmpAgent agent : m_agents.values()) {
                try {
                    LOG.debug("Shutting down agent: {}", agent);
                    agent.shutDownAndWait();
                } catch (final InterruptedException e) {
                    LOG.debug("Unable to shut down agent {}", agent, e);
                    // Thread.currentThread().interrupt();
                }
            }
            m_agents.clear();
        }

        @Override
        public String toString() {
            return "MockSnmpAgentDataProvider[" + m_agents.size() + " agents]";
        }

        @Override
        public void updateIntValue(SnmpAgentAddress address, String oid, int val) {
            final MockSnmpAgent agent = m_agents.get(address);
            if (agent == null) {
                LOG.warn("Unable to set mock SNMP data for {}: no such agent", address);
                return;
            }
            agent.updateIntValue(oid, val);
        }

        @Override
        public void updateStringValue(SnmpAgentAddress address, String oid, String val) {
            final MockSnmpAgent agent = m_agents.get(address);
            if (agent == null) {
                LOG.warn("Unable to set mock SNMP data for {}: no such agent", address);
                return;
            }
            agent.updateStringValue(oid, val);
        }

        @Override
        public void updateCounter32Value(SnmpAgentAddress address, String oid, int val) {
            final MockSnmpAgent agent = m_agents.get(address);
            if (agent == null) {
                LOG.warn("Unable to set mock SNMP data for {}: no such agent", address);
                return;
            }
            agent.updateCounter32Value(oid, val);
        }

        @Override
        public void updateCounter64Value(SnmpAgentAddress address, String oid, long val) {
            final MockSnmpAgent agent = m_agents.get(address);
            if (agent == null) {
                LOG.warn("Unable to set mock SNMP data for {}: no such agent", address);
                return;
            }
            agent.updateCounter64Value(oid, val);
        }

    }

}
