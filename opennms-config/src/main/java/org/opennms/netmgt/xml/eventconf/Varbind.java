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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * The varbind element
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name="varbind")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("serial")
public class Varbind implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _textualConvention.
     */
	@XmlAttribute(name="textual-convention", required=false)
    private String m_textualConvention;

    /**
     * The varbind element number
     */
	@XmlElement(name="vbnumber", required=true)
    private Integer m_vbnumber;

    /**
     * The varbind element value
     */
	@XmlElement(name="vbvalue", required=true)
    private List<String> vbvalueList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Varbind() {
        super();
        this.vbvalueList = new ArrayList<String>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vVbvalue
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addVbvalue(
            final String vVbvalue)
    throws IndexOutOfBoundsException {
        this.vbvalueList.add(vVbvalue);
    }

    /**
     * 
     * 
     * @param index
     * @param vVbvalue
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addVbvalue(
            final int index,
            final String vVbvalue)
    throws IndexOutOfBoundsException {
        this.vbvalueList.add(index, vVbvalue);
    }

    /**
     */
    public void deleteVbnumber(
    ) {
        m_vbnumber = null;
    }

    /**
     * Method enumerateVbvalue.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<String> enumerateVbvalue(
    ) {
        return Collections.enumeration(this.vbvalueList);
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
        
        if (obj instanceof Varbind) {
        
            Varbind temp = (Varbind)obj;
            if (this.m_textualConvention != null) {
                if (temp.m_textualConvention == null) return false;
                else if (!(this.m_textualConvention.equals(temp.m_textualConvention))) 
                    return false;
            }
            else if (temp.m_textualConvention != null)
                return false;
            if (this.m_vbnumber != temp.m_vbnumber)
                return false;
            if (this.vbvalueList != null) {
                if (temp.vbvalueList == null) return false;
                else if (!(this.vbvalueList.equals(temp.vbvalueList))) 
                    return false;
            }
            else if (temp.vbvalueList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'textualConvention'.
     * 
     * @return the value of field 'TextualConvention'.
     */
    public String getTextualConvention(
    ) {
        return this.m_textualConvention;
    }

    /**
     * Returns the value of field 'vbnumber'. The field 'vbnumber'
     * has the following description: The varbind element number
     * 
     * @return the value of field 'Vbnumber'.
     */
    public int getVbnumber(
    ) {
        return this.m_vbnumber;
    }

    /**
     * Method getVbvalue.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getVbvalue(
            final int index)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.vbvalueList.size()) {
            throw new IndexOutOfBoundsException("getVbvalue: Index value '" + index + "' not in range [0.." + (this.vbvalueList.size() - 1) + "]");
        }
        
        return (String) vbvalueList.get(index);
    }

    /**
     * Method getVbvalue.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public String[] getVbvalue(
    ) {
        String[] array = new String[0];
        return (String[]) this.vbvalueList.toArray(array);
    }

    /**
     * Method getVbvalueCollection.Returns a reference to
     * '_vbvalueList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getVbvalueCollection(
    ) {
        return this.vbvalueList;
    }

    /**
     * Method getVbvalueCount.
     * 
     * @return the size of this collection
     */
    public int getVbvalueCount(
    ) {
        return this.vbvalueList.size();
    }

    /**
     * Method hasVbnumber.
     * 
     * @return true if at least one Vbnumber has been added
     */
    public boolean hasVbnumber(
    ) {
        return m_vbnumber != null;
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
    	return new HashCodeBuilder(17,37).append(getTextualConvention()).append(getVbnumber()).append(getVbvalue()).toHashCode();
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
     * Method iterateVbvalue.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<String> iterateVbvalue(
    ) {
        return this.vbvalueList.iterator();
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
    public void removeAllVbvalue(
    ) {
        this.vbvalueList.clear();
    }

    /**
     * Method removeVbvalue.
     * 
     * @param vVbvalue
     * @return true if the object was removed from the collection.
     */
    public boolean removeVbvalue(
            final String vVbvalue) {
        boolean removed = vbvalueList.remove(vVbvalue);
        return removed;
    }

    /**
     * Method removeVbvalueAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeVbvalueAt(
            final int index) {
        Object obj = this.vbvalueList.remove(index);
        return (String) obj;
    }

    /**
     * Sets the value of field 'textualConvention'.
     * 
     * @param textualConvention the value of field
     * 'textualConvention'.
     */
    public void setTextualConvention(
            final String textualConvention) {
        this.m_textualConvention = textualConvention;
    }

    /**
     * Sets the value of field 'vbnumber'. The field 'vbnumber' has
     * the following description: The varbind element number
     * 
     * @param vbnumber the value of field 'vbnumber'.
     */
    public void setVbnumber(
            final int vbnumber) {
        this.m_vbnumber = vbnumber;
    }

    /**
     * 
     * 
     * @param index
     * @param vVbvalue
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setVbvalue(
            final int index,
            final String vVbvalue)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.vbvalueList.size()) {
            throw new IndexOutOfBoundsException("setVbvalue: Index value '" + index + "' not in range [0.." + (this.vbvalueList.size() - 1) + "]");
        }
        
        this.vbvalueList.set(index, vVbvalue);
    }

    /**
     * 
     * 
     * @param vVbvalueArray
     */
    public void setVbvalue(
            final String[] vVbvalueArray) {
        //-- copy array
        vbvalueList.clear();
        
        for (int i = 0; i < vVbvalueArray.length; i++) {
                this.vbvalueList.add(vVbvalueArray[i]);
        }
    }

    /**
     * Sets the value of '_vbvalueList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vVbvalueList the Vector to copy.
     */
    public void setVbvalue(
            final List<String> vVbvalueList) {
        // copy vector
        this.vbvalueList.clear();
        
        this.vbvalueList.addAll(vVbvalueList);
    }

    /**
     * Sets the value of '_vbvalueList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param vbvalueList the Vector to set.
     */
    public void setVbvalueCollection(
            final List<String> vbvalueList) {
        this.vbvalueList = vbvalueList;
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
     * org.opennms.netmgt.xml.eventconf.Varbind
     */
    public static org.opennms.netmgt.xml.eventconf.Varbind unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.Varbind) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.Varbind.class, reader);
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
