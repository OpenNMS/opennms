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

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Log.
 * 
 * @version $Revision$ $Date$
 */

@SuppressWarnings("all") public class Log implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _header.
     */
    private org.opennms.netmgt.xml.event.Header _header;

    /**
     * Field _events.
     */
    private org.opennms.netmgt.xml.event.Events _events;


      //----------------/
     //- Constructors -/
    //----------------/

    public Log() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'events'.
     * 
     * @return the value of field 'Events'.
     */
    public org.opennms.netmgt.xml.event.Events getEvents(
    ) {
        return this._events;
    }

    /**
     * Returns the value of field 'header'.
     * 
     * @return the value of field 'Header'.
     */
    public org.opennms.netmgt.xml.event.Header getHeader(
    ) {
        return this._header;
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
     * Sets the value of field 'events'.
     * 
     * @param events the value of field 'events'.
     */
    public void setEvents(
            final org.opennms.netmgt.xml.event.Events events) {
        this._events = events;
    }

    /**
     * Sets the value of field 'header'.
     * 
     * @param header the value of field 'header'.
     */
    public void setHeader(
            final org.opennms.netmgt.xml.event.Header header) {
        this._header = header;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled org.opennms.netmgt.xml.event.Log
     */
    public static org.opennms.netmgt.xml.event.Log unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.event.Log) Unmarshaller.unmarshal(org.opennms.netmgt.xml.event.Log.class, reader);
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
