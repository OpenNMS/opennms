package org.opennms.features.config.convert;

import org.junit.runners.Parameterized;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.config.snmp.SnmpProfiles;

import javax.xml.bind.JAXB;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

public class SnmpConfigTest extends CmConfigTest<SnmpConfig> {

    public SnmpConfigTest(SnmpConfig sampleObject, String sampleXml) {
        super(sampleObject, sampleXml, "snmp-config.xsd", "snmp-config");
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
                {
                        getConfig(),
                        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" write-community=\"private\" read-community=\"public\" timeout=\"800\" retry=\"3\">\n" +
                                "   <definition version=\"v2c\" ttl=\"7000\" profile-label=\"profile2\">\n" +
                                "      <range begin=\"10.0.0.1\" end=\"10.4.252.40\"/>\n" +
                                "      <range begin=\"192.168.0.1\" end=\"192.168.0.10\"/>\n" +
                                "   </definition>\n" +
                                "   <definition version=\"v1\" ttl=\"6000\" profile-label=\"profile1\">\n" +
                                "      <specific>127.0.0.1</specific>\n" +
                                "   </definition>\n" +
                                "   <profiles>\n" +
                                "      <profile version=\"v1\" read-community=\"horizon\" timeout=\"10000\">\n" +
                                "         <label>profile1</label>\n" +
                                "      </profile>\n" +
                                "      <profile version=\"v1\" ttl=\"6000\">\n" +
                                "         <label>profile2</label>\n" +
                                "         <filter>iphostname LIKE '%opennms%'</filter>\n" +
                                "      </profile>\n" +
                                "      <profile version=\"v1\" read-community=\"meridian\">\n" +
                                "         <label>profile3</label>\n" +
                                "         <filter>IPADDR IPLIKE 172.1.*.*</filter>\n" +
                                "      </profile>\n" +
                                "   </profiles>\n" +
                                "</snmp-config>"
                },
                {
                        new SnmpConfig(),
                        "<snmp-config/>"
                }
        });
    }

    private static SnmpConfig getConfig() {
        final SnmpConfig snmpConfig = new SnmpConfig();
        snmpConfig.setWriteCommunity("private");
        snmpConfig.setReadCommunity("public");
        snmpConfig.setTimeout(800);
        snmpConfig.setRetry(3);

        final Definition definition1 = new Definition();
        definition1.setVersion("v2c");
        definition1.setTTL(7000L);
        final Range range1 = new Range();
        range1.setBegin("10.0.0.1");
        range1.setEnd("10.4.252.40");
        definition1.addRange(range1);
        final Range range2 = new Range();
        range2.setBegin("192.168.0.1");
        range2.setEnd("192.168.0.10");
        definition1.addRange(range2);
        definition1.setProfileLabel("profile2");
        snmpConfig.addDefinition(definition1);

        final Definition definition2 = new Definition();
        definition2.setVersion("v1");
        definition2.setTTL(6000L);
        definition2.addSpecific("127.0.0.1");
        definition2.setProfileLabel("profile1");
        snmpConfig.addDefinition(definition2);

        final SnmpProfiles snmpProfiles = new SnmpProfiles();

        final SnmpProfile snmpProfile1 = new SnmpProfile();
        snmpProfile1.setLabel("profile1");
        snmpProfile1.setVersion("v1");
        snmpProfile1.setReadCommunity("horizon");
        snmpProfile1.setTimeout(10000);
        snmpProfiles.addSnmpProfile(snmpProfile1);

        final SnmpProfile snmpProfile2 = new SnmpProfile();
        snmpProfile2.setLabel("profile2");
        snmpProfile2.setVersion("v1");
        snmpProfile2.setTTL(6000L);
        snmpProfile2.setFilterExpression("iphostname LIKE '%opennms%'");
        snmpProfiles.addSnmpProfile(snmpProfile2);

        final SnmpProfile snmpProfile3 = new SnmpProfile();
        snmpProfile3.setLabel("profile3");
        snmpProfile3.setVersion("v1");
        snmpProfile3.setReadCommunity("meridian");
        snmpProfile3.setFilterExpression("IPADDR IPLIKE 172.1.*.*");
        snmpProfiles.addSnmpProfile(snmpProfile3);

        snmpConfig.setSnmpProfiles(snmpProfiles);

        JAXB.marshal(snmpConfig, System.out);
        return snmpConfig;
    }
}
