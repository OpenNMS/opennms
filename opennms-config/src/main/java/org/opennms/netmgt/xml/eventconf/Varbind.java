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
 * The varbind element
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Varbind implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _textualConvention.
     */
    private java.lang.String _textualConvention;

    /**
     * The varbind element number
     */
    private int _vbnumber;

    /**
     * keeps track of state for field: _vbnumber
     */
    private boolean _has_vbnumber;

    /**
     * The varbind element value
     */
    private java.util.List<java.lang.String> _vbvalueList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Varbind() {
        super();
        this._vbvalueList = new java.util.ArrayList<java.lang.String>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vVbvalue
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addVbvalue(
            final java.lang.String vVbvalue)
    throws java.lang.IndexOutOfBoundsException {
        this._vbvalueList.add(vVbvalue);
    }

    /**
     * 
     * 
     * @param index
     * @param vVbvalue
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addVbvalue(
            final int index,
            final java.lang.String vVbvalue)
    throws java.lang.IndexOutOfBoundsException {
        this._vbvalueList.add(index, vVbvalue);
    }

    /**
     */
    public void deleteVbnumber(
    ) {
        this._has_vbnumber= false;
    }

    /**
     * Method enumerateVbvalue.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<java.lang.String> enumerateVbvalue(
    ) {
        return java.util.Collections.enumeration(this._vbvalueList);
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
        
        if (obj instanceof Varbind) {
        
            Varbind temp = (Varbind)obj;
            if (this._textualConvention != null) {
                if (temp._textualConvention == null) return false;
                else if (!(this._textualConvention.equals(temp._textualConvention))) 
                    return false;
            }
            else if (temp._textualConvention != null)
                return false;
            if (this._vbnumber != temp._vbnumber)
                return false;
            if (this._has_vbnumber != temp._has_vbnumber)
                return false;
            if (this._vbvalueList != null) {
                if (temp._vbvalueList == null) return false;
                else if (!(this._vbvalueList.equals(temp._vbvalueList))) 
                    return false;
            }
            else if (temp._vbvalueList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'textualConvention'.
     * 
     * @return the value of field 'TextualConvention'.
     */
    public java.lang.String getTextualConvention(
    ) {
        return this._textualConvention;
    }

    /**
     * Returns the value of field 'vbnumber'. The field 'vbnumber'
     * has the following description: The varbind element number
     * 
     * @return the value of field 'Vbnumber'.
     */
    public int getVbnumber(
    ) {
        return this._vbnumber;
    }

    /**
     * Method getVbvalue.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the java.lang.String at the given index
     */
    public java.lang.String getVbvalue(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._vbvalueList.size()) {
            throw new IndexOutOfBoundsException("getVbvalue: Index value '" + index + "' not in range [0.." + (this._vbvalueList.size() - 1) + "]");
        }
        
        return (java.lang.String) _vbvalueList.get(index);
    }

    /**
     * Method getVbvalue.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public java.lang.String[] getVbvalue(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._vbvalueList.toArray(array);
    }

    /**
     * Method getVbvalueCollection.Returns a reference to
     * '_vbvalueList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<java.lang.String> getVbvalueCollection(
    ) {
        return this._vbvalueList;
    }

    /**
     * Method getVbvalueCount.
     * 
     * @return the size of this collection
     */
    public int getVbvalueCount(
    ) {
        return this._vbvalueList.size();
    }

    /**
     * Method hasVbnumber.
     * 
     * @return true if at least one Vbnumber has been added
     */
    public boolean hasVbnumber(
    ) {
        return this._has_vbnumber;
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
        if (_textualConvention != null) {
           result = 37 * result + _textualConvention.hashCode();
        }
        result = 37 * result + _vbnumber;
        if (_vbvalueList != null) {
           result = 37 * result + _vbvalueList.hashCode();
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
     * Method iterateVbvalue.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<java.lang.String> iterateVbvalue(
    ) {
        return this._vbvalueList.iterator();
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
    public void removeAllVbvalue(
    ) {
        this._vbvalueList.clear();
    }

    /**
     * Method removeVbvalue.
     * 
     * @param vVbvalue
     * @return true if the object was removed from the collection.
     */
    public boolean removeVbvalue(
            final java.lang.String vVbvalue) {
        boolean removed = _vbvalueList.remove(vVbvalue);
        return removed;
    }

    /**
     * Method removeVbvalueAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeVbvalueAt(
            final int index) {
        java.lang.Object obj = this._vbvalueList.remove(index);
        return (java.lang.String) obj;
    }

    /**
     * Sets the value of field 'textualConvention'.
     * 
     * @param textualConvention the value of field
     * 'textualConvention'.
     */
    public void setTextualConvention(
            final java.lang.String textualConvention) {
        this._textualConvention = textualConvention;
    }

    /**
     * Sets the value of field 'vbnumber'. The field 'vbnumber' has
     * the following description: The varbind element number
     * 
     * @param vbnumber the value of field 'vbnumber'.
     */
    public void setVbnumber(
            final int vbnumber) {
        this._vbnumber = vbnumber;
        this._has_vbnumber = true;
    }

    /**
     * 
     * 
     * @param index
     * @param vVbvalue
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setVbvalue(
            final int index,
            final java.lang.String vVbvalue)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._vbvalueList.size()) {
            throw new IndexOutOfBoundsException("setVbvalue: Index value '" + index + "' not in range [0.." + (this._vbvalueList.size() - 1) + "]");
        }
        
        this._vbvalueList.set(index, vVbvalue);
    }

    /**
     * 
     * 
     * @param vVbvalueArray
     */
    public void setVbvalue(
            final java.lang.String[] vVbvalueArray) {
        //-- copy array
        _vbvalueList.clear();
        
        for (int i = 0; i < vVbvalueArray.length; i++) {
                this._vbvalueList.add(vVbvalueArray[i]);
        }
    }

    /**
     * Sets the value of '_vbvalueList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vVbvalueList the Vector to copy.
     */
    public void setVbvalue(
            final java.util.List<java.lang.String> vVbvalueList) {
        // copy vector
        this._vbvalueList.clear();
        
        this._vbvalueList.addAll(vVbvalueList);
    }

    /**
     * Sets the value of '_vbvalueList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param vbvalueList the Vector to set.
     */
    public void setVbvalueCollection(
            final java.util.List<java.lang.String> vbvalueList) {
        this._vbvalueList = vbvalueList;
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
     * org.opennms.netmgt.xml.eventconf.Varbind
     */
    public static org.opennms.netmgt.xml.eventconf.Varbind unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.Varbind) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.Varbind.class, reader);
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
