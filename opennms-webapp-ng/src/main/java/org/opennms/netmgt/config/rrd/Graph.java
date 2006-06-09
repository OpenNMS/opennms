/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.7</a>, using an XML
 * Schema.
 * $Id: Graph.java,v 1.2 2005/11/03 20:43:58 brozow Exp $
 */

package org.opennms.netmgt.config.rrd;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Graph.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/11/03 20:43:58 $
 */
public class Graph implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _graph_elementList
     */
    private java.util.Vector _graph_elementList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Graph() 
     {
        super();
        _graph_elementList = new Vector();
    } //-- org.opennms.netmgt.config.rrd.Graph()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addGraph_element
     * 
     * 
     * 
     * @param vGraph_element
     */
    public void addGraph_element(org.opennms.netmgt.config.rrd.Graph_element vGraph_element)
        throws java.lang.IndexOutOfBoundsException
    {
        _graph_elementList.addElement(vGraph_element);
    } //-- void addGraph_element(org.opennms.netmgt.config.rrd.Graph_element) 

    /**
     * Method addGraph_element
     * 
     * 
     * 
     * @param index
     * @param vGraph_element
     */
    public void addGraph_element(int index, org.opennms.netmgt.config.rrd.Graph_element vGraph_element)
        throws java.lang.IndexOutOfBoundsException
    {
        _graph_elementList.insertElementAt(vGraph_element, index);
    } //-- void addGraph_element(int, org.opennms.netmgt.config.rrd.Graph_element) 

    /**
     * Method enumerateGraph_element
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateGraph_element()
    {
        return _graph_elementList.elements();
    } //-- java.util.Enumeration enumerateGraph_element() 

    /**
     * Method getGraph_element
     * 
     * 
     * 
     * @param index
     * @return Graph_element
     */
    public org.opennms.netmgt.config.rrd.Graph_element getGraph_element(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _graph_elementList.size())) {
            throw new IndexOutOfBoundsException("getGraph_element: Index value '"+index+"' not in range [0.."+_graph_elementList.size()+ "]");
        }
        
        return (org.opennms.netmgt.config.rrd.Graph_element) _graph_elementList.elementAt(index);
    } //-- org.opennms.netmgt.config.rrd.Graph_element getGraph_element(int) 

    /**
     * Method getGraph_element
     * 
     * 
     * 
     * @return Graph_element
     */
    public org.opennms.netmgt.config.rrd.Graph_element[] getGraph_element()
    {
        int size = _graph_elementList.size();
        org.opennms.netmgt.config.rrd.Graph_element[] mArray = new org.opennms.netmgt.config.rrd.Graph_element[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.opennms.netmgt.config.rrd.Graph_element) _graph_elementList.elementAt(index);
        }
        return mArray;
    } //-- org.opennms.netmgt.config.rrd.Graph_element[] getGraph_element() 

    /**
     * Method getGraph_elementCount
     * 
     * 
     * 
     * @return int
     */
    public int getGraph_elementCount()
    {
        return _graph_elementList.size();
    } //-- int getGraph_elementCount() 

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
     * Method removeAllGraph_element
     * 
     */
    public void removeAllGraph_element()
    {
        _graph_elementList.removeAllElements();
    } //-- void removeAllGraph_element() 

    /**
     * Method removeGraph_element
     * 
     * 
     * 
     * @param index
     * @return Graph_element
     */
    public org.opennms.netmgt.config.rrd.Graph_element removeGraph_element(int index)
    {
        java.lang.Object obj = _graph_elementList.elementAt(index);
        _graph_elementList.removeElementAt(index);
        return (org.opennms.netmgt.config.rrd.Graph_element) obj;
    } //-- org.opennms.netmgt.config.rrd.Graph_element removeGraph_element(int) 

    /**
     * Method setGraph_element
     * 
     * 
     * 
     * @param index
     * @param vGraph_element
     */
    public void setGraph_element(int index, org.opennms.netmgt.config.rrd.Graph_element vGraph_element)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _graph_elementList.size())) {
            throw new IndexOutOfBoundsException("setGraph_element: Index value '"+index+"' not in range [0.."+_graph_elementList.size()+ "]");
        }
        _graph_elementList.setElementAt(vGraph_element, index);
    } //-- void setGraph_element(int, org.opennms.netmgt.config.rrd.Graph_element) 

    /**
     * Method setGraph_element
     * 
     * 
     * 
     * @param graph_elementArray
     */
    public void setGraph_element(org.opennms.netmgt.config.rrd.Graph_element[] graph_elementArray)
    {
        //-- copy array
        _graph_elementList.removeAllElements();
        for (int i = 0; i < graph_elementArray.length; i++) {
            _graph_elementList.addElement(graph_elementArray[i]);
        }
    } //-- void setGraph_element(org.opennms.netmgt.config.rrd.Graph_element) 

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
        return (org.opennms.netmgt.config.rrd.Graph) Unmarshaller.unmarshal(org.opennms.netmgt.config.rrd.Graph.class, reader);
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
