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
package org.opennms.netmgt.config.jmx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Jaxb mbean-server element for the jmx config.
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */
@XmlRootElement(name = "mbean-server")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class MBeanServer {
    private String m_ipAddress;
    private int m_port;
    private List<Parameter> m_parameters = new LinkedList<>();

    @XmlAttribute(name = "ipAddress", required = true)
    public String getIpAddress() {
        return m_ipAddress;
    }

    @XmlAttribute(name = "port", required = true)
    public int getPort() {
        return m_port;
    }

    @XmlElement(name = "parameter", required = false)
    public List<Parameter> getParameters() {
        return m_parameters;
    }

    public void setIpAddress(String ipAddress) {
        this.m_ipAddress = ipAddress;
    }

    public void setPort(int port) {
        this.m_port = port;
    }

    public void setParameters(List<Parameter> parameters) {
        this.m_parameters = parameters;
    }

    public Map<String, String> getParameterMap() {
        Map<String, String> parameterMap = new HashMap<>();
        for (Parameter parameter : getParameters()) {
            parameterMap.put(parameter.getKey(), parameter.getValue());
        }
        return parameterMap;
    }
}