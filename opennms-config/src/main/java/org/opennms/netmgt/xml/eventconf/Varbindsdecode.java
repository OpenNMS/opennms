/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.xml.eventconf;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * This element is used for converting event 
 *  varbind value in static decoded string.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Varbindsdecode implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The identifier of the parameters to be
     *  decoded
     *  
     */
    private java.lang.String _parmid;

    /**
     * The value to string decoding map
     *  
     */
    private java.util.List<org.opennms.netmgt.xml.eventconf.Decode> _decodeList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Varbindsdecode() {
        super();
        this._decodeList = new java.util.ArrayList<org.opennms.netmgt.xml.eventconf.Decode>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vDecode
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addDecode(
            final org.opennms.netmgt.xml.eventconf.Decode vDecode)
    throws java.lang.IndexOutOfBoundsException {
        this._decodeList.add(vDecode);
    }

    /**
     * 
     * 
     * @param index
     * @param vDecode
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addDecode(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Decode vDecode)
    throws java.lang.IndexOutOfBoundsException {
        this._decodeList.add(index, vDecode);
    }

    /**
     * Method enumerateDecode.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.xml.eventconf.Decode> enumerateDecode(
    ) {
        return java.util.Collections.enumeration(this._decodeList);
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
        
        if (obj instanceof Varbindsdecode) {
        
            Varbindsdecode temp = (Varbindsdecode)obj;
            if (this._parmid != null) {
                if (temp._parmid == null) return false;
                else if (!(this._parmid.equals(temp._parmid))) 
                    return false;
            }
            else if (temp._parmid != null)
                return false;
            if (this._decodeList != null) {
                if (temp._decodeList == null) return false;
                else if (!(this._decodeList.equals(temp._decodeList))) 
                    return false;
            }
            else if (temp._decodeList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getDecode.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.xml.eventconf.Decode at the given index
     */
    public org.opennms.netmgt.xml.eventconf.Decode getDecode(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._decodeList.size()) {
            throw new IndexOutOfBoundsException("getDecode: Index value '" + index + "' not in range [0.." + (this._decodeList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.xml.eventconf.Decode) _decodeList.get(index);
    }

    /**
     * Method getDecode.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.xml.eventconf.Decode[] getDecode(
    ) {
        org.opennms.netmgt.xml.eventconf.Decode[] array = new org.opennms.netmgt.xml.eventconf.Decode[0];
        return (org.opennms.netmgt.xml.eventconf.Decode[]) this._decodeList.toArray(array);
    }

    /**
     * Method getDecodeCollection.Returns a reference to
     * '_decodeList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.xml.eventconf.Decode> getDecodeCollection(
    ) {
        return this._decodeList;
    }

    /**
     * Method getDecodeCount.
     * 
     * @return the size of this collection
     */
    public int getDecodeCount(
    ) {
        return this._decodeList.size();
    }

    /**
     * Returns the value of field 'parmid'. The field 'parmid' has
     * the following description: The identifier of the parameters
     * to be
     *  decoded
     *  
     * 
     * @return the value of field 'Parmid'.
     */
    public java.lang.String getParmid(
    ) {
        return this._parmid;
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
        if (_parmid != null) {
           result = 37 * result + _parmid.hashCode();
        }
        if (_decodeList != null) {
           result = 37 * result + _decodeList.hashCode();
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
     * Method iterateDecode.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.xml.eventconf.Decode> iterateDecode(
    ) {
        return this._decodeList.iterator();
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
    public void removeAllDecode(
    ) {
        this._decodeList.clear();
    }

    /**
     * Method removeDecode.
     * 
     * @param vDecode
     * @return true if the object was removed from the collection.
     */
    public boolean removeDecode(
            final org.opennms.netmgt.xml.eventconf.Decode vDecode) {
        boolean removed = _decodeList.remove(vDecode);
        return removed;
    }

    /**
     * Method removeDecodeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.xml.eventconf.Decode removeDecodeAt(
            final int index) {
        java.lang.Object obj = this._decodeList.remove(index);
        return (org.opennms.netmgt.xml.eventconf.Decode) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vDecode
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setDecode(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Decode vDecode)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._decodeList.size()) {
            throw new IndexOutOfBoundsException("setDecode: Index value '" + index + "' not in range [0.." + (this._decodeList.size() - 1) + "]");
        }
        
        this._decodeList.set(index, vDecode);
    }

    /**
     * 
     * 
     * @param vDecodeArray
     */
    public void setDecode(
            final org.opennms.netmgt.xml.eventconf.Decode[] vDecodeArray) {
        //-- copy array
        _decodeList.clear();
        
        for (int i = 0; i < vDecodeArray.length; i++) {
                this._decodeList.add(vDecodeArray[i]);
        }
    }

    /**
     * Sets the value of '_decodeList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vDecodeList the Vector to copy.
     */
    public void setDecode(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Decode> vDecodeList) {
        // copy vector
        this._decodeList.clear();
        
        this._decodeList.addAll(vDecodeList);
    }

    /**
     * Sets the value of '_decodeList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param decodeList the Vector to set.
     */
    public void setDecodeCollection(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Decode> decodeList) {
        this._decodeList = decodeList;
    }

    /**
     * Sets the value of field 'parmid'. The field 'parmid' has the
     * following description: The identifier of the parameters to
     * be
     *  decoded
     *  
     * 
     * @param parmid the value of field 'parmid'.
     */
    public void setParmid(
            final java.lang.String parmid) {
        this._parmid = parmid;
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
     * org.opennms.netmgt.xml.eventconf.Varbindsdecode
     */
    public static org.opennms.netmgt.xml.eventconf.Varbindsdecode unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.Varbindsdecode) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.Varbindsdecode.class, reader);
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
