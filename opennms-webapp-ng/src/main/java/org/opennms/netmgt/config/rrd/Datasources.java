/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.7</a>, using an XML
 * Schema.
 * $Id: Datasources.java,v 1.2 2005/11/03 20:43:58 brozow Exp $
 */

package org.opennms.netmgt.config.rrd;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Datasources.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/11/03 20:43:58 $
 */
public class Datasources implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _defList
     */
    private java.util.Vector _defList;

    /**
     * Field _export_dataList
     */
    private java.util.Vector _export_dataList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Datasources() 
     {
        super();
        _defList = new Vector();
        _export_dataList = new Vector();
    } //-- org.opennms.netmgt.config.rrd.Datasources()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addDef
     * 
     * 
     * 
     * @param vDef
     */
    public void addDef(org.opennms.netmgt.config.rrd.Def vDef)
        throws java.lang.IndexOutOfBoundsException
    {
        _defList.addElement(vDef);
    } //-- void addDef(org.opennms.netmgt.config.rrd.Def) 

    /**
     * Method addDef
     * 
     * 
     * 
     * @param index
     * @param vDef
     */
    public void addDef(int index, org.opennms.netmgt.config.rrd.Def vDef)
        throws java.lang.IndexOutOfBoundsException
    {
        _defList.insertElementAt(vDef, index);
    } //-- void addDef(int, org.opennms.netmgt.config.rrd.Def) 

    /**
     * Method addExport_data
     * 
     * 
     * 
     * @param vExport_data
     */
    public void addExport_data(org.opennms.netmgt.config.rrd.Export_data vExport_data)
        throws java.lang.IndexOutOfBoundsException
    {
        _export_dataList.addElement(vExport_data);
    } //-- void addExport_data(org.opennms.netmgt.config.rrd.Export_data) 

    /**
     * Method addExport_data
     * 
     * 
     * 
     * @param index
     * @param vExport_data
     */
    public void addExport_data(int index, org.opennms.netmgt.config.rrd.Export_data vExport_data)
        throws java.lang.IndexOutOfBoundsException
    {
        _export_dataList.insertElementAt(vExport_data, index);
    } //-- void addExport_data(int, org.opennms.netmgt.config.rrd.Export_data) 

    /**
     * Method enumerateDef
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateDef()
    {
        return _defList.elements();
    } //-- java.util.Enumeration enumerateDef() 

    /**
     * Method enumerateExport_data
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateExport_data()
    {
        return _export_dataList.elements();
    } //-- java.util.Enumeration enumerateExport_data() 

    /**
     * Method getDef
     * 
     * 
     * 
     * @param index
     * @return Def
     */
    public org.opennms.netmgt.config.rrd.Def getDef(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _defList.size())) {
            throw new IndexOutOfBoundsException("getDef: Index value '"+index+"' not in range [0.."+_defList.size()+ "]");
        }
        
        return (org.opennms.netmgt.config.rrd.Def) _defList.elementAt(index);
    } //-- org.opennms.netmgt.config.rrd.Def getDef(int) 

    /**
     * Method getDef
     * 
     * 
     * 
     * @return Def
     */
    public org.opennms.netmgt.config.rrd.Def[] getDef()
    {
        int size = _defList.size();
        org.opennms.netmgt.config.rrd.Def[] mArray = new org.opennms.netmgt.config.rrd.Def[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.opennms.netmgt.config.rrd.Def) _defList.elementAt(index);
        }
        return mArray;
    } //-- org.opennms.netmgt.config.rrd.Def[] getDef() 

    /**
     * Method getDefCount
     * 
     * 
     * 
     * @return int
     */
    public int getDefCount()
    {
        return _defList.size();
    } //-- int getDefCount() 

    /**
     * Method getExport_data
     * 
     * 
     * 
     * @param index
     * @return Export_data
     */
    public org.opennms.netmgt.config.rrd.Export_data getExport_data(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _export_dataList.size())) {
            throw new IndexOutOfBoundsException("getExport_data: Index value '"+index+"' not in range [0.."+_export_dataList.size()+ "]");
        }
        
        return (org.opennms.netmgt.config.rrd.Export_data) _export_dataList.elementAt(index);
    } //-- org.opennms.netmgt.config.rrd.Export_data getExport_data(int) 

    /**
     * Method getExport_data
     * 
     * 
     * 
     * @return Export_data
     */
    public org.opennms.netmgt.config.rrd.Export_data[] getExport_data()
    {
        int size = _export_dataList.size();
        org.opennms.netmgt.config.rrd.Export_data[] mArray = new org.opennms.netmgt.config.rrd.Export_data[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.opennms.netmgt.config.rrd.Export_data) _export_dataList.elementAt(index);
        }
        return mArray;
    } //-- org.opennms.netmgt.config.rrd.Export_data[] getExport_data() 

    /**
     * Method getExport_dataCount
     * 
     * 
     * 
     * @return int
     */
    public int getExport_dataCount()
    {
        return _export_dataList.size();
    } //-- int getExport_dataCount() 

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
     * Method removeAllDef
     * 
     */
    public void removeAllDef()
    {
        _defList.removeAllElements();
    } //-- void removeAllDef() 

    /**
     * Method removeAllExport_data
     * 
     */
    public void removeAllExport_data()
    {
        _export_dataList.removeAllElements();
    } //-- void removeAllExport_data() 

    /**
     * Method removeDef
     * 
     * 
     * 
     * @param index
     * @return Def
     */
    public org.opennms.netmgt.config.rrd.Def removeDef(int index)
    {
        java.lang.Object obj = _defList.elementAt(index);
        _defList.removeElementAt(index);
        return (org.opennms.netmgt.config.rrd.Def) obj;
    } //-- org.opennms.netmgt.config.rrd.Def removeDef(int) 

    /**
     * Method removeExport_data
     * 
     * 
     * 
     * @param index
     * @return Export_data
     */
    public org.opennms.netmgt.config.rrd.Export_data removeExport_data(int index)
    {
        java.lang.Object obj = _export_dataList.elementAt(index);
        _export_dataList.removeElementAt(index);
        return (org.opennms.netmgt.config.rrd.Export_data) obj;
    } //-- org.opennms.netmgt.config.rrd.Export_data removeExport_data(int) 

    /**
     * Method setDef
     * 
     * 
     * 
     * @param index
     * @param vDef
     */
    public void setDef(int index, org.opennms.netmgt.config.rrd.Def vDef)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _defList.size())) {
            throw new IndexOutOfBoundsException("setDef: Index value '"+index+"' not in range [0.."+_defList.size()+ "]");
        }
        _defList.setElementAt(vDef, index);
    } //-- void setDef(int, org.opennms.netmgt.config.rrd.Def) 

    /**
     * Method setDef
     * 
     * 
     * 
     * @param defArray
     */
    public void setDef(org.opennms.netmgt.config.rrd.Def[] defArray)
    {
        //-- copy array
        _defList.removeAllElements();
        for (int i = 0; i < defArray.length; i++) {
            _defList.addElement(defArray[i]);
        }
    } //-- void setDef(org.opennms.netmgt.config.rrd.Def) 

    /**
     * Method setExport_data
     * 
     * 
     * 
     * @param index
     * @param vExport_data
     */
    public void setExport_data(int index, org.opennms.netmgt.config.rrd.Export_data vExport_data)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _export_dataList.size())) {
            throw new IndexOutOfBoundsException("setExport_data: Index value '"+index+"' not in range [0.."+_export_dataList.size()+ "]");
        }
        _export_dataList.setElementAt(vExport_data, index);
    } //-- void setExport_data(int, org.opennms.netmgt.config.rrd.Export_data) 

    /**
     * Method setExport_data
     * 
     * 
     * 
     * @param export_dataArray
     */
    public void setExport_data(org.opennms.netmgt.config.rrd.Export_data[] export_dataArray)
    {
        //-- copy array
        _export_dataList.removeAllElements();
        for (int i = 0; i < export_dataArray.length; i++) {
            _export_dataList.addElement(export_dataArray[i]);
        }
    } //-- void setExport_data(org.opennms.netmgt.config.rrd.Export_data) 

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
        return (org.opennms.netmgt.config.rrd.Datasources) Unmarshaller.unmarshal(org.opennms.netmgt.config.rrd.Datasources.class, reader);
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
