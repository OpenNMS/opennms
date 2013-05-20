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

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Mbean.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="mbean")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all") public class Mbean implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    @XmlAttribute(name="name", required=true)
    private java.lang.String _name;

    /**
     * Field _objectname.
     */
    @XmlAttribute(name="objectname", required=true)
    private java.lang.String _objectname;

    /**
     * Field _keyfield.
     */
    @XmlAttribute(name="keyfield")
    private java.lang.String _keyfield;

    /**
     * Field _exclude.
     */
    @XmlAttribute(name="exclude")
    private java.lang.String _exclude;

    /**
     * Field _keyAlias.
     */
    @XmlAttribute(name="key-alias")
    private java.lang.String _keyAlias;

    /**
     * Field _attribList.
     */
    @XmlElement(name="attrib")
    private java.util.List<Attrib> _attribList;

    /**
     * Field _includeMbeanList.
     */
    @XmlTransient
    private java.util.List<java.lang.String> _includeMbeanList;

    /**
     * Field _compAttribList.
     */
    @XmlElement(name="comp-attrib")
    private java.util.List<CompAttrib> _compAttribList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Mbean() {
        super();
        this._attribList = new java.util.ArrayList<Attrib>();
        this._includeMbeanList = new java.util.ArrayList<java.lang.String>();
        this._compAttribList = new java.util.ArrayList<CompAttrib>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vAttrib
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addAttrib(
            final Attrib vAttrib)
    throws java.lang.IndexOutOfBoundsException {
        this._attribList.add(vAttrib);
    }

    /**
     * 
     * 
     * @param index
     * @param vAttrib
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addAttrib(
            final int index,
            final Attrib vAttrib)
    throws java.lang.IndexOutOfBoundsException {
        this._attribList.add(index, vAttrib);
    }

    /**
     * 
     * 
     * @param vCompAttrib
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addCompAttrib(
            final CompAttrib vCompAttrib)
    throws java.lang.IndexOutOfBoundsException {
        this._compAttribList.add(vCompAttrib);
    }

    /**
     * 
     * 
     * @param index
     * @param vCompAttrib
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addCompAttrib(
            final int index,
            final CompAttrib vCompAttrib)
    throws java.lang.IndexOutOfBoundsException {
        this._compAttribList.add(index, vCompAttrib);
    }

    /**
     * 
     * 
     * @param vIncludeMbean
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeMbean(
            final java.lang.String vIncludeMbean)
    throws java.lang.IndexOutOfBoundsException {
        this._includeMbeanList.add(vIncludeMbean);
    }

    /**
     * 
     * 
     * @param index
     * @param vIncludeMbean
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeMbean(
            final int index,
            final java.lang.String vIncludeMbean)
    throws java.lang.IndexOutOfBoundsException {
        this._includeMbeanList.add(index, vIncludeMbean);
    }

    /**
     * Method enumerateAttrib.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<Attrib> enumerateAttrib(
    ) {
        return java.util.Collections.enumeration(this._attribList);
    }

    /**
     * Method enumerateCompAttrib.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<CompAttrib> enumerateCompAttrib(
    ) {
        return java.util.Collections.enumeration(this._compAttribList);
    }

    /**
     * Method enumerateIncludeMbean.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<java.lang.String> enumerateIncludeMbean(
    ) {
        return java.util.Collections.enumeration(this._includeMbeanList);
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
        
        if (obj instanceof Mbean) {
        
            Mbean temp = (Mbean)obj;
            if (this._name != null) {
                if (temp._name == null) return false;
                else if (!(this._name.equals(temp._name))) 
                    return false;
            }
            else if (temp._name != null)
                return false;
            if (this._objectname != null) {
                if (temp._objectname == null) return false;
                else if (!(this._objectname.equals(temp._objectname))) 
                    return false;
            }
            else if (temp._objectname != null)
                return false;
            if (this._keyfield != null) {
                if (temp._keyfield == null) return false;
                else if (!(this._keyfield.equals(temp._keyfield))) 
                    return false;
            }
            else if (temp._keyfield != null)
                return false;
            if (this._exclude != null) {
                if (temp._exclude == null) return false;
                else if (!(this._exclude.equals(temp._exclude))) 
                    return false;
            }
            else if (temp._exclude != null)
                return false;
            if (this._keyAlias != null) {
                if (temp._keyAlias == null) return false;
                else if (!(this._keyAlias.equals(temp._keyAlias))) 
                    return false;
            }
            else if (temp._keyAlias != null)
                return false;
            if (this._attribList != null) {
                if (temp._attribList == null) return false;
                else if (!(this._attribList.equals(temp._attribList))) 
                    return false;
            }
            else if (temp._attribList != null)
                return false;
            if (this._includeMbeanList != null) {
                if (temp._includeMbeanList == null) return false;
                else if (!(this._includeMbeanList.equals(temp._includeMbeanList))) 
                    return false;
            }
            else if (temp._includeMbeanList != null)
                return false;
            if (this._compAttribList != null) {
                if (temp._compAttribList == null) return false;
                else if (!(this._compAttribList.equals(temp._compAttribList))) 
                    return false;
            }
            else if (temp._compAttribList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getAttrib.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.config.collectd.jmx.Attrib at the given index
     */
    public Attrib getAttrib(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._attribList.size()) {
            throw new IndexOutOfBoundsException("getAttrib: Index value '" + index + "' not in range [0.." + (this._attribList.size() - 1) + "]");
        }
        
        return (Attrib) _attribList.get(index);
    }

    /**
     * Method getAttrib.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Attrib[] getAttrib(
    ) {
        Attrib[] array = new Attrib[0];
        return (Attrib[]) this._attribList.toArray(array);
    }

    /**
     * Method getAttribCollection.Returns a reference to
     * '_attribList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Attrib> getAttribCollection(
    ) {
        return this._attribList;
    }

    /**
     * Method getAttribCount.
     * 
     * @return the size of this collection
     */
    public int getAttribCount(
    ) {
        return this._attribList.size();
    }

    /**
     * Method getCompAttrib.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.config.collectd.jmx.CompAttrib at the given
     * index
     */
    public CompAttrib getCompAttrib(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._compAttribList.size()) {
            throw new IndexOutOfBoundsException("getCompAttrib: Index value '" + index + "' not in range [0.." + (this._compAttribList.size() - 1) + "]");
        }
        
        return (CompAttrib) _compAttribList.get(index);
    }

    /**
     * Method getCompAttrib.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public CompAttrib[] getCompAttrib(
    ) {
        CompAttrib[] array = new CompAttrib[0];
        return (CompAttrib[]) this._compAttribList.toArray(array);
    }

    /**
     * Method getCompAttribCollection.Returns a reference to
     * '_compAttribList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<CompAttrib> getCompAttribCollection(
    ) {
        return this._compAttribList;
    }

    /**
     * Method getCompAttribCount.
     * 
     * @return the size of this collection
     */
    public int getCompAttribCount(
    ) {
        return this._compAttribList.size();
    }

    /**
     * Returns the value of field 'exclude'.
     * 
     * @return the value of field 'Exclude'.
     */
    public java.lang.String getExclude(
    ) {
        return this._exclude;
    }

    /**
     * Method getIncludeMbean.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the java.lang.String at the given index
     */
    public java.lang.String getIncludeMbean(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._includeMbeanList.size()) {
            throw new IndexOutOfBoundsException("getIncludeMbean: Index value '" + index + "' not in range [0.." + (this._includeMbeanList.size() - 1) + "]");
        }
        
        return (java.lang.String) _includeMbeanList.get(index);
    }

    /**
     * Method getIncludeMbean.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public java.lang.String[] getIncludeMbean(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._includeMbeanList.toArray(array);
    }

    /**
     * Method getIncludeMbeanCollection.Returns a reference to
     * '_includeMbeanList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<java.lang.String> getIncludeMbeanCollection(
    ) {
        return this._includeMbeanList;
    }

    /**
     * Method getIncludeMbeanCount.
     * 
     * @return the size of this collection
     */
    public int getIncludeMbeanCount(
    ) {
        return this._includeMbeanList.size();
    }

    /**
     * Returns the value of field 'keyAlias'.
     * 
     * @return the value of field 'KeyAlias'.
     */
    public java.lang.String getKeyAlias(
    ) {
        return this._keyAlias;
    }

    /**
     * Returns the value of field 'keyfield'.
     * 
     * @return the value of field 'Keyfield'.
     */
    public java.lang.String getKeyfield(
    ) {
        return this._keyfield;
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public java.lang.String getName(
    ) {
        return this._name;
    }

    /**
     * Returns the value of field 'objectname'.
     * 
     * @return the value of field 'Objectname'.
     */
    public java.lang.String getObjectname(
    ) {
        return this._objectname;
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
        if (_objectname != null) {
           result = 37 * result + _objectname.hashCode();
        }
        if (_keyfield != null) {
           result = 37 * result + _keyfield.hashCode();
        }
        if (_exclude != null) {
           result = 37 * result + _exclude.hashCode();
        }
        if (_keyAlias != null) {
           result = 37 * result + _keyAlias.hashCode();
        }
        if (_attribList != null) {
           result = 37 * result + _attribList.hashCode();
        }
        if (_includeMbeanList != null) {
           result = 37 * result + _includeMbeanList.hashCode();
        }
        if (_compAttribList != null) {
           result = 37 * result + _compAttribList.hashCode();
        }
        
        return result;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    @Deprecated
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
     * Method iterateAttrib.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<Attrib> iterateAttrib(
    ) {
        return this._attribList.iterator();
    }

    /**
     * Method iterateCompAttrib.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<CompAttrib> iterateCompAttrib(
    ) {
        return this._compAttribList.iterator();
    }

    /**
     * Method iterateIncludeMbean.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<java.lang.String> iterateIncludeMbean(
    ) {
        return this._includeMbeanList.iterator();
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
    @Deprecated
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
    @Deprecated
    public void marshal(
            final org.xml.sax.ContentHandler handler)
    throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllAttrib(
    ) {
        this._attribList.clear();
    }

    /**
     */
    public void removeAllCompAttrib(
    ) {
        this._compAttribList.clear();
    }

    /**
     */
    public void removeAllIncludeMbean(
    ) {
        this._includeMbeanList.clear();
    }

    /**
     * Method removeAttrib.
     * 
     * @param vAttrib
     * @return true if the object was removed from the collection.
     */
    public boolean removeAttrib(
            final Attrib vAttrib) {
        boolean removed = _attribList.remove(vAttrib);
        return removed;
    }

    /**
     * Method removeAttribAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Attrib removeAttribAt(
            final int index) {
        java.lang.Object obj = this._attribList.remove(index);
        return (Attrib) obj;
    }

    /**
     * Method removeCompAttrib.
     * 
     * @param vCompAttrib
     * @return true if the object was removed from the collection.
     */
    public boolean removeCompAttrib(
            final CompAttrib vCompAttrib) {
        boolean removed = _compAttribList.remove(vCompAttrib);
        return removed;
    }

    /**
     * Method removeCompAttribAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public CompAttrib removeCompAttribAt(
            final int index) {
        java.lang.Object obj = this._compAttribList.remove(index);
        return (CompAttrib) obj;
    }

    /**
     * Method removeIncludeMbean.
     * 
     * @param vIncludeMbean
     * @return true if the object was removed from the collection.
     */
    public boolean removeIncludeMbean(
            final java.lang.String vIncludeMbean) {
        boolean removed = _includeMbeanList.remove(vIncludeMbean);
        return removed;
    }

    /**
     * Method removeIncludeMbeanAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeIncludeMbeanAt(
            final int index) {
        java.lang.Object obj = this._includeMbeanList.remove(index);
        return (java.lang.String) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vAttrib
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setAttrib(
            final int index,
            final Attrib vAttrib)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._attribList.size()) {
            throw new IndexOutOfBoundsException("setAttrib: Index value '" + index + "' not in range [0.." + (this._attribList.size() - 1) + "]");
        }
        
        this._attribList.set(index, vAttrib);
    }

    /**
     * 
     * 
     * @param vAttribArray
     */
    public void setAttrib(
            final Attrib[] vAttribArray) {
        //-- copy array
        _attribList.clear();
        
        for (int i = 0; i < vAttribArray.length; i++) {
                this._attribList.add(vAttribArray[i]);
        }
    }

    /**
     * Sets the value of '_attribList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vAttribList the Vector to copy.
     */
    public void setAttrib(
            final java.util.List<Attrib> vAttribList) {
        // copy vector
        this._attribList.clear();
        
        this._attribList.addAll(vAttribList);
    }

    /**
     * Sets the value of '_attribList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param attribList the Vector to set.
     */
    public void setAttribCollection(
            final java.util.List<Attrib> attribList) {
        this._attribList = attribList;
    }

    /**
     * 
     * 
     * @param index
     * @param vCompAttrib
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setCompAttrib(
            final int index,
            final CompAttrib vCompAttrib)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._compAttribList.size()) {
            throw new IndexOutOfBoundsException("setCompAttrib: Index value '" + index + "' not in range [0.." + (this._compAttribList.size() - 1) + "]");
        }
        
        this._compAttribList.set(index, vCompAttrib);
    }

    /**
     * 
     * 
     * @param vCompAttribArray
     */
    public void setCompAttrib(
            final CompAttrib[] vCompAttribArray) {
        //-- copy array
        _compAttribList.clear();
        
        for (int i = 0; i < vCompAttribArray.length; i++) {
                this._compAttribList.add(vCompAttribArray[i]);
        }
    }

    /**
     * Sets the value of '_compAttribList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vCompAttribList the Vector to copy.
     */
    public void setCompAttrib(
            final java.util.List<CompAttrib> vCompAttribList) {
        // copy vector
        this._compAttribList.clear();
        
        this._compAttribList.addAll(vCompAttribList);
    }

    /**
     * Sets the value of '_compAttribList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param compAttribList the Vector to set.
     */
    public void setCompAttribCollection(
            final java.util.List<CompAttrib> compAttribList) {
        this._compAttribList = compAttribList;
    }

    /**
     * Sets the value of field 'exclude'.
     * 
     * @param exclude the value of field 'exclude'.
     */
    public void setExclude(
            final java.lang.String exclude) {
        this._exclude = exclude;
    }

    /**
     * 
     * 
     * @param index
     * @param vIncludeMbean
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setIncludeMbean(
            final int index,
            final java.lang.String vIncludeMbean)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._includeMbeanList.size()) {
            throw new IndexOutOfBoundsException("setIncludeMbean: Index value '" + index + "' not in range [0.." + (this._includeMbeanList.size() - 1) + "]");
        }
        
        this._includeMbeanList.set(index, vIncludeMbean);
    }

    /**
     * 
     * 
     * @param vIncludeMbeanArray
     */
    public void setIncludeMbean(
            final java.lang.String[] vIncludeMbeanArray) {
        //-- copy array
        _includeMbeanList.clear();
        
        for (int i = 0; i < vIncludeMbeanArray.length; i++) {
                this._includeMbeanList.add(vIncludeMbeanArray[i]);
        }
    }

    /**
     * Sets the value of '_includeMbeanList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vIncludeMbeanList the Vector to copy.
     */
    public void setIncludeMbean(
            final java.util.List<java.lang.String> vIncludeMbeanList) {
        // copy vector
        this._includeMbeanList.clear();
        
        this._includeMbeanList.addAll(vIncludeMbeanList);
    }

    /**
     * Sets the value of '_includeMbeanList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param includeMbeanList the Vector to set.
     */
    public void setIncludeMbeanCollection(
            final java.util.List<java.lang.String> includeMbeanList) {
        this._includeMbeanList = includeMbeanList;
    }

    /**
     * Sets the value of field 'keyAlias'.
     * 
     * @param keyAlias the value of field 'keyAlias'.
     */
    public void setKeyAlias(
            final java.lang.String keyAlias) {
        this._keyAlias = keyAlias;
    }

    /**
     * Sets the value of field 'keyfield'.
     * 
     * @param keyfield the value of field 'keyfield'.
     */
    public void setKeyfield(
            final java.lang.String keyfield) {
        this._keyfield = keyfield;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(
            final java.lang.String name) {
        this._name = name;
    }

    /**
     * Sets the value of field 'objectname'.
     * 
     * @param objectname the value of field 'objectname'.
     */
    public void setObjectname(
            final java.lang.String objectname) {
        this._objectname = objectname;
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
     * org.opennms.netmgt.config.collectd.jmx.Mbean
     */
    @Deprecated
    public static Mbean unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (Mbean) Unmarshaller.unmarshal(Mbean.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    @Deprecated
    public void validate(
    )
    throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
