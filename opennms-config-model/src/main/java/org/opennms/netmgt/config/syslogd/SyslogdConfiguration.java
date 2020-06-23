/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
