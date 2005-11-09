package org.opennms.web.jWebUnitTests;

import java.io.FileInputStream;

import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import net.sourceforge.jwebunit.WebTestCase;

public class mhtest extends WebTestCase {

    public static void main(String[] args) {
    }

    protected void setUp() throws Exception {
        ServletRunner sr = new ServletRunner(new FileInputStream("src/web/etc/web.xml"));
        
           ServletUnitClient sc = sr.newClient();
           getTestContext().setWebClient(sc);
           getTestContext().setAuthorization("admin","admin");
           getTestContext().setBaseUrl("http://localhost:8080/opennms");
    }

}
