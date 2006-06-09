/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.7</a>, using an XML
 * Schema.
 * $Id: GraphGprint.java,v 1.2 2005/11/03 20:43:58 brozow Exp $
 */

package org.opennms.netmgt.config.rrd;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class GraphGprint.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/11/03 20:43:58 $
 */
public class GraphGprint implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _datasource
     */
    private java.lang.String _datasource;

    /**
     * Field _cf
     */
    private org.opennms.netmgt.config.rrd.types.Cf _cf;

    /**
     * Field _format
     */
    private java.lang.String _format;

    /**
     * Field _base
     */
    private int _base;

    /**
     * keeps track of state for field: _base
     */
    private boolean _has_base;


      //----------------/
     //- Constructors -/
    //----------------/

    public GraphGprint() 
     {
        super();
    } //-- org.opennms.netmgt.config.rrd.GraphGprint()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method deleteBase
     * 
     */
    public void deleteBase()
    {
        this._has_base= false;
    } //-- void deleteBase() 

    /**
     * Returns the value of field 'base'.
     * 
     * @return int
     * @return the value of field 'base'.
     */
    public int getBase()
    {
        return this._base;
    } //-- int getBase() 

    /**
     * Returns the value of field 'cf'.
     * 
     * @return Cf
     * @return the value of field 'cf'.
     */
    public org.opennms.netmgt.config.rrd.types.Cf getCf()
    {
        return this._cf;
    } //-- org.opennms.netmgt.config.rrd.types.Cf getCf() 

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
     * Returns the value of field 'format'.
     * 
     * @return String
     * @return the value of field 'format'.
     */
    public java.lang.String getFormat()
    {
        return this._format;
    } //-- java.lang.String getFormat() 

    /**
     * Method hasBase
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasBase()
    {
        return this._has_base;
    } //-- boolean hasBase() 

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
     * Sets the value of field 'base'.
     * 
     * @param base the value of field 'base'.
     */
    public void setBase(int base)
    {
        this._base = base;
        this._has_base = true;
    } //-- void setBase(int) 

    /**
     * Sets the value of field 'cf'.
     * 
     * @param cf the value of field 'cf'.
     */
    public void setCf(org.opennms.netmgt.config.rrd.types.Cf cf)
    {
        this._cf = cf;
    } //-- void setCf(org.opennms.netmgt.config.rrd.types.Cf) 

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
     * Sets the value of field 'format'.
     * 
     * @param format the value of field 'format'.
     */
    public void setFormat(java.lang.String format)
    {
        this._format = format;
    } //-- void setFormat(java.lang.String) 

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
        return (org.opennms.netmgt.config.rrd.GraphGprint) Unmarshaller.unmarshal(org.opennms.netmgt.config.rrd.GraphGprint.class, reader);
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
