/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.poller;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.xml.sax.ContentHandler;

/**
 * Range of addresses to be included in this
 *  package.
 */

@XmlRootElement(name="include-range")
@XmlAccessorType(XmlAccessType.FIELD)
public class IncludeRange implements Serializable {
    private static final long serialVersionUID = 3814504369708456000L;

    /**
     * Starting address of the range.
     */
    @XmlAttribute(name="begin")
    private String m_begin;

    /**
     * Ending address of the range.
     */
    @XmlAttribute(name="end")
    private String m_end;

    public IncludeRange() {
        super();
    }

    public IncludeRange(final String begin, final String end) {
        this();
        setBegin(begin);
        setEnd(end);
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
        
        if (obj instanceof IncludeRange) {
            final IncludeRange temp = (IncludeRange)obj;
            if (m_begin != null) {
                if (temp.m_begin == null) {
                    return false;
                } else if (!(m_begin.equals(temp.m_begin))) {
                    return false;
                }
            } else if (temp.m_begin != null) {
                return false;
            }
            if (m_end != null) {
                if (temp.m_end == null) {
                    return false;
                } else if (!(m_end.equals(temp.m_end))) {
                    return false;
                }
            } else if (temp.m_end != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'begin'. The field 'begin' has
     * the following description: Starting address of the range.
     * 
     * @return the value of field 'Begin'.
     */
    public String getBegin() {
        return m_begin;
    }

    /**
     * Returns the value of field 'end'. The field 'end' has the
     * following description: Ending address of the range.
     * 
     * @return the value of field 'End'.
     */
    public String getEnd() {
        return m_end;
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
        
        if (m_begin != null) {
           result = 37 * result + m_begin.hashCode();
        }
        if (m_end != null) {
           result = 37 * result + m_end.hashCode();
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
     * Sets the value of field 'begin'. The field 'begin' has the
     * following description: Starting address of the range.
     * 
     * @param begin the value of field 'begin'.
     */
    public void setBegin(final String begin) {
        m_begin = begin;
    }

    /**
     * Sets the value of field 'end'. The field 'end' has the
     * following description: Ending address of the range.
     * 
     * @param end the value of field 'end'.
     */
    public void setEnd(final String end) {
        m_end = end;
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
     * IncludeRange
     */
    public static IncludeRange unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (IncludeRange) Unmarshaller.unmarshal(IncludeRange.class, reader);
    }

    /**
     * 
     * 
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate()
    throws ValidationException {
        new Validator().validate(this);
    }

    @Override
    public String toString() {
        return "IncludeRange[begin=" + m_begin + ",end=" + m_end + "]";
    }
}
