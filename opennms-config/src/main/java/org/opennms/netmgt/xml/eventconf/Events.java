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

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Events.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Events implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Global settings for this configuration
     */
    private org.opennms.netmgt.xml.eventconf.Global _global;

    /**
     * Field _eventList.
     */
    private java.util.List<org.opennms.netmgt.xml.eventconf.Event> _eventList;

    /**
     * Field _eventFileList.
     */
    private java.util.List<java.lang.String> _eventFileList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Events() {
        super();
        this._eventList = new java.util.ArrayList<org.opennms.netmgt.xml.eventconf.Event>();
        this._eventFileList = new java.util.ArrayList<java.lang.String>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vEvent
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addEvent(
            final org.opennms.netmgt.xml.eventconf.Event vEvent)
    throws java.lang.IndexOutOfBoundsException {
        this._eventList.add(vEvent);
    }

    /**
     * 
     * 
     * @param index
     * @param vEvent
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addEvent(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Event vEvent)
    throws java.lang.IndexOutOfBoundsException {
        this._eventList.add(index, vEvent);
    }

    /**
     * 
     * 
     * @param vEventFile
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addEventFile(
            final java.lang.String vEventFile)
    throws java.lang.IndexOutOfBoundsException {
        this._eventFileList.add(vEventFile);
    }

    /**
     * 
     * 
     * @param index
     * @param vEventFile
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addEventFile(
            final int index,
            final java.lang.String vEventFile)
    throws java.lang.IndexOutOfBoundsException {
        this._eventFileList.add(index, vEventFile);
    }

    /**
     * Method enumerateEvent.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.xml.eventconf.Event> enumerateEvent(
    ) {
        return java.util.Collections.enumeration(this._eventList);
    }

    /**
     * Method enumerateEventFile.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<java.lang.String> enumerateEventFile(
    ) {
        return java.util.Collections.enumeration(this._eventFileList);
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
        
        if (obj instanceof Events) {
        
            Events temp = (Events)obj;
            if (this._global != null) {
                if (temp._global == null) return false;
                else if (!(this._global.equals(temp._global))) 
                    return false;
            }
            else if (temp._global != null)
                return false;
            if (this._eventList != null) {
                if (temp._eventList == null) return false;
                else if (!(this._eventList.equals(temp._eventList))) 
                    return false;
            }
            else if (temp._eventList != null)
                return false;
            if (this._eventFileList != null) {
                if (temp._eventFileList == null) return false;
                else if (!(this._eventFileList.equals(temp._eventFileList))) 
                    return false;
            }
            else if (temp._eventFileList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getEvent.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.xml.eventconf.Event at the given index
     */
    public org.opennms.netmgt.xml.eventconf.Event getEvent(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._eventList.size()) {
            throw new IndexOutOfBoundsException("getEvent: Index value '" + index + "' not in range [0.." + (this._eventList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.xml.eventconf.Event) _eventList.get(index);
    }

    /**
     * Method getEvent.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.xml.eventconf.Event[] getEvent(
    ) {
        org.opennms.netmgt.xml.eventconf.Event[] array = new org.opennms.netmgt.xml.eventconf.Event[0];
        return (org.opennms.netmgt.xml.eventconf.Event[]) this._eventList.toArray(array);
    }

    /**
     * Method getEventCollection.Returns a reference to
     * '_eventList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.xml.eventconf.Event> getEventCollection(
    ) {
        return this._eventList;
    }

    /**
     * Method getEventCount.
     * 
     * @return the size of this collection
     */
    public int getEventCount(
    ) {
        return this._eventList.size();
    }

    /**
     * Method getEventFile.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the java.lang.String at the given index
     */
    public java.lang.String getEventFile(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._eventFileList.size()) {
            throw new IndexOutOfBoundsException("getEventFile: Index value '" + index + "' not in range [0.." + (this._eventFileList.size() - 1) + "]");
        }
        
        return (java.lang.String) _eventFileList.get(index);
    }

    /**
     * Method getEventFile.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public java.lang.String[] getEventFile(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._eventFileList.toArray(array);
    }

    /**
     * Method getEventFileCollection.Returns a reference to
     * '_eventFileList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<java.lang.String> getEventFileCollection(
    ) {
        return this._eventFileList;
    }

    /**
     * Method getEventFileCount.
     * 
     * @return the size of this collection
     */
    public int getEventFileCount(
    ) {
        return this._eventFileList.size();
    }

    /**
     * Returns the value of field 'global'. The field 'global' has
     * the following description: Global settings for this
     * configuration
     * 
     * @return the value of field 'Global'.
     */
    public org.opennms.netmgt.xml.eventconf.Global getGlobal(
    ) {
        return this._global;
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
        if (_global != null) {
           result = 37 * result + _global.hashCode();
        }
        if (_eventList != null) {
           result = 37 * result + _eventList.hashCode();
        }
        if (_eventFileList != null) {
           result = 37 * result + _eventFileList.hashCode();
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
     * Method iterateEvent.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.xml.eventconf.Event> iterateEvent(
    ) {
        return this._eventList.iterator();
    }

    /**
     * Method iterateEventFile.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<java.lang.String> iterateEventFile(
    ) {
        return this._eventFileList.iterator();
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
    public void removeAllEvent(
    ) {
        this._eventList.clear();
    }

    /**
     */
    public void removeAllEventFile(
    ) {
        this._eventFileList.clear();
    }

    /**
     * Method removeEvent.
     * 
     * @param vEvent
     * @return true if the object was removed from the collection.
     */
    public boolean removeEvent(
            final org.opennms.netmgt.xml.eventconf.Event vEvent) {
        boolean removed = _eventList.remove(vEvent);
        return removed;
    }

    /**
     * Method removeEventAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.xml.eventconf.Event removeEventAt(
            final int index) {
        java.lang.Object obj = this._eventList.remove(index);
        return (org.opennms.netmgt.xml.eventconf.Event) obj;
    }

    /**
     * Method removeEventFile.
     * 
     * @param vEventFile
     * @return true if the object was removed from the collection.
     */
    public boolean removeEventFile(
            final java.lang.String vEventFile) {
        boolean removed = _eventFileList.remove(vEventFile);
        return removed;
    }

    /**
     * Method removeEventFileAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeEventFileAt(
            final int index) {
        java.lang.Object obj = this._eventFileList.remove(index);
        return (java.lang.String) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vEvent
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setEvent(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Event vEvent)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._eventList.size()) {
            throw new IndexOutOfBoundsException("setEvent: Index value '" + index + "' not in range [0.." + (this._eventList.size() - 1) + "]");
        }
        
        this._eventList.set(index, vEvent);
    }

    /**
     * 
     * 
     * @param vEventArray
     */
    public void setEvent(
            final org.opennms.netmgt.xml.eventconf.Event[] vEventArray) {
        //-- copy array
        _eventList.clear();
        
        for (int i = 0; i < vEventArray.length; i++) {
                this._eventList.add(vEventArray[i]);
        }
    }

    /**
     * Sets the value of '_eventList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vEventList the Vector to copy.
     */
    public void setEvent(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Event> vEventList) {
        // copy vector
        this._eventList.clear();
        
        this._eventList.addAll(vEventList);
    }

    /**
     * Sets the value of '_eventList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param eventList the Vector to set.
     */
    public void setEventCollection(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Event> eventList) {
        this._eventList = eventList;
    }

    /**
     * 
     * 
     * @param index
     * @param vEventFile
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setEventFile(
            final int index,
            final java.lang.String vEventFile)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._eventFileList.size()) {
            throw new IndexOutOfBoundsException("setEventFile: Index value '" + index + "' not in range [0.." + (this._eventFileList.size() - 1) + "]");
        }
        
        this._eventFileList.set(index, vEventFile);
    }

    /**
     * 
     * 
     * @param vEventFileArray
     */
    public void setEventFile(
            final java.lang.String[] vEventFileArray) {
        //-- copy array
        _eventFileList.clear();
        
        for (int i = 0; i < vEventFileArray.length; i++) {
                this._eventFileList.add(vEventFileArray[i]);
        }
    }

    /**
     * Sets the value of '_eventFileList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vEventFileList the Vector to copy.
     */
    public void setEventFile(
            final java.util.List<java.lang.String> vEventFileList) {
        // copy vector
        this._eventFileList.clear();
        
        this._eventFileList.addAll(vEventFileList);
    }

    /**
     * Sets the value of '_eventFileList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param eventFileList the Vector to set.
     */
    public void setEventFileCollection(
            final java.util.List<java.lang.String> eventFileList) {
        this._eventFileList = eventFileList;
    }

    /**
     * Sets the value of field 'global'. The field 'global' has the
     * following description: Global settings for this
     * configuration
     * 
     * @param global the value of field 'global'.
     */
    public void setGlobal(
            final org.opennms.netmgt.xml.eventconf.Global global) {
        this._global = global;
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
     * org.opennms.netmgt.xml.eventconf.Events
     */
    public static org.opennms.netmgt.xml.eventconf.Events unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.Events) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.Events.class, reader);
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
