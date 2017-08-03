/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.client.schema;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Use this configuration file as an import of nodes, interfaces,
 * and services to OpenNMS by-passing several phases of discovery
 * and capabilities polling.
 *  
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class ModelImport implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _dateStamp.
     */
    private java.util.Date _dateStamp;

    /**
     * Field _lastImport.
     */
    private java.util.Date _lastImport;

    /**
     * Field _foreignSource.
     */
    private java.lang.String _foreignSource = "imported:";

    /**
     * A node in OpenNMS is a network entity that is used to
     * contain network interfaces and those interface's services.
     */
    private java.util.List<org.opennms.client.schema.Node> _nodeList;


      //----------------/
     //- Constructors -/
    //----------------/

    public ModelImport() {
        super();
        setForeignSource("imported:");
        this._nodeList = new java.util.ArrayList<>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vNode
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addNode(
            final org.opennms.client.schema.Node vNode)
    throws java.lang.IndexOutOfBoundsException {
        this._nodeList.add(vNode);
    }

    /**
     * 
     * 
     * @param index
     * @param vNode
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addNode(
            final int index,
            final org.opennms.client.schema.Node vNode)
    throws java.lang.IndexOutOfBoundsException {
        this._nodeList.add(index, vNode);
    }

    /**
     * Method enumerateNode.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.opennms.client.schema.Node> enumerateNode(
    ) {
        return java.util.Collections.enumeration(this._nodeList);
    }

    /**
     * Returns the value of field 'dateStamp'.
     * 
     * @return the value of field 'DateStamp'.
     */
    public java.util.Date getDateStamp(
    ) {
        return this._dateStamp;
    }

    /**
     * Returns the value of field 'foreignSource'.
     * 
     * @return the value of field 'ForeignSource'.
     */
    public java.lang.String getForeignSource(
    ) {
        return this._foreignSource;
    }

    /**
     * Returns the value of field 'lastImport'.
     * 
     * @return the value of field 'LastImport'.
     */
    public java.util.Date getLastImport(
    ) {
        return this._lastImport;
    }

    /**
     * Method getNode.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the org.opennms.client.schema.Node at
     * the given index
     */
    public org.opennms.client.schema.Node getNode(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._nodeList.size()) {
            throw new IndexOutOfBoundsException("getNode: Index value '" + index + "' not in range [0.." + (this._nodeList.size() - 1) + "]");
        }

        return (org.opennms.client.schema.Node) _nodeList.get(index);
    }

    /**
     * Method getNode.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.client.schema.Node[] getNode(
    ) {
        org.opennms.client.schema.Node[] array = new org.opennms.client.schema.Node[0];
        return (org.opennms.client.schema.Node[]) this._nodeList.toArray(array);
    }

    /**
     * Method getNodeCount.
     * 
     * @return the size of this collection
     */
    public int getNodeCount(
    ) {
        return this._nodeList.size();
    }

    /**
     * Method iterateNode.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.opennms.client.schema.Node> iterateNode(
    ) {
        return this._nodeList.iterator();
    }

    /**
     * 
     * 
     * @param out
     * @throws JAXBException 
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(final java.io.Writer out) throws JAXBException {
        final JAXBContext context = JAXBContext.newInstance(Asset.class);
        final Marshaller m = context.createMarshaller();
        m.marshal(this, out);
    }

    /**
     */
    public void removeAllNode(
    ) {
        this._nodeList.clear();
    }

    /**
     * Method removeNode.
     * 
     * @param vNode
     * @return true if the object was removed from the collection.
     */
    public boolean removeNode(
            final org.opennms.client.schema.Node vNode) {
        boolean removed = _nodeList.remove(vNode);
        return removed;
    }

    /**
     * Method removeNodeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.client.schema.Node removeNodeAt(
            final int index) {
        java.lang.Object obj = this._nodeList.remove(index);
        return (org.opennms.client.schema.Node) obj;
    }

    /**
     * Sets the value of field 'dateStamp'.
     * 
     * @param dateStamp the value of field 'dateStamp'.
     */
    public void setDateStamp(
            final java.util.Date dateStamp) {
        this._dateStamp = dateStamp;
    }

    /**
     * Sets the value of field 'foreignSource'.
     * 
     * @param foreignSource the value of field 'foreignSource'.
     */
    public void setForeignSource(
            final java.lang.String foreignSource) {
        this._foreignSource = foreignSource;
    }

    /**
     * Sets the value of field 'lastImport'.
     * 
     * @param lastImport the value of field 'lastImport'.
     */
    public void setLastImport(
            final java.util.Date lastImport) {
        this._lastImport = lastImport;
    }

    /**
     * 
     * 
     * @param index
     * @param vNode
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setNode(
            final int index,
            final org.opennms.client.schema.Node vNode)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._nodeList.size()) {
            throw new IndexOutOfBoundsException("setNode: Index value '" + index + "' not in range [0.." + (this._nodeList.size() - 1) + "]");
        }

        this._nodeList.set(index, vNode);
    }

    /**
     * 
     * 
     * @param vNodeArray
     */
    public void setNode(
            final org.opennms.client.schema.Node[] vNodeArray) {
        //-- copy array
        _nodeList.clear();

        for (int i = 0; i < vNodeArray.length; i++) {
                this._nodeList.add(vNodeArray[i]);
        }
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled org.opennms.client.schema.ModelImport
     * @throws JAXBException 
     */
    public static org.opennms.client.schema.ModelImport unmarshal(final java.io.Reader reader) throws JAXBException {
        final JAXBContext context = JAXBContext.newInstance(ModelImport.class);
        final Unmarshaller um = context.createUnmarshaller();
        return (ModelImport) um.unmarshal(reader);
    }

}
