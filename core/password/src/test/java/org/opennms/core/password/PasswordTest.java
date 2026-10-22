/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.password;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.System.Logger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilderFactory;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Element;

/** The users.xml path of the password CLI (org.opennms.usermgmt.store=xml). */
public class PasswordTest {
    private static final Logger LOG = System.getLogger(PasswordTest.class.getName());

    // admin has an unsalted (MD5) password, as old installations do
    private static final String USERS_XML = "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\"><users>"
            + "<user><user-id>admin</user-id><password>21232F297A57A5A743894A0E4A801FC3</password></user>"
            + "<user><user-id>admin2</user-id><password salt=\"true\">old</password></user>"
            + "</users></userinfo>";

    @Rule
    public TemporaryFolder m_home = new TemporaryFolder();

    private File m_usersXml;

    @Before
    public void setUp() throws Exception {
        m_usersXml = new File(m_home.newFolder("etc"), "users.xml");
        Files.write(m_usersXml.toPath(), USERS_XML.getBytes(StandardCharsets.UTF_8));
    }

    private Element password(final String userId) throws Exception {
        final var users = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(m_usersXml).getElementsByTagName("user");
        for (int i = 0; i < users.getLength(); i++) {
            final Element user = (Element) users.item(i);
            if (userId.equals(user.getElementsByTagName("user-id").item(0).getTextContent())) {
                return (Element) user.getElementsByTagName("password").item(0);
            }
        }
        throw new AssertionError("no user " + userId);
    }

    @Test
    public void testChangesOnlyTheNamedUser() throws Exception {
        // "admin2" contains "admin": the old contains() match changed admin's password too
        assertEquals(0, Password.setPasswordFile(LOG, m_home.getRoot().getAbsolutePath(), "admin2", "n3w"));
        assertTrue(new StrongPasswordEncryptor().checkPassword("n3w", password("admin2").getTextContent()));
        assertEquals("21232F297A57A5A743894A0E4A801FC3", password("admin").getTextContent());
    }

    @Test
    public void testMarksTheNewPasswordAsSalted() throws Exception {
        // the new hash is salted: without salt="true", UserManager compares it as an unsalted MD5 hash
        assertEquals(0, Password.setPasswordFile(LOG, m_home.getRoot().getAbsolutePath(), "admin", "n3w"));
        assertEquals("true", password("admin").getAttribute("salt"));
        assertTrue(new StrongPasswordEncryptor().checkPassword("n3w", password("admin").getTextContent()));
    }

    @Test
    public void testUnknownUserFails() throws Exception {
        assertEquals(1, Password.setPasswordFile(LOG, m_home.getRoot().getAbsolutePath(), "dmin", "n3w"));
    }
}
