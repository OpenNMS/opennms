/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.charts.ChartConfiguration;

/**
 * <p>Abstract ChartConfigManager class.</p>
 *
 * @author david
 * @version $Id: $
 */
public abstract class ChartConfigManager {
    
    static ChartConfiguration m_configuration = null;
    
    /**
     * <p>parseXml</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws java.io.IOException if any.
     */
    public synchronized static void parseXml(InputStream stream) throws IOException {
        try (final Reader reader = new InputStreamReader(stream)) {
            m_configuration = JaxbUtils.unmarshal(ChartConfiguration.class, reader);
        }
    }

    /**
     * <p>saveXml</p>
     *
     * @param xml a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    protected abstract void saveXml(String xml) throws IOException;

    /**
     * <p>saveCurrent</p>
     *
     * @throws java.io.IOException if any.
     */
    public synchronized void saveCurrent() throws IOException {
        // Marshal to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the XML from the marshal is hosed.
        String xml =  JaxbUtils.marshal(m_configuration);
        saveXml(xml);
        update();
    }

    /**
     * <p>getConfiguration</p>
     *
     * @throws java.io.IOException if any.
     * @return a {@link org.opennms.netmgt.config.charts.ChartConfiguration} object.
     */
    public ChartConfiguration getConfiguration() throws IOException {
        return m_configuration;
    }

    /**
     * <p>setConfiguration</p>
     *
     * @param configuration a {@link org.opennms.netmgt.config.charts.ChartConfiguration} object.
     */
    public void setConfiguration(ChartConfiguration configuration) {
        m_configuration = configuration;
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     */
    protected abstract void update() throws IOException;

}
