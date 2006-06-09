/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.7</a>, using an XML
 * Schema.
 * $Id: DataSourceDef.java,v 1.2 2005/11/03 20:43:58 brozow Exp $
 */

package org.opennms.netmgt.config.rrd;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class DataSourceDef.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/11/03 20:43:58 $
 */
public class DataSourceDef implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name
     */
    private java.lang.String _name;

    /**
     * Field _rrd
     */
    private java.lang.String _rrd;

    /**
     * Field _source
     */
    private java.lang.String _source;

    /**
     * Field _cf
     */
    private org.opennms.netmgt.config.rrd.types.Cf _cf;

    /**
     * Field _backend
     */
    private org.opennms.netmgt.config.rrd.types.Backend _backend;

    /**
     * Field _rpn
     */
    private java.lang.String _rpn;


      //----------------/
     //- Constructors -/
    //----------------/

    public DataSourceDef() 
     {
        super();
    } //-- org.opennms.netmgt.config.rrd.DataSourceDef()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'backend'.
     * 
     * @return Backend
     * @return the value of field 'backend'.
     */
    public org.opennms.netmgt.config.rrd.types.Backend getBackend()
    {
        return this._backend;
    } //-- org.opennms.netmgt.config.rrd.types.Backend getBackend() 

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
     * Returns the value of field 'name'.
     * 
     * @return String
     * @return the value of field 'name'.
     */
    public java.lang.String getName()
    {
        return this._name;
    } //-- java.lang.String getName() 

    /**
     * Returns the value of field 'rpn'.
     * 
     * @return String
     * @return the value of field 'rpn'.
     */
    public java.lang.String getRpn()
    {
        return this._rpn;
    } //-- java.lang.String getRpn() 

    /**
     * Returns the value of field 'rrd'.
     * 
     * @return String
     * @return the value of field 'rrd'.
     */
    public java.lang.String getRrd()
    {
        return this._rrd;
    } //-- java.lang.String getRrd() 

    /**
     * Returns the value of field 'source'.
     * 
     * @return String
     * @return the value of field 'source'.
     */
    public java.lang.String getSource()
    {
        return this._source;
    } //-- java.lang.String getSource() 

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
     * Sets the value of field 'backend'.
     * 
     * @param backend the value of field 'backend'.
     */
    public void setBackend(org.opennms.netmgt.config.rrd.types.Backend backend)
    {
        this._backend = backend;
    } //-- void setBackend(org.opennms.netmgt.config.rrd.types.Backend) 

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
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(java.lang.String name)
    {
        this._name = name;
    } //-- void setName(java.lang.String) 

    /**
     * Sets the value of field 'rpn'.
     * 
     * @param rpn the value of field 'rpn'.
     */
    public void setRpn(java.lang.String rpn)
    {
        this._rpn = rpn;
    } //-- void setRpn(java.lang.String) 

    /**
     * Sets the value of field 'rrd'.
     * 
     * @param rrd the value of field 'rrd'.
     */
    public void setRrd(java.lang.String rrd)
    {
        this._rrd = rrd;
    } //-- void setRrd(java.lang.String) 

    /**
     * Sets the value of field 'source'.
     * 
     * @param source the value of field 'source'.
     */
    public void setSource(java.lang.String source)
    {
        this._source = source;
    } //-- void setSource(java.lang.String) 

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
        return (org.opennms.netmgt.config.rrd.DataSourceDef) Unmarshaller.unmarshal(org.opennms.netmgt.config.rrd.DataSourceDef.class, reader);
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
