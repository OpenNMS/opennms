/**
 * 
 */
package org.opennms.web.webtests;

/**
 * @author mhuot
 *
 */

import java.io.File;

import org.opennms.netmgt.mock.MockUtil;

import net.sourceforge.jwebunit.WebTestCase;

import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public class ExampleWebTest extends WebTestCase {
    
    public ExampleWebTest(String name) {
        super(name);
    }
    
    public void setUp() throws Exception {
        MockUtil.setupLogging();
        
	// This test needs to be run from the dist/webapps/opennms directory
        ServletRunner sr = new ServletRunner(new File("WEB-INF/web.xml"), "/opennms");
     
        ServletUnitClient sc = sr.newClient();
        getTestContext().setWebClient(sc);
        getTestContext().setAuthorization("admin","OpenNMS Administrator");
        getTestContext().setBaseUrl("http://localhost:8080/opennms");
    }
    
    public void testHelpPage() {
        
        beginAt("/help/index.jsp");
        assertTitleEquals("Help | OpenNMS Web Console");
        assertLinkPresentWithText("Home");
        clickLinkWithText("About the OpenNMS Web Console");
        assertTitleEquals("About | OpenNMS Web Console");
        assertTextPresent("You should have received a copy of the ");
        assertFormPresent("bookmark");
        clickLinkWithText("Help");
        assertTitleEquals("Help | OpenNMS Web Console");
        assertLinkPresentWithText("Home");
        assertLinkPresentWithText("About the OpenNMS Web Console");
        assertLinkPresentWithText("Frequently Asked Questions");
        assertLinkPresentWithText("Online Documentation");
        assertLinkPresentWithText("Reports");
        clickLinkWithText("Reports");
        assertTitleEquals("Reports | OpenNMS Web Console");
        assertLinkPresentWithText("Performance Reports");
        assertLinkPresentWithText("KSC Performance Reports and Node Reports");
        assertLinkPresentWithText("Availability Reports");
        assertLinkPresentWithText("Response Time Reports");
        clickLinkWithText("Availability Reports");
        assertTitleEquals("Availability | OpenNMS Web Console");
        assertFormPresent("avail");
        assertFormElementPresent("format");
        assertRadioOptionSelected("format", "SVG");
        assertFormElementPresent("category");
        assertRadioOptionSelected("category", "Overall Service Availability");
        setWorkingForm("avail");
        submit();
        //getTester().dumpResponse();
        assertTextPresent("No Email Address Configured");
        
//        assertTextPresent("You should have received a copy of the GNU General Public License along with this program; if not, write to the");
//        assertLinkPresentWithText("Admin");
//        clickLinkWithText("Admin");
//        assertTitleEquals("Admin | OpenNMS Web Console");
//        clickLinkWithText("Add Interface");
//        assertTitleEquals("Admin | OpenNMS Web Console");
//        assertTextPresent("Please enter a new IP address below.");
//        setFormElement("ipAddress", "10.10.10.10");
//        submit();
//        clickLinkWithText("Search");
//        setFormElement("iplike", "10.10.10.10");        
//        submit();
    }

    public void _testAdminPage() {
        
        beginAt("/index.jsp");
        assertTitleEquals("OpenNMS Web Console");
        assertLinkPresentWithText("Admin");
        clickLinkWithText("Admin");
        assertTitleEquals("Admin | OpenNMS Web Console");
    }
    
}
