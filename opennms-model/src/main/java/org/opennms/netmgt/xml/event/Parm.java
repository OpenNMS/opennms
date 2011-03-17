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

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * A varbind from the trap
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="parm")
@XmlAccessorType(XmlAccessType.FIELD)
public class Parm {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * parm name
     */
	@XmlElement(name="parmName", required=true)
    private java.lang.String _parmName;

    /**
     * parm value
     */
	@XmlElement(name="value", required=true)
	private org.opennms.netmgt.xml.event.Value _value;


      //----------------/
     //- Constructors -/
    //----------------/

    public Parm() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    public Parm(final String name, final String value) {
    	this();
    	setParmName(name);
    	this._value = new Value(value);
	}


	/**
     * Returns the value of field 'parmName'. The field 'parmName'
     * has the following description: parm name
     * 
     * @return the value of field 'ParmName'.
     */
    public java.lang.String getParmName(
    ) {
        return this._parmName;
    }

    /**
     * Returns the value of field 'value'. The field 'value' has
     * the following description: parm value
     * 
     * @return the value of field 'Value'.
     */
    public org.opennms.netmgt.xml.event.Value getValue(
    ) {
        return this._value;
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
     * Sets the value of field 'parmName'. The field 'parmName' has
     * the following description: parm name
     * 
     * @param parmName the value of field 'parmName'.
     */
    public void setParmName(
            final java.lang.String parmName) {
        this._parmName = parmName;
    }

    /**
     * Sets the value of field 'value'. The field 'value' has the
     * following description: parm value
     * 
     * @param value the value of field 'value'.
     */
    public void setValue(
            final org.opennms.netmgt.xml.event.Value value) {
        this._value = value;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled org.opennms.netmgt.xml.event.Parm
     */
    public static org.opennms.netmgt.xml.event.Parm unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.event.Parm) Unmarshaller.unmarshal(org.opennms.netmgt.xml.event.Parm.class, reader);
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
