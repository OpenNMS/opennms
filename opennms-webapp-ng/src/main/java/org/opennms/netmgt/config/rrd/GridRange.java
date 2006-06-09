/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.7</a>, using an XML
 * Schema.
 * $Id: GridRange.java,v 1.2 2005/11/03 20:43:58 brozow Exp $
 */

package org.opennms.netmgt.config.rrd;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class GridRange.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/11/03 20:43:58 $
 */
public class GridRange implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _lower
     */
    private double _lower;

    /**
     * keeps track of state for field: _lower
     */
    private boolean _has_lower;

    /**
     * Field _upper
     */
    private double _upper;

    /**
     * keeps track of state for field: _upper
     */
    private boolean _has_upper;

    /**
     * Field _rigid
     */
    private boolean _rigid;

    /**
     * keeps track of state for field: _rigid
     */
    private boolean _has_rigid;


      //----------------/
     //- Constructors -/
    //----------------/

    public GridRange() 
     {
        super();
    } //-- org.opennms.netmgt.config.rrd.GridRange()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method deleteLower
     * 
     */
    public void deleteLower()
    {
        this._has_lower= false;
    } //-- void deleteLower() 

    /**
     * Method deleteRigid
     * 
     */
    public void deleteRigid()
    {
        this._has_rigid= false;
    } //-- void deleteRigid() 

    /**
     * Method deleteUpper
     * 
     */
    public void deleteUpper()
    {
        this._has_upper= false;
    } //-- void deleteUpper() 

    /**
     * Returns the value of field 'lower'.
     * 
     * @return double
     * @return the value of field 'lower'.
     */
    public double getLower()
    {
        return this._lower;
    } //-- double getLower() 

    /**
     * Returns the value of field 'rigid'.
     * 
     * @return boolean
     * @return the value of field 'rigid'.
     */
    public boolean getRigid()
    {
        return this._rigid;
    } //-- boolean getRigid() 

    /**
     * Returns the value of field 'upper'.
     * 
     * @return double
     * @return the value of field 'upper'.
     */
    public double getUpper()
    {
        return this._upper;
    } //-- double getUpper() 

    /**
     * Method hasLower
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasLower()
    {
        return this._has_lower;
    } //-- boolean hasLower() 

    /**
     * Method hasRigid
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasRigid()
    {
        return this._has_rigid;
    } //-- boolean hasRigid() 

    /**
     * Method hasUpper
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasUpper()
    {
        return this._has_upper;
    } //-- boolean hasUpper() 

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
     * Sets the value of field 'lower'.
     * 
     * @param lower the value of field 'lower'.
     */
    public void setLower(double lower)
    {
        this._lower = lower;
        this._has_lower = true;
    } //-- void setLower(double) 

    /**
     * Sets the value of field 'rigid'.
     * 
     * @param rigid the value of field 'rigid'.
     */
    public void setRigid(boolean rigid)
    {
        this._rigid = rigid;
        this._has_rigid = true;
    } //-- void setRigid(boolean) 

    /**
     * Sets the value of field 'upper'.
     * 
     * @param upper the value of field 'upper'.
     */
    public void setUpper(double upper)
    {
        this._upper = upper;
        this._has_upper = true;
    } //-- void setUpper(double) 

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
        return (org.opennms.netmgt.config.rrd.GridRange) Unmarshaller.unmarshal(org.opennms.netmgt.config.rrd.GridRange.class, reader);
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
