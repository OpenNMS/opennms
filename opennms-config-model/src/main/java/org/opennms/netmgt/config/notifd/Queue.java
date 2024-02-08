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
package org.opennms.netmgt.config.notifd;


import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "queue")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("notifd-configuration.xsd")
public class Queue implements java.io.Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElement(name = "queue-id", required = true)
    private String m_queueId;

    @XmlElement(name = "interval", required = true)
    private String m_interval;

    @XmlElement(name = "handler-class", required = true)
    private HandlerClass m_handlerClass;

    public Queue() {
    }

    public String getQueueId() {
        return m_queueId;
    }

    public void setQueueId(final String queueId) {
        m_queueId = ConfigUtils.assertNotEmpty(queueId, "queue-id");
    }

    public String getInterval() {
        return m_interval;
    }

    public void setInterval(final String interval) {
        m_interval = ConfigUtils.assertNotEmpty(interval, "interval");
    }

    public HandlerClass getHandlerClass() {
        return m_handlerClass;
    }

    public void setHandlerClass(final HandlerClass handlerClass) {
        m_handlerClass = ConfigUtils.assertNotNull(handlerClass, "handler-class");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_queueId, 
                            m_interval, 
                            m_handlerClass);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Queue) {
            final Queue that = (Queue)obj;
            return Objects.equals(this.m_queueId, that.m_queueId)
                    && Objects.equals(this.m_interval, that.m_interval)
                    && Objects.equals(this.m_handlerClass, that.m_handlerClass);
        }
        return false;
    }

}
