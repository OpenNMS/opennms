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
package org.opennms.netmgt.config.snmpmetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.NONE)
public class Config implements Container {
    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "sysObjectId")
    private String sysObjectId;

    @XmlAttribute(name = "tree")
    private String tree;

    @XmlElement(name = "entry")
    private List<Entry> entries = new ArrayList<>();

    public String getName() {
        return name;
    }

    public boolean isExact() { return false; }

    public void setName(String name) {
        this.name = name;
    }

    public String getSysObjectId() {
        return sysObjectId;
    }

    public void setSysObjectId(String sysObjectId) {
        this.sysObjectId = sysObjectId;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public String getTree() {
        return tree;
    }

    public void setTree(String tree) {
        this.tree = tree;
    }

    @Override
    public String toString() {
        return "Config{" +
                "name='" + name + '\'' +
                ", sysObjectId='" + sysObjectId + '\'' +
                ", tree='" + tree + '\'' +
                ", entries=" + entries +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Config config = (Config) o;
        return Objects.equals(name, config.name) && Objects.equals(sysObjectId, config.sysObjectId) && Objects.equals(tree, config.tree) && Objects.equals(entries, config.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, sysObjectId, tree, entries);
    }
}
