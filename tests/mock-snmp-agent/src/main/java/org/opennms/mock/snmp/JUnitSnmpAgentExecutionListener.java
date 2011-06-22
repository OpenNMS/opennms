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
 * by the Free Software Foundation, either version 2 of the License,
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



package org.opennms.mock.snmp;

import java.lang.reflect.Method;
import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;
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
public class JUnitSnmpAgentExecutionListener extends
        AbstractTestExecutionListener {

    private static final String MOCK_SNMP_AGENT = MockSnmpAgent.class.getName();

    /*
     * (non-Javadoc)
     * @see
     * org.springframework.test.context.support.AbstractTestExecutionListener
     * #beforeTestMethod(org.springframework.test.context.TestContext)
     */
    /** {@inheritDoc} */
    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        JUnitSnmpAgent config = findAgentAnnotation(testContext);

        if (config == null) return;
        
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
        
        // NOTE: The default value for config.port is specified inside {@link JUnitSnmpAgent}
        
        ResourceLoader loader = new DefaultResourceLoader();
        Resource resource = loader.getResource(config.resource());
        MockSnmpAgent agent = MockSnmpAgent.createAgentAndRun( resource, host +"/"+ config.port());
        
        System.err.println("Set up agent " + agent + ", loaded content from " + config.resource());
        testContext.setAttribute(MOCK_SNMP_AGENT, agent);
        
        // FIXME: Is there a better way to inject the MockSnmpAgent into the test class?  Seems that spring doesn't have appropriate hooks
        if (testContext.getTestInstance() instanceof MockSnmpAgentAware) {
            System.err.println("injecting agent into MockSnmpAgentAware test: " + testContext.getTestInstance());
            ((MockSnmpAgentAware)testContext.getTestInstance()).setMockSnmpAgent(agent);
            
        }
        
    }

    /**
     * @param testContext
     * @return
     */
    private JUnitSnmpAgent findAgentAnnotation(TestContext testContext) {
        Method testMethod = testContext.getTestMethod();
        JUnitSnmpAgent config = testMethod.getAnnotation(JUnitSnmpAgent.class);
        if (config != null) {
            return config;
        }

        Class<?> testClass = testContext.getTestClass();
        return testClass.getAnnotation(JUnitSnmpAgent.class);

    }

    /*
     * (non-Javadoc)
     * @see
     * org.springframework.test.context.support.AbstractTestExecutionListener
     * #afterTestMethod(org.springframework.test.context.TestContext)
     */
    /** {@inheritDoc} */
    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        MockSnmpAgent agent = (MockSnmpAgent) testContext.getAttribute(MOCK_SNMP_AGENT);
        
        if (agent != null) {
            System.err.println("Shutting down agent "+agent);
            agent.shutDownAndWait();
        }
    }

}
