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

package org.opennms.netmgt.config.vmware.cim;

import org.apache.commons.lang.builder.EqualsBuilder;

import javax.xml.bind.annotation.*;

/**
 * RRD parms
 */
@XmlRootElement(name = "rrd")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class Rrd implements java.io.Serializable {

    /**
     * step size for the RRD
     */
    @XmlAttribute(name = "step")
    private Integer _step;

    /**
     * Round Robin Archive definitions
     */
    @XmlElement(name = "rra")
    private java.util.List<java.lang.String> _rraList;

    public Rrd() {
        super();
        this._rraList = new java.util.ArrayList<java.lang.String>();
    }

    /**
     * @param vRra
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void addRra(
            final java.lang.String vRra)
            throws java.lang.IndexOutOfBoundsException {
        this._rraList.add(vRra);
    }

    /**
     * @param index
     * @param vRra
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void addRra(
            final int index,
            final java.lang.String vRra)
            throws java.lang.IndexOutOfBoundsException {
        this._rraList.add(index, vRra);
    }

    /**
     * Method enumerateRra.
     *
     * @return an Enumeration over all possible elements of this
     *         collection
     */
    public java.util.Enumeration<java.lang.String> enumerateRra(
    ) {
        return java.util.Collections.enumeration(this._rraList);
    }

    /**
     * Overrides the java.lang.Object.equals method.
     *
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(
            final java.lang.Object obj) {
        if (obj instanceof Rrd) {
            Rrd other = (Rrd) obj;
            return new EqualsBuilder()
                    .append(getRra(), other.getRra())
                    .append(getStep(), other.getStep())
                    .isEquals();
        }
        return false;
    }

    /**
     * Method getRra.
     *
     * @param index
     * @return the value of the java.lang.String at the given index
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public java.lang.String getRra(
            final int index)
            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._rraList.size()) {
            throw new IndexOutOfBoundsException("getRra: Index value '" + index + "' not in range [0.." + (this._rraList.size() - 1) + "]");
        }

        return (java.lang.String) _rraList.get(index);
    }

    /**
     * Method getRra.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     *
     * @return this collection as an Array
     */
    public java.lang.String[] getRra(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._rraList.toArray(array);
    }

    /**
     * Method getRraCollection.Returns a reference to '_rraList'.
     * No type checking is performed on any modifications to the
     * Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public java.util.List<java.lang.String> getRraCollection(
    ) {
        return this._rraList;
    }

    /**
     * Method getRraCount.
     *
     * @return the size of this collection
     */
    public int getRraCount(
    ) {
        return this._rraList.size();
    }

    /**
     * Returns the value of field 'step'. The field 'step' has the
     * following description: step size for the RRD
     *
     * @return the value of field 'Step'.
     */
    public Integer getStep(
    ) {
        return this._step == null ? 0 : this._step;
    }

    /**
     * Method iterateRra.
     *
     * @return an Iterator over all possible elements in this
     *         collection
     */
    public java.util.Iterator<java.lang.String> iterateRra(
    ) {
        return this._rraList.iterator();
    }

    /**
     */
    public void removeAllRra(
    ) {
        this._rraList.clear();
    }

    /**
     * Method removeRra.
     *
     * @param vRra
     * @return true if the object was removed from the collection.
     */
    public boolean removeRra(
            final java.lang.String vRra) {
        boolean removed = _rraList.remove(vRra);
        return removed;
    }

    /**
     * Method removeRraAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeRraAt(
            final int index) {
        java.lang.Object obj = this._rraList.remove(index);
        return (java.lang.String) obj;
    }

    /**
     * @param index
     * @param vRra
     * @throws java.lang.IndexOutOfBoundsException
     *          if the index
     *          given is outside the bounds of the collection
     */
    public void setRra(
            final int index,
            final java.lang.String vRra)
            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._rraList.size()) {
            throw new IndexOutOfBoundsException("setRra: Index value '" + index + "' not in range [0.." + (this._rraList.size() - 1) + "]");
        }

        this._rraList.set(index, vRra);
    }

    /**
     * @param vRraArray
     */
    public void setRra(
            final java.lang.String[] vRraArray) {
        //-- copy array
        _rraList.clear();

        for (int i = 0; i < vRraArray.length; i++) {
            this._rraList.add(vRraArray[i]);
        }
    }

    /**
     * Sets the value of '_rraList' by copying the given Vector.
     * All elements will be checked for type safety.
     *
     * @param vRraList the Vector to copy.
     */
    public void setRra(
            final java.util.List<java.lang.String> vRraList) {
        // copy vector
        this._rraList.clear();

        this._rraList.addAll(vRraList);
    }

    /**
     * Sets the value of field 'step'. The field 'step' has the
     * following description: step size for the RRD
     *
     * @param step the value of field 'step'.
     */
    public void setStep(
            final int step) {
        this._step = step;
    }
}
