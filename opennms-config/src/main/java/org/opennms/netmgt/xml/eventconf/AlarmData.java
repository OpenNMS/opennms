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
 * This element is used for converting events into alarms.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name="alarm-data")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("serial")
public class AlarmData implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field reductionKey.
     */
	@XmlAttribute(name="reduction-key")
    private String reductionKey;

    /**
     * Field _alarmType.
     */
	@XmlAttribute(name="alarm-type")
    private Integer alarmType;

    /**
     * Field _clearKey.
     */
    @XmlAttribute(name="clear-key")
    private String clearKey;

    /**
     * Field _autoClean.
     */
    @XmlAttribute(name="auto-clean")
    private Boolean autoClean;

    /**
     * Field _x733AlarmType.
     */
    @XmlAttribute(name="x733-alarm-type")
    private String x733AlarmType;

    /**
     * Field _x733ProbableCause.
     */
    @XmlAttribute(name="x733-probable-cause")
    private Integer x733ProbableCause;


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
        alarmType = null;
    }

    /**
     */
    public void deleteX733ProbableCause(
    ) {
        x733ProbableCause= null;
    }

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
        
        if (obj instanceof AlarmData) {
        
            AlarmData temp = (AlarmData)obj;
            if (this.reductionKey != null) {
                if (temp.reductionKey == null) return false;
                else if (!(this.reductionKey.equals(temp.reductionKey))) 
                    return false;
            }
            else if (temp.reductionKey != null)
                return false;
            if (this.alarmType != temp.alarmType)
                return false;
            if (this.clearKey != null) {
                if (temp.clearKey == null) return false;
                else if (!(this.clearKey.equals(temp.clearKey))) 
                    return false;
            }
            else if (temp.clearKey != null)
                return false;
            if (this.autoClean != temp.autoClean)
                return false;
            if (this.x733AlarmType != null) {
                if (temp.x733AlarmType == null) return false;
                else if (!(this.x733AlarmType.equals(temp.x733AlarmType))) 
                    return false;
            }
            else if (temp.x733AlarmType != null)
                return false;
            if (this.x733ProbableCause != temp.x733ProbableCause)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'alarmType'.
     * 
     * @return the value of field 'AlarmType'.
     */
    public int getAlarmType(
    ) {
        return this.alarmType;
    }

    /**
     * Returns the value of field 'autoClean'.
     * 
     * @return the value of field 'AutoClean'.
     */
    public boolean getAutoClean(
    ) {
        return this.autoClean;
    }

    /**
     * Returns the value of field 'clearKey'.
     * 
     * @return the value of field 'ClearKey'.
     */
    public String getClearKey(
    ) {
        return this.clearKey;
    }

    /**
     * Returns the value of field 'reductionKey'.
     * 
     * @return the value of field 'ReductionKey'.
     */
    public String getReductionKey(
    ) {
        return this.reductionKey;
    }

    /**
     * Returns the value of field 'x733AlarmType'.
     * 
     * @return the value of field 'X733AlarmType'.
     */
    public String getX733AlarmType(
    ) {
        return this.x733AlarmType;
    }

    /**
     * Returns the value of field 'x733ProbableCause'.
     * 
     * @return the value of field 'X733ProbableCause'.
     */
    public int getX733ProbableCause(
    ) {
        return this.x733ProbableCause;
    }

    /**
     * Method hasAlarmType.
     * 
     * @return true if at least one AlarmType has been added
     */
    public boolean hasAlarmType(
    ) {
        return alarmType != null;
    }

    /**
     * Method hasX733ProbableCause.
     * 
     * @return true if at least one X733ProbableCause has been added
     */
    public boolean hasX733ProbableCause(
    ) {
        return x733ProbableCause != null;
    }

    public boolean hasAutoClean(
    ) {
        return autoClean != null;
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
        
        if (reductionKey != null) {
           result = 37 * result + reductionKey.hashCode();
        }
        if (alarmType != null) {
        	result = 37 * result + alarmType;
        }
        if (clearKey != null) {
           result = 37 * result + clearKey.hashCode();
        }
        if (autoClean != null) {
        	result = 37 * result + (autoClean?0:1);
        }
        if (x733AlarmType != null) {
           result = 37 * result + x733AlarmType.hashCode();
        }
        if (x733ProbableCause != null) {
        	result = 37 * result + x733ProbableCause;
        }
        return result;
    }

    /**
     * Returns the value of field 'autoClean'.
     * 
     * @return the value of field 'AutoClean'.
     */
    public boolean isAutoClean(
    ) {
        return this.autoClean;
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
        this.alarmType = alarmType;
    }

    /**
     * Sets the value of field 'autoClean'.
     * 
     * @param autoClean the value of field 'autoClean'.
     */
    public void setAutoClean(
            final boolean autoClean) {
        this.autoClean = autoClean;
    }

    /**
     * Sets the value of field 'clearKey'.
     * 
     * @param clearKey the value of field 'clearKey'.
     */
    public void setClearKey(
            final String clearKey) {
        this.clearKey = clearKey;
    }

    /**
     * Sets the value of field 'reductionKey'.
     * 
     * @param reductionKey the value of field 'reductionKey'.
     */
    public void setReductionKey(
            final String reductionKey) {
        this.reductionKey = reductionKey;
    }

    /**
     * Sets the value of field 'x733AlarmType'.
     * 
     * @param x733AlarmType the value of field 'x733AlarmType'.
     */
    public void setX733AlarmType(
            final String x733AlarmType) {
        this.x733AlarmType = x733AlarmType;
    }

    /**
     * Sets the value of field 'x733ProbableCause'.
     * 
     * @param x733ProbableCause the value of field
     * 'x733ProbableCause'.
     */
    public void setX733ProbableCause(
            final int x733ProbableCause) {
        this.x733ProbableCause = x733ProbableCause;
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
     * org.opennms.netmgt.xml.eventconf.AlarmData
     */
    public static org.opennms.netmgt.xml.eventconf.AlarmData unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.AlarmData) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.AlarmData.class, reader);
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

	public void deleteAutoClean() {
		autoClean = null;
	}


}
