package selenium;

import static org.junit.Assert.*

import org.junit.Test

class GroovyRunnerTest {
    
    private String m_baseUrl = "";
    private int m_timeout;
    public GroovyRunnerTest(String baseUrl, int timeoutInSeconds) {
        m_baseUrl = baseUrl;
        m_timeout = timeoutInSeconds;
    }
    
    @Test
    public void testBaseUrl() {
        assertTrue("BaseUrl must be http://www.papajohns.co.uk ", m_baseUrl.equals("http://www.papajohns.co.uk"));
        assertEquals(3, m_timeout);
    }
    
}
