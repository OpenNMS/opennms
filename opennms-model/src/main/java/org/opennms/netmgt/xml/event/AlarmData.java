/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.xml.event;

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
 * This element is used for converting events into alarms.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="event")
@XmlAccessorType(XmlAccessType.FIELD)
public class AlarmData {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _reductionKey.
     */
	@XmlAttribute(name="reduction-key", required=true)
    private java.lang.String _reductionKey;

    /**
     * Field _alarmType.
     */
	@XmlAttribute(name="alarm-type", required=true)
    private Integer _alarmType;

    /**
     * Field _clearKey.
     */
	@XmlAttribute(name="clear-key")
    private java.lang.String _clearKey;

    /**
     * Field _autoClean.
     */
	@XmlAttribute(name="auto-clean")
    private Boolean _autoClean = false;

    /**
     * Field _x733AlarmType.
     */
	@XmlAttribute(name="x733-alarm-type")
    private java.lang.String _x733AlarmType;

    /**
     * Field _x733ProbableCause.
     */
	@XmlAttribute(name="x733-probable-cause")
    private Integer _x733ProbableCause;


      //----------------/
     //- Constructors -/
    //----------------/

    public AlarmData() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteAlarmType(
    ) {
    	this._alarmType = null;
    }

    /**
     */
    public void deleteAutoClean(
    ) {
        this._autoClean = null;
    }

    /**
     */
    public void deleteX733ProbableCause(
    ) {
        this._x733ProbableCause = null;
    }

    /**
     * Returns the value of field 'alarmType'.
     * 
     * @return the value of field 'AlarmType'.
     */
    public int getAlarmType(
    ) {
        return this._alarmType;
    }

    /**
     * Returns the value of field 'autoClean'.
     * 
     * @return the value of field 'AutoClean'.
     */
    public boolean getAutoClean(
    ) {
        return this._autoClean;
    }

    /**
     * Returns the value of field 'clearKey'.
     * 
     * @return the value of field 'ClearKey'.
     */
    public java.lang.String getClearKey(
    ) {
        return this._clearKey;
    }

    /**
     * Returns the value of field 'reductionKey'.
     * 
     * @return the value of field 'ReductionKey'.
     */
    public java.lang.String getReductionKey(
    ) {
        return this._reductionKey;
    }

    /**
     * Returns the value of field 'x733AlarmType'.
     * 
     * @return the value of field 'X733AlarmType'.
     */
    public java.lang.String getX733AlarmType(
    ) {
        return this._x733AlarmType;
    }

    /**
     * Returns the value of field 'x733ProbableCause'.
     * 
     * @return the value of field 'X733ProbableCause'.
     */
    public int getX733ProbableCause(
    ) {
        return this._x733ProbableCause;
    }

    /**
     * Method hasAlarmType.
     * 
     * @return true if at least one AlarmType has been added
     */
    public boolean hasAlarmType(
    ) {
        return this._alarmType != null;
    }

    /**
     * Method hasAutoClean.
     * 
     * @return true if at least one AutoClean has been added
     */
    public boolean hasAutoClean(
    ) {
        return this._autoClean != null;
    }

    /**
     * Method hasX733ProbableCause.
     * 
     * @return true if at least one X733ProbableCause has been added
     */
    public boolean hasX733ProbableCause(
    ) {
        return this._x733ProbableCause != null;
    }

    /**
     * Returns the value of field 'autoClean'.
     * 
     * @return the value of field 'AutoClean'.
     */
    public boolean isAutoClean(
    ) {
        return this._autoClean;
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
     * Sets the value of field 'alarmType'.
     * 
     * @param alarmType the value of field 'alarmType'.
     */
    public void setAlarmType(
            final int alarmType) {
        this._alarmType = alarmType;
    }

    /**
     * Sets the value of field 'autoClean'.
     * 
     * @param autoClean the value of field 'autoClean'.
     */
    public void setAutoClean(
            final boolean autoClean) {
        this._autoClean = autoClean;
    }

    /**
     * Sets the value of field 'clearKey'.
     * 
     * @param clearKey the value of field 'clearKey'.
     */
    public void setClearKey(
            final java.lang.String clearKey) {
        this._clearKey = clearKey;
    }

    /**
     * Sets the value of field 'reductionKey'.
     * 
     * @param reductionKey the value of field 'reductionKey'.
     */
    public void setReductionKey(
            final java.lang.String reductionKey) {
        this._reductionKey = reductionKey;
    }

    /**
     * Sets the value of field 'x733AlarmType'.
     * 
     * @param x733AlarmType the value of field 'x733AlarmType'.
     */
    public void setX733AlarmType(
            final java.lang.String x733AlarmType) {
        this._x733AlarmType = x733AlarmType;
    }

    /**
     * Sets the value of field 'x733ProbableCause'.
     * 
     * @param x733ProbableCause the value of field
     * 'x733ProbableCause'.
     */
    public void setX733ProbableCause(
            final int x733ProbableCause) {
        this._x733ProbableCause = x733ProbableCause;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled org.opennms.netmgt.xml.event.AlarmDat
     */
    public static org.opennms.netmgt.xml.event.AlarmData unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.event.AlarmData) Unmarshaller.unmarshal(org.opennms.netmgt.xml.event.AlarmData.class, reader);
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
