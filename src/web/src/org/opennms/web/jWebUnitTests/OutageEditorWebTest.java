//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.web.jWebUnitTests;

import java.io.FileReader;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.jwebunit.ExpectedCell;
import net.sourceforge.jwebunit.ExpectedRow;
import net.sourceforge.jwebunit.ExpectedTable;
import net.sourceforge.jwebunit.WebTestCase;

import org.exolab.castor.xml.Unmarshaller;
import org.opennms.netmgt.config.common.BasicSchedule;
import org.opennms.netmgt.config.common.Time;
import org.opennms.netmgt.config.poller.Outage;
import org.opennms.netmgt.config.poller.Outages;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockUtil;

import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebImage;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import com.sun.rsasign.l;

public class OutageEditorWebTest extends WebTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(OutageEditorWebTest.class);
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
            "  <!-- Database parameters -->  \n" + 
            "  <context-param>\n" + 
            "    <param-name>opennms.db.poolman</param-name>\n" + 
            "    <param-value>org.opennms.core.resource.db.SimpleDbConnectionFactory</param-value>\n" + 
            "  </context-param>\n" + 
            "  <context-param>\n" + 
            "    <param-name>opennms.db.driver</param-name>\n" + 
            "    <param-value>org.hsqldb.jdbcDriver</param-value>\n" + 
            "  </context-param>\n" + 
            "  <context-param>\n" + 
            "    <param-name>opennms.db.url</param-name>\n" + 
            "    <param-value>jdbc:hsqldb:mem:test</param-value>\n" + 
            "  </context-param>\n" + 
            "  <context-param>\n" + 
            "    <param-name>opennms.db.user</param-name>\n" + 
            "    <param-value>sa</param-value>\n" + 
            "  </context-param>\n" + 
            "  <context-param>\n" + 
            "    <param-name>opennms.db.password</param-name>\n" + 
            "    <param-value></param-value>\n" + 
            "  </context-param>\n" + 
            "\n" + 
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
    private MockNetwork m_network;
    private MockDatabase m_db;
    private Outages m_outages;
    

    protected void setUp() throws Exception {
        MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
        

//        ServletRunner sr = new ServletRunner(new File("dist/webapps/opennms/WEB-INF/web.xml"), "/opennms");
      ServletRunner sr = new ServletRunner(new StringBufferInputStream(web_xml), "/opennms");

      // 
      MockUtil.setupLogging();

        
           ServletUnitClient sc = sr.newClient();
           
           getTestContext().setWebClient(sc);
           getTestContext().setAuthorization("admin","admin");
           getTestContext().setBaseUrl("http://localhost:8080/opennms");

    
           MockUtil.setupLogging();
           MockUtil.resetLogLevel();

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

           m_outages = (Outages)Unmarshaller.unmarshal(Outages.class, new FileReader("../../etc/poll-outages.xml"));
           
}

    protected void tearDown() throws Exception {
        assertTrue("Unexpected WARN or ERROR msgs in Log!", MockUtil.noWarningsOrHigherLogged());
        m_db.drop();
        MockUtil.println("------------ End Test "+getName()+" --------------------------");
    }
    
    public void testOutageList() throws Exception {
        beginAt("/admin/sched-outages/index.jsp");
        assertTitleEquals("Scheduled Outage administration");

        checkOutagesTable(m_outages);

        
        submit();
        assertTitleEquals("Scheduled Outage administration");
        assertTextPresent("Edit Outages");
    }

    private void checkOutagesTable(Outages pollOutages) {
        
        WebTable table = getDialog().getWebTableBySummaryOrId("outages");
        // Top header line
        assertCell(table, 0, 0, "", 4);
        assertCell(table, 0, 4, "Affects...", 4);
        // Second header line
        String[] headerCells = { "Name", "Type", "Nodes/Interfaces", "Times", "Notifications", "Polling", "Thresholds", "Data collection", null, null};
        assertRow(table, 1, headerCells);
        Outage[] outages = pollOutages.getOutage();
        for (int i = 0; i < outages.length; i++) {
            Outage outage = outages[i];
            assertCell(table, i+2, 0, outage.getName());
            assertCell(table, i+2, 1, outage.getType());
            
            // TODO: check for interfaces and nodes
            assertCell(table, i+2, 2, "");
            
            assertCell(table, i+2, 3, getTimeSpanString(outage));
            
            // test the X 's
            assertCellImage(table, i+2, 4, computeImgSrc(outage));
            assertCellImage(table, i+2, 5, computeImgSrc(outage));
            assertCellImage(table, i+2, 6, computeImgSrc(outage));
            assertCellImage(table, i+2, 7, computeImgSrc(outage));
            
            // the the links
            assertCellLink(table, i+2, 8, "Edit", computeEditURL(outage));
            assertCellLink(table, i+2, 9, "Delete", computeDeleteURL(outage));
            
        }
    }
    
    private String computeImgSrc(Outage outage) {
        // TODO: correctly compute this based on the outage
        return "images/redcross.gif";
    }

    private String computeDeleteURL(Outage outage) {
        return "admin/sched-outages/index.jsp?deleteOutage="+outage.getName().replace(' ', '+');
    }

    private String computeEditURL(Outage outage) {
        return "admin/sched-outages/editoutage.jsp?name="+outage.getName().replace(' ','+');
    }
    
    public void assertCellImage(WebTable table, int row, int col, String imgSrc) {
        assertEquals(1, table.getTableCell(row, col).getImages().length);
        WebImage img = table.getTableCell(row, col).getImages()[0];
        assertNotNull(img);
        assertEquals(imgSrc, img.getSource());
    }
    
    public void assertCellLink(WebTable table, int row, int col, String text, String url) {
        assertEquals(1, table.getTableCell(row, col).getLinks().length);
        WebLink link = table.getTableCell(row, col).getLinks()[0];
        assertNotNull(link);
        assertEquals(text, link.getText());
        assertEquals(url, link.getURLString());
    }
    
    public void assertCellLink(WebTable table, int row, int col, String[] text) {
        assertEquals(text.length, table.getTableCell(row, col).getLinks().length);

      for (int i = 0; i < text.length; i++) {
        WebLink link = table.getTableCell(row, col).getLinks()[i];
        assertNotNull(link);
        assertEquals(text[i], link.getText());
      }
    }
    
    public void assertCell(WebTable table, int row, int col, String contents, int colspan) {
        if (contents == null) {
            assertNull(table.getTableCell(row, col));
        } else {
            assertNotNull(table.getTableCell(row, col));
            assertEquals(contents, table.getCellAsText(row, col));
            assertEquals(colspan, table.getTableCell(row, col).getColSpan());
        }
    }
    
    public void assertCell(WebTable table, int row, int col, String contents) {
        assertCell(table, row, col, contents, 1);
    }
    
    public void assertRow(WebTable table, int row, String[] contents) {
        for(int i = 0; i < contents.length; i++) {
            assertCell(table, row, i, contents[i]);
        }
    }
    
    // TODO: Add checks for interfaces and nodes
    // TODO: check that images correctly map to notifications, poling.. etc
    // TODO: test the form
    // TODO: test the edit link and resulting edit page
    // TODO: ensure that created outages add to the file correctly
    // TODO: ensure that new outages added to index page
    // TODO: test that you can't create the same outage twice?
    // TODO: test the delete link
    // TODO: verify header and footer
    // TODO: verify security/authorization

    private void appendRow(ExpectedTable expectedTable, List cells) {
        ExpectedRow row = new ExpectedRow((ExpectedCell[]) cells.toArray(new ExpectedCell[cells.size()]));
        expectedTable.appendRow(row);
    }

    private String getTimeSpanString(BasicSchedule sched) {
        StringBuffer buf = new StringBuffer();
        Time[] times = sched.getTime();
        for(int i = 0; i < times.length; i++) {
            if (i != 0) {
                buf.append('\n');
            }
            Time time = times[i];
            if (time.getDay() != null) {
                buf.append(getDayString(time.getDay()));
                buf.append(' ');
            }
            buf.append(time.getBegins());
            buf.append(" -");
            if ("specific".equals(sched.getType())) {
                buf.append('\n');
            }
            buf.append(' ');
            buf.append(time.getEnds());
        }
        return buf.toString();
    }

    private Object getDayString(String day) {
        if ("sunday".equals(day)) {
            return "Sun";
        } else if ("monday".equals(day)) {
            return "Mon";
        } else if ("tuesday".equals(day)) {
            return "Tue";
        } else if ("wednesday".equals(day)) {
            return "Wed";
        } else if ("thursday".equals(day)) {
            return "Thu";
        } else if ("friday".equals(day)) {
            return "Fri";
        } else if ("saturday".equals(day)) {
            return "Sat";
        } else {
            return day;
        }
    }

}
