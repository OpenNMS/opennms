/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xml.event;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Class EventReceipt.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="event-receipt")
@XmlAccessorType(XmlAccessType.FIELD)
//@ValidateUsing("event.xsd")
public class EventReceipt implements Serializable {
	private static final long serialVersionUID = -3104058231772479313L;

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

	/**
     * Field _uuidList.
     */
	@XmlElement(name="uuid")
    private java.util.List<java.lang.String> _uuidList;


      //----------------/
     //- Constructors -/
    //----------------/

    public EventReceipt() {
        super();
        this._uuidList = new java.util.ArrayList<java.lang.String>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vUuid
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addUuid(
            final java.lang.String vUuid)
    throws java.lang.IndexOutOfBoundsException {
        this._uuidList.add(vUuid);
    }

    /**
     * 
     * 
     * @param index
     * @param vUuid
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addUuid(
            final int index,
            final java.lang.String vUuid)
    throws java.lang.IndexOutOfBoundsException {
        this._uuidList.add(index, vUuid);
    }

    /**
     * Method enumerateUuid.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<java.lang.String> enumerateUuid(
    ) {
        return java.util.Collections.enumeration(this._uuidList);
    }

    /**
     * Method getUuid.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the java.lang.String at the given index
     */
    public java.lang.String getUuid(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._uuidList.size()) {
            throw new IndexOutOfBoundsException("getUuid: Index value '" + index + "' not in range [0.." + (this._uuidList.size() - 1) + "]");
        }
        
        return (java.lang.String) _uuidList.get(index);
    }

    /**
     * Method getUuid.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public java.lang.String[] getUuid(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._uuidList.toArray(array);
    }

    /**
     * Method getUuidCollection.Returns a reference to '_uuidList'.
     * No type checking is performed on any modifications to the
     * Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<java.lang.String> getUuidCollection(
    ) {
        return this._uuidList;
    }

    /**
     * Method getUuidCount.
     * 
     * @return the size of this collection
     */
    public int getUuidCount(
    ) {
        return this._uuidList.size();
    }

    /**
     * Method iterateUuid.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<java.lang.String> iterateUuid(
    ) {
        return this._uuidList.iterator();
    }

    /**
     */
    public void removeAllUuid(
    ) {
        this._uuidList.clear();
    }

    /**
     * Method removeUuid.
     * 
     * @param vUuid
     * @return true if the object was removed from the collection.
     */
    public boolean removeUuid(
            final java.lang.String vUuid) {
        boolean removed = _uuidList.remove(vUuid);
        return removed;
    }

    /**
     * Method removeUuidAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeUuidAt(
            final int index) {
        java.lang.Object obj = this._uuidList.remove(index);
        return (java.lang.String) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vUuid
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setUuid(
            final int index,
            final java.lang.String vUuid)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._uuidList.size()) {
            throw new IndexOutOfBoundsException("setUuid: Index value '" + index + "' not in range [0.." + (this._uuidList.size() - 1) + "]");
        }
        
        this._uuidList.set(index, vUuid);
    }

    /**
     * 
     * 
     * @param vUuidArray
     */
    public void setUuid(
            final java.lang.String[] vUuidArray) {
        //-- copy array
        _uuidList.clear();
        
        for (int i = 0; i < vUuidArray.length; i++) {
                this._uuidList.add(vUuidArray[i]);
        }
    }

    /**
     * Sets the value of '_uuidList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vUuidList the Vector to copy.
     */
    public void setUuid(
            final java.util.List<java.lang.String> vUuidList) {
        // copy vector
        this._uuidList.clear();
        
        this._uuidList.addAll(vUuidList);
    }

    /**
     * Sets the value of '_uuidList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param uuidList the Vector to set.
     */
    public void setUuidCollection(
            final java.util.List<java.lang.String> uuidList) {
        this._uuidList = uuidList;
    }

        @Override
    public String toString() {
    	return new ToStringBuilder(this)
    		.append("uuid", _uuidList)
    		.toString();
    }
}
