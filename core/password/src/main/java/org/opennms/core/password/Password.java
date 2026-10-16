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

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.config.api.UserMgmtStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Password {
    public static void main(String[] args) {
        final String OPENNMS_HOME = System.getProperty("opennms.home");
        final Logger log = System.getLogger(Password.class.getName());

        if (args.length <2){
            log.log(Level.WARNING, "usage: password.jar <username> <password>");
            System.exit(1);
        }

        final String userId = args[0];
        final String newPassword = args[1];

        if (OPENNMS_HOME == null || OPENNMS_HOME.isEmpty() || userId.isEmpty() || newPassword.isEmpty()){
            log.log(Level.ERROR, "Unable to determine OpenNMS home, or no username or password was provided.");
            System.exit(1);
        }

        // User management via database if applicable
        if (UserMgmtStore.isDatabase(new File(OPENNMS_HOME))) {
            final int result = setPasswordDb(log, userId, newPassword);
            System.exit(result);
        }

        // User management via XML file, if database is not used
        final int result = setPasswordFile(log, OPENNMS_HOME, userId, newPassword);
        System.exit(result);
    }

    private static int setPasswordDb(Logger log, String userId, String newPassword) {
        try {
            DatabasePasswords.setPassword(DataSourceFactory.getInstance(), userId, newPassword);
            log.log(Level.INFO, "The password of user " + userId + " was changed in the database.");
            return 0;
        } catch (final IllegalArgumentException e) {
            log.log(Level.ERROR, e.getMessage());
        } catch (final Exception e) {
            log.log(Level.ERROR, "Cannot change the password in the database: " + e.getMessage(), e);
        }

        return 1;
    }

    static int setPasswordFile(Logger log, String opennmsHome, String userId, String newPassword) {
        final Path usersXml = Paths.get(opennmsHome, "etc", "users.xml");
        File file = usersXml.toFile();

        if (!file.exists()) {
            log.log(Level.ERROR, "users.xml does not exist!");
            return 1;
        }

        boolean foundUser = false;
        StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        String encryptedPassword = "";

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("user");

            for (int itr = 0; itr < nodeList.getLength(); itr++) {
                Node node = nodeList.item(itr);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) node;
                    // exact match: JAXB trims the user-id when OpenNMS reads users.xml
                    if (userId.equals(eElement.getElementsByTagName("user-id").item(0).getTextContent().trim())) {
                        foundUser = true;
                        encryptedPassword = passwordEncryptor.encryptPassword(newPassword);
                        final Element password = (Element) eElement.getElementsByTagName("password").item(0);
                        password.setTextContent(encryptedPassword);
                        // the hash is salted; without this, UserManager checks it as an unsalted MD5 hash
                        password.setAttribute("salt", "true");
                    }
                }
            }

            if (foundUser) {
                DOMSource domSource = new DOMSource(doc);
                StreamResult sr = new StreamResult(file);

                try {
                    final var factory = TransformerFactory.newInstance();

                    Transformer tf = factory.newTransformer();
                    tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    tf.setOutputProperty(OutputKeys.METHOD, "xml");
                    tf.transform(domSource, sr);
                    return 0;
                } catch (TransformerFactoryConfigurationError | TransformerException e) {
                    e.printStackTrace();
                }
            } else {
                log.log(Level.ERROR, "User ID couldn't found.");
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return 1;
    }
}
