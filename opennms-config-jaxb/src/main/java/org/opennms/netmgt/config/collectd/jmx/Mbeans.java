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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "mbeans")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
// TODO mvr remove fishy methods
// TODO mvr remove this class, it should be wrapped by JmxCollectoin using JaxB annotations
public class Mbeans implements java.io.Serializable {

    @XmlElement(name = "mbean", required = true)
    private java.util.List<Mbean> _mbeanList = new java.util.ArrayList<Mbean>();

    public void addMbean(final Mbean vMbean) {
        this._mbeanList.add(vMbean);
    }

    /**
     * @throws IndexOutOfBoundsException if the index
     *                                   given is outside the bounds of the collection
     */
    public void addMbean(final int index, final Mbean vMbean)
            throws IndexOutOfBoundsException {
        this._mbeanList.add(index, vMbean);
    }

    /**
     * Method enumerateMbean.
     *
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<Mbean> enumerateMbean() {
        return java.util.Collections.enumeration(this._mbeanList);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof Mbeans) {
            Mbeans temp = (Mbeans) obj;
            if (this._mbeanList != null) {
                if (temp._mbeanList == null) return false;
                else if (!(this._mbeanList.equals(temp._mbeanList)))
                    return false;
            } else if (temp._mbeanList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getMbean.
     *
     * @param index
     * @return the value of the
     * org.opennms.netmgt.config.collectd.jmx.Mbean at the given index
     * @throws IndexOutOfBoundsException if the index
     *                                   given is outside the bounds of the collection
     */
    public Mbean getMbean(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._mbeanList.size()) {
            throw new IndexOutOfBoundsException("getMbean: Index value '" + index + "' not in range [0.." + (this._mbeanList.size() - 1) + "]");
        }

        return (Mbean) _mbeanList.get(index);
    }

    /**
     * Method getMbean.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     *
     * @return this collection as an Array
     */
    public Mbean[] getMbean() {
        Mbean[] array = new Mbean[0];
        return (Mbean[]) this._mbeanList.toArray(array);
    }

    /**
     * Method getMbeanCollection.Returns a reference to
     * '_mbeanList'. No type checking is performed on any
     * modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Mbean> getMbeanCollection() {
        return this._mbeanList;
    }

    /**
     * Method getMbeanCount.
     *
     * @return the size of this collection
     */
    public int getMbeanCount() {
        return this._mbeanList.size();
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
    public int hashCode() {
        int result = 17;

        long tmp;
        if (_mbeanList != null) {
            result = 37 * result + _mbeanList.hashCode();
        }

        return result;
    }

    public java.util.Iterator<Mbean> iterateMbean() {
        return this._mbeanList.iterator();
    }

    public void removeAllMbean() {
        this._mbeanList.clear();
    }

    /**
     * Method removeMbean.
     *
     * @param vMbean the MBean to remove.
     * @return true if the object was removed from the collection.
     */
    public boolean removeMbean( final Mbean vMbean) {
        boolean removed = _mbeanList.remove(vMbean);
        return removed;
    }

    /**
     * Method removeMbeanAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public Mbean removeMbeanAt(final int index) {
        Object obj = this._mbeanList.remove(index);
        return (Mbean) obj;
    }

    /**
     * @param index
     * @param vMbean
     * @throws IndexOutOfBoundsException if the index
     *                                   given is outside the bounds of the collection
     */
    public void setMbean(final int index, final Mbean vMbean) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._mbeanList.size()) {
            throw new IndexOutOfBoundsException("setMbean: Index value '" + index + "' not in range [0.." + (this._mbeanList.size() - 1) + "]");
        }

        this._mbeanList.set(index, vMbean);
    }

    /**
     * @param vMbeanArray
     */
    public void setMbean( final Mbean[] vMbeanArray) {
        //-- copy array
        _mbeanList.clear();

        for (int i = 0; i < vMbeanArray.length; i++) {
            this._mbeanList.add(vMbeanArray[i]);
        }
    }

    /**
     * Sets the value of '_mbeanList' by copying the given Vector.
     * All elements will be checked for type safety.
     *
     * @param vMbeanList the Vector to copy.
     */
    public void setMbean(final java.util.List<Mbean> vMbeanList) {
        // copy vector
        this._mbeanList.clear();

        this._mbeanList.addAll(vMbeanList);
    }

    /**
     * Sets the value of '_mbeanList' by setting it to the given
     * Vector. No type checking is performed.
     *
     * @param mbeanList the Vector to set.
     * @deprecated
     */
    public void setMbeanCollection(final java.util.List<Mbean> mbeanList) {
        this._mbeanList = mbeanList;
    }

}
