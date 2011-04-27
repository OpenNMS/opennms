/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.snmp;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Provides a mechanism for associating one or more specific
 *  IP addresses and/or IP address ranges with a set of SNMP parms
 * which
 *  will be used in place of the default values during SNMP data
 *  collection.
 * 
 * @version $Revision$ $Date$
 */

@SuppressWarnings("all") public class Definition extends org.opennms.netmgt.config.snmp.Configuration 
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * IP address range to which this definition
     *  applies.
     */
    private java.util.List<org.opennms.netmgt.config.snmp.Range> _rangeList;

    /**
     * Specific IP address to which this definition
     *  applies.
     */
    private java.util.List<java.lang.String> _specificList;

    /**
     * Match Octets (as in IPLIKE)
     */
    private java.util.List<java.lang.String> _ipMatchList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Definition() {
        super();
        this._rangeList = new java.util.ArrayList<org.opennms.netmgt.config.snmp.Range>();
        this._specificList = new java.util.ArrayList<java.lang.String>();
        this._ipMatchList = new java.util.ArrayList<java.lang.String>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vIpMatch
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIpMatch(
            final java.lang.String vIpMatch)
    throws java.lang.IndexOutOfBoundsException {
        this._ipMatchList.add(vIpMatch);
    }

    /**
     * 
     * 
     * @param index
     * @param vIpMatch
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIpMatch(
            final int index,
            final java.lang.String vIpMatch)
    throws java.lang.IndexOutOfBoundsException {
        this._ipMatchList.add(index, vIpMatch);
    }

    /**
     * 
     * 
     * @param vRange
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRange(
            final org.opennms.netmgt.config.snmp.Range vRange)
    throws java.lang.IndexOutOfBoundsException {
        this._rangeList.add(vRange);
    }

    /**
     * 
     * 
     * @param index
     * @param vRange
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRange(
            final int index,
            final org.opennms.netmgt.config.snmp.Range vRange)
    throws java.lang.IndexOutOfBoundsException {
        this._rangeList.add(index, vRange);
    }

    /**
     * 
     * 
     * @param vSpecific
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSpecific(
            final java.lang.String vSpecific)
    throws java.lang.IndexOutOfBoundsException {
        this._specificList.add(vSpecific);
    }

    /**
     * 
     * 
     * @param index
     * @param vSpecific
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSpecific(
            final int index,
            final java.lang.String vSpecific)
    throws java.lang.IndexOutOfBoundsException {
        this._specificList.add(index, vSpecific);
    }

    /**
     * Method enumerateIpMatch.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<java.lang.String> enumerateIpMatch(
    ) {
        return java.util.Collections.enumeration(this._ipMatchList);
    }

    /**
     * Method enumerateRange.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.snmp.Range> enumerateRange(
    ) {
        return java.util.Collections.enumeration(this._rangeList);
    }

    /**
     * Method enumerateSpecific.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<java.lang.String> enumerateSpecific(
    ) {
        return java.util.Collections.enumeration(this._specificList);
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
        
        if (super.equals(obj)==false)
            return false;
        
        if (obj instanceof Definition) {
        
            Definition temp = (Definition)obj;
            if (this._rangeList != null) {
                if (temp._rangeList == null) return false;
                else if (!(this._rangeList.equals(temp._rangeList))) 
                    return false;
            }
            else if (temp._rangeList != null)
                return false;
            if (this._specificList != null) {
                if (temp._specificList == null) return false;
                else if (!(this._specificList.equals(temp._specificList))) 
                    return false;
            }
            else if (temp._specificList != null)
                return false;
            if (this._ipMatchList != null) {
                if (temp._ipMatchList == null) return false;
                else if (!(this._ipMatchList.equals(temp._ipMatchList))) 
                    return false;
            }
            else if (temp._ipMatchList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getIpMatch.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the java.lang.String at the given index
     */
    public java.lang.String getIpMatch(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._ipMatchList.size()) {
            throw new IndexOutOfBoundsException("getIpMatch: Index value '" + index + "' not in range [0.." + (this._ipMatchList.size() - 1) + "]");
        }
        
        return (java.lang.String) _ipMatchList.get(index);
    }

    /**
     * Method getIpMatch.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public java.lang.String[] getIpMatch(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._ipMatchList.toArray(array);
    }

    /**
     * Method getIpMatchCollection.Returns a reference to
     * '_ipMatchList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<java.lang.String> getIpMatchCollection(
    ) {
        return this._ipMatchList;
    }

    /**
     * Method getIpMatchCount.
     * 
     * @return the size of this collection
     */
    public int getIpMatchCount(
    ) {
        return this._ipMatchList.size();
    }

    /**
     * Method getRange.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.config.snmp.Range at the given index
     */
    public org.opennms.netmgt.config.snmp.Range getRange(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._rangeList.size()) {
            throw new IndexOutOfBoundsException("getRange: Index value '" + index + "' not in range [0.." + (this._rangeList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.config.snmp.Range) _rangeList.get(index);
    }

    /**
     * Method getRange.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.snmp.Range[] getRange(
    ) {
        org.opennms.netmgt.config.snmp.Range[] array = new org.opennms.netmgt.config.snmp.Range[0];
        return (org.opennms.netmgt.config.snmp.Range[]) this._rangeList.toArray(array);
    }

    /**
     * Method getRangeCollection.Returns a reference to
     * '_rangeList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.snmp.Range> getRangeCollection(
    ) {
        return this._rangeList;
    }

    /**
     * Method getRangeCount.
     * 
     * @return the size of this collection
     */
    public int getRangeCount(
    ) {
        return this._rangeList.size();
    }

    /**
     * Method getSpecific.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the java.lang.String at the given index
     */
    public java.lang.String getSpecific(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._specificList.size()) {
            throw new IndexOutOfBoundsException("getSpecific: Index value '" + index + "' not in range [0.." + (this._specificList.size() - 1) + "]");
        }
        
        return (java.lang.String) _specificList.get(index);
    }

    /**
     * Method getSpecific.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public java.lang.String[] getSpecific(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._specificList.toArray(array);
    }

    /**
     * Method getSpecificCollection.Returns a reference to
     * '_specificList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<java.lang.String> getSpecificCollection(
    ) {
        return this._specificList;
    }

    /**
     * Method getSpecificCount.
     * 
     * @return the size of this collection
     */
    public int getSpecificCount(
    ) {
        return this._specificList.size();
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode(
    ) {
        int result = 17;
        
        long tmp;
        if (_rangeList != null) {
           result = 37 * result + _rangeList.hashCode();
        }
        if (_specificList != null) {
           result = 37 * result + _specificList.hashCode();
        }
        if (_ipMatchList != null) {
           result = 37 * result + _ipMatchList.hashCode();
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
     * Method iterateIpMatch.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<java.lang.String> iterateIpMatch(
    ) {
        return this._ipMatchList.iterator();
    }

    /**
     * Method iterateRange.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.snmp.Range> iterateRange(
    ) {
        return this._rangeList.iterator();
    }

    /**
     * Method iterateSpecific.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<java.lang.String> iterateSpecific(
    ) {
        return this._specificList.iterator();
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
    public void removeAllIpMatch(
    ) {
        this._ipMatchList.clear();
    }

    /**
     */
    public void removeAllRange(
    ) {
        this._rangeList.clear();
    }

    /**
     */
    public void removeAllSpecific(
    ) {
        this._specificList.clear();
    }

    /**
     * Method removeIpMatch.
     * 
     * @param vIpMatch
     * @return true if the object was removed from the collection.
     */
    public boolean removeIpMatch(
            final java.lang.String vIpMatch) {
        boolean removed = _ipMatchList.remove(vIpMatch);
        return removed;
    }

    /**
     * Method removeIpMatchAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeIpMatchAt(
            final int index) {
        java.lang.Object obj = this._ipMatchList.remove(index);
        return (java.lang.String) obj;
    }

    /**
     * Method removeRange.
     * 
     * @param vRange
     * @return true if the object was removed from the collection.
     */
    public boolean removeRange(
            final org.opennms.netmgt.config.snmp.Range vRange) {
        boolean removed = _rangeList.remove(vRange);
        return removed;
    }

    /**
     * Method removeRangeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.snmp.Range removeRangeAt(
            final int index) {
        java.lang.Object obj = this._rangeList.remove(index);
        return (org.opennms.netmgt.config.snmp.Range) obj;
    }

    /**
     * Method removeSpecific.
     * 
     * @param vSpecific
     * @return true if the object was removed from the collection.
     */
    public boolean removeSpecific(
            final java.lang.String vSpecific) {
        boolean removed = _specificList.remove(vSpecific);
        return removed;
    }

    /**
     * Method removeSpecificAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeSpecificAt(
            final int index) {
        java.lang.Object obj = this._specificList.remove(index);
        return (java.lang.String) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vIpMatch
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setIpMatch(
            final int index,
            final java.lang.String vIpMatch)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._ipMatchList.size()) {
            throw new IndexOutOfBoundsException("setIpMatch: Index value '" + index + "' not in range [0.." + (this._ipMatchList.size() - 1) + "]");
        }
        
        this._ipMatchList.set(index, vIpMatch);
    }

    /**
     * 
     * 
     * @param vIpMatchArray
     */
    public void setIpMatch(
            final java.lang.String[] vIpMatchArray) {
        //-- copy array
        _ipMatchList.clear();
        
        for (int i = 0; i < vIpMatchArray.length; i++) {
                this._ipMatchList.add(vIpMatchArray[i]);
        }
    }

    /**
     * Sets the value of '_ipMatchList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vIpMatchList the Vector to copy.
     */
    public void setIpMatch(
            final java.util.List<java.lang.String> vIpMatchList) {
        // copy vector
        this._ipMatchList.clear();
        
        this._ipMatchList.addAll(vIpMatchList);
    }

    /**
     * Sets the value of '_ipMatchList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param ipMatchList the Vector to set.
     */
    public void setIpMatchCollection(
            final java.util.List<java.lang.String> ipMatchList) {
        this._ipMatchList = ipMatchList;
    }

    /**
     * 
     * 
     * @param index
     * @param vRange
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setRange(
            final int index,
            final org.opennms.netmgt.config.snmp.Range vRange)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._rangeList.size()) {
            throw new IndexOutOfBoundsException("setRange: Index value '" + index + "' not in range [0.." + (this._rangeList.size() - 1) + "]");
        }
        
        this._rangeList.set(index, vRange);
    }

    /**
     * 
     * 
     * @param vRangeArray
     */
    public void setRange(
            final org.opennms.netmgt.config.snmp.Range[] vRangeArray) {
        //-- copy array
        _rangeList.clear();
        
        for (int i = 0; i < vRangeArray.length; i++) {
                this._rangeList.add(vRangeArray[i]);
        }
    }

    /**
     * Sets the value of '_rangeList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vRangeList the Vector to copy.
     */
    public void setRange(
            final java.util.List<org.opennms.netmgt.config.snmp.Range> vRangeList) {
        // copy vector
        this._rangeList.clear();
        
        this._rangeList.addAll(vRangeList);
    }

    /**
     * Sets the value of '_rangeList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param rangeList the Vector to set.
     */
    public void setRangeCollection(
            final java.util.List<org.opennms.netmgt.config.snmp.Range> rangeList) {
        this._rangeList = rangeList;
    }

    /**
     * 
     * 
     * @param index
     * @param vSpecific
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setSpecific(
            final int index,
            final java.lang.String vSpecific)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._specificList.size()) {
            throw new IndexOutOfBoundsException("setSpecific: Index value '" + index + "' not in range [0.." + (this._specificList.size() - 1) + "]");
        }
        
        this._specificList.set(index, vSpecific);
    }

    /**
     * 
     * 
     * @param vSpecificArray
     */
    public void setSpecific(
            final java.lang.String[] vSpecificArray) {
        //-- copy array
        _specificList.clear();
        
        for (int i = 0; i < vSpecificArray.length; i++) {
                this._specificList.add(vSpecificArray[i]);
        }
    }

    /**
     * Sets the value of '_specificList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vSpecificList the Vector to copy.
     */
    public void setSpecific(
            final java.util.List<java.lang.String> vSpecificList) {
        // copy vector
        this._specificList.clear();
        
        this._specificList.addAll(vSpecificList);
    }

    /**
     * Sets the value of '_specificList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param specificList the Vector to set.
     */
    public void setSpecificCollection(
            final java.util.List<java.lang.String> specificList) {
        this._specificList = specificList;
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
     * org.opennms.netmgt.config.snmp.Configuration
     */
    public static org.opennms.netmgt.config.snmp.Configuration unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.config.snmp.Configuration) Unmarshaller.unmarshal(org.opennms.netmgt.config.snmp.Definition.class, reader);
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
