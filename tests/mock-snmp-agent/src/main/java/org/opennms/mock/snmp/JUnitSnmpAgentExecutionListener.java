/*
 * This file is part of the OpenNMS(R) Application. OpenNMS(R) is Copyright
 * (C) 2008 The OpenNMS Group, Inc. All rights reserved. OpenNMS(R) is a
 * derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights
 * for modified and included code are below. OpenNMS(R) is a registered
 * trademark of The OpenNMS Group, Inc. Modifications: Original code base
 * Copyright (C) 1999-2001 Oculan Corp. All rights reserved. This program is
 * free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU
 * General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. For more information contact: OpenNMS Licensing
 * <license@opennms.org> http://www.opennms.org/ http://www.opennms.com/
 */

package org.opennms.mock.snmp;

import java.lang.reflect.Method;
import java.net.InetAddress;

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
            //host = InetAddress.getLocalHost().getHostAddress();
            host = "127.0.0.1";
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
