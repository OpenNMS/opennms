package org.opennms.netmgt.model;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class OnmsGroupsTest extends XmlTestNoCastor<OnmsGroupList> {

    public OnmsGroupsTest(final OnmsGroupList sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final OnmsGroupList groupList = new OnmsGroupList();

        final OnmsGroup group = new OnmsGroup();
        group.setName("Admin");
        group.setUsers(Collections.singletonList("admin"));
        groupList.add(group);

        final OnmsGroup otherGroup = new OnmsGroup();
        otherGroup.setName("Blah");
        otherGroup.setComments("Comments!!! OMG!!!1!1one");
        otherGroup.setUsers(Collections.singletonList("chewie"));
        groupList.add(otherGroup);

        return Arrays.asList(new Object[][] {
            {
                new OnmsGroupList(),
                "<groups></groups>"
            },
            {
                groupList,
                "<groups count=\"2\" totalCount=\"2\">"
                + "<group>"
                + "  <name>Admin</name>"
                + "  <user>admin</user>"
                + "</group>"
                + "<group>"
                + "  <name>Blah</name>"
                + "  <comments>Comments!!! OMG!!!1!1one</comments>"
                + "  <user>chewie</user>"
                + "</group>"
                + "</groups>"
            }
        });
    }


}
