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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Events.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name="events")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("serial")
public class Events implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Global settings for this configuration
     */
	@XmlElement(name="global", required=false)
    private Global m_global;

    /**
     * Field _eventList.
     */
	@XmlElement(name="event", required=true)
    private List<Event> m_eventList;

    /**
     * Field _eventFileList.
     */
	@XmlElement(name="event-file", required=false)
    private List<String> m_eventFileList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Events() {
        super();
        this.m_eventList = new ArrayList<Event>();
        this.m_eventFileList = new ArrayList<String>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vEvent
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addEvent(
            final Event vEvent)
    throws IndexOutOfBoundsException {
        this.m_eventList.add(vEvent);
    }

    /**
     * 
     * 
     * @param index
     * @param vEvent
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addEvent(
            final int index,
            final Event vEvent)
    throws IndexOutOfBoundsException {
        this.m_eventList.add(index, vEvent);
    }

    /**
     * 
     * 
     * @param vEventFile
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addEventFile(
            final String vEventFile)
    throws IndexOutOfBoundsException {
        this.m_eventFileList.add(vEventFile);
    }

    /**
     * 
     * 
     * @param index
     * @param vEventFile
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addEventFile(
            final int index,
            final String vEventFile)
    throws IndexOutOfBoundsException {
        this.m_eventFileList.add(index, vEventFile);
    }

    /**
     * Method enumerateEvent.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Event> enumerateEvent(
    ) {
        return Collections.enumeration(this.m_eventList);
    }

    /**
     * Method enumerateEventFile.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<String> enumerateEventFile(
    ) {
        return Collections.enumeration(this.m_eventFileList);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(
            final Object obj) {
        if ( this == obj )
            return true;
        
        if (obj instanceof Events) {
        
            Events temp = (Events)obj;
            if (this.m_global != null) {
                if (temp.m_global == null) return false;
                else if (!(this.m_global.equals(temp.m_global))) 
                    return false;
            }
            else if (temp.m_global != null)
                return false;
            if (this.m_eventList != null) {
                if (temp.m_eventList == null) return false;
                else if (!(this.m_eventList.equals(temp.m_eventList))) 
                    return false;
            }
            else if (temp.m_eventList != null)
                return false;
            if (this.m_eventFileList != null) {
                if (temp.m_eventFileList == null) return false;
                else if (!(this.m_eventFileList.equals(temp.m_eventFileList))) 
                    return false;
            }
            else if (temp.m_eventFileList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getEvent.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Event at the given index
     */
    public Event getEvent(
            final int index)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_eventList.size()) {
            throw new IndexOutOfBoundsException("getEvent: Index value '" + index + "' not in range [0.." + (this.m_eventList.size() - 1) + "]");
        }
        
        return (Event) m_eventList.get(index);
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
    public Event[] getEvent(
    ) {
        Event[] array = new Event[0];
        return (Event[]) this.m_eventList.toArray(array);
    }

    /**
     * Method getEventCollection.Returns a reference to
     * '_eventList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Event> getEventCollection(
    ) {
        return this.m_eventList;
    }

    /**
     * Method getEventCount.
     * 
     * @return the size of this collection
     */
    public int getEventCount(
    ) {
        return this.m_eventList.size();
    }

    /**
     * Method getEventFile.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getEventFile(
            final int index)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_eventFileList.size()) {
            throw new IndexOutOfBoundsException("getEventFile: Index value '" + index + "' not in range [0.." + (this.m_eventFileList.size() - 1) + "]");
        }
        
        return (String) m_eventFileList.get(index);
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
    public String[] getEventFile(
    ) {
        String[] array = new String[0];
        return (String[]) this.m_eventFileList.toArray(array);
    }

    /**
     * Method getEventFileCollection.Returns a reference to
     * '_eventFileList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getEventFileCollection(
    ) {
        return this.m_eventFileList;
    }

    /**
     * Method getEventFileCount.
     * 
     * @return the size of this collection
     */
    public int getEventFileCount(
    ) {
        return this.m_eventFileList.size();
    }

    /**
     * Returns the value of field 'global'. The field 'global' has
     * the following description: Global settings for this
     * configuration
     * 
     * @return the value of field 'Global'.
     */
    public Global getGlobal(
    ) {
        return this.m_global;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode(
    ) {
        return new HashCodeBuilder(17,37).append(getGlobal()).append(getEventCollection()).
          append(getEventFileCollection()).toHashCode();
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
    public Iterator<Event> iterateEvent(
    ) {
        return this.m_eventList.iterator();
    }

    /**
     * Method iterateEventFile.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<String> iterateEventFile(
    ) {
        return this.m_eventFileList.iterator();
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
        this.m_eventList.clear();
    }

    /**
     */
    public void removeAllEventFile(
    ) {
        this.m_eventFileList.clear();
    }

    /**
     * Method removeEvent.
     * 
     * @param vEvent
     * @return true if the object was removed from the collection.
     */
    public boolean removeEvent(
            final Event vEvent) {
        boolean removed = m_eventList.remove(vEvent);
        return removed;
    }

    /**
     * Method removeEventAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Event removeEventAt(
            final int index) {
        Object obj = this.m_eventList.remove(index);
        return (Event) obj;
    }

    /**
     * Method removeEventFile.
     * 
     * @param vEventFile
     * @return true if the object was removed from the collection.
     */
    public boolean removeEventFile(
            final String vEventFile) {
        boolean removed = m_eventFileList.remove(vEventFile);
        return removed;
    }

    /**
     * Method removeEventFileAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeEventFileAt(
            final int index) {
        Object obj = this.m_eventFileList.remove(index);
        return (String) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vEvent
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setEvent(
            final int index,
            final Event vEvent)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_eventList.size()) {
            throw new IndexOutOfBoundsException("setEvent: Index value '" + index + "' not in range [0.." + (this.m_eventList.size() - 1) + "]");
        }
        
        this.m_eventList.set(index, vEvent);
    }

    /**
     * 
     * 
     * @param vEventArray
     */
    public void setEvent(
            final Event[] vEventArray) {
        //-- copy array
        m_eventList.clear();
        
        for (int i = 0; i < vEventArray.length; i++) {
                this.m_eventList.add(vEventArray[i]);
        }
    }

    /**
     * Sets the value of '_eventList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vEventList the Vector to copy.
     */
    public void setEvent(
            final List<Event> vEventList) {
        // copy vector
        this.m_eventList.clear();
        
        this.m_eventList.addAll(vEventList);
    }

    /**
     * Sets the value of '_eventList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param eventList the Vector to set.
     */
    public void setEventCollection(
            final List<Event> eventList) {
        this.m_eventList = eventList;
    }

    /**
     * 
     * 
     * @param index
     * @param vEventFile
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setEventFile(
            final int index,
            final String vEventFile)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_eventFileList.size()) {
            throw new IndexOutOfBoundsException("setEventFile: Index value '" + index + "' not in range [0.." + (this.m_eventFileList.size() - 1) + "]");
        }
        
        this.m_eventFileList.set(index, vEventFile);
    }

    /**
     * 
     * 
     * @param vEventFileArray
     */
    public void setEventFile(
            final String[] vEventFileArray) {
        //-- copy array
        m_eventFileList.clear();
        
        for (int i = 0; i < vEventFileArray.length; i++) {
                this.m_eventFileList.add(vEventFileArray[i]);
        }
    }

    /**
     * Sets the value of '_eventFileList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vEventFileList the Vector to copy.
     */
    public void setEventFile(
            final List<String> vEventFileList) {
        // copy vector
        this.m_eventFileList.clear();
        
        this.m_eventFileList.addAll(vEventFileList);
    }

    /**
     * Sets the value of '_eventFileList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param eventFileList the Vector to set.
     */
    public void setEventFileCollection(
            final List<String> eventFileList) {
        this.m_eventFileList = eventFileList;
    }

    /**
     * Sets the value of field 'global'. The field 'global' has the
     * following description: Global settings for this
     * configuration
     * 
     * @param global the value of field 'global'.
     */
    public void setGlobal(
            final Global global) {
        this.m_global = global;
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
     * Events
     */
    public static Events unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (Events) Unmarshaller.unmarshal(Events.class, reader);
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
