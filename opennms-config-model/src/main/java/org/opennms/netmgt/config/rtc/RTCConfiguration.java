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
package org.opennms.netmgt.config.rtc;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="updaters" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int"&gt;
 *             &lt;minInclusive value="1"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="senders" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int"&gt;
 *             &lt;minInclusive value="1"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="rollingWindow" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="maxEventsBeforeResend" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="lowThresholdInterval" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="highThresholdInterval" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="userRefreshInterval" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="errorsBeforeUrlUnsubscribe" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "RTCConfiguration")
@ValidateUsing("rtc-configuration.xsd")
public class RTCConfiguration {

    @XmlAttribute(name = "updaters", required = true)
    protected int m_updaters;
    @XmlAttribute(name = "senders", required = true)
    protected int m_senders;
    @XmlAttribute(name = "rollingWindow", required = true)
    protected String m_rollingWindow;
    @XmlAttribute(name = "maxEventsBeforeResend", required = true)
    protected int m_maxEventsBeforeResend;
    @XmlAttribute(name = "lowThresholdInterval", required = true)
    protected String m_lowThresholdInterval;
    @XmlAttribute(name = "highThresholdInterval", required = true)
    protected String m_highThresholdInterval;
    @XmlAttribute(name = "userRefreshInterval", required = true)
    protected String m_userRefreshInterval;
    @XmlAttribute(name = "errorsBeforeUrlUnsubscribe", required = true)
    protected int m_errorsBeforeUrlUnsubscribe;

    public int getUpdaters() {
        return m_updaters;
    }

    public void setUpdaters(final int value) {
        m_updaters = ConfigUtils.assertMinimumInclusive(value, 1, "updaters");
    }

    public int getSenders() {
        return m_senders;
    }

    public void setSenders(final int value) {
        m_senders = ConfigUtils.assertMinimumInclusive(value, 1, "value");
    }

    public String getRollingWindow() {
        return m_rollingWindow;
    }

    public void setRollingWindow(final String value) {
        m_rollingWindow = ConfigUtils.assertNotEmpty(value, "rollingWindow");
    }

    public int getMaxEventsBeforeResend() {
        return m_maxEventsBeforeResend;
    }

    public void setMaxEventsBeforeResend(final int value) {
        m_maxEventsBeforeResend = value;
    }

    public String getLowThresholdInterval() {
        return m_lowThresholdInterval;
    }

    public void setLowThresholdInterval(final String value) {
        m_lowThresholdInterval = ConfigUtils.assertNotEmpty(value, "lowThresholdInterval");
    }

    public String getHighThresholdInterval() {
        return m_highThresholdInterval;
    }

    public void setHighThresholdInterval(final String value) {
        m_highThresholdInterval = ConfigUtils.assertNotEmpty(value, "highThresholdInterval");
    }

    public String getUserRefreshInterval() {
        return m_userRefreshInterval;
    }

    public void setUserRefreshInterval(final String value) {
        m_userRefreshInterval = ConfigUtils.assertNotEmpty(value, "userRefreshInterval");
    }

    public int getErrorsBeforeUrlUnsubscribe() {
        return m_errorsBeforeUrlUnsubscribe;
    }

    public void setErrorsBeforeUrlUnsubscribe(final int value) {
        m_errorsBeforeUrlUnsubscribe = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_updaters,
                            m_senders,
                            m_rollingWindow,
                            m_maxEventsBeforeResend,
                            m_lowThresholdInterval,
                            m_highThresholdInterval,
                            m_userRefreshInterval,
                            m_errorsBeforeUrlUnsubscribe);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof RTCConfiguration) {
            final RTCConfiguration that = (RTCConfiguration) obj;
            return Objects.equals(this.m_updaters, that.m_updaters)
                    && Objects.equals(this.m_senders, that.m_senders)
                    && Objects.equals(this.m_rollingWindow, that.m_rollingWindow)
                    && Objects.equals(this.m_maxEventsBeforeResend, that.m_maxEventsBeforeResend)
                    && Objects.equals(this.m_lowThresholdInterval, that.m_lowThresholdInterval)
                    && Objects.equals(this.m_highThresholdInterval, that.m_highThresholdInterval)
                    && Objects.equals(this.m_userRefreshInterval, that.m_userRefreshInterval)
                    && Objects.equals(this.m_errorsBeforeUrlUnsubscribe, that.m_errorsBeforeUrlUnsubscribe);
        }
        return false;
    }

}
