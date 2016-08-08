/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.trapd;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.netmgt.config.snmp.Configuration;



  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

/**
 * Top-level element for the trapd-configuration.xml
 *  configuration file.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "trapd-configuration")
@XmlAccessorType(XmlAccessType.NONE)
@SuppressWarnings("all") 
public class TrapdConfiguration implements  Serializable {
	private static final long serialVersionUID = -3548367130814097723L;

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

	/**
     * The IP address on which trapd listens for connections.
     *  If "" is specified, trapd will bind to all addresses. The
     * default is .
     */
	@XmlAttribute(name="snmp-trap-address")
    private java.lang.String _snmpTrapAddress = "*";

    /**
     * The port on which trapd listens for SNMP traps. The
     *  standard port is 162.
     */
	@XmlAttribute(name="snmp-trap-port")
    private int _snmpTrapPort;

    /**
     * keeps track of state for field: _snmpTrapPort
     */
	@XmlTransient
    private boolean _has_snmpTrapPort;

    /**
     * Whether traps from devices unknown to OpenNMS should
     *  generate newSuspect events.
     */
	@XmlAttribute(name="new-suspect-on-trap")
    private boolean _newSuspectOnTrap;

    /**
     * keeps track of state for field: _newSuspectOnTrap
     */
	@XmlTransient
    private boolean _has_newSuspectOnTrap;

    /**
     * SNMPv3 configuration.
     */
	@XmlElement(name="snmpv3-user")
    private java.util.List<Snmpv3User> _snmpv3UserList;


      //----------------/
     //- Constructors -/
    //----------------/

    public TrapdConfiguration() {
        super();
        setSnmpTrapAddress("*");
        this._snmpv3UserList = new java.util.ArrayList<Snmpv3User>();
    }
    
    /*
     * This constructor is used only for junit
     */
    public TrapdConfiguration(int _snmpTrapPort,String snmpTrapAddress) {
        super();
        setSnmpTrapAddress(snmpTrapAddress);
        this._snmpTrapPort = _snmpTrapPort;
        this._snmpv3UserList = new java.util.ArrayList<Snmpv3User>();
    }

      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vSnmpv3User
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSnmpv3User(
            final Snmpv3User vSnmpv3User)
    throws java.lang.IndexOutOfBoundsException {
        this._snmpv3UserList.add(vSnmpv3User);
    }

    /**
     * 
     * 
     * @param index
     * @param vSnmpv3User
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSnmpv3User(
            final int index,
            final Snmpv3User vSnmpv3User)
    throws java.lang.IndexOutOfBoundsException {
        this._snmpv3UserList.add(index, vSnmpv3User);
    }

    /**
     */
    public void deleteNewSuspectOnTrap(
    ) {
        this._has_newSuspectOnTrap= false;
    }

    /**
     */
    public void deleteSnmpTrapPort(
    ) {
        this._has_snmpTrapPort= false;
    }

    /**
     * Method enumerateSnmpv3User.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<Snmpv3User> enumerateSnmpv3User(
    ) {
        return java.util.Collections.enumeration(this._snmpv3UserList);
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
        
        if (obj instanceof TrapdConfiguration) {
        
            TrapdConfiguration temp = (TrapdConfiguration)obj;
            if (this._snmpTrapAddress != null) {
                if (temp._snmpTrapAddress == null) return false;
                else if (!(this._snmpTrapAddress.equals(temp._snmpTrapAddress))) 
                    return false;
            }
            else if (temp._snmpTrapAddress != null)
                return false;
            if (this._snmpTrapPort != temp._snmpTrapPort)
                return false;
            if (this._has_snmpTrapPort != temp._has_snmpTrapPort)
                return false;
            if (this._newSuspectOnTrap != temp._newSuspectOnTrap)
                return false;
            if (this._has_newSuspectOnTrap != temp._has_newSuspectOnTrap)
                return false;
            if (this._snmpv3UserList != null) {
                if (temp._snmpv3UserList == null) return false;
                else if (!(this._snmpv3UserList.equals(temp._snmpv3UserList))) 
                    return false;
            }
            else if (temp._snmpv3UserList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'newSuspectOnTrap'. The field
     * 'newSuspectOnTrap' has the following description: Whether
     * traps from devices unknown to OpenNMS should
     *  generate newSuspect events.
     * 
     * @return the value of field 'NewSuspectOnTrap'.
     */
    public boolean getNewSuspectOnTrap(
    ) {
        return this._newSuspectOnTrap;
    }

    /**
     * Returns the value of field 'snmpTrapAddress'. The field
     * 'snmpTrapAddress' has the following description: The IP
     * address on which trapd listens for connections.
     *  If "" is specified, trapd will bind to all addresses. The
     * default is .
     * 
     * @return the value of field 'SnmpTrapAddress'.
     */
    public java.lang.String getSnmpTrapAddress(
    ) {
        return this._snmpTrapAddress;
    }

    /**
     * Returns the value of field 'snmpTrapPort'. The field
     * 'snmpTrapPort' has the following description: The port on
     * which trapd listens for SNMP traps. The
     *  standard port is 162.
     * 
     * @return the value of field 'SnmpTrapPort'.
     */
    public int getSnmpTrapPort(
    ) {
    	return this._snmpTrapPort;
    }

    /**
     * Method getSnmpv3User.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Snmpv3User at the given index
     */
    public Snmpv3User getSnmpv3User(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._snmpv3UserList.size()) {
            throw new IndexOutOfBoundsException("getSnmpv3User: Index value '" + index + "' not in range [0.." + (this._snmpv3UserList.size() - 1) + "]");
        }
        
        return (Snmpv3User) _snmpv3UserList.get(index);
    }

    /**
     * Method getSnmpv3User.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Snmpv3User[] getSnmpv3User(
    ) {
        Snmpv3User[] array = new Snmpv3User[0];
        return (Snmpv3User[]) this._snmpv3UserList.toArray(array);
    }

    /**
     * Method getSnmpv3UserCollection.Returns a reference to
     * '_snmpv3UserList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Snmpv3User> getSnmpv3UserCollection(
    ) {
        return this._snmpv3UserList;
    }

    /**
     * Method getSnmpv3UserCount.
     * 
     * @return the size of this collection
     */
    public int getSnmpv3UserCount(
    ) {
        return this._snmpv3UserList.size();
    }

    /**
     * Method hasNewSuspectOnTrap.
     * 
     * @return true if at least one NewSuspectOnTrap has been added
     */
    public boolean hasNewSuspectOnTrap(
    ) {
        return this._has_newSuspectOnTrap;
    }

    /**
     * Method hasSnmpTrapPort.
     * 
     * @return true if at least one SnmpTrapPort has been added
     */
    public boolean hasSnmpTrapPort(
    ) {
        return this._has_snmpTrapPort;
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
        if (_snmpTrapAddress != null) {
           result = 37 * result + _snmpTrapAddress.hashCode();
        }
        result = 37 * result + _snmpTrapPort;
        result = 37 * result + (_newSuspectOnTrap?0:1);
        if (_snmpv3UserList != null) {
           result = 37 * result + _snmpv3UserList.hashCode();
        }
        
        return result;
    }

    /**
     * Returns the value of field 'newSuspectOnTrap'. The field
     * 'newSuspectOnTrap' has the following description: Whether
     * traps from devices unknown to OpenNMS should
     *  generate newSuspect events.
     * 
     * @return the value of field 'NewSuspectOnTrap'.
     */
    public boolean isNewSuspectOnTrap(
    ) {
        return this._newSuspectOnTrap;
    }


    /**
     * Method iterateSnmpv3User.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<Snmpv3User> iterateSnmpv3User(
    ) {
        return this._snmpv3UserList.iterator();
    }

    /**
     */
    public void removeAllSnmpv3User(
    ) {
        this._snmpv3UserList.clear();
    }

    /**
     * Method removeSnmpv3User.
     * 
     * @param vSnmpv3User
     * @return true if the object was removed from the collection.
     */
    public boolean removeSnmpv3User(
            final Snmpv3User vSnmpv3User) {
        boolean removed = _snmpv3UserList.remove(vSnmpv3User);
        return removed;
    }

    /**
     * Method removeSnmpv3UserAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Snmpv3User removeSnmpv3UserAt(
            final int index) {
        java.lang.Object obj = this._snmpv3UserList.remove(index);
        return (Snmpv3User) obj;
    }

    /**
     * Sets the value of field 'newSuspectOnTrap'. The field
     * 'newSuspectOnTrap' has the following description: Whether
     * traps from devices unknown to OpenNMS should
     *  generate newSuspect events.
     * 
     * @param newSuspectOnTrap the value of field 'newSuspectOnTrap'
     */
    public void setNewSuspectOnTrap(
            final boolean newSuspectOnTrap) {
        this._newSuspectOnTrap = newSuspectOnTrap;
        this._has_newSuspectOnTrap = true;
    }

    /**
     * Sets the value of field 'snmpTrapAddress'. The field
     * 'snmpTrapAddress' has the following description: The IP
     * address on which trapd listens for connections.
     *  If "" is specified, trapd will bind to all addresses. The
     * default is .
     * 
     * @param snmpTrapAddress the value of field 'snmpTrapAddress'.
     */
    public void setSnmpTrapAddress(
            final java.lang.String snmpTrapAddress) {
        this._snmpTrapAddress = snmpTrapAddress;
    }

    /**
     * Sets the value of field 'snmpTrapPort'. The field
     * 'snmpTrapPort' has the following description: The port on
     * which trapd listens for SNMP traps. The
     *  standard port is 162.
     * 
     * @param snmpTrapPort the value of field 'snmpTrapPort'.
     */
    public void setSnmpTrapPort(
            final int snmpTrapPort) {
        this._snmpTrapPort = snmpTrapPort;
        this._has_snmpTrapPort = true;
    }

    /**
     * 
     * 
     * @param index
     * @param vSnmpv3User
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setSnmpv3User(
            final int index,
            final Snmpv3User vSnmpv3User)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._snmpv3UserList.size()) {
            throw new IndexOutOfBoundsException("setSnmpv3User: Index value '" + index + "' not in range [0.." + (this._snmpv3UserList.size() - 1) + "]");
        }
        
        this._snmpv3UserList.set(index, vSnmpv3User);
    }

    /**
     * 
     * 
     * @param vSnmpv3UserArray
     */
    public void setSnmpv3User(
            final Snmpv3User[] vSnmpv3UserArray) {
        //-- copy array
        _snmpv3UserList.clear();
        
        for (int i = 0; i < vSnmpv3UserArray.length; i++) {
                this._snmpv3UserList.add(vSnmpv3UserArray[i]);
        }
    }

    /**
     * Sets the value of '_snmpv3UserList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vSnmpv3UserList the Vector to copy.
     */
    public void setSnmpv3User(
            final java.util.List<Snmpv3User> vSnmpv3UserList) {
        // copy vector
        this._snmpv3UserList.clear();
        
        this._snmpv3UserList.addAll(vSnmpv3UserList);
    }

    /**
     * Sets the value of '_snmpv3UserList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param snmpv3UserList the Vector to set.
     */
    public void setSnmpv3UserCollection(
            final java.util.List<Snmpv3User> snmpv3UserList) {
        this._snmpv3UserList = snmpv3UserList;
    }
    

}