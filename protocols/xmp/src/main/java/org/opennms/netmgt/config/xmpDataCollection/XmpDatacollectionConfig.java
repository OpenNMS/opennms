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
 * Top-level element for the xmp-datacollection-config.xml
 *  configuration file.
 * 
 * @version $Revision$ $Date$
 */

@SuppressWarnings("all") public class XmpDatacollectionConfig implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * full path to the RRD repository for collected XMP
     *  data
     */
    private java.lang.String _rrdRepository;

    /**
     * XMP data collection element
     */
    private java.util.List<org.opennms.netmgt.config.xmpDataCollection.XmpCollection> _xmpCollectionList;


      //----------------/
     //- Constructors -/
    //----------------/

    public XmpDatacollectionConfig() {
        super();
        this._xmpCollectionList = new java.util.ArrayList<org.opennms.netmgt.config.xmpDataCollection.XmpCollection>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vXmpCollection
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addXmpCollection(
            final org.opennms.netmgt.config.xmpDataCollection.XmpCollection vXmpCollection)
    throws java.lang.IndexOutOfBoundsException {
        this._xmpCollectionList.add(vXmpCollection);
    }

    /**
     * 
     * 
     * @param index
     * @param vXmpCollection
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addXmpCollection(
            final int index,
            final org.opennms.netmgt.config.xmpDataCollection.XmpCollection vXmpCollection)
    throws java.lang.IndexOutOfBoundsException {
        this._xmpCollectionList.add(index, vXmpCollection);
    }

    /**
     * Method enumerateXmpCollection.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.xmpDataCollection.XmpCollection> enumerateXmpCollection(
    ) {
        return java.util.Collections.enumeration(this._xmpCollectionList);
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
        
        if (obj instanceof XmpDatacollectionConfig) {
        
            XmpDatacollectionConfig temp = (XmpDatacollectionConfig)obj;
            if (this._rrdRepository != null) {
                if (temp._rrdRepository == null) return false;
                else if (!(this._rrdRepository.equals(temp._rrdRepository))) 
                    return false;
            }
            else if (temp._rrdRepository != null)
                return false;
            if (this._xmpCollectionList != null) {
                if (temp._xmpCollectionList == null) return false;
                else if (!(this._xmpCollectionList.equals(temp._xmpCollectionList))) 
                    return false;
            }
            else if (temp._xmpCollectionList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'rrdRepository'. The field
     * 'rrdRepository' has the following description: full path to
     * the RRD repository for collected XMP
     *  data
     * 
     * @return the value of field 'RrdRepository'.
     */
    public java.lang.String getRrdRepository(
    ) {
        return this._rrdRepository;
    }

    /**
     * Method getXmpCollection.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.config.xmpDataCollection.XmpCollection at
     * the given index
     */
    public org.opennms.netmgt.config.xmpDataCollection.XmpCollection getXmpCollection(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._xmpCollectionList.size()) {
            throw new IndexOutOfBoundsException("getXmpCollection: Index value '" + index + "' not in range [0.." + (this._xmpCollectionList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.config.xmpDataCollection.XmpCollection) _xmpCollectionList.get(index);
    }

    /**
     * Method getXmpCollection.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.xmpDataCollection.XmpCollection[] getXmpCollection(
    ) {
        org.opennms.netmgt.config.xmpDataCollection.XmpCollection[] array = new org.opennms.netmgt.config.xmpDataCollection.XmpCollection[0];
        return (org.opennms.netmgt.config.xmpDataCollection.XmpCollection[]) this._xmpCollectionList.toArray(array);
    }

    /**
     * Method getXmpCollectionCollection.Returns a reference to
     * '_xmpCollectionList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.xmpDataCollection.XmpCollection> getXmpCollectionCollection(
    ) {
        return this._xmpCollectionList;
    }

    /**
     * Method getXmpCollectionCount.
     * 
     * @return the size of this collection
     */
    public int getXmpCollectionCount(
    ) {
        return this._xmpCollectionList.size();
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
        if (_xmpCollectionList != null) {
           result = 37 * result + _xmpCollectionList.hashCode();
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
     * Method iterateXmpCollection.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.xmpDataCollection.XmpCollection> iterateXmpCollection(
    ) {
        return this._xmpCollectionList.iterator();
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
    public void removeAllXmpCollection(
    ) {
        this._xmpCollectionList.clear();
    }

    /**
     * Method removeXmpCollection.
     * 
     * @param vXmpCollection
     * @return true if the object was removed from the collection.
     */
    public boolean removeXmpCollection(
            final org.opennms.netmgt.config.xmpDataCollection.XmpCollection vXmpCollection) {
        boolean removed = _xmpCollectionList.remove(vXmpCollection);
        return removed;
    }

    /**
     * Method removeXmpCollectionAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.xmpDataCollection.XmpCollection removeXmpCollectionAt(
            final int index) {
        java.lang.Object obj = this._xmpCollectionList.remove(index);
        return (org.opennms.netmgt.config.xmpDataCollection.XmpCollection) obj;
    }

    /**
     * Sets the value of field 'rrdRepository'. The field
     * 'rrdRepository' has the following description: full path to
     * the RRD repository for collected XMP
     *  data
     * 
     * @param rrdRepository the value of field 'rrdRepository'.
     */
    public void setRrdRepository(
            final java.lang.String rrdRepository) {
        this._rrdRepository = rrdRepository;
    }

    /**
     * 
     * 
     * @param index
     * @param vXmpCollection
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setXmpCollection(
            final int index,
            final org.opennms.netmgt.config.xmpDataCollection.XmpCollection vXmpCollection)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._xmpCollectionList.size()) {
            throw new IndexOutOfBoundsException("setXmpCollection: Index value '" + index + "' not in range [0.." + (this._xmpCollectionList.size() - 1) + "]");
        }
        
        this._xmpCollectionList.set(index, vXmpCollection);
    }

    /**
     * 
     * 
     * @param vXmpCollectionArray
     */
    public void setXmpCollection(
            final org.opennms.netmgt.config.xmpDataCollection.XmpCollection[] vXmpCollectionArray) {
        //-- copy array
        _xmpCollectionList.clear();
        
        for (int i = 0; i < vXmpCollectionArray.length; i++) {
                this._xmpCollectionList.add(vXmpCollectionArray[i]);
        }
    }

    /**
     * Sets the value of '_xmpCollectionList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vXmpCollectionList the Vector to copy.
     */
    public void setXmpCollection(
            final java.util.List<org.opennms.netmgt.config.xmpDataCollection.XmpCollection> vXmpCollectionList) {
        // copy vector
        this._xmpCollectionList.clear();
        
        this._xmpCollectionList.addAll(vXmpCollectionList);
    }

    /**
     * Sets the value of '_xmpCollectionList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param xmpCollectionList the Vector to set.
     */
    public void setXmpCollectionCollection(
            final java.util.List<org.opennms.netmgt.config.xmpDataCollection.XmpCollection> xmpCollectionList) {
        this._xmpCollectionList = xmpCollectionList;
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
     * org.opennms.netmgt.config.xmpDataCollection.XmpDatacollectionConfig
     */
    public static org.opennms.netmgt.config.xmpDataCollection.XmpDatacollectionConfig unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.config.xmpDataCollection.XmpDatacollectionConfig) Unmarshaller.unmarshal(org.opennms.netmgt.config.xmpDataCollection.XmpDatacollectionConfig.class, reader);
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
