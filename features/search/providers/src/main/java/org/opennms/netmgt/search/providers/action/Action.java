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
package org.opennms.netmgt.search.providers.action;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.NONE)
public class Action {
    @XmlElement(name="label")
    private String label;

    @XmlElement(name="url")
    private String url;

    @XmlElement(name="icon")
    private String icon;

    @XmlElementWrapper(name="roles")
    @XmlElement(name="role")
    private List<String> privilegedRoles = Lists.newArrayList();

    @XmlElementWrapper(name="aliases")
    @XmlElement(name="alias")
    private List<String> aliases = Lists.newArrayList();

    @XmlElement(name="weight")
    private int weight;

    public Action() {

    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<String> getPrivilegedRoles() {
        return privilegedRoles;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setPrivilegedRoles(List<String> privilegedRoles) {
        this.privilegedRoles = privilegedRoles;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
