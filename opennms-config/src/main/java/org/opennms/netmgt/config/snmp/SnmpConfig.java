/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.snmp;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Top-level element for the snmp-config.xml configuration
 *  file.
 * 
 * @version $Revision$ $Date$
 */

@SuppressWarnings("all") public class SnmpConfig extends org.opennms.netmgt.config.snmp.Configuration 
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Maps IP addresses to specific SNMP parmeters
     *  (retries, timeouts...)
     */
    private java.util.List<org.opennms.netmgt.config.snmp.Definition> _definitionList;


      //----------------/
     //- Constructors -/
    //----------------/

    public SnmpConfig() {
        super();
        this._definitionList = new java.util.ArrayList<org.opennms.netmgt.config.snmp.Definition>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vDefinition
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addDefinition(
            final org.opennms.netmgt.config.snmp.Definition vDefinition)
    throws java.lang.IndexOutOfBoundsException {
        this._definitionList.add(vDefinition);
    }

    /**
     * 
     * 
     * @param index
     * @param vDefinition
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addDefinition(
            final int index,
            final org.opennms.netmgt.config.snmp.Definition vDefinition)
    throws java.lang.IndexOutOfBoundsException {
        this._definitionList.add(index, vDefinition);
    }

    /**
     * Method enumerateDefinition.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.snmp.Definition> enumerateDefinition(
    ) {
        return java.util.Collections.enumeration(this._definitionList);
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
        
        if (super.equals(obj)==false)
            return false;
        
        if (obj instanceof SnmpConfig) {
        
            SnmpConfig temp = (SnmpConfig)obj;
            if (this._definitionList != null) {
                if (temp._definitionList == null) return false;
                else if (!(this._definitionList.equals(temp._definitionList))) 
                    return false;
            }
            else if (temp._definitionList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getDefinition.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.config.snmp.Definition at the given index
     */
    public org.opennms.netmgt.config.snmp.Definition getDefinition(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._definitionList.size()) {
            throw new IndexOutOfBoundsException("getDefinition: Index value '" + index + "' not in range [0.." + (this._definitionList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.config.snmp.Definition) _definitionList.get(index);
    }

    /**
     * Method getDefinition.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.snmp.Definition[] getDefinition(
    ) {
        org.opennms.netmgt.config.snmp.Definition[] array = new org.opennms.netmgt.config.snmp.Definition[0];
        return (org.opennms.netmgt.config.snmp.Definition[]) this._definitionList.toArray(array);
    }

    /**
     * Method getDefinitionCollection.Returns a reference to
     * '_definitionList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.snmp.Definition> getDefinitionCollection(
    ) {
        return this._definitionList;
    }

    /**
     * Method getDefinitionCount.
     * 
     * @return the size of this collection
     */
    public int getDefinitionCount(
    ) {
        return this._definitionList.size();
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
        
        long tmp;
        if (_definitionList != null) {
           result = 37 * result + _definitionList.hashCode();
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
     * Method iterateDefinition.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.snmp.Definition> iterateDefinition(
    ) {
        return this._definitionList.iterator();
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
    public void removeAllDefinition(
    ) {
        this._definitionList.clear();
    }

    /**
     * Method removeDefinition.
     * 
     * @param vDefinition
     * @return true if the object was removed from the collection.
     */
    public boolean removeDefinition(
            final org.opennms.netmgt.config.snmp.Definition vDefinition) {
        boolean removed = _definitionList.remove(vDefinition);
        return removed;
    }

    /**
     * Method removeDefinitionAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.snmp.Definition removeDefinitionAt(
            final int index) {
        java.lang.Object obj = this._definitionList.remove(index);
        return (org.opennms.netmgt.config.snmp.Definition) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vDefinition
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setDefinition(
            final int index,
            final org.opennms.netmgt.config.snmp.Definition vDefinition)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._definitionList.size()) {
            throw new IndexOutOfBoundsException("setDefinition: Index value '" + index + "' not in range [0.." + (this._definitionList.size() - 1) + "]");
        }
        
        this._definitionList.set(index, vDefinition);
    }

    /**
     * 
     * 
     * @param vDefinitionArray
     */
    public void setDefinition(
            final org.opennms.netmgt.config.snmp.Definition[] vDefinitionArray) {
        //-- copy array
        _definitionList.clear();
        
        for (int i = 0; i < vDefinitionArray.length; i++) {
                this._definitionList.add(vDefinitionArray[i]);
        }
    }

    /**
     * Sets the value of '_definitionList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vDefinitionList the Vector to copy.
     */
    public void setDefinition(
            final java.util.List<org.opennms.netmgt.config.snmp.Definition> vDefinitionList) {
        // copy vector
        this._definitionList.clear();
        
        this._definitionList.addAll(vDefinitionList);
    }

    /**
     * Sets the value of '_definitionList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param definitionList the Vector to set.
     */
    public void setDefinitionCollection(
            final java.util.List<org.opennms.netmgt.config.snmp.Definition> definitionList) {
        this._definitionList = definitionList;
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
     * org.opennms.netmgt.config.snmp.Configuration
     */
    public static org.opennms.netmgt.config.snmp.Configuration unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.config.snmp.Configuration) Unmarshaller.unmarshal(org.opennms.netmgt.config.snmp.SnmpConfig.class, reader);
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
