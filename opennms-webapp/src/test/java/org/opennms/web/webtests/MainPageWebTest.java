//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//Modifications:
//
//2008 Jan 21: Remove database options from web.xml. - dj@opennms.org
//
//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
//For more information contact:
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/
//
package org.opennms.web.webtests;

import java.io.StringBufferInputStream;

import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.eventd.Eventd;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockEventConfigManager;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;

import com.meterware.httpunit.WebTable;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public class MainPageWebTest extends OpenNMSWebTestCase {
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(MainPageWebTest.class);
    }
    
    // TODO: find some way to use the real web.xml and override context parameters.
    String web_xml = 
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + 
        "\n" + 
        "<!DOCTYPE web-app\n" + 
        "    PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\"\n" + 
        "    \"http://java.sun.com/dtd/web-app_2_3.dtd\">\n" + 
        "\n" + 
        "\n" + 
        "<web-app>\n" + 
        "  <context-param>\n" + 
        "    <param-name>opennms.home</param-name>\n" + 
        "    <param-value>../..</param-value>\n" + 
        "  </context-param>\n" + 
        "  \n" + 
        "  <!-- Set this for JRobin graphs and Availablity Reporting -->\n" + 
        "  <context-param>\n" + 
        "   <param-name>java.awt.headless</param-name>\n" + 
        "   <param-value>true</param-value>\n" + 
        "  </context-param>\n" + 
        "  \n" + 
        "  <!-- RTC Subscription parameters -->  \n" + 
        "  <context-param>\n" + 
        "    <param-name>opennms.rtc-client.http-post.username</param-name>\n" + 
        "    <param-value>rtc</param-value>\n" + 
        "    <description>The username the RTC uses when authenticating itself in an HTTP POST.</description>\n" + 
        "  </context-param>\n" + 
        "  <context-param>\n" + 
        "    <param-name>opennms.rtc-client.http-post.password</param-name>\n" + 
        "    <param-value>rtc</param-value>\n" + 
        "    <description>The password the RTC uses when authenticating itself in an HTTP POST.</description>\n" + 
        "  </context-param>\n" + 
        "  <context-param>\n" + 
        "    <param-name>opennms.rtc-client.http-post.base-url</param-name>\n" + 
        "    <param-value>http://localhost:8080/opennms/rtc/post</param-value>\n" + 
        "    <description>\n" + 
        "      The base of a URL that RTC clients use when creating a RTC subscription URL. \n" + 
        "      IMPORTANT: This URL must NOT contain a slash at the end.       \n" + 
        "    </description>\n" + 
        "  </context-param>\n" + 
        "\n" + 
        "  <listener>\n" + 
        "    <!-- This listener handles our custom startup/shutdown behavior. -->\n" + 
        "    <listener-class>org.opennms.web.InitializerServletContextListener</listener-class>\n" + 
        "  </listener>\n" + 
        "\n" + 
        "\n" + 
        "  <error-page>\n" + 
        "    <exception-type>org.opennms.web.category.CategoryNotFoundException</exception-type>\n" + 
        "    <location>/errors/categorynotfound.jsp</location>\n" + 
        "  </error-page>\n" + 
        "  <error-page>\n" + 
        "    <exception-type>org.opennms.web.MissingParameterException</exception-type>\n" + 
        "    <location>/errors/missingparam.jsp</location>\n" + 
        "  </error-page>\n" + 
        "  <error-page>\n" + 
        "    <exception-type>org.opennms.web.event.EventIdNotFoundException</exception-type>\n" + 
        "    <location>/errors/eventidnotfound.jsp</location>\n" + 
        "  </error-page>\n" + 
        "  <error-page>\n" + 
        "    <exception-type>org.opennms.web.notification.NoticeIdNotFoundException</exception-type>\n" + 
        "    <location>/errors/noticeidnotfound.jsp</location>\n" + 
        "  </error-page>\n" + 
        "  <error-page>\n" + 
        "    <exception-type>org.opennms.web.outage.OutageIdNotFoundException</exception-type>\n" + 
        "    <location>/errors/outageidnotfound.jsp</location>\n" + 
        "  </error-page>\n" + 
        "  <error-page>\n" + 
        "    <exception-type>java.lang.SecurityException</exception-type>\n" + 
        "    <location>/errors/sealingviolation.jsp</location>\n" + 
        "  </error-page>\n" + 
        "  <error-page>\n" + 
        "    <exception-type>org.opennms.web.vulnerability.VulnerabilityIdNotFoundException</exception-type>\n" + 
        "    <location>/errors/vulnerabilityidnotfound.jsp</location>\n" + 
        "  </error-page>  \n" + 
        "  <error-page>\n" + 
        "    <exception-type>org.opennms.netmgt.utils.EventProxyException</exception-type>\n" + 
        "    <location>/errors/eventproxyexception.jsp</location>\n" + 
        "  </error-page>  \n" + 
        "  <error-page>\n" + 
        "    <exception-type>java.lang.Exception</exception-type>\n" + 
        "    <location>/errors/unknownexception.jsp</location>\n" + 
        "  </error-page>  \n" + 
        "  \n" + 
        "\n" + 
        "  <!-- Note: The order of these security-constraints is significant! -->\n" + 
        "  <security-constraint>\n" + 
        "    <web-resource-collection>\n" + 
        "      <web-resource-name>Administrative Controls</web-resource-name>\n" + 
        "      <url-pattern>/admin/*</url-pattern>\n" + 
        "    </web-resource-collection>\n" + 
        "    <auth-constraint>\n" + 
        "      <role-name>OpenNMS Administrator</role-name>\n" + 
        "    </auth-constraint>\n" + 
        "  </security-constraint>\n" + 
        "  <security-constraint>\n" + 
        "    <web-resource-collection>\n" + 
        "      <web-resource-name>Real-Time Console Data Update Servlets</web-resource-name>\n" + 
        "      <url-pattern>/rtc/post/*</url-pattern>\n" + 
        "    </web-resource-collection>\n" + 
        "    <auth-constraint>\n" + 
        "      <role-name>OpenNMS RTC Daemon</role-name>\n" + 
        "    </auth-constraint>\n" + 
        "  </security-constraint>\n" + 
        "  <security-constraint>  \n" + 
        "    <web-resource-collection>    \n" + 
        "      <web-resource-name>Entire Application</web-resource-name>\n" + 
        "      <url-pattern>/*</url-pattern>\n" + 
        "    </web-resource-collection>\n" + 
        "    <auth-constraint>\n" + 
        "      <role-name>OpenNMS User</role-name>\n" + 
        "    </auth-constraint>\n" + 
        "  </security-constraint>\n" + 
        "\n" + 
        "  <login-config>\n" + 
        "    <auth-method>BASIC</auth-method>\n" + 
        "    <realm-name>OpenNMS Web Console</realm-name>\n" + 
        "  </login-config>\n" + 
        "\n" + 
        "  <security-role>\n" + 
        "    <description>\n" + 
        "      OpenNMS Administrator\n" + 
        "    </description>\n" + 
        "    <role-name>OpenNMS Administrator</role-name>\n" + 
        "  </security-role>\n" + 
        "\n" + 
        "  <security-role>\n" + 
        "    <description>\n" + 
        "      OpenNMS RTC Daemon\n" + 
        "    </description>\n" + 
        "    <role-name>OpenNMS RTC Daemon</role-name>\n" + 
        "  </security-role>\n" + 
        "\n" + 
        "  <security-role>\n" + 
        "    <description>\n" + 
        "      OpenNMS User\n" + 
        "    </description>\n" + 
        "    <role-name>OpenNMS User</role-name>\n" + 
        "  </security-role>\n" + 
        "\n" + 
        "</web-app>\n" + 
        "";
    
    String config_xml = 
        "<EventdConfiguration\n" + 
        "   TCPPort=\"15817\"\n" + 
        "   UDPPort=\"15817\"\n" + 
        "   receivers=\"5\"\n" + 
        "   getNextEventID=\"SELECT nextval(\'eventsNxtId\')\"\n" + 
        "   getNextAlarmID=\"SELECT nextval(\'alarmsNxtId\')\"\n" + 
        "   socketSoTimeoutRequired=\"yes\"\n" + 
        "   socketSoTimeoutPeriod=\"3000\">\n" + 
        "</EventdConfiguration>";
    
    private MockNetwork m_network;
    private MockDatabase m_db;
    private Eventd m_eventd;
    
    private ServletRunner m_servletRunner;
    
    
    protected void setUp() throws Exception {
        MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
        
        MockLogAppender.setupLogging();
        
        m_network = new MockNetwork();
        m_network.setCriticalService("ICMP");
        m_network.addNode(1, "Router");
        m_network.addInterface("192.168.1.1");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addInterface("192.168.1.2");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addNode(2, "Server");
        m_network.addInterface("192.168.1.3");
        m_network.addService("ICMP");
        m_network.addService("HTTP");
        m_network.addNode(3, "Firewall");
        m_network.addInterface("192.168.1.4");
        m_network.addService("SMTP");
        m_network.addService("HTTP");
        m_network.addInterface("192.168.1.5");
        m_network.addService("SMTP");
        m_network.addService("HTTP");
        
        m_db = new MockDatabase();
        m_db.populate(m_network);
        
        DataSourceFactory.setInstance(m_db);
        
        MockEventConfigManager eventdConfigMgr = new MockEventConfigManager(config_xml);
        MockEventIpcManager ipcMgr = new MockEventIpcManager();
        m_eventd = new Eventd();
        m_eventd.setConfigManager(eventdConfigMgr);
        m_eventd.setEventIpcManager(ipcMgr);
        m_eventd.setDataSource(m_db);
        
        m_eventd.init();
        m_eventd.start();
        
        
        
        m_servletRunner = new ServletRunner(new StringBufferInputStream(web_xml), "/opennms");
        // 
        MockLogAppender.setupLogging();
        
        
        ServletUnitClient sc = m_servletRunner.newClient();
        
        getTestContext().setWebClient(sc);
        getTestContext().setAuthorization("admin","OpenNMS Administrator");
        getTestContext().setBaseUrl("http://localhost:8080/opennms");
        
        
    }
    
    protected void tearDown() throws Exception {
        m_servletRunner.shutDown();
        m_eventd.stop();
        MockLogAppender.assertNoWarningsOrGreater();
        m_db.drop();
        MockUtil.println("------------ End Test "+getName()+" --------------------------");
    }
    
    public void testBogus() {
        // Empty test so JUnit doesn't complain about not having any tests to run
    }
    
    public void FIXMEtestMainPage() throws Exception {
        beginAt("/index.jsp");

        assertMainMargins();

        getTester().dumpResponse();
        assertCategoriesBox();
        assertNotificationBox();
        assertPerformanceBox();
        assertResponseBox();
        assertKSCBox();
        
    }

    private void assertMainMargins() {
        assertTitleEquals("OpenNMS Web Console");
        assertHeaderPresent("Web Console", "", new String[] {});
        assertFooterPresent("");
    }

    private void assertKSCBox() {
        assertTablePresent("ksc");
        
    }

    private void assertResponseBox() {
        assertTablePresent("response");
        
    }

    private void assertPerformanceBox() {
        assertTablePresent("performance");
        
    }

    private void assertNotificationBox() {
        assertTablePresent("notification");
        
    }

    private void assertCategoriesBox() {
        assertTablePresent("categories");
        WebTable outagestable = getDialog().getWebTableBySummaryOrId("categories");
        
    }
    
}
