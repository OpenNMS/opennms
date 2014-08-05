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

@XmlRootElement(name = "jmx-datacollection-config")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
// TODO mvr remove fishy methods
public class JmxDatacollectionConfig implements java.io.Serializable {

    @XmlAttribute(name = "rrdRepository", required = true)
    private String _rrdRepository;

    @XmlElement(name = "jmx-collection", required = true)
    private java.util.List<JmxCollection> _jmxCollectionList = new java.util.ArrayList<>();
    ;


    /**
     * @param vJmxCollection
     * @throws IndexOutOfBoundsException if the index
     *                                   given is outside the bounds of the collection
     */
    public void addJmxCollection(
            final JmxCollection vJmxCollection)
            throws IndexOutOfBoundsException {
        this._jmxCollectionList.add(vJmxCollection);
    }

    /**
     * @param index
     * @param vJmxCollection
     * @throws IndexOutOfBoundsException if the index
     *                                   given is outside the bounds of the collection
     */
    public void addJmxCollection(
            final int index,
            final JmxCollection vJmxCollection)
            throws IndexOutOfBoundsException {
        this._jmxCollectionList.add(index, vJmxCollection);
    }

    /**
     * Method enumerateJmxCollection.
     *
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<JmxCollection> enumerateJmxCollection(
    ) {
        return java.util.Collections.enumeration(this._jmxCollectionList);
    }

    @Override
    public boolean equals(
            final Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof JmxDatacollectionConfig) {

            JmxDatacollectionConfig temp = (JmxDatacollectionConfig) obj;
            if (this._rrdRepository != null) {
                if (temp._rrdRepository == null) return false;
                else if (!(this._rrdRepository.equals(temp._rrdRepository)))
                    return false;
            } else if (temp._rrdRepository != null)
                return false;
            if (this._jmxCollectionList != null) {
                if (temp._jmxCollectionList == null) return false;
                else if (!(this._jmxCollectionList.equals(temp._jmxCollectionList)))
                    return false;
            } else if (temp._jmxCollectionList != null)
                return false;
            return true;
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
    public JmxCollection getJmxCollection(
            final int index)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._jmxCollectionList.size()) {
            throw new IndexOutOfBoundsException("getJmxCollection: Index value '" + index + "' not in range [0.." + (this._jmxCollectionList.size() - 1) + "]");
        }

        return (JmxCollection) _jmxCollectionList.get(index);
    }

    /**
     * Method getJmxCollection.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     *
     * @return this collection as an Array
     */
    public JmxCollection[] getJmxCollection(
    ) {
        JmxCollection[] array = new JmxCollection[0];
        return (JmxCollection[]) this._jmxCollectionList.toArray(array);
    }

    /**
     * Method getJmxCollectionCollection.Returns a reference to
     * '_jmxCollectionList'. No type checking is performed on any
     * modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public java.util.List<JmxCollection> getJmxCollectionCollection(
    ) {
        return this._jmxCollectionList;
    }

    /**
     * Method getJmxCollectionCount.
     *
     * @return the size of this collection
     */
    public int getJmxCollectionCount(
    ) {
        return this._jmxCollectionList.size();
    }

    /**
     * Returns the value of field 'rrdRepository'.
     *
     * @return the value of field 'RrdRepository'.
     */
    public String getRrdRepository(
    ) {
        return this._rrdRepository;
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p/>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     *
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode(
    ) {
        int result = 17;

        long tmp;
        if (_rrdRepository != null) {
            result = 37 * result + _rrdRepository.hashCode();
        }
        if (_jmxCollectionList != null) {
            result = 37 * result + _jmxCollectionList.hashCode();
        }

        return result;
    }

    /**
     * Method iterateJmxCollection.
     *
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<JmxCollection> iterateJmxCollection(
    ) {
        return this._jmxCollectionList.iterator();
    }

    public void removeAllJmxCollection(
    ) {
        this._jmxCollectionList.clear();
    }

    /**
     * Method removeJmxCollection.
     *
     * @param vJmxCollection
     * @return true if the object was removed from the collection.
     */
    public boolean removeJmxCollection(
            final JmxCollection vJmxCollection) {
        boolean removed = _jmxCollectionList.remove(vJmxCollection);
        return removed;
    }

    /**
     * Method removeJmxCollectionAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public JmxCollection removeJmxCollectionAt(
            final int index) {
        Object obj = this._jmxCollectionList.remove(index);
        return (JmxCollection) obj;
    }

    /**
     * @param index
     * @param vJmxCollection
     * @throws IndexOutOfBoundsException if the index
     *                                   given is outside the bounds of the collection
     */
    public void setJmxCollection(
            final int index,
            final JmxCollection vJmxCollection)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._jmxCollectionList.size()) {
            throw new IndexOutOfBoundsException("setJmxCollection: Index value '" + index + "' not in range [0.." + (this._jmxCollectionList.size() - 1) + "]");
        }

        this._jmxCollectionList.set(index, vJmxCollection);
    }

    /**
     * @param vJmxCollectionArray
     */
    public void setJmxCollection(
            final JmxCollection[] vJmxCollectionArray) {
        //-- copy array
        _jmxCollectionList.clear();

        for (int i = 0; i < vJmxCollectionArray.length; i++) {
            this._jmxCollectionList.add(vJmxCollectionArray[i]);
        }
    }

    /**
     * Sets the value of '_jmxCollectionList' by copying the given
     * Vector. All elements will be checked for type safety.
     *
     * @param vJmxCollectionList the Vector to copy.
     */
    public void setJmxCollection(
            final java.util.List<JmxCollection> vJmxCollectionList) {
        // copy vector
        this._jmxCollectionList.clear();

        this._jmxCollectionList.addAll(vJmxCollectionList);
    }

    /**
     * Sets the value of '_jmxCollectionList' by setting it to the
     * given Vector. No type checking is performed.
     *
     * @param jmxCollectionList the Vector to set.
     * @deprecated
     */
    public void setJmxCollectionCollection(
            final java.util.List<JmxCollection> jmxCollectionList) {
        this._jmxCollectionList = jmxCollectionList;
    }

    /**
     * Sets the value of field 'rrdRepository'.
     *
     * @param rrdRepository the value of field 'rrdRepository'.
     */
    public void setRrdRepository(
            final String rrdRepository) {
        this._rrdRepository = rrdRepository;
    }

}
