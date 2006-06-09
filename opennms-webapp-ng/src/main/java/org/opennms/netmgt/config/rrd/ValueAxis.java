/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.7</a>, using an XML
 * Schema.
 * $Id: ValueAxis.java,v 1.2 2005/11/03 20:43:58 brozow Exp $
 */

package org.opennms.netmgt.config.rrd;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class ValueAxis.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/11/03 20:43:58 $
 */
public class ValueAxis implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _grid_step
     */
    private int _grid_step;

    /**
     * keeps track of state for field: _grid_step
     */
    private boolean _has_grid_step;

    /**
     * Field _label_step
     */
    private int _label_step;

    /**
     * keeps track of state for field: _label_step
     */
    private boolean _has_label_step;


      //----------------/
     //- Constructors -/
    //----------------/

    public ValueAxis() 
     {
        super();
    } //-- org.opennms.netmgt.config.rrd.ValueAxis()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method deleteGrid_step
     * 
     */
    public void deleteGrid_step()
    {
        this._has_grid_step= false;
    } //-- void deleteGrid_step() 

    /**
     * Method deleteLabel_step
     * 
     */
    public void deleteLabel_step()
    {
        this._has_label_step= false;
    } //-- void deleteLabel_step() 

    /**
     * Returns the value of field 'grid_step'.
     * 
     * @return int
     * @return the value of field 'grid_step'.
     */
    public int getGrid_step()
    {
        return this._grid_step;
    } //-- int getGrid_step() 

    /**
     * Returns the value of field 'label_step'.
     * 
     * @return int
     * @return the value of field 'label_step'.
     */
    public int getLabel_step()
    {
        return this._label_step;
    } //-- int getLabel_step() 

    /**
     * Method hasGrid_step
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasGrid_step()
    {
        return this._has_grid_step;
    } //-- boolean hasGrid_step() 

    /**
     * Method hasLabel_step
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasLabel_step()
    {
        return this._has_label_step;
    } //-- boolean hasLabel_step() 

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
     * Sets the value of field 'grid_step'.
     * 
     * @param grid_step the value of field 'grid_step'.
     */
    public void setGrid_step(int grid_step)
    {
        this._grid_step = grid_step;
        this._has_grid_step = true;
    } //-- void setGrid_step(int) 

    /**
     * Sets the value of field 'label_step'.
     * 
     * @param label_step the value of field 'label_step'.
     */
    public void setLabel_step(int label_step)
    {
        this._label_step = label_step;
        this._has_label_step = true;
    } //-- void setLabel_step(int) 

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
        return (org.opennms.netmgt.config.rrd.ValueAxis) Unmarshaller.unmarshal(org.opennms.netmgt.config.rrd.ValueAxis.class, reader);
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
