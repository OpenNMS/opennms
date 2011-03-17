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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * The event logmsg with the destination attribute defining
 *  if event is for display only, logonly, log and display or
 * neither. A
 *  destination attribute of 'donotpersist' indicates that Eventd
 * is not to
 *  persist the event to the database. The optional notify
 * attributed can be 
 *  used to suppress notices on a particular event (by default it
 * is true - 
 *  i.e. a notice will be sent.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="value")
@XmlAccessorType(XmlAccessType.FIELD)
public class Logmsg {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * internal content storage
     */
	@XmlValue
    private java.lang.String _content = "";

    /**
     * Field _notify.
     */
	@XmlAttribute(name="notify")
    private Boolean _notify = true;

    /**
     * Field _dest.
     */
    @XmlAttribute(name="dest")
    private java.lang.String _dest = "logndisplay";


      //----------------/
     //- Constructors -/
    //----------------/

    public Logmsg() {
        super();
        setContent("");
        setDest("logndisplay");
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteNotify(
    ) {
        this._notify = null;
    }

    /**
     * Returns the value of field 'content'. The field 'content'
     * has the following description: internal content storage
     * 
     * @return the value of field 'Content'.
     */
    public java.lang.String getContent(
    ) {
        return this._content;
    }

    /**
     * Returns the value of field 'dest'.
     * 
     * @return the value of field 'Dest'.
     */
    public java.lang.String getDest(
    ) {
        return this._dest;
    }

    /**
     * Returns the value of field 'notify'.
     * 
     * @return the value of field 'Notify'.
     */
    public boolean getNotify(
    ) {
        return this._notify;
    }

    /**
     * Method hasNotify.
     * 
     * @return true if at least one Notify has been added
     */
    public boolean hasNotify(
    ) {
        return this._notify != null;
    }

    /**
     * Returns the value of field 'notify'.
     * 
     * @return the value of field 'Notify'.
     */
    public boolean isNotify(
    ) {
        return this._notify;
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
     * Sets the value of field 'content'. The field 'content' has
     * the following description: internal content storage
     * 
     * @param content the value of field 'content'.
     */
    public void setContent(
            final java.lang.String content) {
        this._content = content;
    }

    /**
     * Sets the value of field 'dest'.
     * 
     * @param dest the value of field 'dest'.
     */
    public void setDest(
            final java.lang.String dest) {
        this._dest = dest;
    }

    /**
     * Sets the value of field 'notify'.
     * 
     * @param notify the value of field 'notify'.
     */
    public void setNotify(
            final boolean notify) {
        this._notify = notify;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled org.opennms.netmgt.xml.event.Logmsg
     */
    public static org.opennms.netmgt.xml.event.Logmsg unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.event.Logmsg) Unmarshaller.unmarshal(org.opennms.netmgt.xml.event.Logmsg.class, reader);
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
