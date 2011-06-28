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
 * The operator action to be taken when this event occurs
 *  with state controlling if action takes place. The menutext gets
 *  displayed in the UI.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name="operaction")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("serial")
public class Operaction implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * internal content storage
     */
	@XmlValue
    private String content = "";

    /**
     * Field _state.
     */
	@XmlAttribute
    private String state;

    /**
     * Field _menutext.
     */
	@XmlAttribute
    private String menutext;


      //----------------/
     //- Constructors -/
    //----------------/

    public Operaction() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(
            final Object obj) {
        if ( this == obj )
            return true;
        
        if (obj instanceof Operaction) {
        
            Operaction temp = (Operaction)obj;
            if (this.content != null) {
                if (temp.content == null) return false;
                else if (!(this.content.equals(temp.content))) 
                    return false;
            }
            else if (temp.content != null)
                return false;
            if (this.state != null) {
                if (temp.state == null) return false;
                else if (!(this.state.equals(temp.state))) 
                    return false;
            }
            else if (temp.state != null)
                return false;
            if (this.menutext != null) {
                if (temp.menutext == null) return false;
                else if (!(this.menutext.equals(temp.menutext))) 
                    return false;
            }
            else if (temp.menutext != null)
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
        return this.content;
    }

    /**
     * Returns the value of field 'menutext'.
     * 
     * @return the value of field 'Menutext'.
     */
    public String getMenutext(
    ) {
        return this.menutext;
    }

    /**
     * Returns the value of field 'state'.
     * 
     * @return the value of field 'State'.
     */
    public String getState(
    ) {
        return this.state;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode(
    ) {
        return new HashCodeBuilder(17,37).append(getContent()).append(getMenutext()).append(getState()).toHashCode();
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
        this.content = content;
    }

    /**
     * Sets the value of field 'menutext'.
     * 
     * @param menutext the value of field 'menutext'.
     */
    public void setMenutext(
            final String menutext) {
        this.menutext = menutext;
    }

    /**
     * Sets the value of field 'state'.
     * 
     * @param state the value of field 'state'.
     */
    public void setState(
            final String state) {
        this.state = state;
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
     * org.opennms.netmgt.xml.eventconf.Operaction
     */
    public static org.opennms.netmgt.xml.eventconf.Operaction unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.Operaction) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.Operaction.class, reader);
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
