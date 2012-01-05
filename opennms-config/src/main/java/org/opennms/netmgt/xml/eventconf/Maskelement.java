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
 * The mask element
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name="maskelement")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("serial")
public class Maskelement implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The mask element name. Must be from the following
     *  subset: "uei" (the OpenNMS Universal Event Identifier),
     * "source"
     *  (source of the event; "trapd" for received SNMP traps;
     * warning:
     *  these aren't that standardized), "host" (host related to
     * the
     *  event; for SNMP traps this is the IP source address of the
     * host
     *  that sent the trap to OpenNMS, "snmphost" (SNMP host
     * related to
     *  the event; for SNMPv1 traps this is IP address reported in
     * the
     *  trap; for SNMPv2 traps and later this is the same as
     * "host"),
     *  "nodeid" (the OpenNMS node identifier for the node related
     * to this
     *  event), "interface" (interface related to the event; for
     * SNMP
     *  traps this is the same as "snmphost"), "service", "id"
     * (enterprise
     *  ID in an SNMP trap), "specific" (specific value in an SNMP
     * trap),
     *  "generic" (generic value in an SNMP trap), or "community"
     *  (community string in an SNMP trap).
     */
	@XmlElement(name="mename", required=true)
    private String m_mename;

    /**
     * The mask element value. A case-sensitive, exact
     *  match is performed. If the mask value has a "%" as the last
     *  character, it will match zero or more characters at the end
     * of the
     *  string being matched. The mask element value matching is
     * performed
     *  by
     *  org.opennms.netmgt.eventd.datablock.EventConfData#eventValuePassesMaskValue.
     */
	@XmlElement(name="mevalue", required=true)
    private List<String> m_mevalueList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Maskelement() {
        super();
        this.m_mevalueList = new ArrayList<String>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vMevalue
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMevalue(
            final String vMevalue)
    throws IndexOutOfBoundsException {
        this.m_mevalueList.add(vMevalue.intern());
    }

    /**
     * 
     * 
     * @param index
     * @param vMevalue
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMevalue(
            final int index,
            final String vMevalue)
    throws IndexOutOfBoundsException {
        this.m_mevalueList.add(index, vMevalue.intern());
    }

    /**
     * Method enumerateMevalue.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<String> enumerateMevalue(
    ) {
        return Collections.enumeration(this.m_mevalueList);
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
        
        if (obj instanceof Maskelement) {
        
            Maskelement temp = (Maskelement)obj;
            if (this.m_mename != null) {
                if (temp.m_mename == null) return false;
                else if (!(this.m_mename.equals(temp.m_mename))) 
                    return false;
            }
            else if (temp.m_mename != null)
                return false;
            if (this.m_mevalueList != null) {
                if (temp.m_mevalueList == null) return false;
                else if (!(this.m_mevalueList.equals(temp.m_mevalueList))) 
                    return false;
            }
            else if (temp.m_mevalueList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'mename'. The field 'mename' has
     * the following description: The mask element name. Must be
     * from the following
     *  subset: "uei" (the OpenNMS Universal Event Identifier),
     * "source"
     *  (source of the event; "trapd" for received SNMP traps;
     * warning:
     *  these aren't that standardized), "host" (host related to
     * the
     *  event; for SNMP traps this is the IP source address of the
     * host
     *  that sent the trap to OpenNMS, "snmphost" (SNMP host
     * related to
     *  the event; for SNMPv1 traps this is IP address reported in
     * the
     *  trap; for SNMPv2 traps and later this is the same as
     * "host"),
     *  "nodeid" (the OpenNMS node identifier for the node related
     * to this
     *  event), "interface" (interface related to the event; for
     * SNMP
     *  traps this is the same as "snmphost"), "service", "id"
     * (enterprise
     *  ID in an SNMP trap), "specific" (specific value in an SNMP
     * trap),
     *  "generic" (generic value in an SNMP trap), or "community"
     *  (community string in an SNMP trap).
     * 
     * @return the value of field 'Mename'.
     */
    public String getMename(
    ) {
        return this.m_mename;
    }

    /**
     * Method getMevalue.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getMevalue(
            final int index)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_mevalueList.size()) {
            throw new IndexOutOfBoundsException("getMevalue: Index value '" + index + "' not in range [0.." + (this.m_mevalueList.size() - 1) + "]");
        }
        
        return (String) m_mevalueList.get(index);
    }

    /**
     * Method getMevalue.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public String[] getMevalue(
    ) {
        String[] array = new String[0];
        return (String[]) this.m_mevalueList.toArray(array);
    }

    /**
     * Method getMevalueCollection.Returns a reference to
     * '_mevalueList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getMevalueCollection(
    ) {
        return this.m_mevalueList;
    }

    /**
     * Method getMevalueCount.
     * 
     * @return the size of this collection
     */
    public int getMevalueCount(
    ) {
        return this.m_mevalueList.size();
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
        return new HashCodeBuilder(17,37).append(getMename()).append(getMevalue()).toHashCode();
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
     * Method iterateMevalue.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<String> iterateMevalue(
    ) {
        return this.m_mevalueList.iterator();
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
    public void removeAllMevalue(
    ) {
        this.m_mevalueList.clear();
    }

    /**
     * Method removeMevalue.
     * 
     * @param vMevalue
     * @return true if the object was removed from the collection.
     */
    public boolean removeMevalue(
            final String vMevalue) {
        boolean removed = m_mevalueList.remove(vMevalue);
        return removed;
    }

    /**
     * Method removeMevalueAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeMevalueAt(
            final int index) {
        Object obj = this.m_mevalueList.remove(index);
        return (String) obj;
    }

    /**
     * Sets the value of field 'mename'. The field 'mename' has the
     * following description: The mask element name. Must be from
     * the following
     *  subset: "uei" (the OpenNMS Universal Event Identifier),
     * "source"
     *  (source of the event; "trapd" for received SNMP traps;
     * warning:
     *  these aren't that standardized), "host" (host related to
     * the
     *  event; for SNMP traps this is the IP source address of the
     * host
     *  that sent the trap to OpenNMS, "snmphost" (SNMP host
     * related to
     *  the event; for SNMPv1 traps this is IP address reported in
     * the
     *  trap; for SNMPv2 traps and later this is the same as
     * "host"),
     *  "nodeid" (the OpenNMS node identifier for the node related
     * to this
     *  event), "interface" (interface related to the event; for
     * SNMP
     *  traps this is the same as "snmphost"), "service", "id"
     * (enterprise
     *  ID in an SNMP trap), "specific" (specific value in an SNMP
     * trap),
     *  "generic" (generic value in an SNMP trap), or "community"
     *  (community string in an SNMP trap).
     * 
     * @param mename the value of field 'mename'.
     */
    public void setMename(
            final String mename) {
        this.m_mename = mename.intern();
    }

    /**
     * 
     * 
     * @param index
     * @param vMevalue
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setMevalue(
            final int index,
            final String vMevalue)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_mevalueList.size()) {
            throw new IndexOutOfBoundsException("setMevalue: Index value '" + index + "' not in range [0.." + (this.m_mevalueList.size() - 1) + "]");
        }
        
        this.m_mevalueList.set(index, vMevalue.intern());
    }

    /**
     * 
     * 
     * @param vMevalueArray
     */
    public void setMevalue(
            final String[] vMevalueArray) {
        //-- copy array
        m_mevalueList.clear();
        
        for (int i = 0; i < vMevalueArray.length; i++) {
                this.m_mevalueList.add(vMevalueArray[i].intern());
        }
    }

    /**
     * Sets the value of '_mevalueList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vMevalueList the Vector to copy.
     */
    public void setMevalue(
            final List<String> vMevalueList) {
        // copy vector
        this.m_mevalueList.clear();
        for (final String value : vMevalueList) {
            this.m_mevalueList.add(value.intern());
        }
    }

    /**
     * Sets the value of '_mevalueList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param mevalueList the Vector to set.
     */
    public void setMevalueCollection(
            final List<String> mevalueList) {
        this.m_mevalueList.clear();
        for (final String value : mevalueList) {
            this.m_mevalueList.add(value.intern());
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
     * @return the unmarshaled
     * org.opennms.netmgt.xml.eventconf.Maskelement
     */
    public static org.opennms.netmgt.xml.eventconf.Maskelement unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.Maskelement) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.Maskelement.class, reader);
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
