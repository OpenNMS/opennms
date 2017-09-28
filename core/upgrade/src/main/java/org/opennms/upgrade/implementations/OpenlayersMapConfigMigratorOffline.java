/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
import org.apache.commons.io.IOUtils;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * Make sure that the map configuration in opennms.properties is correct.
 *
 * <ol>
 *   <li>if the user has the old mapquest maps configured, change it to tiles.opennms.org</li>
 *   <li>if the user does not have a map URL configured, set it to tiles.opennms.org</li>
 *   <li>if the user does not have the "attribution" configured, add it</li>
 * </ol>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 */
public class OpenlayersMapConfigMigratorOffline extends AbstractOnmsUpgrade {

    /**
     * Instantiates a new Jetty configuration migrator offline.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public OpenlayersMapConfigMigratorOffline() throws OnmsUpgradeException {
        super();
    }

    @Override
    public int getOrder() {
        return 5;
    }

    @Override
    public String getDescription() {
        return "Make sure tiles.opennms.org is used for non-custom map configuration";
    }

    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {}

    @Override
    public void postExecute() throws OnmsUpgradeException {}

    @Override
    public void rollback() throws OnmsUpgradeException {}

    @Override
    public void execute() throws OnmsUpgradeException {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout(config);
        FileInputStream fis = null;
        InputStreamReader isr = null;
        FileWriter fw = null;
        try {
            final String opennmsPropertiesFilename = getHomeDirectory() + File.separator + "etc" + File.separator + "opennms.properties";
            fis = new FileInputStream(new File(opennmsPropertiesFilename));
            isr = new InputStreamReader(fis);
            layout.load(isr);

            final PropertiesConfiguration configuration = layout.getConfiguration();
            final Object mapUrl = configuration.getProperty("gwt.openlayers.url");
            if (mapUrl == null || "".equals(mapUrl.toString().trim()) || mapUrl.toString().equals("http://otile1.mqcdn.com/tiles/1.0.0/osm/${z}/${x}/${y}.png")) {
                configuration.setProperty("gwt.openlayers.url", "https://tiles.opennms.org/${z}/${x}/${y}.png");
            }

            final Object attribution = configuration.getProperty("gwt.openlayers.options.attribution");
            if (attribution == null || "".equals(attribution.toString().trim())) {
                configuration.setProperty("gwt.openlayers.options.attribution", "Map data &copy; <a tabindex=\"-1\" target=\"_blank\" href=\"http://openstreetmap.org/copyright\">OpenStreetMap</a> contributors under <a tabindex=\"-1\" target=\"_blank\" href=\"http://opendatacommons.org/licenses/odbl/\">ODbL</a>, <a tabindex=\"-1\" target=\"_blank\" href=\"http://creativecommons.org/licenses/by-sa/2.0/\">CC BY-SA 2.0</a>");
            }

            fw = new FileWriter(opennmsPropertiesFilename);
            layout.save(fw);
        } catch (final IOException e) {
            throw new OnmsUpgradeException("Unable to read or write opennms.properties!", e);
        } catch (final ConfigurationException e) {
            throw new OnmsUpgradeException("An error occurred while parsing opennms.properties!", e);
        } finally {
            IOUtils.closeQuietly(fw);
            IOUtils.closeQuietly(isr);
            IOUtils.closeQuietly(fis);
        }
    }

}
