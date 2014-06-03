package org.opennms.netmgt.model;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class OnmsUsersTest extends XmlTestNoCastor<OnmsUserList> {

    public OnmsUsersTest(final OnmsUserList sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final OnmsUserList userList = new OnmsUserList();

        final OnmsUser user = new OnmsUser();
        user.setFullName("Foo Barson");
        user.setUsername("foo");
        user.setEmail("foo@example.com");
        userList.add(user);

        final OnmsUser otherUser = new OnmsUser();
        otherUser.setFullName("Blah Blahtonen");
        otherUser.setUsername("blah");
        otherUser.setEmail("blah@example.com");
        userList.add(otherUser);

        return Arrays.asList(new Object[][] {
            {
                new OnmsUserList(),
                "<users></users>"
            },
            {
                userList,
                "<users count=\"2\" totalCount=\"2\">"
                + "<user>"
                + "  <user-id>foo</user-id>"
                + "  <full-name>Foo Barson</full-name>"
                + "  <email>foo@example.com</email>"
                + "</user>"
                + "<user>"
                + "  <user-id>blah</user-id>"
                + "  <full-name>Blah Blahtonen</full-name>"
                + "  <email>blah@example.com</email>"
                + "</user>"
                + "</users>"
            }
        });
    }


}
