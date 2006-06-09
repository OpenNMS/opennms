/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.7</a>, using an XML
 * Schema.
 * $Id: TimeAxis.java,v 1.2 2005/11/03 20:43:58 brozow Exp $
 */

package org.opennms.netmgt.config.rrd;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class TimeAxis.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/11/03 20:43:58 $
 */
public class TimeAxis implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _min_grid_time_unit
     */
    private org.opennms.netmgt.config.rrd.types.GridTimeUnit _min_grid_time_unit;

    /**
     * Field _min_grid_unit_steps
     */
    private int _min_grid_unit_steps;

    /**
     * keeps track of state for field: _min_grid_unit_steps
     */
    private boolean _has_min_grid_unit_steps;

    /**
     * Field _maj_grid_time_unit
     */
    private org.opennms.netmgt.config.rrd.types.GridTimeUnit _maj_grid_time_unit;

    /**
     * Field _maj_grid_unit_steps
     */
    private int _maj_grid_unit_steps;

    /**
     * keeps track of state for field: _maj_grid_unit_steps
     */
    private boolean _has_maj_grid_unit_steps;

    /**
     * XXX: the type should probably be a simpleType that we
     *  define with a pattern of allowable formats.
     */
    private java.lang.String _date_format;

    /**
     * Field _center_labels
     */
    private boolean _center_labels;

    /**
     * keeps track of state for field: _center_labels
     */
    private boolean _has_center_labels;

    /**
     * Field _first_day_of_week
     */
    private org.opennms.netmgt.config.rrd.types.DayOfWeek _first_day_of_week;


      //----------------/
     //- Constructors -/
    //----------------/

    public TimeAxis() 
     {
        super();
    } //-- org.opennms.netmgt.config.rrd.TimeAxis()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method deleteCenter_labels
     * 
     */
    public void deleteCenter_labels()
    {
        this._has_center_labels= false;
    } //-- void deleteCenter_labels() 

    /**
     * Method deleteMaj_grid_unit_steps
     * 
     */
    public void deleteMaj_grid_unit_steps()
    {
        this._has_maj_grid_unit_steps= false;
    } //-- void deleteMaj_grid_unit_steps() 

    /**
     * Method deleteMin_grid_unit_steps
     * 
     */
    public void deleteMin_grid_unit_steps()
    {
        this._has_min_grid_unit_steps= false;
    } //-- void deleteMin_grid_unit_steps() 

    /**
     * Returns the value of field 'center_labels'.
     * 
     * @return boolean
     * @return the value of field 'center_labels'.
     */
    public boolean getCenter_labels()
    {
        return this._center_labels;
    } //-- boolean getCenter_labels() 

    /**
     * Returns the value of field 'date_format'. The field
     * 'date_format' has the following description: XXX: the type
     * should probably be a simpleType that we
     *  define with a pattern of allowable formats.
     * 
     * @return String
     * @return the value of field 'date_format'.
     */
    public java.lang.String getDate_format()
    {
        return this._date_format;
    } //-- java.lang.String getDate_format() 

    /**
     * Returns the value of field 'first_day_of_week'.
     * 
     * @return DayOfWeek
     * @return the value of field 'first_day_of_week'.
     */
    public org.opennms.netmgt.config.rrd.types.DayOfWeek getFirst_day_of_week()
    {
        return this._first_day_of_week;
    } //-- org.opennms.netmgt.config.rrd.types.DayOfWeek getFirst_day_of_week() 

    /**
     * Returns the value of field 'maj_grid_time_unit'.
     * 
     * @return GridTimeUnit
     * @return the value of field 'maj_grid_time_unit'.
     */
    public org.opennms.netmgt.config.rrd.types.GridTimeUnit getMaj_grid_time_unit()
    {
        return this._maj_grid_time_unit;
    } //-- org.opennms.netmgt.config.rrd.types.GridTimeUnit getMaj_grid_time_unit() 

    /**
     * Returns the value of field 'maj_grid_unit_steps'.
     * 
     * @return int
     * @return the value of field 'maj_grid_unit_steps'.
     */
    public int getMaj_grid_unit_steps()
    {
        return this._maj_grid_unit_steps;
    } //-- int getMaj_grid_unit_steps() 

    /**
     * Returns the value of field 'min_grid_time_unit'.
     * 
     * @return GridTimeUnit
     * @return the value of field 'min_grid_time_unit'.
     */
    public org.opennms.netmgt.config.rrd.types.GridTimeUnit getMin_grid_time_unit()
    {
        return this._min_grid_time_unit;
    } //-- org.opennms.netmgt.config.rrd.types.GridTimeUnit getMin_grid_time_unit() 

    /**
     * Returns the value of field 'min_grid_unit_steps'.
     * 
     * @return int
     * @return the value of field 'min_grid_unit_steps'.
     */
    public int getMin_grid_unit_steps()
    {
        return this._min_grid_unit_steps;
    } //-- int getMin_grid_unit_steps() 

    /**
     * Method hasCenter_labels
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasCenter_labels()
    {
        return this._has_center_labels;
    } //-- boolean hasCenter_labels() 

    /**
     * Method hasMaj_grid_unit_steps
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasMaj_grid_unit_steps()
    {
        return this._has_maj_grid_unit_steps;
    } //-- boolean hasMaj_grid_unit_steps() 

    /**
     * Method hasMin_grid_unit_steps
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasMin_grid_unit_steps()
    {
        return this._has_min_grid_unit_steps;
    } //-- boolean hasMin_grid_unit_steps() 

    /**
     * Method isValid
     * 
     * 
     * 
     * @return boolean
     */
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param out
     */
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param handler
     */
    public void marshal(org.xml.sax.ContentHandler handler)
        throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Sets the value of field 'center_labels'.
     * 
     * @param center_labels the value of field 'center_labels'.
     */
    public void setCenter_labels(boolean center_labels)
    {
        this._center_labels = center_labels;
        this._has_center_labels = true;
    } //-- void setCenter_labels(boolean) 

    /**
     * Sets the value of field 'date_format'. The field
     * 'date_format' has the following description: XXX: the type
     * should probably be a simpleType that we
     *  define with a pattern of allowable formats.
     * 
     * @param date_format the value of field 'date_format'.
     */
    public void setDate_format(java.lang.String date_format)
    {
        this._date_format = date_format;
    } //-- void setDate_format(java.lang.String) 

    /**
     * Sets the value of field 'first_day_of_week'.
     * 
     * @param first_day_of_week the value of field
     * 'first_day_of_week'.
     */
    public void setFirst_day_of_week(org.opennms.netmgt.config.rrd.types.DayOfWeek first_day_of_week)
    {
        this._first_day_of_week = first_day_of_week;
    } //-- void setFirst_day_of_week(org.opennms.netmgt.config.rrd.types.DayOfWeek) 

    /**
     * Sets the value of field 'maj_grid_time_unit'.
     * 
     * @param maj_grid_time_unit the value of field
     * 'maj_grid_time_unit'.
     */
    public void setMaj_grid_time_unit(org.opennms.netmgt.config.rrd.types.GridTimeUnit maj_grid_time_unit)
    {
        this._maj_grid_time_unit = maj_grid_time_unit;
    } //-- void setMaj_grid_time_unit(org.opennms.netmgt.config.rrd.types.GridTimeUnit) 

    /**
     * Sets the value of field 'maj_grid_unit_steps'.
     * 
     * @param maj_grid_unit_steps the value of field
     * 'maj_grid_unit_steps'.
     */
    public void setMaj_grid_unit_steps(int maj_grid_unit_steps)
    {
        this._maj_grid_unit_steps = maj_grid_unit_steps;
        this._has_maj_grid_unit_steps = true;
    } //-- void setMaj_grid_unit_steps(int) 

    /**
     * Sets the value of field 'min_grid_time_unit'.
     * 
     * @param min_grid_time_unit the value of field
     * 'min_grid_time_unit'.
     */
    public void setMin_grid_time_unit(org.opennms.netmgt.config.rrd.types.GridTimeUnit min_grid_time_unit)
    {
        this._min_grid_time_unit = min_grid_time_unit;
    } //-- void setMin_grid_time_unit(org.opennms.netmgt.config.rrd.types.GridTimeUnit) 

    /**
     * Sets the value of field 'min_grid_unit_steps'.
     * 
     * @param min_grid_unit_steps the value of field
     * 'min_grid_unit_steps'.
     */
    public void setMin_grid_unit_steps(int min_grid_unit_steps)
    {
        this._min_grid_unit_steps = min_grid_unit_steps;
        this._has_min_grid_unit_steps = true;
    } //-- void setMin_grid_unit_steps(int) 

    /**
     * Method unmarshal
     * 
     * 
     * 
     * @param reader
     * @return Object
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.opennms.netmgt.config.rrd.TimeAxis) Unmarshaller.unmarshal(org.opennms.netmgt.config.rrd.TimeAxis.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     * 
     */
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
