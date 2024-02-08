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

@XmlRootElement(name = "entry")
@XmlAccessorType(XmlAccessType.NONE)
public class Entry implements Container {
    @XmlAttribute(name = "tree")
    private String tree;

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "index")
    private String index = "false";

    @XmlAttribute(name = "exact")
    private String exact = "false";

    @XmlElement(name = "entry")
    private List<Entry> entries = new ArrayList<>();

    public String getTree() {
        return tree;
    }

    public void setTree(String tree) {
        this.tree = tree;
    }

    public boolean isIndex() {
        return "true".equals(index);
    }

    public void setIndex(boolean index) {
        this.index = index ? "true" : "false";
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public boolean isExact() { return "true".equals(exact); }

    public void setExact(boolean exact) { this.exact = exact ? "true" : "false"; }

    public void setExact(String exact) { this.exact = exact; }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "tree='" + tree + '\'' +
                ", name='" + name + '\'' +
                ", index='" + index + '\'' +
                ", exact='" + exact + '\'' +
                ", entries=" + entries +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entry entry = (Entry) o;
        return Objects.equals(tree, entry.tree) && Objects.equals(name, entry.name) && Objects.equals(index, entry.index) && Objects.equals(exact, entry.exact) && Objects.equals(entries, entry.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tree, name, index, exact, entries);
    }
}