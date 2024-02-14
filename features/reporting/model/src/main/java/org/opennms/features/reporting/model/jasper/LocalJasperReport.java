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
package org.opennms.features.reporting.model.jasper;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "report")
public class LocalJasperReport {

    private String m_id;

    private String m_template;

    private String m_engine;

    @XmlAttribute(name = "id")
    public String getId() {
        return m_id;
    }

    @XmlAttribute(name = "template")
    public String getTemplate() {
        return m_template;
    }

    @XmlAttribute(name = "engine")
    public String getEngine() {
        return m_engine;
    }

    public void setId(String id) {
        m_id = id;
    }
    
    public void setTemplate(String template) {
        m_template = template;
    }
    
    public void setEngine(String engine) {
        m_engine = engine;
    }
}
