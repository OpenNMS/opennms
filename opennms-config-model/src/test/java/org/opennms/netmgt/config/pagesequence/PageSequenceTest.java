package org.opennms.netmgt.config.pagesequence;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.junit.Before;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.MockLogger;
import org.opennms.core.test.xml.XmlTest;

public class PageSequenceTest extends XmlTest<PageSequence> {
    public PageSequenceTest(final PageSequence sampleObject, final Object sampleXml, final String schemaFile, final String[] ignoredNamespaces, final String[] ignoredPrefixes) {
        super(sampleObject, sampleXml, schemaFile, ignoredNamespaces, ignoredPrefixes);
    }

    @Before
    public void setUp() {
        super.setUp();
        final Properties props = new Properties();
        props.put(MockLogger.LOG_KEY_PREFIX + "org.opennms.core.xml.JaxbUtils", "TRACE");
        props.put(MockLogger.LOG_KEY_PREFIX + "org.opennms.core.xml.JaxbClassObjectAdapter", "TRACE");
        MockLogAppender.setupLogging(true, props);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getPageSequence(),
                    new File("target/test-classes/org/opennms/netmgt/config/pagesequence/page-sequence.xml"),
                    "target/classes/xsds/page-sequence.xsd",
                    new String[] { "http://xmlns.opennms.org/xsd/page-sequence" },
                    new String[] { }
                }
        });
    }

    protected static PageSequence getPageSequence() {
        final PageSequence ps = new PageSequence();

        Page page = new Page();
        page.setMethod("GET");
        page.setHttpVersion("1.1");
        page.setScheme("http");
        page.setHost("${ipaddr}");
        page.setDisableSslVerification("true");
        page.setPort(7080);
        page.setPath("/Login.do");
        page.setSuccessMatch("(HQ Login)|(Sign in to Hyperic HQ)");
        page.setResponseRange("100-399");
        ps.addPage(page);

        page = new Page();
        page.setMethod("POST");
        page.setHttpVersion("1.1");
        page.setScheme("http");
        page.setHost("${ipaddr}");
        page.setDisableSslVerification("true");
        page.setPort(7080);
        page.setPath("/j_security_check.do");
        page.setFailureMatch("(?s)(The username or password provided does not match our records)|(You are not signed in)");
        page.setFailureMessage("HQ Login in Failed");
        page.setSuccessMatch("HQ Dashboard");
        page.setResponseRange("100-399");
        org.opennms.netmgt.config.pagesequence.Parameter parameter = new org.opennms.netmgt.config.pagesequence.Parameter();
        parameter.setKey("j_username");
        parameter.setValue("hqadmin");
        page.addParameter(parameter);
        parameter = new org.opennms.netmgt.config.pagesequence.Parameter();
        parameter.setKey("j_password");
        parameter.setValue("hqadmin");
        page.addParameter(parameter);
        ps.addPage(page);

        page = new Page();
        page.setPath("/Logout.do");
        page.setPort(7080);
        page.setSuccessMatch("HQ Login");
        ps.addPage(page);

        return ps;
    }
}
