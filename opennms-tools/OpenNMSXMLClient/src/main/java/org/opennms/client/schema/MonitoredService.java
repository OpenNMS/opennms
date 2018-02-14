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
 * This element is defined outside of the interface element for
 * future use where it is expected that services on other network
 * entities will be able to be
 *  defined (i.e. networks, nodes and applications)
 *  
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class MonitoredService implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _serviceName.
     */
    private java.lang.String _serviceName;

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


      //----------------/
     //- Constructors -/
    //----------------/

    public MonitoredService() {
        super();
        this._categoryList = new java.util.ArrayList<>();
    }


      //-----------/
     //- Methods -/
    //-----------/

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
     * Returns the value of field 'serviceName'.
     * 
     * @return the value of field 'ServiceName'.
     */
    public java.lang.String getServiceName(
    ) {
        return this._serviceName;
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
        final JAXBContext context = JAXBContext.newInstance(MonitoredService.class);
        final Marshaller m = context.createMarshaller();
        m.marshal(this, out);
    }

    /**
     */
    public void removeAllCategory(
    ) {
        this._categoryList.clear();
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
     * Sets the value of field 'serviceName'.
     * 
     * @param serviceName the value of field 'serviceName'.
     */
    public void setServiceName(
            final java.lang.String serviceName) {
        this._serviceName = serviceName;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @return the unmarshaled
     * org.opennms.client.schema.MonitoredService
     * @throws JAXBException 
     */
    public static org.opennms.client.schema.MonitoredService unmarshal(final java.io.Reader reader) throws JAXBException {
        final JAXBContext context = JAXBContext.newInstance(MonitoredService.class);
        final Unmarshaller um = context.createUnmarshaller();
        return (MonitoredService) um.unmarshal(reader);
    }

}
