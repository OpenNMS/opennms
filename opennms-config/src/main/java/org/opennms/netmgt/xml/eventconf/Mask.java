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

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.HashCodeBuilder;
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
@XmlRootElement(name="mask")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("serial")
public class Mask implements Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The mask element
     */
	@XmlElement(name="maskelement")
    private List<Maskelement> m_maskelementList;

    /**
     * The varbind element
     */
	@XmlElement(name="varbind")
    private List<Varbind> m_varbindList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Mask() {
        super();
        this.m_maskelementList = new ArrayList<Maskelement>();
        this.m_varbindList = new ArrayList<Varbind>();
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
            final Maskelement vMaskelement)
    throws java.lang.IndexOutOfBoundsException {
        this.m_maskelementList.add(vMaskelement);
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
            final Maskelement vMaskelement)
    throws java.lang.IndexOutOfBoundsException {
        this.m_maskelementList.add(index, vMaskelement);
    }

    /**
     * 
     * 
     * @param vVarbind
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addVarbind(
            final Varbind vVarbind)
    throws java.lang.IndexOutOfBoundsException {
        this.m_varbindList.add(vVarbind);
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
            final Varbind vVarbind)
    throws java.lang.IndexOutOfBoundsException {
        this.m_varbindList.add(index, vVarbind);
    }

    /**
     * Method enumerateMaskelement.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Maskelement> enumerateMaskelement(
    ) {
        return Collections.enumeration(this.m_maskelementList);
    }

    /**
     * Method enumerateVarbind.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Varbind> enumerateVarbind(
    ) {
        return Collections.enumeration(this.m_varbindList);
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
            if (this.m_maskelementList != null) {
                if (temp.m_maskelementList == null) return false;
                else if (!(this.m_maskelementList.equals(temp.m_maskelementList))) 
                    return false;
            }
            else if (temp.m_maskelementList != null)
                return false;
            if (this.m_varbindList != null) {
                if (temp.m_varbindList == null) return false;
                else if (!(this.m_varbindList.equals(temp.m_varbindList))) 
                    return false;
            }
            else if (temp.m_varbindList != null)
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
     * Maskelement at the given
     * index
     */
    public Maskelement getMaskelement(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_maskelementList.size()) {
            throw new IndexOutOfBoundsException("getMaskelement: Index value '" + index + "' not in range [0.." + (this.m_maskelementList.size() - 1) + "]");
        }
        
        return (Maskelement) m_maskelementList.get(index);
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
    public Maskelement[] getMaskelement(
    ) {
        Maskelement[] array = new Maskelement[0];
        return (Maskelement[]) this.m_maskelementList.toArray(array);
    }

    /**
     * Method getMaskelementCollection.Returns a reference to
     * '_maskelementList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Maskelement> getMaskelementCollection(
    ) {
        return this.m_maskelementList;
    }

    /**
     * Method getMaskelementCount.
     * 
     * @return the size of this collection
     */
    public int getMaskelementCount(
    ) {
        return this.m_maskelementList.size();
    }

    /**
     * Method getVarbind.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Varbind at the given index
     */
    public Varbind getVarbind(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_varbindList.size()) {
            throw new IndexOutOfBoundsException("getVarbind: Index value '" + index + "' not in range [0.." + (this.m_varbindList.size() - 1) + "]");
        }
        
        return (Varbind) m_varbindList.get(index);
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
    public Varbind[] getVarbind(
    ) {
        Varbind[] array = new Varbind[0];
        return (Varbind[]) this.m_varbindList.toArray(array);
    }

    /**
     * Method getVarbindCollection.Returns a reference to
     * '_varbindList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Varbind> getVarbindCollection(
    ) {
        return this.m_varbindList;
    }

    /**
     * Method getVarbindCount.
     * 
     * @return the size of this collection
     */
    public int getVarbindCount(
    ) {
        return this.m_varbindList.size();
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
        return new HashCodeBuilder(17,37).append(getMaskelement()).append(getVarbind()).toHashCode();
        
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
    public Iterator<Maskelement> iterateMaskelement(
    ) {
        return this.m_maskelementList.iterator();
    }

    /**
     * Method iterateVarbind.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Varbind> iterateVarbind(
    ) {
        return this.m_varbindList.iterator();
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
            final Writer out)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws IOException if an IOException occurs during
     * marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(
            final org.xml.sax.ContentHandler handler)
    throws IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllMaskelement(
    ) {
        this.m_maskelementList.clear();
    }

    /**
     */
    public void removeAllVarbind(
    ) {
        this.m_varbindList.clear();
    }

    /**
     * Method removeMaskelement.
     * 
     * @param vMaskelement
     * @return true if the object was removed from the collection.
     */
    public boolean removeMaskelement(
            final Maskelement vMaskelement) {
        boolean removed = m_maskelementList.remove(vMaskelement);
        return removed;
    }

    /**
     * Method removeMaskelementAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Maskelement removeMaskelementAt(
            final int index) {
        java.lang.Object obj = this.m_maskelementList.remove(index);
        return (Maskelement) obj;
    }

    /**
     * Method removeVarbind.
     * 
     * @param vVarbind
     * @return true if the object was removed from the collection.
     */
    public boolean removeVarbind(
            final Varbind vVarbind) {
        boolean removed = m_varbindList.remove(vVarbind);
        return removed;
    }

    /**
     * Method removeVarbindAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Varbind removeVarbindAt(
            final int index) {
        java.lang.Object obj = this.m_varbindList.remove(index);
        return (Varbind) obj;
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
            final Maskelement vMaskelement)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_maskelementList.size()) {
            throw new IndexOutOfBoundsException("setMaskelement: Index value '" + index + "' not in range [0.." + (this.m_maskelementList.size() - 1) + "]");
        }
        
        this.m_maskelementList.set(index, vMaskelement);
    }

    /**
     * 
     * 
     * @param vMaskelementArray
     */
    public void setMaskelement(
            final Maskelement[] vMaskelementArray) {
        //-- copy array
        m_maskelementList.clear();
        
        for (int i = 0; i < vMaskelementArray.length; i++) {
                this.m_maskelementList.add(vMaskelementArray[i]);
        }
    }

    /**
     * Sets the value of '_maskelementList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vMaskelementList the Vector to copy.
     */
    public void setMaskelement(
            final List<Maskelement> vMaskelementList) {
        // copy vector
        this.m_maskelementList.clear();
        
        this.m_maskelementList.addAll(vMaskelementList);
    }

    /**
     * Sets the value of '_maskelementList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param maskelementList the Vector to set.
     */
    public void setMaskelementCollection(
            final List<Maskelement> maskelementList) {
        this.m_maskelementList = maskelementList;
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
            final Varbind vVarbind)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_varbindList.size()) {
            throw new IndexOutOfBoundsException("setVarbind: Index value '" + index + "' not in range [0.." + (this.m_varbindList.size() - 1) + "]");
        }
        
        this.m_varbindList.set(index, vVarbind);
    }

    /**
     * 
     * 
     * @param vVarbindArray
     */
    public void setVarbind(
            final Varbind[] vVarbindArray) {
        //-- copy array
        m_varbindList.clear();
        
        for (int i = 0; i < vVarbindArray.length; i++) {
                this.m_varbindList.add(vVarbindArray[i]);
        }
    }

    /**
     * Sets the value of '_varbindList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vVarbindList the Vector to copy.
     */
    public void setVarbind(
            final List<Varbind> vVarbindList) {
        // copy vector
        this.m_varbindList.clear();
        
        this.m_varbindList.addAll(vVarbindList);
    }

    /**
     * Sets the value of '_varbindList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param varbindList the Vector to set.
     */
    public void setVarbindCollection(
            final List<Varbind> varbindList) {
        this.m_varbindList = varbindList;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled Mask
     */
    public static Mask unmarshal(
            final Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (Mask) Unmarshaller.unmarshal(Mask.class, reader);
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
