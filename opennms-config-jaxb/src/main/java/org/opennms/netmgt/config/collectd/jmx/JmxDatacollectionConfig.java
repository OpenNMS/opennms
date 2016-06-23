/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.collectd.jmx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement(name = "jmx-datacollection-config")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class JmxDatacollectionConfig implements java.io.Serializable {

    @XmlAttribute(name = "rrdRepository", required = true)
    private String _rrdRepository;

    @XmlElement(name = "jmx-collection", required = true)
    private java.util.List<JmxCollection> _jmxCollectionList = new java.util.ArrayList<>();

    public void addJmxCollection(final JmxCollection jmxCollection) {
        this._jmxCollectionList.add(jmxCollection);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (obj instanceof JmxDatacollectionConfig) {
            JmxDatacollectionConfig temp = (JmxDatacollectionConfig) obj;
            boolean equals = Objects.equals(_rrdRepository, temp._rrdRepository)
                    && Objects.equals(_jmxCollectionList, temp._jmxCollectionList);
            return equals;
        }
        return false;
    }

    /**
     * Method getJmxCollection.
     *
     * @param index
     * @return the value of the
     * org.opennms.netmgt.config.collectd.jmx.JmxCollection at the
     * given index
     * @throws IndexOutOfBoundsException if the index
     *                                   given is outside the bounds of the collection
     */
    public JmxCollection getJmxCollection(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._jmxCollectionList.size()) {
            throw new IndexOutOfBoundsException("getJmxCollection: Index value '" + index + "' not in range [0.." + (this._jmxCollectionList.size() - 1) + "]");
        }

        return (JmxCollection) _jmxCollectionList.get(index);
    }

    public JmxCollection getJmxCollection(final String collectionName) {
        for (JmxCollection eachCollection : _jmxCollectionList) {
            if (eachCollection != null 
                    && eachCollection.getName() != null
                    && eachCollection.getName().equals(collectionName)) {
                return eachCollection;
            }
        }
        return null;
    }

    public int getJmxCollectionCount() {
        return this._jmxCollectionList.size();
    }

    public String getRrdRepository() {
        return this._rrdRepository;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_rrdRepository, _jmxCollectionList);
    }

    public boolean removeJmxCollection(final JmxCollection vJmxCollection) {
        boolean removed = _jmxCollectionList.remove(vJmxCollection);
        return removed;
    }

    public void setJmxCollectionList(final java.util.List<JmxCollection> jmxCollectionList) {
        this._jmxCollectionList.clear();
        this._jmxCollectionList.addAll(jmxCollectionList);
    }

    public java.util.List<JmxCollection> getJmxCollectionList() {
        return _jmxCollectionList;
    }

    public void setRrdRepository(final String rrdRepository) {
        this._rrdRepository = rrdRepository;
    }

}
