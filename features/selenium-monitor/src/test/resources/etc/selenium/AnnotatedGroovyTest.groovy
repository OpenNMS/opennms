package selenium;

import static org.junit.Assert.*

import org.junit.Test
import org.junit.runner.RunWith
import org.opennms.netmgt.junit.runner.SeleniumJUnitRunner
import org.opennms.netmgt.junit.runner.SeleniumJUnitRunner.BaseUrl
import org.opennms.netmgt.junit.runner.SeleniumJUnitRunner.TimeoutInSeconds

@RunWith(SeleniumJUnitRunner.class)
@BaseUrl(url="http://www.papajohns.co.uk")
@TimeoutInSeconds(timeout=3)
class AnnotatedGroovyTest {

    private String m_baseUrl = "";
    private int m_timeout;
    public AnnotatedGroovyTest(String baseUrl, int timeoutInSeconds) {
        m_baseUrl = baseUrl;
        m_timeout = timeoutInSeconds;
    }
    
    @Test
    public void testBaseUrl() {
        assertTrue("Base Url must be http://www.papajohns.co.uk", m_baseUrl.equals("http://www.papajohns.co.uk"));
        assertEquals(3, m_timeout);
    }
}
