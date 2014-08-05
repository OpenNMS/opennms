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

@XmlRootElement(name="comp-attrib")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class CompAttrib implements java.io.Serializable {


    @XmlAttribute(name="name", required=true)
    private String _name;

    @XmlAttribute(name="alias")
    private String _alias;

    @XmlAttribute(name="type", required=true)
    private String _type;

    @XmlElement(name="comp-member")
    private java.util.List<CompMember> _compMemberList = new java.util.ArrayList<CompMember>();

    /**
     *
     *
     * @param vCompMember
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addCompMember(final CompMember vCompMember)
    throws IndexOutOfBoundsException {
        this._compMemberList.add(vCompMember);
    }

    /**
     *
     *
     * @param index
     * @param vCompMember
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addCompMember(
            final int index,
            final CompMember vCompMember)
    throws IndexOutOfBoundsException {
        this._compMemberList.add(index, vCompMember);
    }

    /**
     * Method enumerateCompMember.
     *
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<CompMember> enumerateCompMember(
    ) {
        return java.util.Collections.enumeration(this._compMemberList);
    }

    /**
     * Overrides the java.lang.Object.equals method.
     *
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(
            final Object obj) {
        if ( this == obj )
            return true;

        if (obj instanceof CompAttrib) {

            CompAttrib temp = (CompAttrib)obj;
            if (this._name != null) {
                if (temp._name == null) return false;
                else if (!(this._name.equals(temp._name)))
                    return false;
            }
            else if (temp._name != null)
                return false;
            if (this._alias != null) {
                if (temp._alias == null) return false;
                else if (!(this._alias.equals(temp._alias)))
                    return false;
            }
            else if (temp._alias != null)
                return false;
            if (this._type != null) {
                if (temp._type == null) return false;
                else if (!(this._type.equals(temp._type)))
                    return false;
            }
            else if (temp._type != null)
                return false;
            if (this._compMemberList != null) {
                if (temp._compMemberList == null) return false;
                else if (!(this._compMemberList.equals(temp._compMemberList)))
                    return false;
            }
            else if (temp._compMemberList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'alias'.
     *
     * @return the value of field 'Alias'.
     */
    public String getAlias(
    ) {
        return this._alias;
    }

    /**
     * Method getCompMember.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.config.collectd.jmx.CompMember at the given
     * index
     */
    public CompMember getCompMember(
            final int index)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._compMemberList.size()) {
            throw new IndexOutOfBoundsException("getCompMember: Index value '" + index + "' not in range [0.." + (this._compMemberList.size() - 1) + "]");
        }

        return (CompMember) _compMemberList.get(index);
    }

    /**
     * Method getCompMember.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     *
     * @return this collection as an Array
     */
    public CompMember[] getCompMember(
    ) {
        CompMember[] array = new CompMember[0];
        return (CompMember[]) this._compMemberList.toArray(array);
    }

    /**
     * Method getCompMemberCollection.Returns a reference to
     * '_compMemberList'. No type checking is performed on any
     * modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public java.util.List<CompMember> getCompMemberCollection(
    ) {
        return this._compMemberList;
    }

    /**
     * Method getCompMemberCount.
     *
     * @return the size of this collection
     */
    public int getCompMemberCount(
    ) {
        return this._compMemberList.size();
    }

    /**
     * Returns the value of field 'name'.
     *
     * @return the value of field 'Name'.
     */
    public String getName(
    ) {
        return this._name;
    }

    /**
     * Returns the value of field 'type'.
     *
     * @return the value of field 'Type'.
     */
    public String getType(
    ) {
        return this._type;
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
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
        if (_name != null) {
           result = 37 * result + _name.hashCode();
        }
        if (_alias != null) {
           result = 37 * result + _alias.hashCode();
        }
        if (_type != null) {
           result = 37 * result + _type.hashCode();
        }
        if (_compMemberList != null) {
           result = 37 * result + _compMemberList.hashCode();
        }

        return result;
    }

    /**
     * Method iterateCompMember.
     *
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<CompMember> iterateCompMember(
    ) {
        return this._compMemberList.iterator();
    }

    /**
     */
    public void removeAllCompMember(
    ) {
        this._compMemberList.clear();
    }

    /**
     * Method removeCompMember.
     *
     * @param vCompMember
     * @return true if the object was removed from the collection.
     */
    public boolean removeCompMember(
            final CompMember vCompMember) {
        boolean removed = _compMemberList.remove(vCompMember);
        return removed;
    }

    /**
     * Method removeCompMemberAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public CompMember removeCompMemberAt(
            final int index) {
        Object obj = this._compMemberList.remove(index);
        return (CompMember) obj;
    }

    /**
     * Sets the value of field 'alias'.
     *
     * @param alias the value of field 'alias'.
     */
    public void setAlias(
            final String alias) {
        this._alias = alias;
    }

    /**
     *
     *
     * @param index
     * @param vCompMember
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setCompMember(
            final int index,
            final CompMember vCompMember)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._compMemberList.size()) {
            throw new IndexOutOfBoundsException("setCompMember: Index value '" + index + "' not in range [0.." + (this._compMemberList.size() - 1) + "]");
        }

        this._compMemberList.set(index, vCompMember);
    }

    /**
     *
     *
     * @param vCompMemberArray
     */
    public void setCompMember(
            final CompMember[] vCompMemberArray) {
        //-- copy array
        _compMemberList.clear();

        for (int i = 0; i < vCompMemberArray.length; i++) {
                this._compMemberList.add(vCompMemberArray[i]);
        }
    }

    /**
     * Sets the value of '_compMemberList' by copying the given
     * Vector. All elements will be checked for type safety.
     *
     * @param vCompMemberList the Vector to copy.
     */
    public void setCompMember(
            final java.util.List<CompMember> vCompMemberList) {
        // copy vector
        this._compMemberList.clear();

        this._compMemberList.addAll(vCompMemberList);
    }

    /**
     * Sets the value of '_compMemberList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     *
     * @param compMemberList the Vector to set.
     */
    public void setCompMemberCollection(
            final java.util.List<CompMember> compMemberList) {
        this._compMemberList = compMemberList;
    }

    /**
     * Sets the value of field 'name'.
     *
     * @param name the value of field 'name'.
     */
    public void setName(
            final String name) {
        this._name = name;
    }

    /**
     * Sets the value of field 'type'.
     *
     * @param type the value of field 'type'.
     */
    public void setType(
            final String type) {
        this._type = type;
    }
}
