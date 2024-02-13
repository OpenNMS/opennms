/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Upgrade opennms-datasources.xml to include new connection poll configuration.
 *
 * <p>See:</p>
 * <ul>
 * <li>NMS-16107</li>
 * </ul>
 *
 * @author Benjamin Reed &lt;ranger@opennms.com&gt;
 */
public class OpennmsDatasourcesConnectionPoolOffline extends AbstractOnmsUpgrade {

    /** The source file. */
    private File sourceFile;

    /** The backup file. */
    private File backupFile;

    private String connectionPoolFragment = "<connection-pool idleTimeout=\"600\"\n"
            + "  minPool=\"0\"\n"
            + "  maxPool=\"10\"\n"
            + "  maxSize=\"50\"\n"
            + "/>";

    private String monitorDatasourceFragment = "<jdbc-data-source name=\"opennms-monitor\"\n"
            + "  database-name=\"postgres\"\n"
            + "  class-name=\"org.postgresql.Driver\"\n"
            + "  url=\"jdbc:postgresql://localhost:5432/postgres\"\n"
            + "  user-name=\"replaceme\"\n"
            + "  password=\"replaceme\"\n"
            + "/>";

    /**
     * Instantiates a new opennms-datasources.xml converter.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public OpennmsDatasourcesConnectionPoolOffline() throws OnmsUpgradeException {
        super();
        sourceFile = Paths.get(ConfigFileConstants.getHome(), "etc", "opennms-datasources.xml").toFile();
        backupFile = new File(sourceFile.getAbsolutePath() + ZIP_EXT);
    }

    /* this should happen early, so other upgrade stuff that might want to use the new datasources can get them */
    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Fixes the missing resource types on datacollection-config.xml. See NMS-7816.";
    }

    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
        log("Backing up %s\n", sourceFile);
        zipFile(sourceFile);
    }

    @Override
    public void execute() throws OnmsUpgradeException {
        log("Patching %s\n", sourceFile);

        try (final var out = new StringWriter()) {
            final var docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            docFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            final var docBuilder = docFactory.newDocumentBuilder();
            final var doc = docBuilder.parse(sourceFile);

            final Map<String,Node> nodes = new LinkedHashMap<>();
            final NodeList sources = doc.getElementsByTagName("jdbc-data-source");

            for (int i = 0; i < sources.getLength(); i++) {
                final var node = sources.item(i);
                final var attributes = node.getAttributes();
                final String datasourceName = attributes.getNamedItem("name").getNodeValue();
                nodes.put(datasourceName, node);
            }

            final Node newConnectionPool = docBuilder.parse(new InputSource(new StringReader(connectionPoolFragment))).getDocumentElement();

            final Node opennmsAdminConfig = nodes.get("opennms-admin");
            if (opennmsAdminConfig == null) {
                throw new OnmsUpgradeException("expected datasource 'opennms-admin' is missing");
            }
            if (hasConnectionPool(opennmsAdminConfig)) {
                log("The 'opennms-admin' datasource already has a <connection-pool /> configuration... skipping.\n");
            } else {
                log("Adding missing <connection-pool /> config to the 'opennms-admin' datasource.\n");
                opennmsAdminConfig.appendChild(doc.importNode(newConnectionPool, true));
            }

            Node opennmsMonitorConfig = nodes.get("opennms-monitor");
            if (opennmsMonitorConfig == null) {
                log("Adding missing 'opennms-monitor' datasource.\n");

                // clone the admin config and append it
                final var fragmentImportSource = new InputSource(new StringReader(monitorDatasourceFragment));
                opennmsMonitorConfig = doc.importNode(docBuilder.parse(fragmentImportSource).getDocumentElement(), true);
                doc.getDocumentElement().appendChild(opennmsMonitorConfig);

                final var adminAttributes = opennmsAdminConfig.getAttributes();
                final var monitorConfigAttributes = opennmsMonitorConfig.getAttributes();

                // fix the username to match opennms-admin
                final var username = adminAttributes.getNamedItem("user-name");
                monitorConfigAttributes.getNamedItem("user-name").setNodeValue(username.getNodeValue());

                // fix the password to match opennms-admin
                final var password = adminAttributes.getNamedItem("password");
                monitorConfigAttributes.getNamedItem("password").setNodeValue(password.getNodeValue());
            }
            if (hasConnectionPool(opennmsMonitorConfig)) {
                log("The 'opennms-monitor' datasource already has a <connection-pool /> configuration... skipping.\n");
            } else {
                opennmsMonitorConfig.appendChild(doc.importNode(newConnectionPool, true));
            }

            final var tFactory = TransformerFactory.newInstance();
            final var tf = tFactory.newTransformer();
            tf.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.transform(new DOMSource(doc), new StreamResult(out));
            FileUtils.write(sourceFile, out.toString(), Charset.defaultCharset(), false);
        } catch (final IOException | SAXException | ParserConfigurationException | TransformerException e) {
            throw new OnmsUpgradeException("Failed to upgrade opennms-datasources.xml", e);
        }

    }

    private boolean hasConnectionPool(final Node node) {
        final NodeList children = node.getChildNodes();
        for (int i=0; i < children.getLength(); i++) {
            final var child = children.item(i);
            final var attributes = child.getAttributes();
            if (attributes.getNamedItem("connection-pool") != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void postExecute() {
        if (backupFile.exists()) {
            log("Upgrade complete, removing temporary backup %s\n", backupFile);
            FileUtils.deleteQuietly(backupFile);
        }
    }

    @Override
    public void rollback() throws OnmsUpgradeException {
        if (!backupFile.exists()) {
            throw new OnmsUpgradeException(String.format("Backup %s not found. Can't rollback.", backupFile));
        }

        log("Unziping backup %s to %s\n", backupFile, sourceFile.getParentFile());
        unzipFile(backupFile, sourceFile.getParentFile());

        log("Rollback succesful. The backup file %s will be kept.\n", backupFile);
    }
}
