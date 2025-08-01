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
package org.opennms.protocols.xml.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class XmlGroups.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="xml-groups")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlGroups implements Serializable, Cloneable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8626665420339279584L;

    /** The XML groups list. */
    @XmlElement(name="xml-group", required=true)
    private List<XmlGroup> m_xmlGroups = new ArrayList<>();

    /**
     * Instantiates a new XML source.
     */
    public XmlGroups() {
    }

    public XmlGroups(XmlGroups copy) {
        copy.m_xmlGroups.stream().forEach(g -> m_xmlGroups.add(g.clone()));
    }

    /**
     * Gets the XML groups.
     *
     * @return the XML groups
     */
    public List<XmlGroup> getXmlGroups() {
        return m_xmlGroups;
    }

    /**
     * Sets the XML groups.
     *
     * @param xmlGroups the new XML groups
     */
    public void setXmlGroups(List<XmlGroup> xmlGroups) {
        m_xmlGroups = xmlGroups;
    }

    /**
     * Adds the XML group.
     *
     * @param group the group
     */
    public void addXmlGroup(XmlGroup group) {
        m_xmlGroups.add(group);
    }

    /**
     * Removes the XML group.
     *
     * @param group the group
     */
    public void removeXmlGroup(XmlGroup group) {
        m_xmlGroups.remove(group);
    }

    /**
     * Removes the group by name.
     *
     * @param name the name
     */
    public void removeGroupByName(String name) {
        for (Iterator<XmlGroup> itr = m_xmlGroups.iterator(); itr.hasNext();) {
            XmlGroup query = itr.next();
            if(query.getName().equals(name)) {
                m_xmlGroups.remove(query);
                return;
            }
        }
    }

    @Override
    public XmlGroups clone() {
        return new XmlGroups(this);
    }
}
