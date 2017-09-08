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

package org.opennms.report.inventory;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

/**
 * Top-level element for the nodeinventoryinventory.xml configuration file.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "rws-nbinventoryreport")
@XmlAccessorType(XmlAccessType.FIELD)
public class RwsNbinventoryreport implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "totalGroups")
    private Integer totalGroups;

    @XmlElement(name = "groupsMatching")
    private Integer groupsMatching;

    @XmlElement(name = "groupWithoutNodes")
    private Integer groupWithoutNodes;

    @XmlElement(name = "groupsWithNodesWithoutinventoryAtAll")
    private Integer groupsWithNodesWithoutinventoryAtAll;

    @XmlElement(name = "groupsWithNodesWithoutinventoryAtReportDate")
    private Integer groupsWithNodesWithoutinventoryAtReportDate;

    @XmlElement(name = "user")
    private String user;

    @XmlElement(name = "theField")
    private String theField;

    @XmlElement(name = "reportDate")
    private String reportDate;

    @XmlElement(name = "reportRequestDate")
    private String reportRequestDate;

    @XmlElement(name = "groupSet")
    private java.util.List<GroupSet> groupSetList;

    public RwsNbinventoryreport() {
        this.groupSetList = new java.util.ArrayList<>();
    }

    /**
     * 
     * 
     * @param vGroupSet
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addGroupSet(final GroupSet vGroupSet) throws IndexOutOfBoundsException {
        this.groupSetList.add(vGroupSet);
    }

    /**
     * 
     * 
     * @param index
     * @param vGroupSet
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addGroupSet(final int index, final GroupSet vGroupSet) throws IndexOutOfBoundsException {
        this.groupSetList.add(index, vGroupSet);
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
    public void deleteGroupsWithNodesWithoutinventoryAtAll() {
        this.groupsWithNodesWithoutinventoryAtAll= null;
    }

    /**
     */
    public void deleteGroupsWithNodesWithoutinventoryAtReportDate() {
        this.groupsWithNodesWithoutinventoryAtReportDate= null;
    }

    /**
     */
    public void deleteTotalGroups() {
        this.totalGroups= null;
    }

    /**
     * Method enumerateGroupSet.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<GroupSet> enumerateGroupSet() {
        return java.util.Collections.enumeration(this.groupSetList);
    }

    /**
     * Method getGroupSet.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the GroupSet at the given
     * index
     */
    public GroupSet getGroupSet(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.groupSetList.size()) {
            throw new IndexOutOfBoundsException("getGroupSet: Index value '" + index + "' not in range [0.." + (this.groupSetList.size() - 1) + "]");
        }
        
        return (GroupSet) groupSetList.get(index);
    }

    /**
     * Method getGroupSet.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public GroupSet[] getGroupSet() {
        GroupSet[] array = new GroupSet[0];
        return (GroupSet[]) this.groupSetList.toArray(array);
    }

    /**
     * Method getGroupSetCollection.Returns a reference to 'groupSetList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<GroupSet> getGroupSetCollection() {
        return this.groupSetList;
    }

    /**
     * Method getGroupSetCount.
     * 
     * @return the size of this collection
     */
    public int getGroupSetCount() {
        return this.groupSetList.size();
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
     * Returns the value of field 'groupsMatching'.
     * 
     * @return the value of field 'GroupsMatching'.
     */
    public Integer getGroupsMatching() {
        return this.groupsMatching;
    }

    /**
     * Returns the value of field 'groupsWithNodesWithoutinventoryAtAll'.
     * 
     * @return the value of field 'GroupsWithNodesWithoutinventoryAtAll'.
     */
    public Integer getGroupsWithNodesWithoutinventoryAtAll() {
        return this.groupsWithNodesWithoutinventoryAtAll;
    }

    /**
     * Returns the value of field 'groupsWithNodesWithoutinventoryAtReportDate'.
     * 
     * @return the value of field 'GroupsWithNodesWithoutinventoryAtReportDate'.
     */
    public Integer getGroupsWithNodesWithoutinventoryAtReportDate() {
        return this.groupsWithNodesWithoutinventoryAtReportDate;
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
     * Returns the value of field 'theField'.
     * 
     * @return the value of field 'TheField'.
     */
    public String getTheField() {
        return this.theField;
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
     * Method hasGroupsWithNodesWithoutinventoryAtAll.
     * 
     * @return true if at least one GroupsWithNodesWithoutinventoryAtAll has been
     * added
     */
    public boolean hasGroupsWithNodesWithoutinventoryAtAll() {
        return this.groupsWithNodesWithoutinventoryAtAll != null;
    }

    /**
     * Method hasGroupsWithNodesWithoutinventoryAtReportDate.
     * 
     * @return true if at least one GroupsWithNodesWithoutinventoryAtReportDate
     * has been added
     */
    public boolean hasGroupsWithNodesWithoutinventoryAtReportDate() {
        return this.groupsWithNodesWithoutinventoryAtReportDate != null;
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
     * Method iterateGroupSet.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<GroupSet> iterateGroupSet() {
        return this.groupSetList.iterator();
    }

    /**
     */
    public void removeAllGroupSet() {
        this.groupSetList.clear();
    }

    /**
     * Method removeGroupSet.
     * 
     * @param vGroupSet
     * @return true if the object was removed from the collection.
     */
    public boolean removeGroupSet(final GroupSet vGroupSet) {
        boolean removed = groupSetList.remove(vGroupSet);
        return removed;
    }

    /**
     * Method removeGroupSetAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public GroupSet removeGroupSetAt(final int index) {
        Object obj = this.groupSetList.remove(index);
        return (GroupSet) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vGroupSet
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setGroupSet(final int index, final GroupSet vGroupSet) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.groupSetList.size()) {
            throw new IndexOutOfBoundsException("setGroupSet: Index value '" + index + "' not in range [0.." + (this.groupSetList.size() - 1) + "]");
        }
        
        this.groupSetList.set(index, vGroupSet);
    }

    /**
     * 
     * 
     * @param vGroupSetArray
     */
    public void setGroupSet(final GroupSet[] vGroupSetArray) {
        //-- copy array
        groupSetList.clear();
        
        for (int i = 0; i < vGroupSetArray.length; i++) {
                this.groupSetList.add(vGroupSetArray[i]);
        }
    }

    /**
     * Sets the value of 'groupSetList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vGroupSetList the Vector to copy.
     */
    public void setGroupSet(final java.util.List<GroupSet> vGroupSetList) {
        // copy vector
        this.groupSetList.clear();
        
        this.groupSetList.addAll(vGroupSetList);
    }

    /**
     * Sets the value of 'groupSetList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param groupSetList the Vector to set.
     */
    public void setGroupSetCollection(final java.util.List<GroupSet> groupSetList) {
        this.groupSetList = groupSetList;
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
     * Sets the value of field 'groupsMatching'.
     * 
     * @param groupsMatching the value of field 'groupsMatching'.
     */
    public void setGroupsMatching(final Integer groupsMatching) {
        this.groupsMatching = groupsMatching;
    }

    /**
     * Sets the value of field 'groupsWithNodesWithoutinventoryAtAll'.
     * 
     * @param groupsWithNodesWithoutinventoryAtAll the value of field
     * 'groupsWithNodesWithoutinventoryAtAll'.
     */
    public void setGroupsWithNodesWithoutinventoryAtAll(final Integer groupsWithNodesWithoutinventoryAtAll) {
        this.groupsWithNodesWithoutinventoryAtAll = groupsWithNodesWithoutinventoryAtAll;
    }

    /**
     * Sets the value of field 'groupsWithNodesWithoutinventoryAtReportDate'.
     * 
     * @param groupsWithNodesWithoutinventoryAtReportDate the value of field
     * 'groupsWithNodesWithoutinventoryAtReportDate'.
     */
    public void setGroupsWithNodesWithoutinventoryAtReportDate(final Integer groupsWithNodesWithoutinventoryAtReportDate) {
        this.groupsWithNodesWithoutinventoryAtReportDate = groupsWithNodesWithoutinventoryAtReportDate;
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
     * Sets the value of field 'theField'.
     * 
     * @param theField the value of field 'theField'.
     */
    public void setTheField(final String theField) {
        this.theField = theField;
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
        if (!(other instanceof RwsNbinventoryreport)) {
            return false;
        }
        RwsNbinventoryreport castOther = (RwsNbinventoryreport) other;
        return Objects.equals(totalGroups, castOther.totalGroups)
                && Objects.equals(groupsMatching, castOther.groupsMatching)
                && Objects.equals(groupWithoutNodes, castOther.groupWithoutNodes)
                && Objects.equals(groupsWithNodesWithoutinventoryAtAll, castOther.groupsWithNodesWithoutinventoryAtAll)
                && Objects.equals(groupsWithNodesWithoutinventoryAtReportDate,
                        castOther.groupsWithNodesWithoutinventoryAtReportDate)
                && Objects.equals(user, castOther.user) && Objects.equals(theField, castOther.theField)
                && Objects.equals(reportDate, castOther.reportDate)
                && Objects.equals(reportRequestDate, castOther.reportRequestDate)
                && Objects.equals(groupSetList, castOther.groupSetList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalGroups, groupsMatching, groupWithoutNodes, groupsWithNodesWithoutinventoryAtAll,
                groupsWithNodesWithoutinventoryAtReportDate, user, theField, reportDate, reportRequestDate,
                groupSetList);
    }

}
