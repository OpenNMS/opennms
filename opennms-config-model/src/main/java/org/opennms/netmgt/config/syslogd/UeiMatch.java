/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.syslogd;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * List of Strings to UEI matches
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "ueiMatch")
@XmlAccessorType(XmlAccessType.FIELD)
public class UeiMatch implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The name of a syslog facility. If present, the facility of
     *  an incoming message must match one of the facilities named
     *  by an instance of this tag within the ueiMatch.
     *  
     */
    @XmlElement(name = "facility")
    private java.util.List<String> facilityList;

    /**
     * The name of a syslog severity. If present, the severity of
     *  an incoming message must match one of the severities named
     *  by an instance of this tag within the ueiMatch.
     *  
     */
    @XmlElement(name = "severity")
    private java.util.List<String> severityList;

    /**
     * String against which to match the process name; interpreted
     *  as a regular expression. If no process name is present in
     *  the incoming message, any process-match elements will be
     *  considered non-matches.
     *  
     */
    @XmlElement(name = "process-match")
    private org.opennms.netmgt.config.syslogd.ProcessMatch processMatch;

    /**
     * String against which to match the hostname; interpreted
     *  as a regular expression.
     *  
     */
    @XmlElement(name = "hostname-match")
    private org.opennms.netmgt.config.syslogd.HostnameMatch hostnameMatch;

    /**
     * String against which to match the host IP address; interpreted
     *  as a regular expression.
     *  
     */
    @XmlElement(name = "hostaddr-match")
    private org.opennms.netmgt.config.syslogd.HostaddrMatch hostaddrMatch;

    /**
     * String against which to match the message body; interpreted
     *  as a substring or a regular expression according to the
     *  value of the "type" attribute
     *  
     */
    @XmlElement(name = "match", required = true)
    private org.opennms.netmgt.config.syslogd.Match match;

    /**
     * UEI
     */
    @XmlElement(name = "uei", required = true)
    private String uei;

    /**
     * For regex matches, assign the value of a matching group
     *  to a named event parameter
     *  
     */
    @XmlElement(name = "parameter-assignment")
    private java.util.List<org.opennms.netmgt.config.syslogd.ParameterAssignment> parameterAssignmentList;

    public UeiMatch() {
        this.facilityList = new java.util.ArrayList<String>();
        this.severityList = new java.util.ArrayList<String>();
        this.parameterAssignmentList = new java.util.ArrayList<org.opennms.netmgt.config.syslogd.ParameterAssignment>();
    }

    /**
     * 
     * 
     * @param vFacility
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addFacility(final String vFacility) throws IndexOutOfBoundsException {
        this.facilityList.add(vFacility);
    }

    /**
     * 
     * 
     * @param index
     * @param vFacility
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addFacility(final int index, final String vFacility) throws IndexOutOfBoundsException {
        this.facilityList.add(index, vFacility);
    }

    /**
     * 
     * 
     * @param vParameterAssignment
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addParameterAssignment(final org.opennms.netmgt.config.syslogd.ParameterAssignment vParameterAssignment) throws IndexOutOfBoundsException {
        this.parameterAssignmentList.add(vParameterAssignment);
    }

    /**
     * 
     * 
     * @param index
     * @param vParameterAssignment
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addParameterAssignment(final int index, final org.opennms.netmgt.config.syslogd.ParameterAssignment vParameterAssignment) throws IndexOutOfBoundsException {
        this.parameterAssignmentList.add(index, vParameterAssignment);
    }

    /**
     * 
     * 
     * @param vSeverity
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addSeverity(final String vSeverity) throws IndexOutOfBoundsException {
        this.severityList.add(vSeverity);
    }

    /**
     * 
     * 
     * @param index
     * @param vSeverity
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addSeverity(final int index, final String vSeverity) throws IndexOutOfBoundsException {
        this.severityList.add(index, vSeverity);
    }

    /**
     * Method enumerateFacility.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<String> enumerateFacility() {
        return java.util.Collections.enumeration(this.facilityList);
    }

    /**
     * Method enumerateParameterAssignment.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.syslogd.ParameterAssignment> enumerateParameterAssignment() {
        return java.util.Collections.enumeration(this.parameterAssignmentList);
    }

    /**
     * Method enumerateSeverity.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<String> enumerateSeverity() {
        return java.util.Collections.enumeration(this.severityList);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof UeiMatch) {
            UeiMatch temp = (UeiMatch)obj;
            boolean equals = Objects.equals(temp.facilityList, facilityList)
                && Objects.equals(temp.severityList, severityList)
                && Objects.equals(temp.processMatch, processMatch)
                && Objects.equals(temp.hostnameMatch, hostnameMatch)
                && Objects.equals(temp.hostaddrMatch, hostaddrMatch)
                && Objects.equals(temp.match, match)
                && Objects.equals(temp.uei, uei)
                && Objects.equals(temp.parameterAssignmentList, parameterAssignmentList);
            return equals;
        }
        return false;
    }

    /**
     * Method getFacility.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getFacility(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.facilityList.size()) {
            throw new IndexOutOfBoundsException("getFacility: Index value '" + index + "' not in range [0.." + (this.facilityList.size() - 1) + "]");
        }
        
        return (String) facilityList.get(index);
    }

    /**
     * Method getFacility.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public String[] getFacility() {
        String[] array = new String[0];
        return (String[]) this.facilityList.toArray(array);
    }

    /**
     * Method getFacilityCollection.Returns a reference to 'facilityList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<String> getFacilityCollection() {
        return this.facilityList;
    }

    /**
     * Method getFacilityCount.
     * 
     * @return the size of this collection
     */
    public int getFacilityCount() {
        return this.facilityList.size();
    }

    /**
     * Returns the value of field 'hostaddrMatch'. The field 'hostaddrMatch' has
     * the following description: String against which to match the host IP
     * address; interpreted
     *  as a regular expression.
     *  
     * 
     * @return the value of field 'HostaddrMatch'.
     */
    public org.opennms.netmgt.config.syslogd.HostaddrMatch getHostaddrMatch() {
        return this.hostaddrMatch;
    }

    /**
     * Returns the value of field 'hostnameMatch'. The field 'hostnameMatch' has
     * the following description: String against which to match the hostname;
     * interpreted
     *  as a regular expression.
     *  
     * 
     * @return the value of field 'HostnameMatch'.
     */
    public org.opennms.netmgt.config.syslogd.HostnameMatch getHostnameMatch() {
        return this.hostnameMatch;
    }

    /**
     * Returns the value of field 'match'. The field 'match' has the following
     * description: String against which to match the message body; interpreted
     *  as a substring or a regular expression according to the
     *  value of the "type" attribute
     *  
     * 
     * @return the value of field 'Match'.
     */
    public org.opennms.netmgt.config.syslogd.Match getMatch() {
        return this.match;
    }

    /**
     * Method getParameterAssignment.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.config.syslogd.types.ParameterAssignment at the given
     * index
     */
    public org.opennms.netmgt.config.syslogd.ParameterAssignment getParameterAssignment(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.parameterAssignmentList.size()) {
            throw new IndexOutOfBoundsException("getParameterAssignment: Index value '" + index + "' not in range [0.." + (this.parameterAssignmentList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.config.syslogd.ParameterAssignment) parameterAssignmentList.get(index);
    }

    /**
     * Method getParameterAssignment.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are changing in
     * another thread, we pass a 0-length Array of the correct type into the API
     * call.  This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.syslogd.ParameterAssignment[] getParameterAssignment() {
        org.opennms.netmgt.config.syslogd.ParameterAssignment[] array = new org.opennms.netmgt.config.syslogd.ParameterAssignment[0];
        return (org.opennms.netmgt.config.syslogd.ParameterAssignment[]) this.parameterAssignmentList.toArray(array);
    }

    /**
     * Method getParameterAssignmentCollection.Returns a reference to
     * 'parameterAssignmentList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.syslogd.ParameterAssignment> getParameterAssignmentCollection() {
        return this.parameterAssignmentList;
    }

    /**
     * Method getParameterAssignmentCount.
     * 
     * @return the size of this collection
     */
    public int getParameterAssignmentCount() {
        return this.parameterAssignmentList.size();
    }

    /**
     * Returns the value of field 'processMatch'. The field 'processMatch' has the
     * following description: String against which to match the process name;
     * interpreted
     *  as a regular expression. If no process name is present in
     *  the incoming message, any process-match elements will be
     *  considered non-matches.
     *  
     * 
     * @return the value of field 'ProcessMatch'.
     */
    public org.opennms.netmgt.config.syslogd.ProcessMatch getProcessMatch() {
        return this.processMatch;
    }

    /**
     * Method getSeverity.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getSeverity(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.severityList.size()) {
            throw new IndexOutOfBoundsException("getSeverity: Index value '" + index + "' not in range [0.." + (this.severityList.size() - 1) + "]");
        }
        
        return (String) severityList.get(index);
    }

    /**
     * Method getSeverity.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public String[] getSeverity() {
        String[] array = new String[0];
        return (String[]) this.severityList.toArray(array);
    }

    /**
     * Method getSeverityCollection.Returns a reference to 'severityList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<String> getSeverityCollection() {
        return this.severityList;
    }

    /**
     * Method getSeverityCount.
     * 
     * @return the size of this collection
     */
    public int getSeverityCount() {
        return this.severityList.size();
    }

    /**
     * Returns the value of field 'uei'. The field 'uei' has the following
     * description: UEI
     * 
     * @return the value of field 'Uei'.
     */
    public String getUei() {
        return this.uei;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            facilityList, 
            severityList, 
            processMatch, 
            hostnameMatch, 
            hostaddrMatch, 
            match, 
            uei, 
            parameterAssignmentList);
        return hash;
    }

    /**
     * Method iterateFacility.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<String> iterateFacility() {
        return this.facilityList.iterator();
    }

    /**
     * Method iterateParameterAssignment.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.syslogd.ParameterAssignment> iterateParameterAssignment() {
        return this.parameterAssignmentList.iterator();
    }

    /**
     * Method iterateSeverity.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<String> iterateSeverity() {
        return this.severityList.iterator();
    }

    /**
     */
    public void removeAllFacility() {
        this.facilityList.clear();
    }

    /**
     */
    public void removeAllParameterAssignment() {
        this.parameterAssignmentList.clear();
    }

    /**
     */
    public void removeAllSeverity() {
        this.severityList.clear();
    }

    /**
     * Method removeFacility.
     * 
     * @param vFacility
     * @return true if the object was removed from the collection.
     */
    public boolean removeFacility(final String vFacility) {
        boolean removed = facilityList.remove(vFacility);
        return removed;
    }

    /**
     * Method removeFacilityAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeFacilityAt(final int index) {
        Object obj = this.facilityList.remove(index);
        return (String) obj;
    }

    /**
     * Method removeParameterAssignment.
     * 
     * @param vParameterAssignment
     * @return true if the object was removed from the collection.
     */
    public boolean removeParameterAssignment(final org.opennms.netmgt.config.syslogd.ParameterAssignment vParameterAssignment) {
        boolean removed = parameterAssignmentList.remove(vParameterAssignment);
        return removed;
    }

    /**
     * Method removeParameterAssignmentAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.syslogd.ParameterAssignment removeParameterAssignmentAt(final int index) {
        Object obj = this.parameterAssignmentList.remove(index);
        return (org.opennms.netmgt.config.syslogd.ParameterAssignment) obj;
    }

    /**
     * Method removeSeverity.
     * 
     * @param vSeverity
     * @return true if the object was removed from the collection.
     */
    public boolean removeSeverity(final String vSeverity) {
        boolean removed = severityList.remove(vSeverity);
        return removed;
    }

    /**
     * Method removeSeverityAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeSeverityAt(final int index) {
        Object obj = this.severityList.remove(index);
        return (String) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vFacility
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setFacility(final int index, final String vFacility) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.facilityList.size()) {
            throw new IndexOutOfBoundsException("setFacility: Index value '" + index + "' not in range [0.." + (this.facilityList.size() - 1) + "]");
        }
        
        this.facilityList.set(index, vFacility);
    }

    /**
     * 
     * 
     * @param vFacilityArray
     */
    public void setFacility(final String[] vFacilityArray) {
        //-- copy array
        facilityList.clear();
        
        for (int i = 0; i < vFacilityArray.length; i++) {
                this.facilityList.add(vFacilityArray[i]);
        }
    }

    /**
     * Sets the value of 'facilityList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vFacilityList the Vector to copy.
     */
    public void setFacility(final java.util.List<String> vFacilityList) {
        // copy vector
        this.facilityList.clear();
        
        this.facilityList.addAll(vFacilityList);
    }

    /**
     * Sets the value of 'facilityList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param facilityList the Vector to set.
     */
    public void setFacilityCollection(final java.util.List<String> facilityList) {
        this.facilityList = facilityList;
    }

    /**
     * Sets the value of field 'hostaddrMatch'. The field 'hostaddrMatch' has the
     * following description: String against which to match the host IP address;
     * interpreted
     *  as a regular expression.
     *  
     * 
     * @param hostaddrMatch the value of field 'hostaddrMatch'.
     */
    public void setHostaddrMatch(final org.opennms.netmgt.config.syslogd.HostaddrMatch hostaddrMatch) {
        this.hostaddrMatch = hostaddrMatch;
    }

    /**
     * Sets the value of field 'hostnameMatch'. The field 'hostnameMatch' has the
     * following description: String against which to match the hostname;
     * interpreted
     *  as a regular expression.
     *  
     * 
     * @param hostnameMatch the value of field 'hostnameMatch'.
     */
    public void setHostnameMatch(final org.opennms.netmgt.config.syslogd.HostnameMatch hostnameMatch) {
        this.hostnameMatch = hostnameMatch;
    }

    /**
     * Sets the value of field 'match'. The field 'match' has the following
     * description: String against which to match the message body; interpreted
     *  as a substring or a regular expression according to the
     *  value of the "type" attribute
     *  
     * 
     * @param match the value of field 'match'.
     */
    public void setMatch(final org.opennms.netmgt.config.syslogd.Match match) {
        this.match = match;
    }

    /**
     * 
     * 
     * @param index
     * @param vParameterAssignment
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setParameterAssignment(final int index, final org.opennms.netmgt.config.syslogd.ParameterAssignment vParameterAssignment) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.parameterAssignmentList.size()) {
            throw new IndexOutOfBoundsException("setParameterAssignment: Index value '" + index + "' not in range [0.." + (this.parameterAssignmentList.size() - 1) + "]");
        }
        
        this.parameterAssignmentList.set(index, vParameterAssignment);
    }

    /**
     * 
     * 
     * @param vParameterAssignmentArray
     */
    public void setParameterAssignment(final org.opennms.netmgt.config.syslogd.ParameterAssignment[] vParameterAssignmentArray) {
        //-- copy array
        parameterAssignmentList.clear();
        
        for (int i = 0; i < vParameterAssignmentArray.length; i++) {
                this.parameterAssignmentList.add(vParameterAssignmentArray[i]);
        }
    }

    /**
     * Sets the value of 'parameterAssignmentList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vParameterAssignmentList the Vector to copy.
     */
    public void setParameterAssignment(final java.util.List<org.opennms.netmgt.config.syslogd.ParameterAssignment> vParameterAssignmentList) {
        // copy vector
        this.parameterAssignmentList.clear();
        
        this.parameterAssignmentList.addAll(vParameterAssignmentList);
    }

    /**
     * Sets the value of 'parameterAssignmentList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param parameterAssignmentList the Vector to set.
     */
    public void setParameterAssignmentCollection(final java.util.List<org.opennms.netmgt.config.syslogd.ParameterAssignment> parameterAssignmentList) {
        this.parameterAssignmentList = parameterAssignmentList;
    }

    /**
     * Sets the value of field 'processMatch'. The field 'processMatch' has the
     * following description: String against which to match the process name;
     * interpreted
     *  as a regular expression. If no process name is present in
     *  the incoming message, any process-match elements will be
     *  considered non-matches.
     *  
     * 
     * @param processMatch the value of field 'processMatch'.
     */
    public void setProcessMatch(final org.opennms.netmgt.config.syslogd.ProcessMatch processMatch) {
        this.processMatch = processMatch;
    }

    /**
     * 
     * 
     * @param index
     * @param vSeverity
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setSeverity(final int index, final String vSeverity) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.severityList.size()) {
            throw new IndexOutOfBoundsException("setSeverity: Index value '" + index + "' not in range [0.." + (this.severityList.size() - 1) + "]");
        }
        
        this.severityList.set(index, vSeverity);
    }

    /**
     * 
     * 
     * @param vSeverityArray
     */
    public void setSeverity(final String[] vSeverityArray) {
        //-- copy array
        severityList.clear();
        
        for (int i = 0; i < vSeverityArray.length; i++) {
                this.severityList.add(vSeverityArray[i]);
        }
    }

    /**
     * Sets the value of 'severityList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vSeverityList the Vector to copy.
     */
    public void setSeverity(final java.util.List<String> vSeverityList) {
        // copy vector
        this.severityList.clear();
        
        this.severityList.addAll(vSeverityList);
    }

    /**
     * Sets the value of 'severityList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param severityList the Vector to set.
     */
    public void setSeverityCollection(final java.util.List<String> severityList) {
        this.severityList = severityList;
    }

    /**
     * Sets the value of field 'uei'. The field 'uei' has the following
     * description: UEI
     * 
     * @param uei the value of field 'uei'.
     */
    public void setUei(final String uei) {
        this.uei = uei;
    }

}
