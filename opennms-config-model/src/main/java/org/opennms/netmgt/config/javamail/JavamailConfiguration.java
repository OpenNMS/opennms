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
package org.opennms.netmgt.config.javamail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * The Class JavamailConfiguration.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="javamail-configuration", namespace="http://xmlns.opennms.org/xsd/config/javamail-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("javamail-configuration.xsd")
public class JavamailConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="default-send-config-name", required=true)
    private String m_defaultSendConfigName;

    @XmlAttribute(name="default-read-config-name", required=true)
    private String m_defaultReadConfigName;

    /**
     * This entity defines the test for sending mail. Attributes are used to
     *  derive values of java mail properties, or, they can be specified directly
     *  as key value pairs. Attributes will are easier to read but there isn't 
     *  an attribute for every javamail property possible (some are fairly obscure).
     */
    @XmlElement(name="sendmail-config", required=true)
    private List<SendmailConfig> m_sendmailConfigs = new ArrayList<>();

    /** Configuration container for configuration all settings for reading email. */
    @XmlElement(name="readmail-config", required=true)
    private List<ReadmailConfig> m_readmailConfigs = new ArrayList<>();

    /** Read and Send configuration list. */
    @XmlElement(name="end2end-mail-config", required=true)
    private List<End2endMailConfig> m_end2endMailConfigs = new ArrayList<>();

    public JavamailConfiguration() {
    }

    public String getDefaultSendConfigName() {
        return m_defaultSendConfigName;
    }

    public void setDefaultSendConfigName(final String defaultSendConfigName) {
        m_defaultSendConfigName = ConfigUtils.assertNotEmpty(defaultSendConfigName, "default-send-config-name");
    }

    public String getDefaultReadConfigName() {
        return m_defaultReadConfigName;
    }

    public void setDefaultReadConfigName(final String defaultReadConfigName) {
        m_defaultReadConfigName = ConfigUtils.assertNotEmpty(defaultReadConfigName, "default-read-config-name");
    }

    public List<SendmailConfig> getSendmailConfigs() {
        return m_sendmailConfigs;
    }

    public void setSendmailConfigs(final List<SendmailConfig> configs) {
        if (configs == m_sendmailConfigs) return;
        m_sendmailConfigs.clear();
        if (configs != null) m_sendmailConfigs.addAll(configs);
    }

    public List<ReadmailConfig> getReadmailConfigs() {
        return m_readmailConfigs;
    }

    public void setReadmailConfigs(final List<ReadmailConfig> configs) {
        if (configs == m_readmailConfigs) return;
        m_readmailConfigs.clear();
        if (configs != null) m_readmailConfigs.addAll(configs);
    }

    public List<End2endMailConfig> getEnd2endMailConfigs() {
        return m_end2endMailConfigs;
    }

    public void setEnd2endMailConfigs(final List<End2endMailConfig> configs) {
        if (configs == m_end2endMailConfigs) return;
        m_end2endMailConfigs.clear();
        if (configs != null) m_end2endMailConfigs.addAll(configs);
    }

    @Override()
    public int hashCode() {
        return Objects.hash(m_defaultSendConfigName, m_defaultReadConfigName, m_sendmailConfigs, m_readmailConfigs, m_end2endMailConfigs);
    }

    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof JavamailConfiguration) {
            final JavamailConfiguration that = (JavamailConfiguration)obj;
            return Objects.equals(this.m_defaultSendConfigName, that.m_defaultSendConfigName)
                    && Objects.equals(this.m_defaultReadConfigName, that.m_defaultReadConfigName)
                    && Objects.equals(this.m_sendmailConfigs, that.m_sendmailConfigs)
                    && Objects.equals(this.m_readmailConfigs, that.m_readmailConfigs)
                    && Objects.equals(this.m_end2endMailConfigs, that.m_end2endMailConfigs);
        }
        return false;
    }

}
