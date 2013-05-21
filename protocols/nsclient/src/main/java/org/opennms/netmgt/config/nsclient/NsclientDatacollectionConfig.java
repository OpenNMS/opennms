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

package org.opennms.netmgt.config.nsclient;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class NsclientDatacollectionConfig.
 * 
 * @version $Revision$ $Date$
 */

@SuppressWarnings("all") public class NsclientDatacollectionConfig implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _rrdRepository.
     */
    private java.lang.String _rrdRepository;

    /**
     * A grouping of Nsclient related RRD parms and Nsclient
     *  object groups
     *  
     */
    private java.util.List<org.opennms.netmgt.config.nsclient.NsclientCollection> _nsclientCollectionList;


      //----------------/
     //- Constructors -/
    //----------------/

    public NsclientDatacollectionConfig() {
        super();
        this._nsclientCollectionList = new java.util.ArrayList<org.opennms.netmgt.config.nsclient.NsclientCollection>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vNsclientCollection
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addNsclientCollection(
            final org.opennms.netmgt.config.nsclient.NsclientCollection vNsclientCollection)
    throws java.lang.IndexOutOfBoundsException {
        this._nsclientCollectionList.add(vNsclientCollection);
    }

    /**
     * 
     * 
     * @param index
     * @param vNsclientCollection
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addNsclientCollection(
            final int index,
            final org.opennms.netmgt.config.nsclient.NsclientCollection vNsclientCollection)
    throws java.lang.IndexOutOfBoundsException {
        this._nsclientCollectionList.add(index, vNsclientCollection);
    }

    /**
     * Method enumerateNsclientCollection.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.nsclient.NsclientCollection> enumerateNsclientCollection(
    ) {
        return java.util.Collections.enumeration(this._nsclientCollectionList);
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
        
        if (obj instanceof NsclientDatacollectionConfig) {
        
            NsclientDatacollectionConfig temp = (NsclientDatacollectionConfig)obj;
            if (this._rrdRepository != null) {
                if (temp._rrdRepository == null) return false;
                else if (!(this._rrdRepository.equals(temp._rrdRepository))) 
                    return false;
            }
            else if (temp._rrdRepository != null)
                return false;
            if (this._nsclientCollectionList != null) {
                if (temp._nsclientCollectionList == null) return false;
                else if (!(this._nsclientCollectionList.equals(temp._nsclientCollectionList))) 
                    return false;
            }
            else if (temp._nsclientCollectionList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getNsclientCollection.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.config.nsclient.NsclientCollection at the
     * given index
     */
    public org.opennms.netmgt.config.nsclient.NsclientCollection getNsclientCollection(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._nsclientCollectionList.size()) {
            throw new IndexOutOfBoundsException("getNsclientCollection: Index value '" + index + "' not in range [0.." + (this._nsclientCollectionList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.config.nsclient.NsclientCollection) _nsclientCollectionList.get(index);
    }

    /**
     * Method getNsclientCollection.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.nsclient.NsclientCollection[] getNsclientCollection(
    ) {
        org.opennms.netmgt.config.nsclient.NsclientCollection[] array = new org.opennms.netmgt.config.nsclient.NsclientCollection[0];
        return (org.opennms.netmgt.config.nsclient.NsclientCollection[]) this._nsclientCollectionList.toArray(array);
    }

    /**
     * Method getNsclientCollectionCollection.Returns a reference
     * to '_nsclientCollectionList'. No type checking is performed
     * on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.nsclient.NsclientCollection> getNsclientCollectionCollection(
    ) {
        return this._nsclientCollectionList;
    }

    /**
     * Method getNsclientCollectionCount.
     * 
     * @return the size of this collection
     */
    public int getNsclientCollectionCount(
    ) {
        return this._nsclientCollectionList.size();
    }

    /**
     * Returns the value of field 'rrdRepository'.
     * 
     * @return the value of field 'RrdRepository'.
     */
    public java.lang.String getRrdRepository(
    ) {
        return this._rrdRepository;
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
        if (_rrdRepository != null) {
           result = 37 * result + _rrdRepository.hashCode();
        }
        if (_nsclientCollectionList != null) {
           result = 37 * result + _nsclientCollectionList.hashCode();
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
     * Method iterateNsclientCollection.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.nsclient.NsclientCollection> iterateNsclientCollection(
    ) {
        return this._nsclientCollectionList.iterator();
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
    public void removeAllNsclientCollection(
    ) {
        this._nsclientCollectionList.clear();
    }

    /**
     * Method removeNsclientCollection.
     * 
     * @param vNsclientCollection
     * @return true if the object was removed from the collection.
     */
    public boolean removeNsclientCollection(
            final org.opennms.netmgt.config.nsclient.NsclientCollection vNsclientCollection) {
        boolean removed = _nsclientCollectionList.remove(vNsclientCollection);
        return removed;
    }

    /**
     * Method removeNsclientCollectionAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.nsclient.NsclientCollection removeNsclientCollectionAt(
            final int index) {
        java.lang.Object obj = this._nsclientCollectionList.remove(index);
        return (org.opennms.netmgt.config.nsclient.NsclientCollection) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vNsclientCollection
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setNsclientCollection(
            final int index,
            final org.opennms.netmgt.config.nsclient.NsclientCollection vNsclientCollection)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._nsclientCollectionList.size()) {
            throw new IndexOutOfBoundsException("setNsclientCollection: Index value '" + index + "' not in range [0.." + (this._nsclientCollectionList.size() - 1) + "]");
        }
        
        this._nsclientCollectionList.set(index, vNsclientCollection);
    }

    /**
     * 
     * 
     * @param vNsclientCollectionArray
     */
    public void setNsclientCollection(
            final org.opennms.netmgt.config.nsclient.NsclientCollection[] vNsclientCollectionArray) {
        //-- copy array
        _nsclientCollectionList.clear();
        
        for (int i = 0; i < vNsclientCollectionArray.length; i++) {
                this._nsclientCollectionList.add(vNsclientCollectionArray[i]);
        }
    }

    /**
     * Sets the value of '_nsclientCollectionList' by copying the
     * given Vector. All elements will be checked for type safety.
     * 
     * @param vNsclientCollectionList the Vector to copy.
     */
    public void setNsclientCollection(
            final java.util.List<org.opennms.netmgt.config.nsclient.NsclientCollection> vNsclientCollectionList) {
        // copy vector
        this._nsclientCollectionList.clear();
        
        this._nsclientCollectionList.addAll(vNsclientCollectionList);
    }

    /**
     * Sets the value of '_nsclientCollectionList' by setting it to
     * the given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param nsclientCollectionList the Vector to set.
     */
    public void setNsclientCollectionCollection(
            final java.util.List<org.opennms.netmgt.config.nsclient.NsclientCollection> nsclientCollectionList) {
        this._nsclientCollectionList = nsclientCollectionList;
    }

    /**
     * Sets the value of field 'rrdRepository'.
     * 
     * @param rrdRepository the value of field 'rrdRepository'.
     */
    public void setRrdRepository(
            final java.lang.String rrdRepository) {
        this._rrdRepository = rrdRepository;
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
     * org.opennms.netmgt.config.nsclient.NsclientDatacollectionConfig
     */
    public static org.opennms.netmgt.config.nsclient.NsclientDatacollectionConfig unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.config.nsclient.NsclientDatacollectionConfig) Unmarshaller.unmarshal(org.opennms.netmgt.config.nsclient.NsclientDatacollectionConfig.class, reader);
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
