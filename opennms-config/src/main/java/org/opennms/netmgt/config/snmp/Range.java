package org.opennms.netmgt.config.snmp;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.xml.sax.ContentHandler;

/**
 * IP Address Range
 */

@XmlRootElement(name="range")
@XmlAccessorType(XmlAccessType.FIELD)
public class Range implements Serializable {
	private static final long serialVersionUID = 3386982883357355619L;

	/**
     * Starting IP address of the range.
     */
	@XmlAttribute(name="begin", required=true)
    private String _begin;

    /**
     * Ending IP address of the range.
     */
	@XmlAttribute(name="end", required=true)
    private String _end;

    public Range() {
        super();
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
		if (obj instanceof Range == false) return false;
		if (this == obj) return true;

		final Range temp = (Range)obj;

		return new EqualsBuilder()
			.append(getBegin(), temp.getBegin())
			.append(getEnd(), temp.getEnd())
			.isEquals();
    }

    /**
     * Returns the value of field 'begin'. The field 'begin' has
     * the following description: Starting IP address of the range.
     * 
     * @return the value of field 'Begin'.
     */
    public String getBegin() {
        return this._begin;
    }

    /**
     * Returns the value of field 'end'. The field 'end' has the
     * following description: Ending IP address of the range.
     * 
     * @return the value of field 'End'.
     */
    public String getEnd() {
        return this._end;
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
        
        if (_begin != null) {
           result = 37 * result + _begin.hashCode();
        }
        if (_end != null) {
           result = 37 * result + _end.hashCode();
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
     * following description: Starting IP address of the range.
     * 
     * @param begin the value of field 'begin'.
     */
    public void setBegin(final String begin) {
        this._begin = begin;
    }

    /**
     * Sets the value of field 'end'. The field 'end' has the
     * following description: Ending IP address of the range.
     * 
     * @param end the value of field 'end'.
     */
    public void setEnd(final String end) {
        this._end = end;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled Range
     */
    public static Range unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Range) Unmarshaller.unmarshal(Range.class, reader);
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
    	return new ToStringBuilder(this)
    		.appendSuper(super.toString())
    		.append("begin", getBegin())
    		.append("end", getEnd())
    		.toString();
    }
}
