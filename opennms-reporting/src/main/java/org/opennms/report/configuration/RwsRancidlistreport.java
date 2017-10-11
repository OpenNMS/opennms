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

package org.opennms.report.configuration;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the rancidlistreport.xml report file.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "rws-rancidlistreport")
@XmlAccessorType(XmlAccessType.FIELD)
public class RwsRancidlistreport implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "totalGroups")
    private Integer totalGroups;

    @XmlElement(name = "groupsMatching")
    private Integer groupsMatching;

    @XmlElement(name = "groupWithoutNodes")
    private Integer groupWithoutNodes;

    @XmlElement(name = "groupsWithNodesWithoutconfigurationAtAll")
    private Integer groupsWithNodesWithoutconfigurationAtAll;

    @XmlElement(name = "groupsWithNodesWithoutconfigurationAtReportDate")
    private Integer groupsWithNodesWithoutconfigurationAtReportDate;

    @XmlElement(name = "user")
    private String user;

    @XmlElement(name = "reportDate")
    private String reportDate;

    @XmlElement(name = "reportRequestDate")
    private String reportRequestDate;

    @XmlElement(name = "groupXSet")
    private List<GroupXSet> groupXSetList;

    public RwsRancidlistreport() {
        this.groupXSetList = new ArrayList<>();
    }

    /**
     * 
     * 
     * @param vGroupXSet
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addGroupXSet(final GroupXSet vGroupXSet) throws IndexOutOfBoundsException {
        this.groupXSetList.add(vGroupXSet);
    }

    /**
     * 
     * 
     * @param index
     * @param vGroupXSet
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addGroupXSet(final int index, final GroupXSet vGroupXSet) throws IndexOutOfBoundsException {
        this.groupXSetList.add(index, vGroupXSet);
    }

    /**
     */
    public void deleteGroupWithoutNodes() {
        this.groupWithoutNodes= null;
    }

    /**
     */
    public void deleteGroupsMatching() {
        this.groupsMatching= null;
    }

    /**
     */
    public void deleteGroupsWithNodesWithoutconfigurationAtAll() {
        this.groupsWithNodesWithoutconfigurationAtAll= null;
    }

    /**
     */
    public void deleteGroupsWithNodesWithoutconfigurationAtReportDate() {
        this.groupsWithNodesWithoutconfigurationAtReportDate= null;
    }

    /**
     */
    public void deleteTotalGroups() {
        this.totalGroups= null;
    }

    /**
     * Method enumerateGroupXSet.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<GroupXSet> enumerateGroupXSet() {
        return Collections.enumeration(this.groupXSetList);
    }

    /**
     * Returns the value of field 'groupWithoutNodes'.
     * 
     * @return the value of field 'GroupWithoutNodes'.
     */
    public Integer getGroupWithoutNodes() {
        return this.groupWithoutNodes;
    }

    /**
     * Method getGroupXSet.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the GroupXSet at the
     * given index
     */
    public GroupXSet getGroupXSet(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.groupXSetList.size()) {
            throw new IndexOutOfBoundsException("getGroupXSet: Index value '" + index + "' not in range [0.." + (this.groupXSetList.size() - 1) + "]");
        }
        
        return (GroupXSet) groupXSetList.get(index);
    }

    /**
     * Method getGroupXSet.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public GroupXSet[] getGroupXSet() {
        GroupXSet[] array = new GroupXSet[0];
        return (GroupXSet[]) this.groupXSetList.toArray(array);
    }

    /**
     * Method getGroupXSetCollection.Returns a reference to 'groupXSetList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<GroupXSet> getGroupXSetCollection() {
        return this.groupXSetList;
    }

    /**
     * Method getGroupXSetCount.
     * 
     * @return the size of this collection
     */
    public int getGroupXSetCount() {
        return this.groupXSetList.size();
    }

    /**
     * Returns the value of field 'groupsMatching'.
     * 
     * @return the value of field 'GroupsMatching'.
     */
    public Integer getGroupsMatching() {
        return this.groupsMatching;
    }

    /**
     * Returns the value of field 'groupsWithNodesWithoutconfigurationAtAll'.
     * 
     * @return the value of field 'GroupsWithNodesWithoutconfigurationAtAll'.
     */
    public Integer getGroupsWithNodesWithoutconfigurationAtAll() {
        return this.groupsWithNodesWithoutconfigurationAtAll;
    }

    /**
     * Returns the value of field
     * 'groupsWithNodesWithoutconfigurationAtReportDate'.
     * 
     * @return the value of field 'GroupsWithNodesWithoutconfigurationAtReportDate'
     */
    public Integer getGroupsWithNodesWithoutconfigurationAtReportDate() {
        return this.groupsWithNodesWithoutconfigurationAtReportDate;
    }

    /**
     * Returns the value of field 'reportDate'.
     * 
     * @return the value of field 'ReportDate'.
     */
    public String getReportDate() {
        return this.reportDate;
    }

    /**
     * Returns the value of field 'reportRequestDate'.
     * 
     * @return the value of field 'ReportRequestDate'.
     */
    public String getReportRequestDate() {
        return this.reportRequestDate;
    }

    /**
     * Returns the value of field 'totalGroups'.
     * 
     * @return the value of field 'TotalGroups'.
     */
    public Integer getTotalGroups() {
        return this.totalGroups;
    }

    /**
     * Returns the value of field 'user'.
     * 
     * @return the value of field 'User'.
     */
    public String getUser() {
        return this.user;
    }

    /**
     * Method hasGroupWithoutNodes.
     * 
     * @return true if at least one GroupWithoutNodes has been added
     */
    public boolean hasGroupWithoutNodes() {
        return this.groupWithoutNodes != null;
    }

    /**
     * Method hasGroupsMatching.
     * 
     * @return true if at least one GroupsMatching has been added
     */
    public boolean hasGroupsMatching() {
        return this.groupsMatching != null;
    }

    /**
     * Method hasGroupsWithNodesWithoutconfigurationAtAll.
     * 
     * @return true if at least one GroupsWithNodesWithoutconfigurationAtAll has
     * been added
     */
    public boolean hasGroupsWithNodesWithoutconfigurationAtAll() {
        return this.groupsWithNodesWithoutconfigurationAtAll != null;
    }

    /**
     * Method hasGroupsWithNodesWithoutconfigurationAtReportDate.
     * 
     * @return true if at least one
     * GroupsWithNodesWithoutconfigurationAtReportDate has been added
     */
    public boolean hasGroupsWithNodesWithoutconfigurationAtReportDate() {
        return this.groupsWithNodesWithoutconfigurationAtReportDate != null;
    }

    /**
     * Method hasTotalGroups.
     * 
     * @return true if at least one TotalGroups has been added
     */
    public boolean hasTotalGroups() {
        return this.totalGroups != null;
    }

    /**
     * Method iterateGroupXSet.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<GroupXSet> iterateGroupXSet() {
        return this.groupXSetList.iterator();
    }

    /**
     */
    public void removeAllGroupXSet() {
        this.groupXSetList.clear();
    }

    /**
     * Method removeGroupXSet.
     * 
     * @param vGroupXSet
     * @return true if the object was removed from the collection.
     */
    public boolean removeGroupXSet(final GroupXSet vGroupXSet) {
        boolean removed = groupXSetList.remove(vGroupXSet);
        return removed;
    }

    /**
     * Method removeGroupXSetAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public GroupXSet removeGroupXSetAt(final int index) {
        Object obj = this.groupXSetList.remove(index);
        return (GroupXSet) obj;
    }

    /**
     * Sets the value of field 'groupWithoutNodes'.
     * 
     * @param groupWithoutNodes the value of field 'groupWithoutNodes'.
     */
    public void setGroupWithoutNodes(final Integer groupWithoutNodes) {
        this.groupWithoutNodes = groupWithoutNodes;
    }

    /**
     * 
     * 
     * @param index
     * @param vGroupXSet
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setGroupXSet(final int index, final GroupXSet vGroupXSet) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.groupXSetList.size()) {
            throw new IndexOutOfBoundsException("setGroupXSet: Index value '" + index + "' not in range [0.." + (this.groupXSetList.size() - 1) + "]");
        }
        
        this.groupXSetList.set(index, vGroupXSet);
    }

    /**
     * 
     * 
     * @param vGroupXSetArray
     */
    public void setGroupXSet(final GroupXSet[] vGroupXSetArray) {
        //-- copy array
        groupXSetList.clear();
        
        for (int i = 0; i < vGroupXSetArray.length; i++) {
                this.groupXSetList.add(vGroupXSetArray[i]);
        }
    }

    /**
     * Sets the value of 'groupXSetList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vGroupXSetList the Vector to copy.
     */
    public void setGroupXSet(final List<GroupXSet> vGroupXSetList) {
        // copy vector
        this.groupXSetList.clear();
        
        this.groupXSetList.addAll(vGroupXSetList);
    }

    /**
     * Sets the value of 'groupXSetList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param groupXSetList the Vector to set.
     */
    public void setGroupXSetCollection(final List<GroupXSet> groupXSetList) {
        this.groupXSetList = groupXSetList;
    }

    /**
     * Sets the value of field 'groupsMatching'.
     * 
     * @param groupsMatching the value of field 'groupsMatching'.
     */
    public void setGroupsMatching(final Integer groupsMatching) {
        this.groupsMatching = groupsMatching;
    }

    /**
     * Sets the value of field 'groupsWithNodesWithoutconfigurationAtAll'.
     * 
     * @param groupsWithNodesWithoutconfigurationAtAll the value of field
     * 'groupsWithNodesWithoutconfigurationAtAll'.
     */
    public void setGroupsWithNodesWithoutconfigurationAtAll(final Integer groupsWithNodesWithoutconfigurationAtAll) {
        this.groupsWithNodesWithoutconfigurationAtAll = groupsWithNodesWithoutconfigurationAtAll;
    }

    /**
     * Sets the value of field 'groupsWithNodesWithoutconfigurationAtReportDate'.
     * 
     * @param groupsWithNodesWithoutconfigurationAtReportDate the value of field
     * 'groupsWithNodesWithoutconfigurationAtReportDate'.
     */
    public void setGroupsWithNodesWithoutconfigurationAtReportDate(final Integer groupsWithNodesWithoutconfigurationAtReportDate) {
        this.groupsWithNodesWithoutconfigurationAtReportDate = groupsWithNodesWithoutconfigurationAtReportDate;
    }

    /**
     * Sets the value of field 'reportDate'.
     * 
     * @param reportDate the value of field 'reportDate'.
     */
    public void setReportDate(final String reportDate) {
        this.reportDate = reportDate;
    }

    /**
     * Sets the value of field 'reportRequestDate'.
     * 
     * @param reportRequestDate the value of field 'reportRequestDate'.
     */
    public void setReportRequestDate(final String reportRequestDate) {
        this.reportRequestDate = reportRequestDate;
    }

    /**
     * Sets the value of field 'totalGroups'.
     * 
     * @param totalGroups the value of field 'totalGroups'.
     */
    public void setTotalGroups(final Integer totalGroups) {
        this.totalGroups = totalGroups;
    }

    /**
     * Sets the value of field 'user'.
     * 
     * @param user the value of field 'user'.
     */
    public void setUser(final String user) {
        this.user = user;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof RwsRancidlistreport)) {
            return false;
        }
        RwsRancidlistreport castOther = (RwsRancidlistreport) other;
        return Objects.equals(totalGroups, castOther.totalGroups)
                && Objects.equals(groupsMatching, castOther.groupsMatching)
                && Objects.equals(groupWithoutNodes, castOther.groupWithoutNodes)
                && Objects.equals(groupsWithNodesWithoutconfigurationAtAll,
                        castOther.groupsWithNodesWithoutconfigurationAtAll)
                && Objects.equals(groupsWithNodesWithoutconfigurationAtReportDate,
                        castOther.groupsWithNodesWithoutconfigurationAtReportDate)
                && Objects.equals(user, castOther.user) && Objects.equals(reportDate, castOther.reportDate)
                && Objects.equals(reportRequestDate, castOther.reportRequestDate)
                && Objects.equals(groupXSetList, castOther.groupXSetList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalGroups, groupsMatching, groupWithoutNodes, groupsWithNodesWithoutconfigurationAtAll,
                groupsWithNodesWithoutconfigurationAtReportDate, user, reportDate, reportRequestDate, groupXSetList);
    }

}
