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
 * The event correlation information
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name="correlation")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("serial")
public class Correlation implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The state determines if event is
     *  correlated
     */
	@XmlAttribute(name="state")
    private String m_state;

    /**
     * The correlation path
     */
	@XmlAttribute(name="path")
    private String m_path;

    /**
     * A canceling UEI for this event
     */
	@XmlElement(name="cuei")
    private List<String> m_cueiList;

    /**
     * The minimum count for this event
     */
	@XmlElement(name="cmin")
    private String m_cmin;

    /**
     * The maximum count for this event
     */
	@XmlElement(name="cmax")
    private String m_cmax;

    /**
     * The correlation time for this event
     */
	@XmlElement(name="ctime")
    private String m_ctime;


      //----------------/
     //- Constructors -/
    //----------------/

    public Correlation() {
        super();
        this.m_cueiList = new ArrayList<String>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vCuei
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addCuei(
            final String vCuei)
    throws java.lang.IndexOutOfBoundsException {
        this.m_cueiList.add(vCuei);
    }

    /**
     * 
     * 
     * @param index
     * @param vCuei
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addCuei(
            final int index,
            final String vCuei)
    throws java.lang.IndexOutOfBoundsException {
        this.m_cueiList.add(index, vCuei);
    }

    /**
     * Method enumerateCuei.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<String> enumerateCuei(
    ) {
        return Collections.enumeration(this.m_cueiList);
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
        
        if (obj instanceof Correlation) {
        
            Correlation temp = (Correlation)obj;
            if (this.m_state != null) {
                if (temp.m_state == null) return false;
                else if (!(this.m_state.equals(temp.m_state))) 
                    return false;
            }
            else if (temp.m_state != null)
                return false;
            if (this.m_path != null) {
                if (temp.m_path == null) return false;
                else if (!(this.m_path.equals(temp.m_path))) 
                    return false;
            }
            else if (temp.m_path != null)
                return false;
            if (this.m_cueiList != null) {
                if (temp.m_cueiList == null) return false;
                else if (!(this.m_cueiList.equals(temp.m_cueiList))) 
                    return false;
            }
            else if (temp.m_cueiList != null)
                return false;
            if (this.m_cmin != null) {
                if (temp.m_cmin == null) return false;
                else if (!(this.m_cmin.equals(temp.m_cmin))) 
                    return false;
            }
            else if (temp.m_cmin != null)
                return false;
            if (this.m_cmax != null) {
                if (temp.m_cmax == null) return false;
                else if (!(this.m_cmax.equals(temp.m_cmax))) 
                    return false;
            }
            else if (temp.m_cmax != null)
                return false;
            if (this.m_ctime != null) {
                if (temp.m_ctime == null) return false;
                else if (!(this.m_ctime.equals(temp.m_ctime))) 
                    return false;
            }
            else if (temp.m_ctime != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'cmax'. The field 'cmax' has the
     * following description: The maximum count for this event
     * 
     * @return the value of field 'Cmax'.
     */
    public String getCmax(
    ) {
        return this.m_cmax;
    }

    /**
     * Returns the value of field 'cmin'. The field 'cmin' has the
     * following description: The minimum count for this event
     * 
     * @return the value of field 'Cmin'.
     */
    public String getCmin(
    ) {
        return this.m_cmin;
    }

    /**
     * Returns the value of field 'ctime'. The field 'ctime' has
     * the following description: The correlation time for this
     * event
     * 
     * @return the value of field 'Ctime'.
     */
    public String getCtime(
    ) {
        return this.m_ctime;
    }

    /**
     * Method getCuei.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getCuei(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_cueiList.size()) {
            throw new IndexOutOfBoundsException("getCuei: Index value '" + index + "' not in range [0.." + (this.m_cueiList.size() - 1) + "]");
        }
        
        return (String) m_cueiList.get(index);
    }

    /**
     * Method getCuei.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public String[] getCuei(
    ) {
        String[] array = new String[0];
        return (String[]) this.m_cueiList.toArray(array);
    }

    /**
     * Method getCueiCollection.Returns a reference to '_cueiList'.
     * No type checking is performed on any modifications to the
     * Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getCueiCollection(
    ) {
        return this.m_cueiList;
    }

    /**
     * Method getCueiCount.
     * 
     * @return the size of this collection
     */
    public int getCueiCount(
    ) {
        return this.m_cueiList.size();
    }

    /**
     * Returns the value of field 'path'. The field 'path' has the
     * following description: The correlation path
     * 
     * @return the value of field 'Path'.
     */
    public String getPath(
    ) {
        return this.m_path;
    }

    /**
     * Returns the value of field 'state'. The field 'state' has
     * the following description: The state determines if event is
     *  correlated
     * 
     * @return the value of field 'State'.
     */
    public String getState(
    ) {
        return this.m_state;
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * @return a hash code value for the object.
     */
    public int hashCode(
    ) {
        return new HashCodeBuilder(17,37).append(getCmax()).append(getCmin()).append(getCtime()).
        	append(getCueiCollection()).append(getPath()).append(getState()).toHashCode();
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
     * Method iterateCuei.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<String> iterateCuei(
    ) {
        return this.m_cueiList.iterator();
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
    public void removeAllCuei(
    ) {
        this.m_cueiList.clear();
    }

    /**
     * Method removeCuei.
     * 
     * @param vCuei
     * @return true if the object was removed from the collection.
     */
    public boolean removeCuei(
            final String vCuei) {
        boolean removed = m_cueiList.remove(vCuei);
        return removed;
    }

    /**
     * Method removeCueiAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeCueiAt(
            final int index) {
        java.lang.Object obj = this.m_cueiList.remove(index);
        return (String) obj;
    }

    /**
     * Sets the value of field 'cmax'. The field 'cmax' has the
     * following description: The maximum count for this event
     * 
     * @param cmax the value of field 'cmax'.
     */
    public void setCmax(
            final String cmax) {
        this.m_cmax = cmax;
    }

    /**
     * Sets the value of field 'cmin'. The field 'cmin' has the
     * following description: The minimum count for this event
     * 
     * @param cmin the value of field 'cmin'.
     */
    public void setCmin(
            final String cmin) {
        this.m_cmin = cmin;
    }

    /**
     * Sets the value of field 'ctime'. The field 'ctime' has the
     * following description: The correlation time for this event
     * 
     * @param ctime the value of field 'ctime'.
     */
    public void setCtime(
            final String ctime) {
        this.m_ctime = ctime;
    }

    /**
     * 
     * 
     * @param index
     * @param vCuei
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setCuei(
            final int index,
            final String vCuei)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_cueiList.size()) {
            throw new IndexOutOfBoundsException("setCuei: Index value '" + index + "' not in range [0.." + (this.m_cueiList.size() - 1) + "]");
        }
        
        this.m_cueiList.set(index, vCuei);
    }

    /**
     * 
     * 
     * @param vCueiArray
     */
    public void setCuei(
            final String[] vCueiArray) {
        //-- copy array
        m_cueiList.clear();
        
        for (int i = 0; i < vCueiArray.length; i++) {
                this.m_cueiList.add(vCueiArray[i]);
        }
    }

    /**
     * Sets the value of '_cueiList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vCueiList the Vector to copy.
     */
    public void setCuei(
            final List<String> vCueiList) {
        // copy vector
        this.m_cueiList.clear();
        
        this.m_cueiList.addAll(vCueiList);
    }

    /**
     * Sets the value of '_cueiList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param cueiList the Vector to set.
     */
    public void setCueiCollection(
            final List<String> cueiList) {
        this.m_cueiList = cueiList;
    }

    /**
     * Sets the value of field 'path'. The field 'path' has the
     * following description: The correlation path
     * 
     * @param path the value of field 'path'.
     */
    public void setPath(
            final String path) {
        this.m_path = path.intern();
    }

    /**
     * Sets the value of field 'state'. The field 'state' has the
     * following description: The state determines if event is
     *  correlated
     * 
     * @param state the value of field 'state'.
     */
    public void setState(
            final String state) {
        this.m_state = state.intern();
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
     * org.opennms.netmgt.xml.eventconf.Correlation
     */
    public static org.opennms.netmgt.xml.eventconf.Correlation unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.Correlation) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.Correlation.class, reader);
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
