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
package org.opennms.upgrade.implementations;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DiscoveryConfigurationMigratorOffline extends AbstractOnmsUpgrade {
    private File m_configFile;
    private File m_backupFile;

    public DiscoveryConfigurationMigratorOffline() throws OnmsUpgradeException {
        super();
        m_configFile = Paths.get(ConfigFileConstants.getHome(), "etc", "discovery-configuration.xml").toFile();
    }

    @Override
    public int getOrder() {
        return 11;
    }

    @Override
    public String getDescription() {
        return "Removes the 'threads' attribute from discovery-configuration.xml. See HZN-599.";
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
        log("Backing up discovery-configuration.xml\n");
        m_backupFile = zipFile(m_configFile);
    }

    @Override
    public void execute() throws OnmsUpgradeException {
        Writer out = null;
        try {
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final Document doc = docBuilder.parse(m_configFile);
            final NodeList found = doc.getElementsByTagName("discovery-configuration");
            if (found.getLength() == 1 && found.item(0) instanceof Element) {
                final Element el = (Element)found.item(0);
                el.removeAttribute("threads");
                final Transformer tf = TransformerFactory.newInstance().newTransformer();
                tf.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
                tf.setOutputProperty(OutputKeys.INDENT, "yes");
                out = new StringWriter();
                tf.transform(new DOMSource(doc), new StreamResult(out));
                FileUtils.write(m_configFile, out.toString());
            } else {
                throw new OnmsUpgradeException("Unsure how to handle XML node(s): " + found);
            }
        } catch (final IOException | SAXException | ParserConfigurationException | TransformerException e) {
            throw new OnmsUpgradeException("Failed to upgrade discovery-configuration.xml", e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    @Override
    public void postExecute() throws OnmsUpgradeException {
        if (m_backupFile.exists()) {
            log("Removing backup %s\n", m_backupFile);
            FileUtils.deleteQuietly(m_backupFile);
        }
    }

    @Override
    public void rollback() throws OnmsUpgradeException {
        if (!m_backupFile.exists()) {
            throw new OnmsUpgradeException(String.format("Backup %s not found. Can't rollback.", m_backupFile));
        }

        log("Unzipping backup %s to %s\n", m_backupFile, m_configFile.getParentFile());
        unzipFile(m_backupFile, m_configFile.getParentFile());

        log("Rollback succesful. The backup file %s will be kept.\n", m_backupFile);
    }

    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

}
