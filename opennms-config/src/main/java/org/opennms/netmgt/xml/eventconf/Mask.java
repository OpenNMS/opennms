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
 * The Mask for event configuration: The mask contains one
 *  or more 'maskelements' which uniquely identify an event. XXX
 * need to add
 *  information about varbind
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Mask implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The mask element
     */
    private java.util.List<org.opennms.netmgt.xml.eventconf.Maskelement> _maskelementList;

    /**
     * The varbind element
     */
    private java.util.List<org.opennms.netmgt.xml.eventconf.Varbind> _varbindList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Mask() {
        super();
        this._maskelementList = new java.util.ArrayList<org.opennms.netmgt.xml.eventconf.Maskelement>();
        this._varbindList = new java.util.ArrayList<org.opennms.netmgt.xml.eventconf.Varbind>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vMaskelement
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMaskelement(
            final org.opennms.netmgt.xml.eventconf.Maskelement vMaskelement)
    throws java.lang.IndexOutOfBoundsException {
        this._maskelementList.add(vMaskelement);
    }

    /**
     * 
     * 
     * @param index
     * @param vMaskelement
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMaskelement(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Maskelement vMaskelement)
    throws java.lang.IndexOutOfBoundsException {
        this._maskelementList.add(index, vMaskelement);
    }

    /**
     * 
     * 
     * @param vVarbind
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addVarbind(
            final org.opennms.netmgt.xml.eventconf.Varbind vVarbind)
    throws java.lang.IndexOutOfBoundsException {
        this._varbindList.add(vVarbind);
    }

    /**
     * 
     * 
     * @param index
     * @param vVarbind
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addVarbind(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Varbind vVarbind)
    throws java.lang.IndexOutOfBoundsException {
        this._varbindList.add(index, vVarbind);
    }

    /**
     * Method enumerateMaskelement.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.xml.eventconf.Maskelement> enumerateMaskelement(
    ) {
        return java.util.Collections.enumeration(this._maskelementList);
    }

    /**
     * Method enumerateVarbind.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.xml.eventconf.Varbind> enumerateVarbind(
    ) {
        return java.util.Collections.enumeration(this._varbindList);
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
        
        if (obj instanceof Mask) {
        
            Mask temp = (Mask)obj;
            if (this._maskelementList != null) {
                if (temp._maskelementList == null) return false;
                else if (!(this._maskelementList.equals(temp._maskelementList))) 
                    return false;
            }
            else if (temp._maskelementList != null)
                return false;
            if (this._varbindList != null) {
                if (temp._varbindList == null) return false;
                else if (!(this._varbindList.equals(temp._varbindList))) 
                    return false;
            }
            else if (temp._varbindList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getMaskelement.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.xml.eventconf.Maskelement at the given
     * index
     */
    public org.opennms.netmgt.xml.eventconf.Maskelement getMaskelement(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._maskelementList.size()) {
            throw new IndexOutOfBoundsException("getMaskelement: Index value '" + index + "' not in range [0.." + (this._maskelementList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.xml.eventconf.Maskelement) _maskelementList.get(index);
    }

    /**
     * Method getMaskelement.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.xml.eventconf.Maskelement[] getMaskelement(
    ) {
        org.opennms.netmgt.xml.eventconf.Maskelement[] array = new org.opennms.netmgt.xml.eventconf.Maskelement[0];
        return (org.opennms.netmgt.xml.eventconf.Maskelement[]) this._maskelementList.toArray(array);
    }

    /**
     * Method getMaskelementCollection.Returns a reference to
     * '_maskelementList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.xml.eventconf.Maskelement> getMaskelementCollection(
    ) {
        return this._maskelementList;
    }

    /**
     * Method getMaskelementCount.
     * 
     * @return the size of this collection
     */
    public int getMaskelementCount(
    ) {
        return this._maskelementList.size();
    }

    /**
     * Method getVarbind.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.xml.eventconf.Varbind at the given index
     */
    public org.opennms.netmgt.xml.eventconf.Varbind getVarbind(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._varbindList.size()) {
            throw new IndexOutOfBoundsException("getVarbind: Index value '" + index + "' not in range [0.." + (this._varbindList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.xml.eventconf.Varbind) _varbindList.get(index);
    }

    /**
     * Method getVarbind.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.xml.eventconf.Varbind[] getVarbind(
    ) {
        org.opennms.netmgt.xml.eventconf.Varbind[] array = new org.opennms.netmgt.xml.eventconf.Varbind[0];
        return (org.opennms.netmgt.xml.eventconf.Varbind[]) this._varbindList.toArray(array);
    }

    /**
     * Method getVarbindCollection.Returns a reference to
     * '_varbindList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.xml.eventconf.Varbind> getVarbindCollection(
    ) {
        return this._varbindList;
    }

    /**
     * Method getVarbindCount.
     * 
     * @return the size of this collection
     */
    public int getVarbindCount(
    ) {
        return this._varbindList.size();
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
        if (_maskelementList != null) {
           result = 37 * result + _maskelementList.hashCode();
        }
        if (_varbindList != null) {
           result = 37 * result + _varbindList.hashCode();
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
     * Method iterateMaskelement.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.xml.eventconf.Maskelement> iterateMaskelement(
    ) {
        return this._maskelementList.iterator();
    }

    /**
     * Method iterateVarbind.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.xml.eventconf.Varbind> iterateVarbind(
    ) {
        return this._varbindList.iterator();
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
    public void removeAllMaskelement(
    ) {
        this._maskelementList.clear();
    }

    /**
     */
    public void removeAllVarbind(
    ) {
        this._varbindList.clear();
    }

    /**
     * Method removeMaskelement.
     * 
     * @param vMaskelement
     * @return true if the object was removed from the collection.
     */
    public boolean removeMaskelement(
            final org.opennms.netmgt.xml.eventconf.Maskelement vMaskelement) {
        boolean removed = _maskelementList.remove(vMaskelement);
        return removed;
    }

    /**
     * Method removeMaskelementAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.xml.eventconf.Maskelement removeMaskelementAt(
            final int index) {
        java.lang.Object obj = this._maskelementList.remove(index);
        return (org.opennms.netmgt.xml.eventconf.Maskelement) obj;
    }

    /**
     * Method removeVarbind.
     * 
     * @param vVarbind
     * @return true if the object was removed from the collection.
     */
    public boolean removeVarbind(
            final org.opennms.netmgt.xml.eventconf.Varbind vVarbind) {
        boolean removed = _varbindList.remove(vVarbind);
        return removed;
    }

    /**
     * Method removeVarbindAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.xml.eventconf.Varbind removeVarbindAt(
            final int index) {
        java.lang.Object obj = this._varbindList.remove(index);
        return (org.opennms.netmgt.xml.eventconf.Varbind) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vMaskelement
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setMaskelement(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Maskelement vMaskelement)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._maskelementList.size()) {
            throw new IndexOutOfBoundsException("setMaskelement: Index value '" + index + "' not in range [0.." + (this._maskelementList.size() - 1) + "]");
        }
        
        this._maskelementList.set(index, vMaskelement);
    }

    /**
     * 
     * 
     * @param vMaskelementArray
     */
    public void setMaskelement(
            final org.opennms.netmgt.xml.eventconf.Maskelement[] vMaskelementArray) {
        //-- copy array
        _maskelementList.clear();
        
        for (int i = 0; i < vMaskelementArray.length; i++) {
                this._maskelementList.add(vMaskelementArray[i]);
        }
    }

    /**
     * Sets the value of '_maskelementList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vMaskelementList the Vector to copy.
     */
    public void setMaskelement(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Maskelement> vMaskelementList) {
        // copy vector
        this._maskelementList.clear();
        
        this._maskelementList.addAll(vMaskelementList);
    }

    /**
     * Sets the value of '_maskelementList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param maskelementList the Vector to set.
     */
    public void setMaskelementCollection(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Maskelement> maskelementList) {
        this._maskelementList = maskelementList;
    }

    /**
     * 
     * 
     * @param index
     * @param vVarbind
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setVarbind(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Varbind vVarbind)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._varbindList.size()) {
            throw new IndexOutOfBoundsException("setVarbind: Index value '" + index + "' not in range [0.." + (this._varbindList.size() - 1) + "]");
        }
        
        this._varbindList.set(index, vVarbind);
    }

    /**
     * 
     * 
     * @param vVarbindArray
     */
    public void setVarbind(
            final org.opennms.netmgt.xml.eventconf.Varbind[] vVarbindArray) {
        //-- copy array
        _varbindList.clear();
        
        for (int i = 0; i < vVarbindArray.length; i++) {
                this._varbindList.add(vVarbindArray[i]);
        }
    }

    /**
     * Sets the value of '_varbindList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vVarbindList the Vector to copy.
     */
    public void setVarbind(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Varbind> vVarbindList) {
        // copy vector
        this._varbindList.clear();
        
        this._varbindList.addAll(vVarbindList);
    }

    /**
     * Sets the value of '_varbindList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param varbindList the Vector to set.
     */
    public void setVarbindCollection(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Varbind> varbindList) {
        this._varbindList = varbindList;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled org.opennms.netmgt.xml.eventconf.Mask
     */
    public static org.opennms.netmgt.xml.eventconf.Mask unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.Mask) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.Mask.class, reader);
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
