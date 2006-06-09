/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.7</a>, using an XML
 * Schema.
 * $Id: GraphChoice.java,v 1.2 2005/11/03 20:43:58 brozow Exp $
 */

package org.opennms.netmgt.config.rrd;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class GraphChoice.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/11/03 20:43:58 $
 */
public class GraphChoice implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Internal choice value storage
     */
    private java.lang.Object _choiceValue;

    /**
     * Field _area
     */
    private org.opennms.netmgt.config.rrd.Area _area;

    /**
     * Field _stack
     */
    private org.opennms.netmgt.config.rrd.Stack _stack;

    /**
     * Field _line
     */
    private org.opennms.netmgt.config.rrd.Line _line;

    /**
     * Field _gprint
     */
    private org.opennms.netmgt.config.rrd.Gprint _gprint;

    /**
     * Field _hrule
     */
    private org.opennms.netmgt.config.rrd.Hrule _hrule;

    /**
     * Field _vrule
     */
    private org.opennms.netmgt.config.rrd.Vrule _vrule;

    /**
     * Field _time
     */
    private org.opennms.netmgt.config.rrd.Time _time;

    /**
     * Field _comment
     */
    private java.lang.String _comment;


      //----------------/
     //- Constructors -/
    //----------------/

    public GraphChoice() 
     {
        super();
    } //-- org.opennms.netmgt.config.rrd.GraphChoice()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'area'.
     * 
     * @return Area
     * @return the value of field 'area'.
     */
    public org.opennms.netmgt.config.rrd.Area getArea()
    {
        return this._area;
    } //-- org.opennms.netmgt.config.rrd.Area getArea() 

    /**
     * Returns the value of field 'choiceValue'. The field
     * 'choiceValue' has the following description: Internal choice
     * value storage
     * 
     * @return Object
     * @return the value of field 'choiceValue'.
     */
    public java.lang.Object getChoiceValue()
    {
        return this._choiceValue;
    } //-- java.lang.Object getChoiceValue() 

    /**
     * Returns the value of field 'comment'.
     * 
     * @return String
     * @return the value of field 'comment'.
     */
    public java.lang.String getComment()
    {
        return this._comment;
    } //-- java.lang.String getComment() 

    /**
     * Returns the value of field 'gprint'.
     * 
     * @return Gprint
     * @return the value of field 'gprint'.
     */
    public org.opennms.netmgt.config.rrd.Gprint getGprint()
    {
        return this._gprint;
    } //-- org.opennms.netmgt.config.rrd.Gprint getGprint() 

    /**
     * Returns the value of field 'hrule'.
     * 
     * @return Hrule
     * @return the value of field 'hrule'.
     */
    public org.opennms.netmgt.config.rrd.Hrule getHrule()
    {
        return this._hrule;
    } //-- org.opennms.netmgt.config.rrd.Hrule getHrule() 

    /**
     * Returns the value of field 'line'.
     * 
     * @return Line
     * @return the value of field 'line'.
     */
    public org.opennms.netmgt.config.rrd.Line getLine()
    {
        return this._line;
    } //-- org.opennms.netmgt.config.rrd.Line getLine() 

    /**
     * Returns the value of field 'stack'.
     * 
     * @return Stack
     * @return the value of field 'stack'.
     */
    public org.opennms.netmgt.config.rrd.Stack getStack()
    {
        return this._stack;
    } //-- org.opennms.netmgt.config.rrd.Stack getStack() 

    /**
     * Returns the value of field 'time'.
     * 
     * @return Time
     * @return the value of field 'time'.
     */
    public org.opennms.netmgt.config.rrd.Time getTime()
    {
        return this._time;
    } //-- org.opennms.netmgt.config.rrd.Time getTime() 

    /**
     * Returns the value of field 'vrule'.
     * 
     * @return Vrule
     * @return the value of field 'vrule'.
     */
    public org.opennms.netmgt.config.rrd.Vrule getVrule()
    {
        return this._vrule;
    } //-- org.opennms.netmgt.config.rrd.Vrule getVrule() 

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
     * Sets the value of field 'area'.
     * 
     * @param area the value of field 'area'.
     */
    public void setArea(org.opennms.netmgt.config.rrd.Area area)
    {
        this._area = area;
        this._choiceValue = area;
    } //-- void setArea(org.opennms.netmgt.config.rrd.Area) 

    /**
     * Sets the value of field 'comment'.
     * 
     * @param comment the value of field 'comment'.
     */
    public void setComment(java.lang.String comment)
    {
        this._comment = comment;
        this._choiceValue = comment;
    } //-- void setComment(java.lang.String) 

    /**
     * Sets the value of field 'gprint'.
     * 
     * @param gprint the value of field 'gprint'.
     */
    public void setGprint(org.opennms.netmgt.config.rrd.Gprint gprint)
    {
        this._gprint = gprint;
        this._choiceValue = gprint;
    } //-- void setGprint(org.opennms.netmgt.config.rrd.Gprint) 

    /**
     * Sets the value of field 'hrule'.
     * 
     * @param hrule the value of field 'hrule'.
     */
    public void setHrule(org.opennms.netmgt.config.rrd.Hrule hrule)
    {
        this._hrule = hrule;
        this._choiceValue = hrule;
    } //-- void setHrule(org.opennms.netmgt.config.rrd.Hrule) 

    /**
     * Sets the value of field 'line'.
     * 
     * @param line the value of field 'line'.
     */
    public void setLine(org.opennms.netmgt.config.rrd.Line line)
    {
        this._line = line;
        this._choiceValue = line;
    } //-- void setLine(org.opennms.netmgt.config.rrd.Line) 

    /**
     * Sets the value of field 'stack'.
     * 
     * @param stack the value of field 'stack'.
     */
    public void setStack(org.opennms.netmgt.config.rrd.Stack stack)
    {
        this._stack = stack;
        this._choiceValue = stack;
    } //-- void setStack(org.opennms.netmgt.config.rrd.Stack) 

    /**
     * Sets the value of field 'time'.
     * 
     * @param time the value of field 'time'.
     */
    public void setTime(org.opennms.netmgt.config.rrd.Time time)
    {
        this._time = time;
        this._choiceValue = time;
    } //-- void setTime(org.opennms.netmgt.config.rrd.Time) 

    /**
     * Sets the value of field 'vrule'.
     * 
     * @param vrule the value of field 'vrule'.
     */
    public void setVrule(org.opennms.netmgt.config.rrd.Vrule vrule)
    {
        this._vrule = vrule;
        this._choiceValue = vrule;
    } //-- void setVrule(org.opennms.netmgt.config.rrd.Vrule) 

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
        return (org.opennms.netmgt.config.rrd.GraphChoice) Unmarshaller.unmarshal(org.opennms.netmgt.config.rrd.GraphChoice.class, reader);
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
