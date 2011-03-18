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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * The Mask for event configuration: The mask contains one
 *  or more 'maskelements' which uniquely identify an event. This
 * can only
 *  include elements from the following subset: uei, source, host,
 * snmphost,
 *  nodeid, interface, service, id(SNMP EID), specific, generic,
 *  community
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="mask")
@XmlAccessorType(XmlAccessType.FIELD)
public class Mask {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The mask element
     */
	@XmlElement(name="maskelement", required=true, nillable = false)
    private java.util.List<org.opennms.netmgt.xml.event.Maskelement> _maskelementList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Mask() {
        super();
        this._maskelementList = new java.util.ArrayList<org.opennms.netmgt.xml.event.Maskelement>();
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
            final org.opennms.netmgt.xml.event.Maskelement vMaskelement)
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
            final org.opennms.netmgt.xml.event.Maskelement vMaskelement)
    throws java.lang.IndexOutOfBoundsException {
        this._maskelementList.add(index, vMaskelement);
    }

    /**
     * Method enumerateMaskelement.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.xml.event.Maskelement> enumerateMaskelement(
    ) {
        return java.util.Collections.enumeration(this._maskelementList);
    }

    /**
     * Method getMaskelement.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.xml.event.Maskelement at the given index
     */
    public org.opennms.netmgt.xml.event.Maskelement getMaskelement(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._maskelementList.size()) {
            throw new IndexOutOfBoundsException("getMaskelement: Index value '" + index + "' not in range [0.." + (this._maskelementList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.xml.event.Maskelement) _maskelementList.get(index);
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
    public org.opennms.netmgt.xml.event.Maskelement[] getMaskelement(
    ) {
        org.opennms.netmgt.xml.event.Maskelement[] array = new org.opennms.netmgt.xml.event.Maskelement[0];
        return (org.opennms.netmgt.xml.event.Maskelement[]) this._maskelementList.toArray(array);
    }

    /**
     * Method getMaskelementCollection.Returns a reference to
     * '_maskelementList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.xml.event.Maskelement> getMaskelementCollection(
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
    public java.util.Iterator<org.opennms.netmgt.xml.event.Maskelement> iterateMaskelement(
    ) {
        return this._maskelementList.iterator();
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
     * Method removeMaskelement.
     * 
     * @param vMaskelement
     * @return true if the object was removed from the collection.
     */
    public boolean removeMaskelement(
            final org.opennms.netmgt.xml.event.Maskelement vMaskelement) {
        boolean removed = _maskelementList.remove(vMaskelement);
        return removed;
    }

    /**
     * Method removeMaskelementAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.xml.event.Maskelement removeMaskelementAt(
            final int index) {
        java.lang.Object obj = this._maskelementList.remove(index);
        return (org.opennms.netmgt.xml.event.Maskelement) obj;
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
            final org.opennms.netmgt.xml.event.Maskelement vMaskelement)
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
            final org.opennms.netmgt.xml.event.Maskelement[] vMaskelementArray) {
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
            final java.util.List<org.opennms.netmgt.xml.event.Maskelement> vMaskelementList) {
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
            final java.util.List<org.opennms.netmgt.xml.event.Maskelement> maskelementList) {
        this._maskelementList = maskelementList;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled org.opennms.netmgt.xml.event.Mask
     */
    public static org.opennms.netmgt.xml.event.Mask unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.event.Mask) Unmarshaller.unmarshal(org.opennms.netmgt.xml.event.Mask.class, reader);
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

    public String toString() {
    	return new ToStringBuilder(this)
    		.append("maskelement", _maskelementList)
    		.toString();
    }
}
