/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.vmmgr;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.config.service.Service;
import org.opennms.netmgt.config.service.types.InvokeAtType;
import org.opennms.test.OpenNMSConfigurationExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    DirtiesContextTestExecutionListener.class
})
public class InvokerTest {
    private List<InvokerService> m_services = null;
    private MBeanServer m_server;

    @Before
    public void setUp() throws Throwable {
        m_server = MBeanServerFactory.createMBeanServer("OpenNMS");
        m_services = InvokerService.createServiceList(getServiceList());
        
        Invoker invoker = new Invoker();
        invoker.setServer(m_server);
        invoker.setAtType(InvokeAtType.START);
        invoker.setServices(m_services);
        invoker.instantiateClasses();

        invokeMethods(invoker);
        
        Thread.sleep(1000);
    }
    
    @Test
    public void tryStatus() throws Throwable {
        Invoker invoker = new Invoker();
        invoker.setServer(m_server);
        invoker.setAtType(InvokeAtType.STATUS);
        invoker.setFailFast(false);

        invoker.setServices(m_services);
        invoker.getObjectInstances();

        invokeMethods(invoker);
    }

    @After
    public void tearDown() throws Throwable {
        Invoker invoker = new Invoker();
        invoker.setServer(m_server);
        invoker.setAtType(InvokeAtType.STOP);
        invoker.setReverse(true);
        invoker.setFailFast(false);

        invoker.setServices(m_services);
        invoker.getObjectInstances();

        invokeMethods(invoker);
    }

    private static void invokeMethods(Invoker invoker) throws Throwable {
        for (InvokerService iservice : invoker.getServices()) {
            Service service = iservice.getService();
            ObjectName name = new ObjectName(service.getName());
            System.err.println("object instance = " + getObjectInstanceString(invoker.getServer().getObjectInstance(name)));
        }

        List<InvokerResult> results = invoker.invokeMethods();
        System.err.println(invoker.getAtType().toString() + ": got " + results.size() + " results");

        for (InvokerResult result : results) {
            System.err.println(invoker.getAtType().toString() + ": result = " + getResultString(result));

            if (result.getThrowable() != null) {
                throw result.getThrowable();
            }
        }
    }

    private static String getObjectInstanceString(ObjectInstance objectInstance) {
        return new ToStringBuilder(objectInstance)
            .append("class", objectInstance.getClassName())
            .append("object", objectInstance.getObjectName())
            .toString();
    }

    private static Service[] getServiceList() throws Exception {
        List<Service> serviceList = new ArrayList<Service>();

        serviceList.add(Service.unmarshal(new StringReader("  <service>\n" + 
        		"    <name>:Name=XSLTProcessor</name>\n" + 
        		"    <class-name>mx4j.tools.adaptor.http.XSLTProcessor</class-name>\n" + 
        		"  </service>\n" + 
        		"")));
        serviceList.add(Service.unmarshal(new StringReader("  <service>\n" + 
        		"    <name>:Name=HttpAdaptor</name>\n" + 
        		"    <class-name>mx4j.tools.adaptor.http.HttpAdaptor</class-name>\n" + 
        		"    <attribute>\n" + 
        		"      <name>Port</name>\n" + 
        		"      <value type=\"java.lang.Integer\">58180</value>\n" + 
        		"    </attribute>\n" + 
        		"    <attribute>\n" + 
        		"      <name>Host</name>\n" + 
        		"      <value type=\"java.lang.String\">127.0.0.1</value>\n" + 
        		"    </attribute>\n" + 
        		"    <attribute>\n" + 
        		"      <name>ProcessorName</name>\n" + 
        		"      <value type=\"javax.management.ObjectName\">:Name=XSLTProcessor</value>\n" + 
        		"    </attribute>\n" + 
        		"    <attribute>\n" + 
        		"      <name>AuthenticationMethod</name>\n" + 
        		"      <value type=\"java.lang.String\">basic</value>\n" + 
        		"    </attribute>\n" + 
        		"    <invoke at=\"start\" pass=\"0\" method=\"addAuthorization\">\n" + 
        		"      <argument type=\"java.lang.String\">admin</argument>\n" + 
        		"      <argument type=\"java.lang.String\">admin</argument>\n" + 
        		"    </invoke>\n" + 
        		"    <invoke at=\"start\" pass=\"0\" method=\"start\"/>\n" + 
        		"  </service>\n" + 
        		"")));
        serviceList.add(Service.unmarshal(new StringReader("  <service>\n" + 
        		"    <name>:Name=HttpAdaptorMgmt</name>\n" + 
        		"    <class-name>mx4j.tools.adaptor.http.HttpAdaptor</class-name>\n" + 
        		"    <attribute>\n" + 
        		"      <name>Port</name>\n" + 
        		"      <value type=\"java.lang.Integer\">58181</value>\n" + 
        		"    </attribute>\n" + 
        		"    <attribute>\n" + 
        		"      <name>Host</name>\n" + 
        		"      <value type=\"java.lang.String\">127.0.0.1</value>\n" + 
        		"    </attribute>\n" + 
        		"    <attribute>\n" + 
        		"      <name>AuthenticationMethod</name>\n" + 
        		"      <value type=\"java.lang.String\">basic</value>\n" + 
        		"    </attribute>\n" + 
        		"    <invoke at=\"start\" pass=\"0\" method=\"addAuthorization\">\n" + 
        		"      <argument type=\"java.lang.String\">manager</argument>\n" + 
        		"      <argument type=\"java.lang.String\">manager</argument>\n" + 
        		"    </invoke>\n" + 
        		"    <invoke at=\"start\" pass=\"0\" method=\"start\"/>\n" + 
        		"  </service>\n" + 
        		"")));

        return serviceList.toArray(new Service[0]);
    }

    private static String getResultString(InvokerResult result) {
        return new ToStringBuilder(result)
            .append("result", result.getResult())
            .append("mbean", result.getMbean())
            .append("service", getServiceString(result.getService()))
            .append("throwable", result.getThrowable())
            .toString();
    }

    private static String getServiceString(Service service) {
        return new ToStringBuilder(service)
            .append("name", service.getName())
            .append("class", service.getClassName())
            .append("attributes", service.getAttributeCollection())
            .append("invoke", service.getInvokeCollection())
            .toString();
    }

}
