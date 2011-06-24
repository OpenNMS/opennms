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
 * Security settings for this configuration
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Security implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Event element whose value cannot be overridden by a
     *  value in an incoming event
     */
    private java.util.List<java.lang.String> _doNotOverrideList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Security() {
        super();
        this._doNotOverrideList = new java.util.ArrayList<java.lang.String>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vDoNotOverride
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addDoNotOverride(
            final java.lang.String vDoNotOverride)
    throws java.lang.IndexOutOfBoundsException {
        this._doNotOverrideList.add(vDoNotOverride);
    }

    /**
     * 
     * 
     * @param index
     * @param vDoNotOverride
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addDoNotOverride(
            final int index,
            final java.lang.String vDoNotOverride)
    throws java.lang.IndexOutOfBoundsException {
        this._doNotOverrideList.add(index, vDoNotOverride);
    }

    /**
     * Method enumerateDoNotOverride.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<java.lang.String> enumerateDoNotOverride(
    ) {
        return java.util.Collections.enumeration(this._doNotOverrideList);
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
        
        if (obj instanceof Security) {
        
            Security temp = (Security)obj;
            if (this._doNotOverrideList != null) {
                if (temp._doNotOverrideList == null) return false;
                else if (!(this._doNotOverrideList.equals(temp._doNotOverrideList))) 
                    return false;
            }
            else if (temp._doNotOverrideList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getDoNotOverride.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the java.lang.String at the given index
     */
    public java.lang.String getDoNotOverride(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._doNotOverrideList.size()) {
            throw new IndexOutOfBoundsException("getDoNotOverride: Index value '" + index + "' not in range [0.." + (this._doNotOverrideList.size() - 1) + "]");
        }
        
        return (java.lang.String) _doNotOverrideList.get(index);
    }

    /**
     * Method getDoNotOverride.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public java.lang.String[] getDoNotOverride(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._doNotOverrideList.toArray(array);
    }

    /**
     * Method getDoNotOverrideCollection.Returns a reference to
     * '_doNotOverrideList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<java.lang.String> getDoNotOverrideCollection(
    ) {
        return this._doNotOverrideList;
    }

    /**
     * Method getDoNotOverrideCount.
     * 
     * @return the size of this collection
     */
    public int getDoNotOverrideCount(
    ) {
        return this._doNotOverrideList.size();
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
        if (_doNotOverrideList != null) {
           result = 37 * result + _doNotOverrideList.hashCode();
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
     * Method iterateDoNotOverride.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<java.lang.String> iterateDoNotOverride(
    ) {
        return this._doNotOverrideList.iterator();
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
    public void removeAllDoNotOverride(
    ) {
        this._doNotOverrideList.clear();
    }

    /**
     * Method removeDoNotOverride.
     * 
     * @param vDoNotOverride
     * @return true if the object was removed from the collection.
     */
    public boolean removeDoNotOverride(
            final java.lang.String vDoNotOverride) {
        boolean removed = _doNotOverrideList.remove(vDoNotOverride);
        return removed;
    }

    /**
     * Method removeDoNotOverrideAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeDoNotOverrideAt(
            final int index) {
        java.lang.Object obj = this._doNotOverrideList.remove(index);
        return (java.lang.String) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vDoNotOverride
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setDoNotOverride(
            final int index,
            final java.lang.String vDoNotOverride)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._doNotOverrideList.size()) {
            throw new IndexOutOfBoundsException("setDoNotOverride: Index value '" + index + "' not in range [0.." + (this._doNotOverrideList.size() - 1) + "]");
        }
        
        this._doNotOverrideList.set(index, vDoNotOverride);
    }

    /**
     * 
     * 
     * @param vDoNotOverrideArray
     */
    public void setDoNotOverride(
            final java.lang.String[] vDoNotOverrideArray) {
        //-- copy array
        _doNotOverrideList.clear();
        
        for (int i = 0; i < vDoNotOverrideArray.length; i++) {
                this._doNotOverrideList.add(vDoNotOverrideArray[i]);
        }
    }

    /**
     * Sets the value of '_doNotOverrideList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vDoNotOverrideList the Vector to copy.
     */
    public void setDoNotOverride(
            final java.util.List<java.lang.String> vDoNotOverrideList) {
        // copy vector
        this._doNotOverrideList.clear();
        
        this._doNotOverrideList.addAll(vDoNotOverrideList);
    }

    /**
     * Sets the value of '_doNotOverrideList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param doNotOverrideList the Vector to set.
     */
    public void setDoNotOverrideCollection(
            final java.util.List<java.lang.String> doNotOverrideList) {
        this._doNotOverrideList = doNotOverrideList;
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
     * org.opennms.netmgt.xml.eventconf.Security
     */
    public static org.opennms.netmgt.xml.eventconf.Security unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.Security) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.Security.class, reader);
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
