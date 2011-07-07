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
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * The forwarding information for this event - state
 *  determines if event is forwarded, mechanism determines how
 * event is
 *  forwarded
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name="forward")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("serial")
public class Forward implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * internal content storage
     */
	@XmlValue
    private String m_content = "";

    /**
     * Field _state.
     */
	@XmlAttribute(name="state", required=false)
    private String m_state;

    /**
     * Field _mechanism.
     */
	@XmlAttribute(name="mechanism", required=false)
    private String m_mechanism;


      //----------------/
     //- Constructors -/
    //----------------/

    public Forward() {
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
        
        if (obj instanceof Forward) {
        
            Forward temp = (Forward)obj;
            if (this.m_content != null) {
                if (temp.m_content == null) return false;
                else if (!(this.m_content.equals(temp.m_content))) 
                    return false;
            }
            else if (temp.m_content != null)
                return false;
            if (this.m_state != null) {
                if (temp.m_state == null) return false;
                else if (!(this.m_state.equals(temp.m_state))) 
                    return false;
            }
            else if (temp.m_state != null)
                return false;
            if (this.m_mechanism != null) {
                if (temp.m_mechanism == null) return false;
                else if (!(this.m_mechanism.equals(temp.m_mechanism))) 
                    return false;
            }
            else if (temp.m_mechanism != null)
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
    public String getContent(
    ) {
        return this.m_content;
    }

    /**
     * Returns the value of field 'mechanism'.
     * 
     * @return the value of field 'Mechanism'.
     */
    public String getMechanism(
    ) {
        return this.m_mechanism;
    }

    /**
     * Returns the value of field 'state'.
     * 
     * @return the value of field 'State'.
     */
    public String getState(
    ) {
        return this.m_state;
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
        return new HashCodeBuilder(17,37).append(getContent()).append(getMechanism()).append(getState()).toHashCode();
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
            final String content) {
        this.m_content = content;
    }

    /**
     * Sets the value of field 'mechanism'.
     * 
     * @param mechanism the value of field 'mechanism'.
     */
    public void setMechanism(
            final String mechanism) {
        this.m_mechanism = mechanism;
    }

    /**
     * Sets the value of field 'state'.
     * 
     * @param state the value of field 'state'.
     */
    public void setState(
            final String state) {
        this.m_state = state;
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
     * org.opennms.netmgt.xml.eventconf.Forward
     */
    public static org.opennms.netmgt.xml.eventconf.Forward unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.Forward) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.Forward.class, reader);
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
