/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.7</a>, using an XML
 * Schema.
 * $Id: GraphLine.java,v 1.2 2005/11/03 20:43:58 brozow Exp $
 */

package org.opennms.netmgt.config.rrd;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class GraphLine.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/11/03 20:43:58 $
 */
public class GraphLine implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _datasource
     */
    private java.lang.String _datasource;

    /**
     * Field _time1
     */
    private java.lang.String _time1;

    /**
     * Field _time2
     */
    private java.lang.String _time2;

    /**
     * Field _value1
     */
    private double _value1;

    /**
     * keeps track of state for field: _value1
     */
    private boolean _has_value1;

    /**
     * Field _value2
     */
    private double _value2;

    /**
     * keeps track of state for field: _value2
     */
    private boolean _has_value2;

    /**
     * Field _color
     */
    private java.lang.String _color;

    /**
     * Field _legend
     */
    private java.lang.String _legend;

    /**
     * Field _width
     */
    private int _width;

    /**
     * keeps track of state for field: _width
     */
    private boolean _has_width;


      //----------------/
     //- Constructors -/
    //----------------/

    public GraphLine() 
     {
        super();
    } //-- org.opennms.netmgt.config.rrd.GraphLine()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method deleteValue1
     * 
     */
    public void deleteValue1()
    {
        this._has_value1= false;
    } //-- void deleteValue1() 

    /**
     * Method deleteValue2
     * 
     */
    public void deleteValue2()
    {
        this._has_value2= false;
    } //-- void deleteValue2() 

    /**
     * Method deleteWidth
     * 
     */
    public void deleteWidth()
    {
        this._has_width= false;
    } //-- void deleteWidth() 

    /**
     * Returns the value of field 'color'.
     * 
     * @return String
     * @return the value of field 'color'.
     */
    public java.lang.String getColor()
    {
        return this._color;
    } //-- java.lang.String getColor() 

    /**
     * Returns the value of field 'datasource'.
     * 
     * @return String
     * @return the value of field 'datasource'.
     */
    public java.lang.String getDatasource()
    {
        return this._datasource;
    } //-- java.lang.String getDatasource() 

    /**
     * Returns the value of field 'legend'.
     * 
     * @return String
     * @return the value of field 'legend'.
     */
    public java.lang.String getLegend()
    {
        return this._legend;
    } //-- java.lang.String getLegend() 

    /**
     * Returns the value of field 'time1'.
     * 
     * @return String
     * @return the value of field 'time1'.
     */
    public java.lang.String getTime1()
    {
        return this._time1;
    } //-- java.lang.String getTime1() 

    /**
     * Returns the value of field 'time2'.
     * 
     * @return String
     * @return the value of field 'time2'.
     */
    public java.lang.String getTime2()
    {
        return this._time2;
    } //-- java.lang.String getTime2() 

    /**
     * Returns the value of field 'value1'.
     * 
     * @return double
     * @return the value of field 'value1'.
     */
    public double getValue1()
    {
        return this._value1;
    } //-- double getValue1() 

    /**
     * Returns the value of field 'value2'.
     * 
     * @return double
     * @return the value of field 'value2'.
     */
    public double getValue2()
    {
        return this._value2;
    } //-- double getValue2() 

    /**
     * Returns the value of field 'width'.
     * 
     * @return int
     * @return the value of field 'width'.
     */
    public int getWidth()
    {
        return this._width;
    } //-- int getWidth() 

    /**
     * Method hasValue1
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasValue1()
    {
        return this._has_value1;
    } //-- boolean hasValue1() 

    /**
     * Method hasValue2
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasValue2()
    {
        return this._has_value2;
    } //-- boolean hasValue2() 

    /**
     * Method hasWidth
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasWidth()
    {
        return this._has_width;
    } //-- boolean hasWidth() 

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
     * Sets the value of field 'color'.
     * 
     * @param color the value of field 'color'.
     */
    public void setColor(java.lang.String color)
    {
        this._color = color;
    } //-- void setColor(java.lang.String) 

    /**
     * Sets the value of field 'datasource'.
     * 
     * @param datasource the value of field 'datasource'.
     */
    public void setDatasource(java.lang.String datasource)
    {
        this._datasource = datasource;
    } //-- void setDatasource(java.lang.String) 

    /**
     * Sets the value of field 'legend'.
     * 
     * @param legend the value of field 'legend'.
     */
    public void setLegend(java.lang.String legend)
    {
        this._legend = legend;
    } //-- void setLegend(java.lang.String) 

    /**
     * Sets the value of field 'time1'.
     * 
     * @param time1 the value of field 'time1'.
     */
    public void setTime1(java.lang.String time1)
    {
        this._time1 = time1;
    } //-- void setTime1(java.lang.String) 

    /**
     * Sets the value of field 'time2'.
     * 
     * @param time2 the value of field 'time2'.
     */
    public void setTime2(java.lang.String time2)
    {
        this._time2 = time2;
    } //-- void setTime2(java.lang.String) 

    /**
     * Sets the value of field 'value1'.
     * 
     * @param value1 the value of field 'value1'.
     */
    public void setValue1(double value1)
    {
        this._value1 = value1;
        this._has_value1 = true;
    } //-- void setValue1(double) 

    /**
     * Sets the value of field 'value2'.
     * 
     * @param value2 the value of field 'value2'.
     */
    public void setValue2(double value2)
    {
        this._value2 = value2;
        this._has_value2 = true;
    } //-- void setValue2(double) 

    /**
     * Sets the value of field 'width'.
     * 
     * @param width the value of field 'width'.
     */
    public void setWidth(int width)
    {
        this._width = width;
        this._has_width = true;
    } //-- void setWidth(int) 

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
        return (org.opennms.netmgt.config.rrd.GraphLine) Unmarshaller.unmarshal(org.opennms.netmgt.config.rrd.GraphLine.class, reader);
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
