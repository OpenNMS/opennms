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
