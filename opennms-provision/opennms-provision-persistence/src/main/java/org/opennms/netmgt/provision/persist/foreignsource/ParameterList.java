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
package org.opennms.netmgt.provision.persist.foreignsource;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement
/**
 * <p>ParameterList class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="pluginConfigParameter")
public class ParameterList {
    public List<PluginParameter> parameter;
    /**
     * <p>Constructor for ParameterList.</p>
     */
    public ParameterList() {
        parameter = new LinkedList<>();
    }

    /**
     * <p>Constructor for ParameterList.</p>
     *
     * @param m a {@link java.util.Map} object.
     */
    public ParameterList(Map<String,String> m) {
        parameter = new LinkedList<>();
        for (Map.Entry<String,String> e : m.entrySet()) {
            parameter.add(new PluginParameter(e));
        }
    }

    /**
     * <p>Setter for the field <code>parameter</code>.</p>
     *
     * @param list a {@link java.util.List} object.
     */
    public void setParameter(List<PluginParameter> list) {
        parameter = list;
    }
    
    /**
     * <p>Getter for the field <code>parameter</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<PluginParameter> getParameter() {
        return parameter;
    }
}
