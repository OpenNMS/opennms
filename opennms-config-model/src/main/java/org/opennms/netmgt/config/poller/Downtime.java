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
import javax.xml.bind.annotation.XmlTransient;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.xml.sax.ContentHandler;

/**
 * Downtime model. This determines the rates at which
 *  addresses are to be polled when they remain down for extended
 * periods.
 *  Usually polling is done at lower rates when a node is down
 * until a
 *  certain amount of downtime at which the node is marked
 *  'deleted'.
 */

@XmlRootElement(name="downtime")
@XmlAccessorType(XmlAccessType.FIELD)
public class Downtime implements Serializable {
    private static final long serialVersionUID = 6968410431750700270L;

    /**
     * Start of the interval.
     */
    @XmlAttribute(name="begin")
    private Long m_begin;

    /**
     * End of the interval.
     */
    @XmlAttribute(name="end")
    private Long m_end;

    /**
     * Attribute that determines if service is to be deleted
     *  when down continuously until the start time.
     */
    @XmlAttribute(name="delete")
    private String m_delete;

    /**
     * Interval at which service is to be polled between the
     *  specified start and end when service has been continously
     *  down.
     */
    @XmlAttribute(name="interval")
    private Long m_interval;


    public Downtime() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    public Downtime(final long interval, final long begin, final long end) {
        this();
        setInterval(interval);
        setBegin(begin);
        setEnd(end);
    }

    public Downtime(final long begin, final boolean delete) {
        this();
        setBegin(begin);
        setDelete(delete? "true":"false");
    }

    /**
     */
    public void deleteBegin() {
        m_begin = null;
    }

    /**
     */
    public void deleteEnd() {
        m_end = null;
    }

    /**
     */
    public void deleteInterval() {
        m_interval = null;
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

        if (obj instanceof Downtime) {
            final Downtime temp = (Downtime)obj;
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
            if (m_delete != null) {
                if (temp.m_delete == null) return false;
                else if (!(m_delete.equals(temp.m_delete))) 
                    return false;
            }
            else if (temp.m_delete != null)
                return false;
            if (m_interval != null) {
                if (temp.m_interval == null) {
                    return false;
                } else if (!(m_interval.equals(temp.m_interval))) {
                    return false;
                }
            } else if (temp.m_interval != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'begin'. The field 'begin' has
     * the following description: Start of the interval.
     * 
     * @return the value of field 'Begin'.
     */
    public Long getBegin() {
        return m_begin == null? 0 : m_begin;
    }

    /**
     * Returns the value of field 'delete'. The field 'delete' has
     * the following description: Attribute that determines if
     * service is to be deleted
     *  when down continously until the start time.
     * 
     * @return the value of field 'Delete'.
     */
    public String getDelete() {
        return m_delete;
    }

    /**
     * Returns the value of field 'end'. The field 'end' has the
     * following description: End of the interval.
     * 
     * @return the value of field 'End'.
     */
    public Long getEnd() {
        return m_end == null? 0 : m_end;
    }

    /**
     * Returns the value of field 'interval'. The field 'interval'
     * has the following description: Interval at which service is
     * to be polled between the
     *  specified start and end when service has been continuously
     *  down.
     * 
     * @return the value of field 'Interval'.
     */
    public Long getInterval() {
        return m_interval == null? 0 : m_interval;
    }

    /**
     * Method hasBegin.
     * 
     * @return true if at least one Begin has been added
     */
    public boolean hasBegin() {
        return m_begin != null;
    }

    /**
     * Method hasEnd.
     * 
     * @return true if at least one End has been added
     */
    public boolean hasEnd() {
        return m_end != null;
    }

    /**
     * Method hasInterval.
     * 
     * @return true if at least one Interval has been added
     */
    public boolean hasInterval() {
        return m_interval != null;
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
        if (m_delete != null) {
            result = 37 * result + m_delete.hashCode();
         }
        if (m_interval != null) {
            result = 37 * result + m_interval.hashCode();
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
     * following description: Start of the interval.
     * 
     * @param begin the value of field 'begin'.
     */
    public void setBegin(final Long begin) {
        m_begin = begin;
    }

    @XmlTransient
    public void setBegin(final Integer begin) {
        m_begin = begin == null? null : begin.longValue();
    }

    /**
     * Sets the value of field 'delete'. The field 'delete' has the
     * following description: Attribute that determines if service
     * is to be deleted
     *  when down continously until the start time.
     * 
     * @param delete the value of field 'delete'.
     */
    public void setDelete(final String delete) {
        m_delete = delete;
    }

    /**
     * Sets the value of field 'end'. The field 'end' has the
     * following description: End of the interval.
     * 
     * @param end the value of field 'end'.
     */
    public void setEnd(final Long end) {
        m_end = end;
    }

    @XmlTransient
    public void setEnd(final Integer end) {
        m_end = end == null? null : end.longValue();
    }

    /**
     * Sets the value of field 'interval'. The field 'interval' has
     * the following description: Interval at which service is to
     * be polled between the
     *  specified start and end when service has been continously
     *  down.
     * 
     * @param interval the value of field 'interval'.
     */
    public void setInterval(final Long interval) {
        m_interval = interval;
    }

    @XmlTransient
    public void setInterval(final Integer interval) {
        m_interval = interval == null? null : interval.longValue();
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
     * Downtime
     */
    public static Downtime unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Downtime) Unmarshaller.unmarshal(Downtime.class, reader);
    }

    /**
     * 
     * 
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate(
    )
    throws ValidationException {
        new Validator().validate(this);
    }

}
