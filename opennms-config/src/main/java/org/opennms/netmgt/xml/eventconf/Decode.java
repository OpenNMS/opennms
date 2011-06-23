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
public class Decode implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _varbindvalue.
     */
    private java.lang.String _varbindvalue;

    /**
     * Field _varbinddecodedstring.
     */
    private java.lang.String _varbinddecodedstring;


      //----------------/
     //- Constructors -/
    //----------------/

    public Decode() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

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
        
        if (obj instanceof Decode) {
        
            Decode temp = (Decode)obj;
            if (this._varbindvalue != null) {
                if (temp._varbindvalue == null) return false;
                else if (!(this._varbindvalue.equals(temp._varbindvalue))) 
                    return false;
            }
            else if (temp._varbindvalue != null)
                return false;
            if (this._varbinddecodedstring != null) {
                if (temp._varbinddecodedstring == null) return false;
                else if (!(this._varbinddecodedstring.equals(temp._varbinddecodedstring))) 
                    return false;
            }
            else if (temp._varbinddecodedstring != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'varbinddecodedstring'.
     * 
     * @return the value of field 'Varbinddecodedstring'.
     */
    public java.lang.String getVarbinddecodedstring(
    ) {
        return this._varbinddecodedstring;
    }

    /**
     * Returns the value of field 'varbindvalue'.
     * 
     * @return the value of field 'Varbindvalue'.
     */
    public java.lang.String getVarbindvalue(
    ) {
        return this._varbindvalue;
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
        if (_varbindvalue != null) {
           result = 37 * result + _varbindvalue.hashCode();
        }
        if (_varbinddecodedstring != null) {
           result = 37 * result + _varbinddecodedstring.hashCode();
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
     * Sets the value of field 'varbinddecodedstring'.
     * 
     * @param varbinddecodedstring the value of field
     * 'varbinddecodedstring'.
     */
    public void setVarbinddecodedstring(
            final java.lang.String varbinddecodedstring) {
        this._varbinddecodedstring = varbinddecodedstring;
    }

    /**
     * Sets the value of field 'varbindvalue'.
     * 
     * @param varbindvalue the value of field 'varbindvalue'.
     */
    public void setVarbindvalue(
            final java.lang.String varbindvalue) {
        this._varbindvalue = varbindvalue;
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
     * org.opennms.netmgt.xml.eventconf.Decode
     */
    public static org.opennms.netmgt.xml.eventconf.Decode unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.Decode) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.Decode.class, reader);
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
