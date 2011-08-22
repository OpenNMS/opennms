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

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * The mask element
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="filter")
@XmlAccessorType(XmlAccessType.FIELD)
public class Filter implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * 
     */
    private static final long serialVersionUID = -1342650136073016207L;

    /**
     * Field _eventparm.
     */
    private java.lang.String _eventparm;

    /**
     * Field _pattern.
     */
    private java.lang.String _pattern;

    /**
     * Field _replacement.
     */
    private java.lang.String _replacement;


      //----------------/
     //- Constructors -/
    //----------------/

    public Filter() {
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
        
        if (obj instanceof Filter) {
        
            Filter temp = (Filter)obj;
            if (this._eventparm != null) {
                if (temp._eventparm == null) return false;
                else if (!(this._eventparm.equals(temp._eventparm))) 
                    return false;
            }
            else if (temp._eventparm != null)
                return false;
            if (this._pattern != null) {
                if (temp._pattern == null) return false;
                else if (!(this._pattern.equals(temp._pattern))) 
                    return false;
            }
            else if (temp._pattern != null)
                return false;
            if (this._replacement != null) {
                if (temp._replacement == null) return false;
                else if (!(this._replacement.equals(temp._replacement))) 
                    return false;
            }
            else if (temp._replacement != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'eventparm'.
     * 
     * @return the value of field 'Eventparm'.
     */
    @XmlAttribute(name="eventparm")
    public java.lang.String getEventparm(
    ) {
        return this._eventparm;
    }

    /**
     * Returns the value of field 'pattern'.
     * 
     * @return the value of field 'Pattern'.
     */
    @XmlAttribute(name="pattern")
    public java.lang.String getPattern(
    ) {
        return this._pattern;
    }

    /**
     * Returns the value of field 'replacement'.
     * 
     * @return the value of field 'Replacement'.
     */
    @XmlAttribute(name="replacement")
    public java.lang.String getReplacement(
    ) {
        return this._replacement;
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
        
        if (_eventparm != null) {
           result = 37 * result + _eventparm.hashCode();
        }
        if (_pattern != null) {
           result = 37 * result + _pattern.hashCode();
        }
        if (_replacement != null) {
           result = 37 * result + _replacement.hashCode();
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
     * Sets the value of field 'eventparm'.
     * 
     * @param eventparm the value of field 'eventparm'.
     */
    public void setEventparm(
            final java.lang.String eventparm) {
        this._eventparm = eventparm;
    }

    /**
     * Sets the value of field 'pattern'.
     * 
     * @param pattern the value of field 'pattern'.
     */
    public void setPattern(
            final java.lang.String pattern) {
        this._pattern = pattern;
    }

    /**
     * Sets the value of field 'replacement'.
     * 
     * @param replacement the value of field 'replacement'.
     */
    public void setReplacement(
            final java.lang.String replacement) {
        this._replacement = replacement;
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
     * org.opennms.netmgt.xml.eventconf.Filter
     */
    public static org.opennms.netmgt.xml.eventconf.Filter unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.Filter) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.Filter.class, reader);
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
