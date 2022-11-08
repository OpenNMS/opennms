/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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