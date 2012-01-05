/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.xml.eventconf;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * The filters for the event, contains one or more filter tags.
 */

@XmlRootElement(name="filters")
@XmlAccessorType(XmlAccessType.FIELD)
public class Filters implements java.io.Serializable {

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * 
     */
    private static final long serialVersionUID = 3252659384385972761L;

    /**
     * The mask element
     */
    @XmlElement(name="filter", required=true)
    private java.util.List<org.opennms.netmgt.xml.eventconf.Filter> _filterList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Filters() {
        super();
        this._filterList = new java.util.ArrayList<org.opennms.netmgt.xml.eventconf.Filter>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vFilter
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addFilter(
            final org.opennms.netmgt.xml.eventconf.Filter vFilter)
    throws java.lang.IndexOutOfBoundsException {
        this._filterList.add(vFilter);
    }

    /**
     * 
     * 
     * @param index
     * @param vFilter
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addFilter(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Filter vFilter)
    throws java.lang.IndexOutOfBoundsException {
        this._filterList.add(index, vFilter);
    }

    /**
     * Method enumerateFilter.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.xml.eventconf.Filter> enumerateFilter(
    ) {
        return java.util.Collections.enumeration(this._filterList);
    }

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(
            final java.lang.Object obj) {
        if ( this == obj )
            return true;
        
        if (obj instanceof Filters) {
        
            Filters temp = (Filters)obj;
            if (this._filterList != null) {
                if (temp._filterList == null) return false;
                else if (!(this._filterList.equals(temp._filterList))) 
                    return false;
            }
            else if (temp._filterList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getFilter.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.xml.eventconf.Filter at the given index
     */
    public org.opennms.netmgt.xml.eventconf.Filter getFilter(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._filterList.size()) {
            throw new IndexOutOfBoundsException("getFilter: Index value '" + index + "' not in range [0.." + (this._filterList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.xml.eventconf.Filter) _filterList.get(index);
    }

    /**
     * Method getFilter.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.xml.eventconf.Filter[] getFilter(
    ) {
        org.opennms.netmgt.xml.eventconf.Filter[] array = new org.opennms.netmgt.xml.eventconf.Filter[0];
        return (org.opennms.netmgt.xml.eventconf.Filter[]) this._filterList.toArray(array);
    }

    /**
     * Method getFilterCollection.Returns a reference to
     * '_filterList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.xml.eventconf.Filter> getFilterCollection(
    ) {
        return this._filterList;
    }

    /**
     * Method getFilterCount.
     * 
     * @return the size of this collection
     */
    public int getFilterCount(
    ) {
        return this._filterList.size();
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode(
    ) {
        int result = 17;
        
        if (_filterList != null) {
           result = 37 * result + _filterList.hashCode();
        }
        
        return result;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid(
    ) {
        try {
            validate();
        } catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * Method iterateFilter.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.xml.eventconf.Filter> iterateFilter(
    ) {
        return this._filterList.iterator();
    }

    /**
     * 
     * 
     * @param out
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(
            final java.io.Writer out)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws java.io.IOException if an IOException occurs during
     * marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(
            final org.xml.sax.ContentHandler handler)
    throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllFilter(
    ) {
        this._filterList.clear();
    }

    /**
     * Method removeFilter.
     * 
     * @param vFilter
     * @return true if the object was removed from the collection.
     */
    public boolean removeFilter(
            final org.opennms.netmgt.xml.eventconf.Filter vFilter) {
        boolean removed = _filterList.remove(vFilter);
        return removed;
    }

    /**
     * Method removeFilterAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.xml.eventconf.Filter removeFilterAt(
            final int index) {
        java.lang.Object obj = this._filterList.remove(index);
        return (org.opennms.netmgt.xml.eventconf.Filter) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vFilter
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setFilter(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Filter vFilter)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._filterList.size()) {
            throw new IndexOutOfBoundsException("setFilter: Index value '" + index + "' not in range [0.." + (this._filterList.size() - 1) + "]");
        }
        
        this._filterList.set(index, vFilter);
    }

    /**
     * 
     * 
     * @param vFilterArray
     */
    public void setFilter(
            final org.opennms.netmgt.xml.eventconf.Filter[] vFilterArray) {
        //-- copy array
        _filterList.clear();
        
        for (int i = 0; i < vFilterArray.length; i++) {
                this._filterList.add(vFilterArray[i]);
        }
    }

    /**
     * Sets the value of '_filterList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vFilterList the Vector to copy.
     */
    public void setFilter(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Filter> vFilterList) {
        // copy vector
        this._filterList.clear();
        
        this._filterList.addAll(vFilterList);
    }

    /**
     * Sets the value of '_filterList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param filterList the Vector to set.
     */
    public void setFilterCollection(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Filter> filterList) {
        this._filterList = filterList;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * org.opennms.netmgt.xml.eventconf.Filters
     */
    public static org.opennms.netmgt.xml.eventconf.Filters unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.Filters) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.Filters.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate(
    )
    throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
