package org.opennms.netmgt.model;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class OnmsGroupTest extends XmlTestNoCastor<OnmsGroup> {

    public OnmsGroupTest(final OnmsGroup sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final OnmsGroup group = new OnmsGroup();
        group.setName("Admin");
        group.setUsers(Collections.singletonList("admin"));

        return Arrays.asList(new Object[][] {
            {
                group,
                "<group><name>Admin</name><user>admin</user></group>"
            } 
        });
    }


}
