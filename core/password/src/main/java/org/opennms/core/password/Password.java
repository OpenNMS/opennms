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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Password {

    private static final Pattern ENV_VAR = Pattern.compile("\\$\\{env:([^|{}]+)\\|([^}]*)\\}");

    public static void main(String[] args) {
        final Logger log = System.getLogger(Password.class.getName());
        final String opennmsHome = System.getProperty("opennms.home");

        if (args.length < 2) {
            log.log(Level.WARNING, "usage: password.jar <username> <password>");
            System.exit(1);
        }

        if (opennmsHome == null || opennmsHome.isEmpty()) {
            log.log(Level.ERROR, "opennms.home system property is not set.");
            System.exit(1);
        }

        final String userId = args[0];
        final String newPassword = args[1];

        final String encryptedPassword = new StrongPasswordEncryptor().encryptPassword(newPassword);

        if (!updateInDatabase(opennmsHome, userId, encryptedPassword, log)) {
            log.log(Level.ERROR, "Failed to update password for user '" + userId + "' in database.");
            System.exit(1);
        }
    }

    // -------------------------------------------------------------------------
    // Database path
    // -------------------------------------------------------------------------

    static boolean updateInDatabase(final String opennmsHome, final String userId,
                                    final String encryptedPassword, final Logger log) {
        final Path datasourcesXml = Paths.get(opennmsHome, "etc", "opennms-datasources.xml");
        if (!Files.exists(datasourcesXml)) {
            log.log(Level.DEBUG, "opennms-datasources.xml not found, skipping DB update");
            return false;
        }

        final String[] jdbcParams = parseDatasourcesXml(datasourcesXml.toFile(), log);
        if (jdbcParams == null) {
            return false;
        }

        final String url = resolveEnvVars(jdbcParams[0]);
        final String user = resolveEnvVars(jdbcParams[1]);
        final String pass = resolveEnvVars(jdbcParams[2]);

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET password = ?, password_salt = true WHERE user_id = ?")) {
            ps.setString(1, encryptedPassword);
            ps.setString(2, userId);
            final int rows = ps.executeUpdate();
            if (rows == 0) {
                log.log(Level.ERROR, "User '" + userId + "' not found in database.");
                System.exit(1);
            }
            log.log(Level.INFO, "Password updated in database for user: " + userId);
            return true;
        } catch (final SQLException e) {
            log.log(Level.ERROR, "DB update failed: " + e.getMessage());
            return false;
        }
    }

    /** Returns [url, user-name, password] for the 'opennms' datasource, or null on failure. */
    private static String[] parseDatasourcesXml(final File file, final Logger log) {
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            final NodeList sources = doc.getElementsByTagName("jdbc-data-source");
            for (int i = 0; i < sources.getLength(); i++) {
                final Node node = sources.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    final Element e = (Element) node;
                    if ("opennms".equals(e.getAttribute("name"))) {
                        return new String[]{
                            e.getAttribute("url"),
                            e.getAttribute("user-name"),
                            e.getAttribute("password")
                        };
                    }
                }
            }
            log.log(Level.WARNING, "Could not find 'opennms' datasource in opennms-datasources.xml");
        } catch (final ParserConfigurationException | SAXException | IOException e) {
            log.log(Level.WARNING, "Could not parse opennms-datasources.xml: " + e.getMessage());
        }
        return null;
    }

    /** Resolves {@code ${env:VAR|default}} expressions using the current environment. */
    static String resolveEnvVars(final String template) {
        final Matcher m = ENV_VAR.matcher(template);
        final StringBuffer sb = new StringBuffer();
        while (m.find()) {
            final String envVal = System.getenv(m.group(1));
            m.appendReplacement(sb, Matcher.quoteReplacement(envVal != null ? envVal : m.group(2)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

}
