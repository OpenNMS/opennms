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
package org.opennms.netmgt.config.syslogd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "syslogd-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("syslog.xsd")
public class SyslogdConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Top-level element for the syslogd-configuration.xml
     *  configuration file.
     *  
     */
    @XmlElement(name = "configuration", required = true)
    private Configuration m_configuration;

    @XmlElementWrapper(name = "ueiList", required=false)
    @XmlElement(name="ueiMatch")
    private List<UeiMatch> m_ueiMatches = new ArrayList<>();

    @XmlElementWrapper(name = "hideMessage", required=false)
    @XmlElement(name="hideMatch")
    private List<HideMatch> m_hideMatches = new ArrayList<>();

    @XmlElement(name = "import-file")
    private List<String> m_importFiles = new ArrayList<>();

    public Configuration getConfiguration() {
        return m_configuration;
    }

    public void setConfiguration(final Configuration configuration) {
        m_configuration = ConfigUtils.assertNotNull(configuration, "configuration");
    }

    public List<UeiMatch> getUeiMatches() {
        return m_ueiMatches;
    }

    public void setUeiMatches(final List<UeiMatch> ueiMatches) {
        if (ueiMatches == m_ueiMatches) return;
        m_ueiMatches.clear();
        if (ueiMatches != null) m_ueiMatches.addAll(ueiMatches);
    }

    public void addUeiMatch(final UeiMatch ueiMatch) {
        m_ueiMatches.add(ueiMatch);
    }

    public boolean removeUeiMatch(final UeiMatch ueiMatch) {
        return m_ueiMatches.remove(ueiMatch);
    }

    public List<HideMatch> getHideMatches() {
        return m_hideMatches;
    }

    public void setHideMatches(final List<HideMatch> hideMatches) {
        if (hideMatches == m_hideMatches) return;
        m_hideMatches.clear();
        if (hideMatches != null) m_hideMatches.addAll(hideMatches);
    }

    public void addHideMatch(final HideMatch hideMatch) {
        m_hideMatches.add(hideMatch);
    }

    public boolean removeHideMatch(final HideMatch hideMatch) {
        return m_hideMatches.remove(hideMatch);
    }

    public List<String> getImportFiles() {
        return m_importFiles;
    }

    public void setImportFiles(final List<String> importFiles) {
        if (importFiles == null) return;
        m_importFiles.clear();
        if (importFiles != null) m_importFiles.addAll(importFiles);
    }

    public void addImportFile(final String importFile) {
        m_importFiles.add(importFile);
    }

    public boolean removeImportFile(final String importFile) {
        return m_importFiles.remove(importFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_configuration, 
                            m_ueiMatches, 
                            m_hideMatches, 
                            m_importFiles);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof SyslogdConfiguration) {
            final SyslogdConfiguration that = (SyslogdConfiguration)obj;
            return Objects.equals(this.m_configuration, that.m_configuration)
                    && Objects.equals(this.m_ueiMatches, that.m_ueiMatches)
                    && Objects.equals(this.m_hideMatches, that.m_hideMatches)
                    && Objects.equals(this.m_importFiles, that.m_importFiles);
        }
        return false;
    }

}
