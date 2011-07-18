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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * This element is used for converting event 
 *  varbind value in static decoded string.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name="decode")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("serial")
public class Decode implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _varbindvalue.
     */
	@XmlAttribute(name="varbindvalue", required=true)
    private String m_varbindvalue;

    /**
     * Field _varbinddecodedstring.
     */
	@XmlAttribute(name="varbinddecodedstring",required=true)
    private String m_varbinddecodedstring;


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
            if (this.m_varbindvalue != null) {
                if (temp.m_varbindvalue == null) return false;
                else if (!(this.m_varbindvalue.equals(temp.m_varbindvalue))) 
                    return false;
            }
            else if (temp.m_varbindvalue != null)
                return false;
            if (this.m_varbinddecodedstring != null) {
                if (temp.m_varbinddecodedstring == null) return false;
                else if (!(this.m_varbinddecodedstring.equals(temp.m_varbinddecodedstring))) 
                    return false;
            }
            else if (temp.m_varbinddecodedstring != null)
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
    public String getVarbinddecodedstring(
    ) {
        return this.m_varbinddecodedstring;
    }

    /**
     * Returns the value of field 'varbindvalue'.
     * 
     * @return the value of field 'Varbindvalue'.
     */
    public String getVarbindvalue(
    ) {
        return this.m_varbindvalue;
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
        return new HashCodeBuilder(17,37).append(getVarbinddecodedstring()).append(getVarbindvalue()).toHashCode();
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
            final String varbinddecodedstring) {
        this.m_varbinddecodedstring = varbinddecodedstring;
    }

    /**
     * Sets the value of field 'varbindvalue'.
     * 
     * @param varbindvalue the value of field 'varbindvalue'.
     */
    public void setVarbindvalue(
            final String varbindvalue) {
        this.m_varbindvalue = varbindvalue;
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
