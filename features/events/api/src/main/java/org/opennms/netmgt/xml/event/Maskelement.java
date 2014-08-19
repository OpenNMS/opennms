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
 * The mask element
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="maskelement")
@XmlAccessorType(XmlAccessType.FIELD)
//@ValidateUsing("event.xsd")
public class Maskelement implements Serializable {
	private static final long serialVersionUID = 6355834996920103487L;

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

	/**
     * The mask element name can only be one of those
     *  specified above
     */

	@XmlElement(name="mename", required=true)
    private java.lang.String _mename;

    /**
     * The mask element value
     */
	@XmlElement(name="mevalue", required=true)
    private java.util.List<java.lang.String> _mevalueList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Maskelement() {
        super();
        this._mevalueList = new java.util.ArrayList<java.lang.String>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vMevalue
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMevalue(
            final java.lang.String vMevalue)
    throws java.lang.IndexOutOfBoundsException {
        this._mevalueList.add(vMevalue.intern());
    }

    /**
     * 
     * 
     * @param index
     * @param vMevalue
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMevalue(
            final int index,
            final java.lang.String vMevalue)
    throws java.lang.IndexOutOfBoundsException {
        this._mevalueList.add(index, vMevalue.intern());
    }

    /**
     * Method enumerateMevalue.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<java.lang.String> enumerateMevalue(
    ) {
        return java.util.Collections.enumeration(this._mevalueList);
    }

    /**
     * Returns the value of field 'mename'. The field 'mename' has
     * the following description: The mask element name can only be
     * one of those
     *  specified above
     * 
     * @return the value of field 'Mename'.
     */
    public java.lang.String getMename(
    ) {
        return this._mename;
    }

    /**
     * Method getMevalue.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the java.lang.String at the given index
     */
    public java.lang.String getMevalue(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._mevalueList.size()) {
            throw new IndexOutOfBoundsException("getMevalue: Index value '" + index + "' not in range [0.." + (this._mevalueList.size() - 1) + "]");
        }
        
        return (java.lang.String) _mevalueList.get(index);
    }

    /**
     * Method getMevalue.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public java.lang.String[] getMevalue(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._mevalueList.toArray(array);
    }

    /**
     * Method getMevalueCollection.Returns a reference to
     * '_mevalueList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<java.lang.String> getMevalueCollection(
    ) {
        return this._mevalueList;
    }

    /**
     * Method getMevalueCount.
     * 
     * @return the size of this collection
     */
    public int getMevalueCount(
    ) {
        return this._mevalueList.size();
    }

    /**
     * Method iterateMevalue.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<java.lang.String> iterateMevalue(
    ) {
        return this._mevalueList.iterator();
    }

    /**
     */
    public void removeAllMevalue(
    ) {
        this._mevalueList.clear();
    }

    /**
     * Method removeMevalue.
     * 
     * @param vMevalue
     * @return true if the object was removed from the collection.
     */
    public boolean removeMevalue(
            final java.lang.String vMevalue) {
        boolean removed = _mevalueList.remove(vMevalue);
        return removed;
    }

    /**
     * Method removeMevalueAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeMevalueAt(
            final int index) {
        java.lang.Object obj = this._mevalueList.remove(index);
        return (java.lang.String) obj;
    }

    /**
     * Sets the value of field 'mename'. The field 'mename' has the
     * following description: The mask element name can only be one
     * of those
     *  specified above
     * 
     * @param mename the value of field 'mename'.
     */
    public void setMename(
            final java.lang.String mename) {
        this._mename = mename.intern();
    }

    /**
     * 
     * 
     * @param index
     * @param vMevalue
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setMevalue(
            final int index,
            final java.lang.String vMevalue)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._mevalueList.size()) {
            throw new IndexOutOfBoundsException("setMevalue: Index value '" + index + "' not in range [0.." + (this._mevalueList.size() - 1) + "]");
        }
        
        this._mevalueList.set(index, vMevalue.intern());
    }

    /**
     * 
     * 
     * @param vMevalueArray
     */
    public void setMevalue(
            final java.lang.String[] vMevalueArray) {
        //-- copy array
        _mevalueList.clear();
        
        for (int i = 0; i < vMevalueArray.length; i++) {
                this._mevalueList.add(vMevalueArray[i].intern());
        }
    }

    /**
     * Sets the value of '_mevalueList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vMevalueList the Vector to copy.
     */
    public void setMevalue(
            final java.util.List<java.lang.String> vMevalueList) {
        // copy vector
        this._mevalueList.clear();
        for (final String value : vMevalueList) {
            this._mevalueList.add(value.intern());
        }
    }

    /**
     * Sets the value of '_mevalueList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param mevalueList the Vector to set.
     */
    public void setMevalueCollection(
            final java.util.List<java.lang.String> mevalueList) {
        this._mevalueList.clear();
        for (final String value : mevalueList) {
            this._mevalueList.add(value.intern());
        }
    }

        @Override
    public String toString() {
    	return new ToStringBuilder(this)
    		.append("mename", _mename)
    		.append("mevalue", _mevalueList)
    		.toString();
    }
}
