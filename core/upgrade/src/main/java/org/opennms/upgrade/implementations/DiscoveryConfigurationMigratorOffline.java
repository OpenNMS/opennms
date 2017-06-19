/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
