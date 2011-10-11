package groovy;

import static org.junit.Assert.*

import org.junit.Test

class GroovyRunnerTest {
    
    private String m_baseUrl = "";
    
    public GroovyRunnerTest(String baseUrl) {
        m_baseUrl = baseUrl;
    }
    
    @Test
    public void testBaseUrl() {
        assertTrue("BaseUrl must be http://www.papajohns.co.uk ", m_baseUrl.equals("http://www.papajohns.co.uk"));
    }
    
}
