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
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    DirtiesContextTestExecutionListener.class
})
@ContextConfiguration(locations={
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

    private void invokeMethods(Invoker invoker) throws Throwable {
        for (InvokerService iservice : invoker.getServices()) {
            Service service = iservice.getService();
            ObjectName name = new ObjectName(service.getName());
            System.err.println("object instance = " + getObjectInstanceString(m_server.getObjectInstance(name)));
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

    private String getObjectInstanceString(ObjectInstance objectInstance) {
        return new ToStringBuilder(objectInstance)
            .append("class", objectInstance.getClassName())
            .append("object", objectInstance.getObjectName())
            .toString();
    }

    private Service[] getServiceList() throws Exception {
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
        		"      <value type=\"java.lang.Integer\">8180</value>\n" + 
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
        		"      <value type=\"java.lang.Integer\">8181</value>\n" + 
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

    private String getResultString(InvokerResult result) {
        return new ToStringBuilder(result)
            .append("result", result.getResult())
            .append("mbean", result.getMbean())
            .append("service", getServiceString(result.getService()))
            .append("throwable", result.getThrowable())
            .toString();
    }

    private String getServiceString(Service service) {
        return new ToStringBuilder(service)
            .append("name", service.getName())
            .append("class", service.getClassName())
            .append("attributes", service.getAttributeCollection())
            .append("invoke", service.getInvokeCollection())
            .toString();
    }

}
