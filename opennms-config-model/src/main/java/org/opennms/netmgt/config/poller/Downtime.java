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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.opennms.core.xml.ValidateUsing;

/**
 * Downtime model. This determines the rates at which
 *  addresses are to be polled when they remain down for extended
 * periods.
 *  Usually polling is done at lower rates when a node is down
 * until a
 *  certain amount of downtime at which the node is marked
 *  'deleted'.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="downtime")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("poller-configuration.xsd")
@SuppressWarnings("all") public class Downtime implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Start of the interval.
     */
	@XmlAttribute(name="begin", required=true)
    private long _begin;

    /**
     * End of the interval.
     */
	@XmlAttribute(name="end")
    private long _end;

    /**
     * Attribute that determines if service is to be deleted
     *  when down continously until the start time.
     */
	@XmlAttribute(name="delete")
    private java.lang.String _delete;

    /**
     * Interval at which service is to be polled between the
     *  specified start and end when service has been continously
     *  down.
     */
	@XmlAttribute(name="interval")
    private long _interval;


      //----------------/
     //- Constructors -/
    //----------------/

    public Downtime() {
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
        
        if (obj instanceof Downtime) {
        
            Downtime temp = (Downtime)obj;
            if (this._begin != temp._begin)
                return false;
            if (this._end != temp._end)
                return false;
            if (this._delete != null) {
                if (temp._delete == null) return false;
                else if (!(this._delete.equals(temp._delete))) 
                    return false;
            }
            else if (temp._delete != null)
                return false;
            if (this._interval != temp._interval)
                return false;
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
    public long getBegin(
    ) {
        return this._begin;
    }

    /**
     * Returns the value of field 'delete'. The field 'delete' has
     * the following description: Attribute that determines if
     * service is to be deleted
     *  when down continously until the start time.
     * 
     * @return the value of field 'Delete'.
     */
    public java.lang.String getDelete(
    ) {
        return this._delete;
    }

    /**
     * Returns the value of field 'end'. The field 'end' has the
     * following description: End of the interval.
     * 
     * @return the value of field 'End'.
     */
    public long getEnd(
    ) {
        return this._end;
    }

    /**
     * Returns the value of field 'interval'. The field 'interval'
     * has the following description: Interval at which service is
     * to be polled between the
     *  specified start and end when service has been continously
     *  down.
     * 
     * @return the value of field 'Interval'.
     */
    public long getInterval(
    ) {
        return this._interval;
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
        result = 37 * result + (int)(_begin^(_begin>>>32));
        result = 37 * result + (int)(_end^(_end>>>32));
        if (_delete != null) {
           result = 37 * result + _delete.hashCode();
        }
        result = 37 * result + (int)(_interval^(_interval>>>32));
        
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
     * Sets the value of field 'begin'. The field 'begin' has the
     * following description: Start of the interval.
     * 
     * @param begin the value of field 'begin'.
     */
    public void setBegin(
            final long begin) {
        this._begin = begin;
    }

    /**
     * Sets the value of field 'delete'. The field 'delete' has the
     * following description: Attribute that determines if service
     * is to be deleted
     *  when down continously until the start time.
     * 
     * @param delete the value of field 'delete'.
     */
    public void setDelete(
            final java.lang.String delete) {
        this._delete = delete;
    }

    /**
     * Sets the value of field 'end'. The field 'end' has the
     * following description: End of the interval.
     * 
     * @param end the value of field 'end'.
     */
    public void setEnd(
            final long end) {
        this._end = end;
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
    public void setInterval(
            final long interval) {
        this._interval = interval;
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
     * org.opennms.netmgt.config.poller.Downtime
     */
    public static org.opennms.netmgt.config.poller.Downtime unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.config.poller.Downtime) Unmarshaller.unmarshal(org.opennms.netmgt.config.poller.Downtime.class, reader);
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
