/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.poller;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.StringWriter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.opennms.core.xml.ValidateUsing;

/**
 * Parameters to be used for polling this service. E.g.: for
 *  polling HTTP, the URL to hit is configurable via a parameter.
 * Parameters
 *  are specfic to the service monitor.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="parameter")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("poller-configuration.xsd")
@SuppressWarnings("serial")
public class Parameter implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _key.
     */
    @XmlAttribute(name="key", required=true)
    private java.lang.String _key;

    /**
     * Field _value.
     */
    @XmlAttribute(name="value", required=true)
    private java.lang.String _value;

    /**
     * Field _anyObject.
     * <p>In order to return the final object representation, the class must be explicitly
     * declared in @XmlSeeAlso, or explicitly added to the JAXBContext, besides adding lax=true
     * to the @XmlAnyElement. Otherwise, a instance of org.w3c.dom.Element will be returned.</p>
     * <p>The XSD must be defined like the following:</p>
     * <p>&lt;any processContents="skip" id="configuration" minOccurs="0" maxOccurs="1" /&gt;</p>
     */
    @XmlAnyElement
    private org.w3c.dom.Node _anyObject;


      //----------------/
     //- Constructors -/
    //----------------/

    public Parameter() {
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
        
        if (obj instanceof Parameter) {
        
            Parameter temp = (Parameter)obj;
            if (this._key != null) {
                if (temp._key == null) return false;
                else if (!(this._key.equals(temp._key))) 
                    return false;
            }
            else if (temp._key != null)
                return false;
            if (this._value != null) {
                if (temp._value == null) return false;
                else if (!(this._value.equals(temp._value))) 
                    return false;
            }
            else if (temp._value != null)
                return false;
            if (this._anyObject != null) {
                if (temp._anyObject == null) return false;
                else if (!(this._anyObject.equals(temp._anyObject))) 
                    return false;
            }
            else if (temp._anyObject != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'anyObject'.
     * 
     * @return the value of field 'AnyObject'.
     */
    public org.w3c.dom.Node getAnyObject(
    ) {
        return _anyObject;
    }

    /**
     * Returns the value of field 'anyObject' as String.
     * 
     * @return the String XML representation of field 'AnyObject'.
     */
    public String getAnyObjectAsString(
    ) {
        if (_anyObject != null)  {
            try {
                StringWriter w = new StringWriter();
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer t = tf.newTransformer();
                t.setOutputProperty(OutputKeys.INDENT, "yes");
                t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                t.transform(new DOMSource(_anyObject), new StreamResult(w));
                // Removing <?xml> header
                // Removing Namename for all tags
                // Removing trailing \r\n if exist
                return w.toString().replaceFirst("\\<\\?xml[^>]+\\>[\\r\\n]*", "").replaceAll(" xmlns=\"[^\"]+\"","").replaceFirst("[\\r\\n]*$", "");
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Returns the value of field 'key'.
     * 
     * @return the value of field 'Key'.
     */
    public java.lang.String getKey(
    ) {
        return this._key;
    }

    /**
     * Returns the value of field 'value'.
     * 
     * @return the value of field 'Value'.
     */
    public java.lang.String getValue(
    ) {
        return this._value;
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
        
        if (_key != null) {
           result = 37 * result + _key.hashCode();
        }
        if (_value != null) {
           result = 37 * result + _value.hashCode();
        }
        if (_anyObject != null) {
           result = 37 * result + _anyObject.hashCode();
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
     * Sets the value of field 'anyObject'.
     * 
     * @param anyObject the value of field 'anyObject'.
     */
    public void setAnyObject(
            final org.w3c.dom.Node anyObject) {
        this._anyObject = anyObject;
    }

    /**
     * Sets the value of field 'key'.
     * 
     * @param key the value of field 'key'.
     */
    public void setKey(
            final java.lang.String key) {
        this._key = key;
    }

    /**
     * Sets the value of field 'value'.
     * 
     * @param value the value of field 'value'.
     */
    public void setValue(
            final java.lang.String value) {
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
     * @return the unmarshaled
     * org.opennms.netmgt.config.poller.Parameter
     */
    public static org.opennms.netmgt.config.poller.Parameter unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.config.poller.Parameter) Unmarshaller.unmarshal(org.opennms.netmgt.config.poller.Parameter.class, reader);
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
