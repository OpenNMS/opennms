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

package org.opennms.netmgt.config.xmpDataCollection;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * a MIB object group
 * 
 * @version $Revision$ $Date$
 */

@SuppressWarnings("all") public class Group implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * group name
     */
    private java.lang.String _name;

    /**
     * Name of resource or null if scalars.
     *  Resource type matches declaration in
     *  datacollection-config.xml for presentation in Web UI and in
     *  RRD storage. Each table, that is collected, should
     *  generally be given their own resource type. MIB2 tables,
     *  however, need not be given a separate resourceType as they
     *  are handled out-of-box by ONMS.
     */
    private java.lang.String _resourceType;

    /**
     * a MIB object
     */
    private java.util.List<org.opennms.netmgt.config.xmpDataCollection.MibObj> _mibObjList;

    /**
     * sub group
     */
    private java.util.List<java.lang.String> _includeGroupList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Group() {
        super();
        this._mibObjList = new java.util.ArrayList<org.opennms.netmgt.config.xmpDataCollection.MibObj>();
        this._includeGroupList = new java.util.ArrayList<java.lang.String>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vIncludeGroup
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeGroup(
            final java.lang.String vIncludeGroup)
    throws java.lang.IndexOutOfBoundsException {
        this._includeGroupList.add(vIncludeGroup);
    }

    /**
     * 
     * 
     * @param index
     * @param vIncludeGroup
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeGroup(
            final int index,
            final java.lang.String vIncludeGroup)
    throws java.lang.IndexOutOfBoundsException {
        this._includeGroupList.add(index, vIncludeGroup);
    }

    /**
     * 
     * 
     * @param vMibObj
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMibObj(
            final org.opennms.netmgt.config.xmpDataCollection.MibObj vMibObj)
    throws java.lang.IndexOutOfBoundsException {
        this._mibObjList.add(vMibObj);
    }

    /**
     * 
     * 
     * @param index
     * @param vMibObj
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMibObj(
            final int index,
            final org.opennms.netmgt.config.xmpDataCollection.MibObj vMibObj)
    throws java.lang.IndexOutOfBoundsException {
        this._mibObjList.add(index, vMibObj);
    }

    /**
     * Method enumerateIncludeGroup.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<java.lang.String> enumerateIncludeGroup(
    ) {
        return java.util.Collections.enumeration(this._includeGroupList);
    }

    /**
     * Method enumerateMibObj.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.xmpDataCollection.MibObj> enumerateMibObj(
    ) {
        return java.util.Collections.enumeration(this._mibObjList);
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
        if ( this == obj )
            return true;
        
        if (obj instanceof Group) {
        
            Group temp = (Group)obj;
            if (this._name != null) {
                if (temp._name == null) return false;
                else if (!(this._name.equals(temp._name))) 
                    return false;
            }
            else if (temp._name != null)
                return false;
            if (this._resourceType != null) {
                if (temp._resourceType == null) return false;
                else if (!(this._resourceType.equals(temp._resourceType))) 
                    return false;
            }
            else if (temp._resourceType != null)
                return false;
            if (this._mibObjList != null) {
                if (temp._mibObjList == null) return false;
                else if (!(this._mibObjList.equals(temp._mibObjList))) 
                    return false;
            }
            else if (temp._mibObjList != null)
                return false;
            if (this._includeGroupList != null) {
                if (temp._includeGroupList == null) return false;
                else if (!(this._includeGroupList.equals(temp._includeGroupList))) 
                    return false;
            }
            else if (temp._includeGroupList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getIncludeGroup.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the java.lang.String at the given index
     */
    public java.lang.String getIncludeGroup(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._includeGroupList.size()) {
            throw new IndexOutOfBoundsException("getIncludeGroup: Index value '" + index + "' not in range [0.." + (this._includeGroupList.size() - 1) + "]");
        }
        
        return (java.lang.String) _includeGroupList.get(index);
    }

    /**
     * Method getIncludeGroup.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public java.lang.String[] getIncludeGroup(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._includeGroupList.toArray(array);
    }

    /**
     * Method getIncludeGroupCollection.Returns a reference to
     * '_includeGroupList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<java.lang.String> getIncludeGroupCollection(
    ) {
        return this._includeGroupList;
    }

    /**
     * Method getIncludeGroupCount.
     * 
     * @return the size of this collection
     */
    public int getIncludeGroupCount(
    ) {
        return this._includeGroupList.size();
    }

    /**
     * Method getMibObj.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.config.xmpDataCollection.MibObj at the
     * given index
     */
    public org.opennms.netmgt.config.xmpDataCollection.MibObj getMibObj(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._mibObjList.size()) {
            throw new IndexOutOfBoundsException("getMibObj: Index value '" + index + "' not in range [0.." + (this._mibObjList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.config.xmpDataCollection.MibObj) _mibObjList.get(index);
    }

    /**
     * Method getMibObj.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.xmpDataCollection.MibObj[] getMibObj(
    ) {
        org.opennms.netmgt.config.xmpDataCollection.MibObj[] array = new org.opennms.netmgt.config.xmpDataCollection.MibObj[0];
        return (org.opennms.netmgt.config.xmpDataCollection.MibObj[]) this._mibObjList.toArray(array);
    }

    /**
     * Method getMibObjCollection.Returns a reference to
     * '_mibObjList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.xmpDataCollection.MibObj> getMibObjCollection(
    ) {
        return this._mibObjList;
    }

    /**
     * Method getMibObjCount.
     * 
     * @return the size of this collection
     */
    public int getMibObjCount(
    ) {
        return this._mibObjList.size();
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the
     * following description: group name
     * 
     * @return the value of field 'Name'.
     */
    public java.lang.String getName(
    ) {
        return this._name;
    }

    /**
     * Returns the value of field 'resourceType'. The field
     * 'resourceType' has the following description: Name of
     * resource or null if scalars.
     *  Resource type matches declaration in
     *  datacollection-config.xml for presentation in Web UI and in
     *  RRD storage. Each table, that is collected, should
     *  generally be given their own resource type. MIB2 tables,
     *  however, need not be given a separate resourceType as they
     *  are handled out-of-box by ONMS.
     * 
     * @return the value of field 'ResourceType'.
     */
    public java.lang.String getResourceType(
    ) {
        return this._resourceType;
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
        if (_resourceType != null) {
           result = 37 * result + _resourceType.hashCode();
        }
        if (_mibObjList != null) {
           result = 37 * result + _mibObjList.hashCode();
        }
        if (_includeGroupList != null) {
           result = 37 * result + _includeGroupList.hashCode();
        }
        
        return result;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid(
    ) {
        try {
            validate();
        } catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * Method iterateIncludeGroup.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<java.lang.String> iterateIncludeGroup(
    ) {
        return this._includeGroupList.iterator();
    }

    /**
     * Method iterateMibObj.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.xmpDataCollection.MibObj> iterateMibObj(
    ) {
        return this._mibObjList.iterator();
    }

    /**
     * 
     * 
     * @param out
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(
            final java.io.Writer out)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws java.io.IOException if an IOException occurs during
     * marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(
            final org.xml.sax.ContentHandler handler)
    throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllIncludeGroup(
    ) {
        this._includeGroupList.clear();
    }

    /**
     */
    public void removeAllMibObj(
    ) {
        this._mibObjList.clear();
    }

    /**
     * Method removeIncludeGroup.
     * 
     * @param vIncludeGroup
     * @return true if the object was removed from the collection.
     */
    public boolean removeIncludeGroup(
            final java.lang.String vIncludeGroup) {
        boolean removed = _includeGroupList.remove(vIncludeGroup);
        return removed;
    }

    /**
     * Method removeIncludeGroupAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeIncludeGroupAt(
            final int index) {
        java.lang.Object obj = this._includeGroupList.remove(index);
        return (java.lang.String) obj;
    }

    /**
     * Method removeMibObj.
     * 
     * @param vMibObj
     * @return true if the object was removed from the collection.
     */
    public boolean removeMibObj(
            final org.opennms.netmgt.config.xmpDataCollection.MibObj vMibObj) {
        boolean removed = _mibObjList.remove(vMibObj);
        return removed;
    }

    /**
     * Method removeMibObjAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.xmpDataCollection.MibObj removeMibObjAt(
            final int index) {
        java.lang.Object obj = this._mibObjList.remove(index);
        return (org.opennms.netmgt.config.xmpDataCollection.MibObj) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vIncludeGroup
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setIncludeGroup(
            final int index,
            final java.lang.String vIncludeGroup)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._includeGroupList.size()) {
            throw new IndexOutOfBoundsException("setIncludeGroup: Index value '" + index + "' not in range [0.." + (this._includeGroupList.size() - 1) + "]");
        }
        
        this._includeGroupList.set(index, vIncludeGroup);
    }

    /**
     * 
     * 
     * @param vIncludeGroupArray
     */
    public void setIncludeGroup(
            final java.lang.String[] vIncludeGroupArray) {
        //-- copy array
        _includeGroupList.clear();
        
        for (int i = 0; i < vIncludeGroupArray.length; i++) {
                this._includeGroupList.add(vIncludeGroupArray[i]);
        }
    }

    /**
     * Sets the value of '_includeGroupList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vIncludeGroupList the Vector to copy.
     */
    public void setIncludeGroup(
            final java.util.List<java.lang.String> vIncludeGroupList) {
        // copy vector
        this._includeGroupList.clear();
        
        this._includeGroupList.addAll(vIncludeGroupList);
    }

    /**
     * Sets the value of '_includeGroupList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param includeGroupList the Vector to set.
     */
    public void setIncludeGroupCollection(
            final java.util.List<java.lang.String> includeGroupList) {
        this._includeGroupList = includeGroupList;
    }

    /**
     * 
     * 
     * @param index
     * @param vMibObj
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setMibObj(
            final int index,
            final org.opennms.netmgt.config.xmpDataCollection.MibObj vMibObj)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._mibObjList.size()) {
            throw new IndexOutOfBoundsException("setMibObj: Index value '" + index + "' not in range [0.." + (this._mibObjList.size() - 1) + "]");
        }
        
        this._mibObjList.set(index, vMibObj);
    }

    /**
     * 
     * 
     * @param vMibObjArray
     */
    public void setMibObj(
            final org.opennms.netmgt.config.xmpDataCollection.MibObj[] vMibObjArray) {
        //-- copy array
        _mibObjList.clear();
        
        for (int i = 0; i < vMibObjArray.length; i++) {
                this._mibObjList.add(vMibObjArray[i]);
        }
    }

    /**
     * Sets the value of '_mibObjList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vMibObjList the Vector to copy.
     */
    public void setMibObj(
            final java.util.List<org.opennms.netmgt.config.xmpDataCollection.MibObj> vMibObjList) {
        // copy vector
        this._mibObjList.clear();
        
        this._mibObjList.addAll(vMibObjList);
    }

    /**
     * Sets the value of '_mibObjList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param mibObjList the Vector to set.
     */
    public void setMibObjCollection(
            final java.util.List<org.opennms.netmgt.config.xmpDataCollection.MibObj> mibObjList) {
        this._mibObjList = mibObjList;
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the
     * following description: group name
     * 
     * @param name the value of field 'name'.
     */
    public void setName(
            final java.lang.String name) {
        this._name = name;
    }

    /**
     * Sets the value of field 'resourceType'. The field
     * 'resourceType' has the following description: Name of
     * resource or null if scalars.
     *  Resource type matches declaration in
     *  datacollection-config.xml for presentation in Web UI and in
     *  RRD storage. Each table, that is collected, should
     *  generally be given their own resource type. MIB2 tables,
     *  however, need not be given a separate resourceType as they
     *  are handled out-of-box by ONMS.
     * 
     * @param resourceType the value of field 'resourceType'.
     */
    public void setResourceType(
            final java.lang.String resourceType) {
        this._resourceType = resourceType;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * org.opennms.netmgt.config.xmpDataCollection.Group
     */
    public static org.opennms.netmgt.config.xmpDataCollection.Group unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.config.xmpDataCollection.Group) Unmarshaller.unmarshal(org.opennms.netmgt.config.xmpDataCollection.Group.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate(
    )
    throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
