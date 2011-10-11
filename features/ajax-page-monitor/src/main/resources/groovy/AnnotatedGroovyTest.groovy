package groovy;

import static org.junit.Assert.*

import org.junit.Test
import org.junit.runner.RunWith
import org.opennms.netmgt.junit.runner.SeleniumJUnitRunner
import org.opennms.netmgt.junit.runner.SeleniumJUnitRunner.BaseUrl

@RunWith(SeleniumJUnitRunner.class)
@BaseUrl(url="http://www.papajohns.co.uk")
class AnnotatedGroovyTest {

    private String m_baseUrl = "";
    public AnnotatedGroovyTest(String baseUrl) {
        m_baseUrl = baseUrl;
    }
    
    @Test
    public void testBaseUrl() {
        assertTrue("Base Url must be http://www.papajohns.co.uk", m_baseUrl.equals("http://www.papajohns.co.uk"));
    }
}
