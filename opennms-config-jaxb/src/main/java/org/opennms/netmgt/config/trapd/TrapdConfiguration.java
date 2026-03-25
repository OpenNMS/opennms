/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.config.trapd;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.opennms.core.xml.ValidateUsing;



/**
 * Top-level element for the trapd-configuration.xml
 *  configuration file.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "trapd-configuration")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("trapd-configuration.xsd")
@SuppressWarnings("all") 
public class TrapdConfiguration implements  Serializable {
	private static final long serialVersionUID = 2;

	public static final boolean DEFAULT_USE_ADDRESS_FROM_VARBIND = false;

	/**
     * The IP address on which trapd listens for connections.
     *  If "" is specified, trapd will bind to all addresses. The
     * default is .
     */
	@XmlAttribute(name="snmp-trap-address")
    private java.lang.String snmpTrapAddress = "*";

    /**
     * The port on which trapd listens for SNMP traps. The
     *  standard port is 162.
     */
	@XmlAttribute(name="snmp-trap-port", required=true)
    private int snmpTrapPort;

    /**
     * keeps track of state for field: _snmpTrapPort
     */
	@XmlTransient
    @JsonIgnore
    private boolean hasSnmpTrapPort;

    /**
     * Whether traps from devices unknown to OpenNMS should
     *  generate newSuspect events.
     */
	@XmlAttribute(name="new-suspect-on-trap", required=true)
    private boolean newSuspectOnTrap;

	@XmlAttribute(name="include-raw-message", required=false)
    private boolean includeRawMessage;

    /**
     * Number of threads used for consuming/dispatching messages.
     * Defaults to 2 x the number of available processors.
     */
	@XmlAttribute(name="threads", required=false)
    private int threads = 0;

    /**
     * Maximum number of messages to keep in memory while waiting
     to be dispatched.
     */
	@XmlAttribute(name="queue-size", required=false)
    private int queueSize = 10000;

    /**
     * Messages are aggregated in batches before being dispatched.
     * When the batch reaches this size, it will be dispatched.
     */
	@XmlAttribute(name="batch-size", required=false)
    private int batchSize = 1000;

    /**
     * Messages are aggregated in batches before being dispatched.
     * When the batch has been created for longer than this interval (ms)
     * it will be dispatched, regardless of the current size.
     */
	@XmlAttribute(name="batch-interval", required=false)
    private int batchInterval = 500;

    /**
     * keeps track of state for field: _newSuspectOnTrap
     */
	@XmlTransient
    @JsonIgnore
    private boolean hasNewSuspectOnTrap;

    /**
     * SNMPv3 configuration.
     */
	@XmlElement(name="snmpv3-user")
    private java.util.List<Snmpv3User> snmpv3User;

	/**
	 * When enabled, the source address of the trap will be pulled
     * from the snmpTrapAddress (1.3.6.1.6.3.18.1.3.0) varbind when available.
     * This varbind is appended by certain trap forwarders when forwarding
     * SNMPv2 traps.
	 */
	@XmlAttribute(name="use-address-from-varbind", required=false)
    private Boolean useAddressFromVarbind;

    public TrapdConfiguration() {
        super();
        setSnmpTrapAddress("*");
        this.snmpv3User = new java.util.ArrayList<>();
    }
    
    /*
     * This constructor is used only for junit
     */
    public TrapdConfiguration(int snmpTrapPort, String snmpTrapAddress) {
        super();
        setSnmpTrapAddress(snmpTrapAddress);
        this.snmpTrapPort = snmpTrapPort;
        this.snmpv3User = new java.util.ArrayList<>();
    }

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
        this.snmpv3User.add(vSnmpv3User);
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
        this.snmpv3User.add(index, vSnmpv3User);
    }

    /**
     */
    public void deleteNewSuspectOnTrap(
    ) {
        this.hasNewSuspectOnTrap = false;
    }

    /**
     */
    public void deleteSnmpTrapPort(
    ) {
        this.hasSnmpTrapPort = false;
    }

    /**
     * Method enumerateSnmpv3User.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<Snmpv3User> enumerateSnmpv3User(
    ) {
        return java.util.Collections.enumeration(this.snmpv3User);
    }

    public int hashCode() {
        return Objects.hash(snmpTrapAddress, snmpTrapPort, hasSnmpTrapPort, newSuspectOnTrap, snmpv3User,
                includeRawMessage, threads, queueSize, batchSize, batchInterval, useAddressFromVarbind);
    }

    @Override()
    public boolean equals(final java.lang.Object obj) {
        if ( this == obj) return true;
        if (obj == null) return false;

        if (obj instanceof TrapdConfiguration) {
            final TrapdConfiguration other = (TrapdConfiguration)obj;
            final boolean equals = Objects.equals(snmpTrapAddress, other.snmpTrapAddress)
                    && Objects.equals(snmpTrapPort, other.snmpTrapPort)
                    && Objects.equals(hasSnmpTrapPort, other.hasSnmpTrapPort)
                    && Objects.equals(newSuspectOnTrap, other.newSuspectOnTrap)
                    && Objects.equals(snmpv3User, other.snmpv3User)
                    && Objects.equals(includeRawMessage, other.includeRawMessage)
                    && Objects.equals(threads, other.threads)
                    && Objects.equals(queueSize, other.queueSize)
                    && Objects.equals(batchSize, other.batchSize)
                    && Objects.equals(batchInterval, other.batchInterval)
                    && Objects.equals(useAddressFromVarbind, other.useAddressFromVarbind);
            return equals;
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
        return this.newSuspectOnTrap;
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
        return this.snmpTrapAddress;
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
    	return this.snmpTrapPort;
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
        if (index < 0 || index >= this.snmpv3User.size()) {
            throw new IndexOutOfBoundsException("getSnmpv3User: Index value '" + index + "' not in range [0.." + (this.snmpv3User.size() - 1) + "]");
        }
        
        return (Snmpv3User) snmpv3User.get(index);
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
        return (Snmpv3User[]) this.snmpv3User.toArray(array);
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
        return this.snmpv3User;
    }

    /**
     * Method getSnmpv3UserCount.
     * 
     * @return the size of this collection
     */
    @XmlTransient
    public int getSnmpv3UserCount(
    ) {
        return this.snmpv3User.size();
    }

    /**
     * Method hasNewSuspectOnTrap.
     * 
     * @return true if at least one NewSuspectOnTrap has been added
     */
    public boolean hasNewSuspectOnTrap(
    ) {
        return this.hasNewSuspectOnTrap;
    }

    /**
     * Method hasSnmpTrapPort.
     * 
     * @return true if at least one SnmpTrapPort has been added
     */
    public boolean hasSnmpTrapPort(
    ) {
        return this.hasSnmpTrapPort;
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
        return this.newSuspectOnTrap;
    }

    public boolean shouldUseAddressFromVarbind() {
        return useAddressFromVarbind != null ? useAddressFromVarbind : DEFAULT_USE_ADDRESS_FROM_VARBIND;
    }

    public void setUseAddressFromVarbind(Boolean useAddressFromVarbind) {
        this.useAddressFromVarbind = useAddressFromVarbind;
    }

    /**
     * Method iterateSnmpv3User.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<Snmpv3User> iterateSnmpv3User(
    ) {
        return this.snmpv3User.iterator();
    }

    /**
     */
    public void removeAllSnmpv3User(
    ) {
        this.snmpv3User.clear();
    }

    /**
     * Method removeSnmpv3User.
     * 
     * @param vSnmpv3User
     * @return true if the object was removed from the collection.
     */
    public boolean removeSnmpv3User(
            final Snmpv3User vSnmpv3User) {
        boolean removed = snmpv3User.remove(vSnmpv3User);
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
        java.lang.Object obj = this.snmpv3User.remove(index);
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
        this.newSuspectOnTrap = newSuspectOnTrap;
        this.hasNewSuspectOnTrap = true;
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
        this.snmpTrapAddress = snmpTrapAddress;
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
        this.snmpTrapPort = snmpTrapPort;
        this.hasSnmpTrapPort = true;
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
        if (index < 0 || index >= this.snmpv3User.size()) {
            throw new IndexOutOfBoundsException("setSnmpv3User: Index value '" + index + "' not in range [0.." + (this.snmpv3User.size() - 1) + "]");
        }
        
        this.snmpv3User.set(index, vSnmpv3User);
    }

    /**
     * 
     * 
     * @param vSnmpv3UserArray
     */
    public void setSnmpv3User(
            final Snmpv3User[] vSnmpv3UserArray) {
        //-- copy array
        snmpv3User.clear();
        
        for (int i = 0; i < vSnmpv3UserArray.length; i++) {
                this.snmpv3User.add(vSnmpv3UserArray[i]);
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
        this.snmpv3User.clear();
        
        this.snmpv3User.addAll(vSnmpv3UserList);
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
        this.snmpv3User = snmpv3UserList;
    }

    public boolean isIncludeRawMessage() {
        return includeRawMessage;
    }

    public void setIncludeRawMessage(boolean _includeRawMessage) {
        this.includeRawMessage = _includeRawMessage;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int _threads) {
        this.threads = _threads;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int _queueSize) {
        this.queueSize = _queueSize;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int _batchSize) {
        this.batchSize = _batchSize;
    }

    public int getBatchInterval() {
        return batchInterval;
    }

    public void setBatchInterval(int _batchInterval) {
        this.batchInterval = _batchInterval;
    }
}
