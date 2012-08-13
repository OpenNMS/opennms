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
 * Class Rrd.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="rrd")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all") public class Rrd implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _step.
     */
    @XmlAttribute(name="step", required=true)
    private int _step = 0;

    /**
     * Field _rraList.
     */
    @XmlElement(name="rra", required=true)
    private java.util.List<java.lang.String> _rraList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Rrd() {
        super();
        this._rraList = new java.util.ArrayList<java.lang.String>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vRra
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRra(
            final java.lang.String vRra)
    throws java.lang.IndexOutOfBoundsException {
        this._rraList.add(vRra);
    }

    /**
     * 
     * 
     * @param index
     * @param vRra
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRra(
            final int index,
            final java.lang.String vRra)
    throws java.lang.IndexOutOfBoundsException {
        this._rraList.add(index, vRra);
    }

    /**
     * Method enumerateRra.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<java.lang.String> enumerateRra(
    ) {
        return java.util.Collections.enumeration(this._rraList);
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
        
        if (obj instanceof Rrd) {
        
            Rrd temp = (Rrd)obj;
            if (this._step != temp._step)
                return false;
            if (this._rraList != null) {
                if (temp._rraList == null) return false;
                else if (!(this._rraList.equals(temp._rraList))) 
                    return false;
            }
            else if (temp._rraList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getRra.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the java.lang.String at the given index
     */
    public java.lang.String getRra(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._rraList.size()) {
            throw new IndexOutOfBoundsException("getRra: Index value '" + index + "' not in range [0.." + (this._rraList.size() - 1) + "]");
        }
        
        return (java.lang.String) _rraList.get(index);
    }

    /**
     * Method getRra.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public java.lang.String[] getRra(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._rraList.toArray(array);
    }

    /**
     * Method getRraCollection.Returns a reference to '_rraList'.
     * No type checking is performed on any modifications to the
     * Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<java.lang.String> getRraCollection(
    ) {
        return this._rraList;
    }

    /**
     * Method getRraCount.
     * 
     * @return the size of this collection
     */
    public int getRraCount(
    ) {
        return this._rraList.size();
    }

    /**
     * Returns the value of field 'step'.
     * 
     * @return the value of field 'Step'.
     */
    public int getStep(
    ) {
        return this._step;
    }

    /**
     * Method hasStep.
     * 
     * @return true if at least one Step has been added
     */
    public boolean hasStep(
    ) {
        return this._step > 0;
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
        result = 37 * result + _step;
        if (_rraList != null) {
           result = 37 * result + _rraList.hashCode();
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
     * Method iterateRra.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<java.lang.String> iterateRra(
    ) {
        return this._rraList.iterator();
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
    public void removeAllRra(
    ) {
        this._rraList.clear();
    }

    /**
     * Method removeRra.
     * 
     * @param vRra
     * @return true if the object was removed from the collection.
     */
    public boolean removeRra(
            final java.lang.String vRra) {
        boolean removed = _rraList.remove(vRra);
        return removed;
    }

    /**
     * Method removeRraAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeRraAt(
            final int index) {
        java.lang.Object obj = this._rraList.remove(index);
        return (java.lang.String) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vRra
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setRra(
            final int index,
            final java.lang.String vRra)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._rraList.size()) {
            throw new IndexOutOfBoundsException("setRra: Index value '" + index + "' not in range [0.." + (this._rraList.size() - 1) + "]");
        }
        
        this._rraList.set(index, vRra);
    }

    /**
     * 
     * 
     * @param vRraArray
     */
    public void setRra(
            final java.lang.String[] vRraArray) {
        //-- copy array
        _rraList.clear();
        
        for (int i = 0; i < vRraArray.length; i++) {
                this._rraList.add(vRraArray[i]);
        }
    }

    /**
     * Sets the value of '_rraList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vRraList the Vector to copy.
     */
    public void setRra(
            final java.util.List<java.lang.String> vRraList) {
        // copy vector
        this._rraList.clear();
        
        this._rraList.addAll(vRraList);
    }

    /**
     * Sets the value of '_rraList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param rraList the Vector to set.
     */
    public void setRraCollection(
            final java.util.List<java.lang.String> rraList) {
        this._rraList = rraList;
    }

    /**
     * Sets the value of field 'step'.
     * 
     * @param step the value of field 'step'.
     */
    public void setStep(
            final int step) {
        this._step = step;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled org.opennms.netmgt.config.collectd.jmx.Rrd
     */
    @Deprecated
    public static Rrd unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (Rrd) Unmarshaller.unmarshal(Rrd.class, reader);
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
