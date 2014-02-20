/*
 * This class was converted to JAXB from Castor.
 */

package org.opennms.netmgt.config.poller;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.JaxbClassObjectAdapter;
import org.xml.sax.ContentHandler;


/**
 * Parameters to be used for polling this service. E.g.: for
 *  polling HTTP, the URL to hit is configurable via a parameter.
 * Parameters
 *  are specific to the service monitor.
 */

@XmlRootElement(name="parameter")
@XmlAccessorType(XmlAccessType.FIELD)
public class Parameter implements Serializable {
    private static final long serialVersionUID = 8306069510469943913L;

    /**
     * Field m_key.
     */
    @XmlAttribute(name="key")
    private String m_key;

    /**
     * Field m_value.
     */
    @XmlAttribute(name="value")
    private String m_value;

    /**
     * Field m_anyObject.
     */
    @XmlAnyElement(lax=false)
    @XmlJavaTypeAdapter(JaxbClassObjectAdapter.class)
    private Object m_anyObject;


    public Parameter() {
        super();
    }

    public Parameter(final String key, final String value) {
        this();
        m_key = key;
        m_value = value;
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;
        
        if (obj instanceof Parameter) {
            final Parameter temp = (Parameter)obj;
            if (this.m_key != null) {
                if (temp.m_key == null) {
                    return false;
                } else if (!(this.m_key.equals(temp.m_key))) {
                    return false;
                }
            } else if (temp.m_key != null) {
                return false;
            }
            if (this.m_value != null) {
                if (temp.m_value == null) {
                    return false;
                } else if (!(this.m_value.equals(temp.m_value))) {
                    return false;
                }
            } else if (temp.m_value != null) {
                return false;
            }
            if (this.m_anyObject != null) {
                if (temp.m_anyObject == null) {
                    return false;
                } else if (!(this.m_anyObject.equals(temp.m_anyObject))) {
                    return false;
                }
            } else if (temp.m_anyObject != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'anyObject'.
     * 
     * @return the value of field 'AnyObject'.
     */
    public Object getAnyObject() {
        return m_anyObject;
    }

    /**
     * Returns the value of field 'key'.
     * 
     * @return the value of field 'Key'.
     */
    public String getKey() {
        return m_key;
    }

    /**
     * Returns the value of field 'value'.
     * 
     * @return the value of field 'Value'.
     */
    public String getValue() {
        return m_value;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;

        if (m_key != null) {
           result = 37 * result + m_key.hashCode();
        }
        if (m_value != null) {
           result = 37 * result + m_value.hashCode();
        }
        if (m_anyObject != null) {
           result = 37 * result + m_anyObject.hashCode();
        }
        
        return result;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid() {
        try {
            validate();
        } catch (final ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * 
     * 
     * @param out
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws IOException if an IOException occurs during
     * marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     * Sets the value of field 'anyObject'.
     * 
     * @param anyObject the value of field 'anyObject'.
     */
    public void setAnyObject(final Object anyObject) {
        m_anyObject = anyObject;
    }

    /**
     * Sets the value of field 'key'.
     * 
     * @param key the value of field 'key'.
     */
    public void setKey(final String key) {
        m_key = key;
    }

    /**
     * Sets the value of field 'value'.
     * 
     * @param value the value of field 'value'.
     */
    public void setValue(final String value) {
        m_value = value;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * Parameter
     */
    public static Parameter unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Parameter) Unmarshaller.unmarshal(Parameter.class, reader);
    }

    /**
     * 
     * 
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

    @Override
    public String toString() {
        return "Parameter[key=" + m_key +
                ",value=" + (m_anyObject == null? m_value : m_anyObject) +
                "]";
    }
}
