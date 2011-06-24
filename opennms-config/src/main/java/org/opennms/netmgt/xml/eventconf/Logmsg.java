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
 * The event logmsg with the destination attribute defining
 *  if event is for display only, logonly, log and display or
 * neither. A
 *  destination attribute of 'donotpersist' indicates that Eventd
 * is not to
 *  persist the event to the database. A value of 'discardtraps'
 * instructs
 *  the SNMP trap daemon to not create events for incoming traps
 * that match
 *  this event. The optional notify attributed can be used to
 * suppress notices
 *  on a particular event (by default it is true - i.e. a notice
 * will be sent.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Logmsg implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * internal content storage
     */
    private java.lang.String _content = "";

    /**
     * Field _notify.
     */
    private boolean _notify = true;

    /**
     * keeps track of state for field: _notify
     */
    private boolean _has_notify;

    /**
     * Field _dest.
     */
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
        this._has_notify= false;
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
        
        if (obj instanceof Logmsg) {
        
            Logmsg temp = (Logmsg)obj;
            if (this._content != null) {
                if (temp._content == null) return false;
                else if (!(this._content.equals(temp._content))) 
                    return false;
            }
            else if (temp._content != null)
                return false;
            if (this._notify != temp._notify)
                return false;
            if (this._has_notify != temp._has_notify)
                return false;
            if (this._dest != null) {
                if (temp._dest == null) return false;
                else if (!(this._dest.equals(temp._dest))) 
                    return false;
            }
            else if (temp._dest != null)
                return false;
            return true;
        }
        return false;
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
        return this._has_notify;
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
        if (_content != null) {
           result = 37 * result + _content.hashCode();
        }
        result = 37 * result + (_notify?0:1);
        if (_dest != null) {
           result = 37 * result + _dest.hashCode();
        }
        
        return result;
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
        this._has_notify = true;
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
     * org.opennms.netmgt.xml.eventconf.Logmsg
     */
    public static org.opennms.netmgt.xml.eventconf.Logmsg unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.Logmsg) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.Logmsg.class, reader);
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
