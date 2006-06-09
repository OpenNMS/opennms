/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.7</a>, using an XML
 * Schema.
 * $Id: Rrd_graph_def.java,v 1.2 2005/11/03 20:43:58 brozow Exp $
 */

package org.opennms.netmgt.config.rrd;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Rrd_graph_def.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/11/03 20:43:58 $
 */
public class Rrd_graph_def implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _span
     */
    private org.opennms.netmgt.config.rrd.Span _span;

    /**
     * Field _options
     */
    private org.opennms.netmgt.config.rrd.Options _options;

    /**
     * Field _datasources
     */
    private org.opennms.netmgt.config.rrd.Datasources _datasources;

    /**
     * Field _graph
     */
    private org.opennms.netmgt.config.rrd.Graph _graph;


      //----------------/
     //- Constructors -/
    //----------------/

    public Rrd_graph_def() 
     {
        super();
    } //-- org.opennms.netmgt.config.rrd.Rrd_graph_def()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'datasources'.
     * 
     * @return Datasources
     * @return the value of field 'datasources'.
     */
    public org.opennms.netmgt.config.rrd.Datasources getDatasources()
    {
        return this._datasources;
    } //-- org.opennms.netmgt.config.rrd.Datasources getDatasources() 

    /**
     * Returns the value of field 'graph'.
     * 
     * @return Graph
     * @return the value of field 'graph'.
     */
    public org.opennms.netmgt.config.rrd.Graph getGraph()
    {
        return this._graph;
    } //-- org.opennms.netmgt.config.rrd.Graph getGraph() 

    /**
     * Returns the value of field 'options'.
     * 
     * @return Options
     * @return the value of field 'options'.
     */
    public org.opennms.netmgt.config.rrd.Options getOptions()
    {
        return this._options;
    } //-- org.opennms.netmgt.config.rrd.Options getOptions() 

    /**
     * Returns the value of field 'span'.
     * 
     * @return Span
     * @return the value of field 'span'.
     */
    public org.opennms.netmgt.config.rrd.Span getSpan()
    {
        return this._span;
    } //-- org.opennms.netmgt.config.rrd.Span getSpan() 

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
     * Sets the value of field 'datasources'.
     * 
     * @param datasources the value of field 'datasources'.
     */
    public void setDatasources(org.opennms.netmgt.config.rrd.Datasources datasources)
    {
        this._datasources = datasources;
    } //-- void setDatasources(org.opennms.netmgt.config.rrd.Datasources) 

    /**
     * Sets the value of field 'graph'.
     * 
     * @param graph the value of field 'graph'.
     */
    public void setGraph(org.opennms.netmgt.config.rrd.Graph graph)
    {
        this._graph = graph;
    } //-- void setGraph(org.opennms.netmgt.config.rrd.Graph) 

    /**
     * Sets the value of field 'options'.
     * 
     * @param options the value of field 'options'.
     */
    public void setOptions(org.opennms.netmgt.config.rrd.Options options)
    {
        this._options = options;
    } //-- void setOptions(org.opennms.netmgt.config.rrd.Options) 

    /**
     * Sets the value of field 'span'.
     * 
     * @param span the value of field 'span'.
     */
    public void setSpan(org.opennms.netmgt.config.rrd.Span span)
    {
        this._span = span;
    } //-- void setSpan(org.opennms.netmgt.config.rrd.Span) 

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
        return (org.opennms.netmgt.config.rrd.Rrd_graph_def) Unmarshaller.unmarshal(org.opennms.netmgt.config.rrd.Rrd_graph_def.class, reader);
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
