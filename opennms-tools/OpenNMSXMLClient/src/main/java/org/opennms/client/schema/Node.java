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
 * A node in OpenNMS is a network entity that is used to contain
 * network interfaces and those interface's services.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Node implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _nodeLabel.
     */
    private java.lang.String _nodeLabel;

    /**
     * This optional attribute can be used to facilitate
     * integration by making the key from the integrating DB
     * available adjacent to the OpenNMS key.
     *  
     */
    private java.lang.String _foreignId;

    /**
     * This optional attribute can be used to facilitate building
     * node relationships.
     *  
     */
    private java.lang.String _parentForeignSource;

    /**
     * This optional attribute can be used to facilitate building
     * node relationships.
     *  
     */
    private java.lang.String _parentForeignId;

    /**
     * This optional attribute can be used to facilitate building
     * node relationships.
     *  
     */
    private java.lang.String _parentNodeLabel;

    /**
     * This optional attribute can be used to persist an asset
     * value to the building column in the OpenNMS DB.
     *  
     */
    private java.lang.String _city;

    /**
     * This optional attribute can be used to persist an asset
     * value to the building column in the OpenNMS DB.
     *  
     */
    private java.lang.String _building;

    /**
     * A network interface.
     */
    private java.util.List<org.opennms.client.schema.Interface> _interfaceList;

    /**
     * This element is used to specify OpenNMS specific categories.
     * Note: currently, these categories are defined in a separate
     * configuration file and
     *  are related directly to monitored services. I have
     * separated out this element so that it can be referenced by
     * other entities (nodes, interfaces, etc.)
     *  however, they will be ignored until the domain model is
     * changed and the service layer is adapted for this behavior. 
     *  
     */
    private java.util.List<org.opennms.client.schema.Category> _categoryList;

    /**
     * This element is used to specify an asset record attribute.
     */
    private java.util.List<org.opennms.client.schema.Asset> _assetList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Node() {
        super();
        this._interfaceList = new java.util.ArrayList<>();
        this._categoryList = new java.util.ArrayList<>();
        this._assetList = new java.util.ArrayList<>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vAsset
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addAsset(
            final org.opennms.client.schema.Asset vAsset)
    throws java.lang.IndexOutOfBoundsException {
        this._assetList.add(vAsset);
    }

    /**
     * 
     * 
     * @param index
     * @param vAsset
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addAsset(
            final int index,
            final org.opennms.client.schema.Asset vAsset)
    throws java.lang.IndexOutOfBoundsException {
        this._assetList.add(index, vAsset);
    }

    /**
     * 
     * 
     * @param vCategory
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addCategory(
            final org.opennms.client.schema.Category vCategory)
    throws java.lang.IndexOutOfBoundsException {
        this._categoryList.add(vCategory);
    }

    /**
     * 
     * 
     * @param index
     * @param vCategory
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addCategory(
            final int index,
            final org.opennms.client.schema.Category vCategory)
    throws java.lang.IndexOutOfBoundsException {
        this._categoryList.add(index, vCategory);
    }

    /**
     * 
     * 
     * @param vInterface
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addInterface(
            final org.opennms.client.schema.Interface vInterface)
    throws java.lang.IndexOutOfBoundsException {
        this._interfaceList.add(vInterface);
    }

    /**
     * 
     * 
     * @param index
     * @param vInterface
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addInterface(
            final int index,
            final org.opennms.client.schema.Interface vInterface)
    throws java.lang.IndexOutOfBoundsException {
        this._interfaceList.add(index, vInterface);
    }

    /**
     * Method enumerateAsset.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.opennms.client.schema.Asset> enumerateAsset(
    ) {
        return java.util.Collections.enumeration(this._assetList);
    }

    /**
     * Method enumerateCategory.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.opennms.client.schema.Category> enumerateCategory(
    ) {
        return java.util.Collections.enumeration(this._categoryList);
    }

    /**
     * Method enumerateInterface.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.opennms.client.schema.Interface> enumerateInterface(
    ) {
        return java.util.Collections.enumeration(this._interfaceList);
    }

    /**
     * Method getAsset.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the org.opennms.client.schema.Asset at
     * the given index
     */
    public org.opennms.client.schema.Asset getAsset(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._assetList.size()) {
            throw new IndexOutOfBoundsException("getAsset: Index value '" + index + "' not in range [0.." + (this._assetList.size() - 1) + "]");
        }

        return (org.opennms.client.schema.Asset) _assetList.get(index);
    }

    /**
     * Method getAsset.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.client.schema.Asset[] getAsset(
    ) {
        org.opennms.client.schema.Asset[] array = new org.opennms.client.schema.Asset[0];
        return (org.opennms.client.schema.Asset[]) this._assetList.toArray(array);
    }

    /**
     * Method getAssetCount.
     * 
     * @return the size of this collection
     */
    public int getAssetCount(
    ) {
        return this._assetList.size();
    }

    /**
     * Returns the value of field 'building'. The field 'building'
     * has the following description: This optional attribute can
     * be used to persist an asset value to the building column in
     * the OpenNMS DB.
     *  
     * 
     * @return the value of field 'Building'.
     */
    public java.lang.String getBuilding(
    ) {
        return this._building;
    }

    /**
     * Method getCategory.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the org.opennms.client.schema.Category
     * at the given index
     */
    public org.opennms.client.schema.Category getCategory(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._categoryList.size()) {
            throw new IndexOutOfBoundsException("getCategory: Index value '" + index + "' not in range [0.." + (this._categoryList.size() - 1) + "]");
        }

        return (org.opennms.client.schema.Category) _categoryList.get(index);
    }

    /**
     * Method getCategory.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.client.schema.Category[] getCategory(
    ) {
        org.opennms.client.schema.Category[] array = new org.opennms.client.schema.Category[0];
        return (org.opennms.client.schema.Category[]) this._categoryList.toArray(array);
    }

    /**
     * Method getCategoryCount.
     * 
     * @return the size of this collection
     */
    public int getCategoryCount(
    ) {
        return this._categoryList.size();
    }

    /**
     * Returns the value of field 'city'. The field 'city' has the
     * following description: This optional attribute can be used
     * to persist an asset value to the building column in the
     * OpenNMS DB.
     *  
     * 
     * @return the value of field 'City'.
     */
    public java.lang.String getCity(
    ) {
        return this._city;
    }

    /**
     * Returns the value of field 'foreignId'. The field
     * 'foreignId' has the following description: This optional
     * attribute can be used to facilitate integration by making
     * the key from the integrating DB available adjacent to the
     * OpenNMS key.
     *  
     * 
     * @return the value of field 'ForeignId'.
     */
    public java.lang.String getForeignId(
    ) {
        return this._foreignId;
    }

    /**
     * Method getInterface.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the org.opennms.client.schema.Interface
     * at the given index
     */
    public org.opennms.client.schema.Interface getInterface(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._interfaceList.size()) {
            throw new IndexOutOfBoundsException("getInterface: Index value '" + index + "' not in range [0.." + (this._interfaceList.size() - 1) + "]");
        }

        return (org.opennms.client.schema.Interface) _interfaceList.get(index);
    }

    /**
     * Method getInterface.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.client.schema.Interface[] getInterface(
    ) {
        org.opennms.client.schema.Interface[] array = new org.opennms.client.schema.Interface[0];
        return (org.opennms.client.schema.Interface[]) this._interfaceList.toArray(array);
    }

    /**
     * Method getInterfaceCount.
     * 
     * @return the size of this collection
     */
    public int getInterfaceCount(
    ) {
        return this._interfaceList.size();
    }

    /**
     * Returns the value of field 'nodeLabel'.
     * 
     * @return the value of field 'NodeLabel'.
     */
    public java.lang.String getNodeLabel(
    ) {
        return this._nodeLabel;
    }

    /**
     * Returns the value of field 'parentForeignId'. The field
     * 'parentForeignId' has the following description: This
     * optional attribute can be used to facilitate building node
     * relationships.
     *  
     * 
     * @return the value of field 'ParentForeignId'.
     */
    public java.lang.String getParentForeignId(
    ) {
        return this._parentForeignId;
    }

    /**
     * Returns the value of field 'parentForeignSource'. The field
     * 'parentForeignSource' has the following description: This
     * optional attribute can be used to facilitate building node
     * relationships.
     *  
     * 
     * @return the value of field 'ParentForeignSource'.
     */
    public java.lang.String getParentForeignSource(
    ) {
        return this._parentForeignSource;
    }

    /**
     * Returns the value of field 'parentNodeLabel'. The field
     * 'parentNodeLabel' has the following description: This
     * optional attribute can be used to facilitate building node
     * relationships.
     *  
     * 
     * @return the value of field 'ParentNodeLabel'.
     */
    public java.lang.String getParentNodeLabel(
    ) {
        return this._parentNodeLabel;
    }

    /**
     * Method iterateAsset.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.opennms.client.schema.Asset> iterateAsset(
    ) {
        return this._assetList.iterator();
    }

    /**
     * Method iterateCategory.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.opennms.client.schema.Category> iterateCategory(
    ) {
        return this._categoryList.iterator();
    }

    /**
     * Method iterateInterface.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.opennms.client.schema.Interface> iterateInterface(
    ) {
        return this._interfaceList.iterator();
    }

    /**
     * 
     * 
     * @param out
     * @throws JAXBException 
     */
    public void marshal(final java.io.Writer out) throws JAXBException {
        final JAXBContext context = JAXBContext.newInstance(Node.class);
        final Marshaller m = context.createMarshaller();
        m.marshal(this, out);
    }

    /**
     */
    public void removeAllAsset(
    ) {
        this._assetList.clear();
    }

    /**
     */
    public void removeAllCategory(
    ) {
        this._categoryList.clear();
    }

    /**
     */
    public void removeAllInterface(
    ) {
        this._interfaceList.clear();
    }

    /**
     * Method removeAsset.
     * 
     * @param vAsset
     * @return true if the object was removed from the collection.
     */
    public boolean removeAsset(
            final org.opennms.client.schema.Asset vAsset) {
        boolean removed = _assetList.remove(vAsset);
        return removed;
    }

    /**
     * Method removeAssetAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.client.schema.Asset removeAssetAt(
            final int index) {
        java.lang.Object obj = this._assetList.remove(index);
        return (org.opennms.client.schema.Asset) obj;
    }

    /**
     * Method removeCategory.
     * 
     * @param vCategory
     * @return true if the object was removed from the collection.
     */
    public boolean removeCategory(
            final org.opennms.client.schema.Category vCategory) {
        boolean removed = _categoryList.remove(vCategory);
        return removed;
    }

    /**
     * Method removeCategoryAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.client.schema.Category removeCategoryAt(
            final int index) {
        java.lang.Object obj = this._categoryList.remove(index);
        return (org.opennms.client.schema.Category) obj;
    }

    /**
     * Method removeInterface.
     * 
     * @param vInterface
     * @return true if the object was removed from the collection.
     */
    public boolean removeInterface(
            final org.opennms.client.schema.Interface vInterface) {
        boolean removed = _interfaceList.remove(vInterface);
        return removed;
    }

    /**
     * Method removeInterfaceAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.client.schema.Interface removeInterfaceAt(
            final int index) {
        java.lang.Object obj = this._interfaceList.remove(index);
        return (org.opennms.client.schema.Interface) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vAsset
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setAsset(
            final int index,
            final org.opennms.client.schema.Asset vAsset)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._assetList.size()) {
            throw new IndexOutOfBoundsException("setAsset: Index value '" + index + "' not in range [0.." + (this._assetList.size() - 1) + "]");
        }

        this._assetList.set(index, vAsset);
    }

    /**
     * 
     * 
     * @param vAssetArray
     */
    public void setAsset(
            final org.opennms.client.schema.Asset[] vAssetArray) {
        //-- copy array
        _assetList.clear();

        for (int i = 0; i < vAssetArray.length; i++) {
                this._assetList.add(vAssetArray[i]);
        }
    }

    /**
     * Sets the value of field 'building'. The field 'building' has
     * the following description: This optional attribute can be
     * used to persist an asset value to the building column in the
     * OpenNMS DB.
     *  
     * 
     * @param building the value of field 'building'.
     */
    public void setBuilding(
            final java.lang.String building) {
        this._building = building;
    }

    /**
     * 
     * 
     * @param index
     * @param vCategory
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setCategory(
            final int index,
            final org.opennms.client.schema.Category vCategory)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._categoryList.size()) {
            throw new IndexOutOfBoundsException("setCategory: Index value '" + index + "' not in range [0.." + (this._categoryList.size() - 1) + "]");
        }

        this._categoryList.set(index, vCategory);
    }

    /**
     * 
     * 
     * @param vCategoryArray
     */
    public void setCategory(
            final org.opennms.client.schema.Category[] vCategoryArray) {
        //-- copy array
        _categoryList.clear();

        for (int i = 0; i < vCategoryArray.length; i++) {
                this._categoryList.add(vCategoryArray[i]);
        }
    }

    /**
     * Sets the value of field 'city'. The field 'city' has the
     * following description: This optional attribute can be used
     * to persist an asset value to the building column in the
     * OpenNMS DB.
     *  
     * 
     * @param city the value of field 'city'.
     */
    public void setCity(
            final java.lang.String city) {
        this._city = city;
    }

    /**
     * Sets the value of field 'foreignId'. The field 'foreignId'
     * has the following description: This optional attribute can
     * be used to facilitate integration by making the key from the
     * integrating DB available adjacent to the OpenNMS key.
     *  
     * 
     * @param foreignId the value of field 'foreignId'.
     */
    public void setForeignId(
            final java.lang.String foreignId) {
        this._foreignId = foreignId;
    }

    /**
     * 
     * 
     * @param index
     * @param vInterface
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setInterface(
            final int index,
            final org.opennms.client.schema.Interface vInterface)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._interfaceList.size()) {
            throw new IndexOutOfBoundsException("setInterface: Index value '" + index + "' not in range [0.." + (this._interfaceList.size() - 1) + "]");
        }

        this._interfaceList.set(index, vInterface);
    }

    /**
     * 
     * 
     * @param vInterfaceArray
     */
    public void setInterface(
            final org.opennms.client.schema.Interface[] vInterfaceArray) {
        //-- copy array
        _interfaceList.clear();

        for (int i = 0; i < vInterfaceArray.length; i++) {
                this._interfaceList.add(vInterfaceArray[i]);
        }
    }

    /**
     * Sets the value of field 'nodeLabel'.
     * 
     * @param nodeLabel the value of field 'nodeLabel'.
     */
    public void setNodeLabel(
            final java.lang.String nodeLabel) {
        this._nodeLabel = nodeLabel;
    }

    /**
     * Sets the value of field 'parentForeignId'. The field
     * 'parentForeignId' has the following description: This
     * optional attribute can be used to facilitate building node
     * relationships.
     *  
     * 
     * @param parentForeignId the value of field 'parentForeignId'.
     */
    public void setParentForeignId(
            final java.lang.String parentForeignId) {
        this._parentForeignId = parentForeignId;
    }

    /**
     * Sets the value of field 'parentForeignSource'. The field
     * 'parentForeignSource' has the following description: This
     * optional attribute can be used to facilitate building node
     * relationships.
     *  
     * 
     * @param parentForeignSource the value of field
     * 'parentForeignSource'.
     */
    public void setParentForeignSource(
            final java.lang.String parentForeignSource) {
        this._parentForeignSource = parentForeignSource;
    }

    /**
     * Sets the value of field 'parentNodeLabel'. The field
     * 'parentNodeLabel' has the following description: This
     * optional attribute can be used to facilitate building node
     * relationships.
     *  
     * 
     * @param parentNodeLabel the value of field 'parentNodeLabel'.
     */
    public void setParentNodeLabel(
            final java.lang.String parentNodeLabel) {
        this._parentNodeLabel = parentNodeLabel;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled org.opennms.client.schema.Node
     * @throws JAXBException 
     */
    public static org.opennms.client.schema.Node unmarshal(final java.io.Reader reader) throws JAXBException {
        final JAXBContext context = JAXBContext.newInstance(Node.class);
        final Unmarshaller um = context.createUnmarshaller();
        return (Node) um.unmarshal(reader);
    }

}
