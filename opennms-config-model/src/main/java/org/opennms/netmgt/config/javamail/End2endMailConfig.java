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
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * The Class End2endMailConfig.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="end2end-mail-config", namespace="http://xmlns.opennms.org/xsd/config/javamail-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("javamail-configuration.xsd")
public class End2endMailConfig implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="name", required=true)
    private String m_name;

    @XmlAttribute(name="sendmail-config-name", required=true)
    private String m_sendmailConfigName;

    @XmlAttribute(name="readmail-config-name", required=true)
    private String m_readmailConfigName;

    public End2endMailConfig() {
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getSendmailConfigName() {
        return m_sendmailConfigName;
    }

    public void setSendmailConfigName(final String sendmailConfigName) {
        m_sendmailConfigName = ConfigUtils.assertNotEmpty(sendmailConfigName, "sendmail-config-name");
    }

    public String getReadmailConfigName() {
        return m_readmailConfigName;
    }

    public void setReadmailConfigName(final String readmailConfigName) {
        m_readmailConfigName = ConfigUtils.assertNotEmpty(readmailConfigName, "readmail-config-name");
    }

    @Override()
    public int hashCode() {
        return Objects.hash(m_name, m_sendmailConfigName, m_readmailConfigName);
    }

    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof End2endMailConfig) {
            final End2endMailConfig that = (End2endMailConfig)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_sendmailConfigName, that.m_sendmailConfigName)
                    && Objects.equals(this.m_readmailConfigName, that.m_readmailConfigName);
        }
        return false;
    }

}
