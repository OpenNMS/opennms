/**
 * 
 */
package org.opennms.web.jWebUnitTests;

/**
 * @author mhuot
 *
 */

import java.io.FileInputStream;
import net.sourceforge.jwebunit.WebTestCase;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public class ExampleWebTestCase extends WebTestCase {
    
    public ExampleWebTestCase(String name) {
        super(name);
    }
    
    public void setUp() throws Exception {
        ServletRunner sr = new ServletRunner(new FileInputStream("src/web/etc/web.xml"));
     
        ServletUnitClient sc = sr.newClient();
        getTestContext().setWebClient(sc);
        getTestContext().setAuthorization("admin","admin");
        getTestContext().setBaseUrl("http://localhost:8080/opennms");
    }
    
    public void _testAddInterface() {
        
        beginAt("/index.jsp");
        assertTitleEquals("OpenNMS Web Console");
        assertLinkPresentWithText("Admin");
        clickLinkWithText("Admin");
        assertTitleEquals("Admin | OpenNMS Web Console");
        clickLinkWithText("Add Interface");
        assertTitleEquals("Admin | OpenNMS Web Console");
        assertTextPresent("Please enter a new IP address below.");
        setFormElement("ipAddress", "10.10.10.10");
        submit();
        clickLinkWithText("Search");
        setFormElement("iplike", "10.10.10.10");        
        submit();
    }

    public void _testAdminPage() {
        
        beginAt("/index.jsp");
        assertTitleEquals("OpenNMS Web Console");
        assertLinkPresentWithText("Admin");
        clickLinkWithText("Admin");
        assertTitleEquals("Admin | OpenNMS Web Console");
    }
    
}