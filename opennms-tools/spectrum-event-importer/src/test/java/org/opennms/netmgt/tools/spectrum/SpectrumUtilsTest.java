/**
 * 
 */
package org.opennms.netmgt.tools.spectrum;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.xml.eventconf.Varbindsdecode;
import org.opennms.test.mock.MockLogAppender;

/**
 * @author jeffg
 *
 */
public class SpectrumUtilsTest {
    private SpectrumUtils m_utils;
    
    @Before
    public void setUp() {
        m_utils = new SpectrumUtils();
        m_utils.setModelTypeAssetField("manufacturer");
        MockLogAppender.setupLogging();
    }
    
    @Test
    public void translateSimpleVarbindTokens() {
        /*
        Assert.assertEquals("Check the contents against known good copy",
                            "{d \"%w- %d %m-, %Y - %T\"} - A \"systemHalt\" event has occurred, from {t} device, named {m}.\n" +
                            "\n" +
                            "\"system: report that a system halt has been initiated\"\n" +
                            "\n" +
                            "(event [{e}])\n", ef.getContents());  */
        
        Assert.assertEquals("Simple single-digit integer varbind token", "%parm[#1]%", m_utils.translateFormatSubstToken("{I 1}"));
        Assert.assertEquals("Simple two-digit integer varbind token", "%parm[#42]%", m_utils.translateFormatSubstToken("{I 42}"));
        Assert.assertEquals("Model type substitution token", "%asset[manufacturer]%", m_utils.translateFormatSubstToken("{t}"));
        Assert.assertEquals("Model type substitution token", "%nodelabel%", m_utils.translateFormatSubstToken("{m}"));
        Assert.assertEquals("Event code substitution token", "%uei%", m_utils.translateFormatSubstToken("{e}"));
    }
    
    @Test
    public void translateAllVarbindTokensInMessage() {
        String input =  "{d \"%w- %d %m-, %Y - %T\"} - A \"empSIP4xxErrorsBelowEvent\" event has occurred, from {t} device, named {m}.\n\n" +
                        "\"Event generated when one of the SIP 400s error\n" +
                        "values goes below the range specified for\n" +
                        "that particular object\"\n\n" +
                        "empSIP4xxStatEventInfo = {S 1}\n" +
                        "(event [{e}])\n";
        EventFormat format = new EventFormat("0xdeadbeef");
        format.setContents(input);
        String output = m_utils.translateAllSubstTokens(format);
        Assert.assertTrue("Date token got removed", !output.contains("{d"));
        Assert.assertTrue("Date token got replaced correctly", output.contains("%eventtime%"));
        Assert.assertTrue("Model-type token got removed", !output.contains("{t}"));
        Assert.assertTrue("Model-type token got replaced correctly", output.contains("%asset[manufacturer]%"));
        Assert.assertTrue("Model-name token got removed", !output.contains("{m}"));
        Assert.assertTrue("Model-name token got replaced correctly", output.contains("%nodelabel%"));
        Assert.assertTrue("Varbind 1 token got removed", !output.contains("{S 1}"));
        Assert.assertTrue("Varbind 1 token got replaced correctly", output.contains("%parm[#1]%"));
        Assert.assertTrue("Event-ID token got removed", !output.contains("{e}"));
        Assert.assertTrue("Event-ID token got replaced correctly", output.contains("%uei%"));
    }
    
    @Test
    public void translateSimpleEventTable() {
        EventTable et = new EventTable("foobar");
        et.put(1, "foo");
        et.put(2, "bar");
        et.put(3, "baz");
        Varbindsdecode vbd = m_utils.translateEventTable(et, "parm[#1]");
        Assert.assertEquals("Parm ID is parm[#1]", "parm[#1]", vbd.getParmid());
        Assert.assertEquals("Three decode elements", 3, vbd.getDecodeCount());
        Assert.assertEquals("First key is 1", "1", vbd.getDecode(0).getVarbindvalue());
        Assert.assertEquals("First value is foo", "foo", vbd.getDecode(0).getVarbinddecodedstring());
        Assert.assertEquals("Second key is 2", "2", vbd.getDecode(1).getVarbindvalue());
        Assert.assertEquals("Second value is bar", "bar", vbd.getDecode(1).getVarbinddecodedstring());
        Assert.assertEquals("Third key is 3", "3", vbd.getDecode(2).getVarbindvalue());
        Assert.assertEquals("Third value is baz", "baz", vbd.getDecode(2).getVarbinddecodedstring());
    }
}
