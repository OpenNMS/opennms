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
 * The mask element
 * 
 * @version $Revision$ $Date$
 */
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
    private java.lang.String _mename;

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
    private java.util.List<java.lang.String> _mevalueList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Maskelement() {
        super();
        this._mevalueList = new java.util.ArrayList<java.lang.String>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vMevalue
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMevalue(
            final java.lang.String vMevalue)
    throws java.lang.IndexOutOfBoundsException {
        this._mevalueList.add(vMevalue);
    }

    /**
     * 
     * 
     * @param index
     * @param vMevalue
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMevalue(
            final int index,
            final java.lang.String vMevalue)
    throws java.lang.IndexOutOfBoundsException {
        this._mevalueList.add(index, vMevalue);
    }

    /**
     * Method enumerateMevalue.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<java.lang.String> enumerateMevalue(
    ) {
        return java.util.Collections.enumeration(this._mevalueList);
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
        
        if (obj instanceof Maskelement) {
        
            Maskelement temp = (Maskelement)obj;
            if (this._mename != null) {
                if (temp._mename == null) return false;
                else if (!(this._mename.equals(temp._mename))) 
                    return false;
            }
            else if (temp._mename != null)
                return false;
            if (this._mevalueList != null) {
                if (temp._mevalueList == null) return false;
                else if (!(this._mevalueList.equals(temp._mevalueList))) 
                    return false;
            }
            else if (temp._mevalueList != null)
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
    public java.lang.String getMename(
    ) {
        return this._mename;
    }

    /**
     * Method getMevalue.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the java.lang.String at the given index
     */
    public java.lang.String getMevalue(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._mevalueList.size()) {
            throw new IndexOutOfBoundsException("getMevalue: Index value '" + index + "' not in range [0.." + (this._mevalueList.size() - 1) + "]");
        }
        
        return (java.lang.String) _mevalueList.get(index);
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
    public java.lang.String[] getMevalue(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._mevalueList.toArray(array);
    }

    /**
     * Method getMevalueCollection.Returns a reference to
     * '_mevalueList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<java.lang.String> getMevalueCollection(
    ) {
        return this._mevalueList;
    }

    /**
     * Method getMevalueCount.
     * 
     * @return the size of this collection
     */
    public int getMevalueCount(
    ) {
        return this._mevalueList.size();
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
        if (_mename != null) {
           result = 37 * result + _mename.hashCode();
        }
        if (_mevalueList != null) {
           result = 37 * result + _mevalueList.hashCode();
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
     * Method iterateMevalue.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<java.lang.String> iterateMevalue(
    ) {
        return this._mevalueList.iterator();
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
        this._mevalueList.clear();
    }

    /**
     * Method removeMevalue.
     * 
     * @param vMevalue
     * @return true if the object was removed from the collection.
     */
    public boolean removeMevalue(
            final java.lang.String vMevalue) {
        boolean removed = _mevalueList.remove(vMevalue);
        return removed;
    }

    /**
     * Method removeMevalueAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeMevalueAt(
            final int index) {
        java.lang.Object obj = this._mevalueList.remove(index);
        return (java.lang.String) obj;
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
            final java.lang.String mename) {
        this._mename = mename;
    }

    /**
     * 
     * 
     * @param index
     * @param vMevalue
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setMevalue(
            final int index,
            final java.lang.String vMevalue)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._mevalueList.size()) {
            throw new IndexOutOfBoundsException("setMevalue: Index value '" + index + "' not in range [0.." + (this._mevalueList.size() - 1) + "]");
        }
        
        this._mevalueList.set(index, vMevalue);
    }

    /**
     * 
     * 
     * @param vMevalueArray
     */
    public void setMevalue(
            final java.lang.String[] vMevalueArray) {
        //-- copy array
        _mevalueList.clear();
        
        for (int i = 0; i < vMevalueArray.length; i++) {
                this._mevalueList.add(vMevalueArray[i]);
        }
    }

    /**
     * Sets the value of '_mevalueList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vMevalueList the Vector to copy.
     */
    public void setMevalue(
            final java.util.List<java.lang.String> vMevalueList) {
        // copy vector
        this._mevalueList.clear();
        
        this._mevalueList.addAll(vMevalueList);
    }

    /**
     * Sets the value of '_mevalueList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param mevalueList the Vector to set.
     */
    public void setMevalueCollection(
            final java.util.List<java.lang.String> mevalueList) {
        this._mevalueList = mevalueList;
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
