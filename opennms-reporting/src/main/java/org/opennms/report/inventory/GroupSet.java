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
 * Class GroupSet.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "groupSet")
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupSet implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "groupSetName", required = true)
    private String groupSetName;

    @XmlElement(name = "totalNodes")
    private Integer totalNodes;

    @XmlElement(name = "nodesMatching")
    private Integer nodesMatching;

    @XmlElement(name = "nodesWithoutinventoryAtAll")
    private Integer nodesWithoutinventoryAtAll;

    @XmlElement(name = "nodesWithoutinventoryAtReportDate")
    private Integer nodesWithoutinventoryAtReportDate;

    @XmlElement(name = "nbisinglenode")
    private java.util.List<Nbisinglenode> nbisinglenodeList;

    public GroupSet() {
        this.nbisinglenodeList = new java.util.ArrayList<>();
    }

    /**
     * 
     * 
     * @param vNbisinglenode
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addNbisinglenode(final Nbisinglenode vNbisinglenode) throws IndexOutOfBoundsException {
        this.nbisinglenodeList.add(vNbisinglenode);
    }

    /**
     * 
     * 
     * @param index
     * @param vNbisinglenode
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addNbisinglenode(final int index, final Nbisinglenode vNbisinglenode) throws IndexOutOfBoundsException {
        this.nbisinglenodeList.add(index, vNbisinglenode);
    }

    /**
     */
    public void deleteNodesMatching() {
        this.nodesMatching= null;
    }

    /**
     */
    public void deleteNodesWithoutinventoryAtAll() {
        this.nodesWithoutinventoryAtAll= null;
    }

    /**
     */
    public void deleteNodesWithoutinventoryAtReportDate() {
        this.nodesWithoutinventoryAtReportDate= null;
    }

    /**
     */
    public void deleteTotalNodes() {
        this.totalNodes= null;
    }

    /**
     * Method enumerateNbisinglenode.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<Nbisinglenode> enumerateNbisinglenode() {
        return java.util.Collections.enumeration(this.nbisinglenodeList);
    }

    /**
     * Returns the value of field 'groupSetName'.
     * 
     * @return the value of field 'GroupSetName'.
     */
    public String getGroupSetName() {
        return this.groupSetName;
    }

    /**
     * Method getNbisinglenode.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Nbisinglenode at the
     * given index
     */
    public Nbisinglenode getNbisinglenode(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.nbisinglenodeList.size()) {
            throw new IndexOutOfBoundsException("getNbisinglenode: Index value '" + index + "' not in range [0.." + (this.nbisinglenodeList.size() - 1) + "]");
        }
        
        return (Nbisinglenode) nbisinglenodeList.get(index);
    }

    /**
     * Method getNbisinglenode.Returns the contents of the collection in an Array.
     *  <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Nbisinglenode[] getNbisinglenode() {
        Nbisinglenode[] array = new Nbisinglenode[0];
        return (Nbisinglenode[]) this.nbisinglenodeList.toArray(array);
    }

    /**
     * Method getNbisinglenodeCollection.Returns a reference to
     * 'nbisinglenodeList'. No type checking is performed on any modifications to
     * the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Nbisinglenode> getNbisinglenodeCollection() {
        return this.nbisinglenodeList;
    }

    /**
     * Method getNbisinglenodeCount.
     * 
     * @return the size of this collection
     */
    public int getNbisinglenodeCount() {
        return this.nbisinglenodeList.size();
    }

    /**
     * Returns the value of field 'nodesMatching'.
     * 
     * @return the value of field 'NodesMatching'.
     */
    public Integer getNodesMatching() {
        return this.nodesMatching;
    }

    /**
     * Returns the value of field 'nodesWithoutinventoryAtAll'.
     * 
     * @return the value of field 'NodesWithoutinventoryAtAll'.
     */
    public Integer getNodesWithoutinventoryAtAll() {
        return this.nodesWithoutinventoryAtAll;
    }

    /**
     * Returns the value of field 'nodesWithoutinventoryAtReportDate'.
     * 
     * @return the value of field 'NodesWithoutinventoryAtReportDate'.
     */
    public Integer getNodesWithoutinventoryAtReportDate() {
        return this.nodesWithoutinventoryAtReportDate;
    }

    /**
     * Returns the value of field 'totalNodes'.
     * 
     * @return the value of field 'TotalNodes'.
     */
    public Integer getTotalNodes() {
        return this.totalNodes;
    }

    /**
     * Method hasNodesMatching.
     * 
     * @return true if at least one NodesMatching has been added
     */
    public boolean hasNodesMatching() {
        return this.nodesMatching != null;
    }

    /**
     * Method hasNodesWithoutinventoryAtAll.
     * 
     * @return true if at least one NodesWithoutinventoryAtAll has been added
     */
    public boolean hasNodesWithoutinventoryAtAll() {
        return this.nodesWithoutinventoryAtAll != null;
    }

    /**
     * Method hasNodesWithoutinventoryAtReportDate.
     * 
     * @return true if at least one NodesWithoutinventoryAtReportDate has been adde
     */
    public boolean hasNodesWithoutinventoryAtReportDate() {
        return this.nodesWithoutinventoryAtReportDate != null;
    }

    /**
     * Method hasTotalNodes.
     * 
     * @return true if at least one TotalNodes has been added
     */
    public boolean hasTotalNodes() {
        return this.totalNodes != null;
    }

    /**
     * Method iterateNbisinglenode.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<Nbisinglenode> iterateNbisinglenode() {
        return this.nbisinglenodeList.iterator();
    }

    /**
     */
    public void removeAllNbisinglenode() {
        this.nbisinglenodeList.clear();
    }

    /**
     * Method removeNbisinglenode.
     * 
     * @param vNbisinglenode
     * @return true if the object was removed from the collection.
     */
    public boolean removeNbisinglenode(final Nbisinglenode vNbisinglenode) {
        boolean removed = nbisinglenodeList.remove(vNbisinglenode);
        return removed;
    }

    /**
     * Method removeNbisinglenodeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Nbisinglenode removeNbisinglenodeAt(final int index) {
        Object obj = this.nbisinglenodeList.remove(index);
        return (Nbisinglenode) obj;
    }

    /**
     * Sets the value of field 'groupSetName'.
     * 
     * @param groupSetName the value of field 'groupSetName'.
     */
    public void setGroupSetName(final String groupSetName) {
        this.groupSetName = groupSetName;
    }

    /**
     * 
     * 
     * @param index
     * @param vNbisinglenode
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setNbisinglenode(final int index, final Nbisinglenode vNbisinglenode) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.nbisinglenodeList.size()) {
            throw new IndexOutOfBoundsException("setNbisinglenode: Index value '" + index + "' not in range [0.." + (this.nbisinglenodeList.size() - 1) + "]");
        }
        
        this.nbisinglenodeList.set(index, vNbisinglenode);
    }

    /**
     * 
     * 
     * @param vNbisinglenodeArray
     */
    public void setNbisinglenode(final Nbisinglenode[] vNbisinglenodeArray) {
        //-- copy array
        nbisinglenodeList.clear();
        
        for (int i = 0; i < vNbisinglenodeArray.length; i++) {
                this.nbisinglenodeList.add(vNbisinglenodeArray[i]);
        }
    }

    /**
     * Sets the value of 'nbisinglenodeList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vNbisinglenodeList the Vector to copy.
     */
    public void setNbisinglenode(final java.util.List<Nbisinglenode> vNbisinglenodeList) {
        // copy vector
        this.nbisinglenodeList.clear();
        
        this.nbisinglenodeList.addAll(vNbisinglenodeList);
    }

    /**
     * Sets the value of 'nbisinglenodeList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param nbisinglenodeList the Vector to set.
     */
    public void setNbisinglenodeCollection(final java.util.List<Nbisinglenode> nbisinglenodeList) {
        this.nbisinglenodeList = nbisinglenodeList;
    }

    /**
     * Sets the value of field 'nodesMatching'.
     * 
     * @param nodesMatching the value of field 'nodesMatching'.
     */
    public void setNodesMatching(final Integer nodesMatching) {
        this.nodesMatching = nodesMatching;
    }

    /**
     * Sets the value of field 'nodesWithoutinventoryAtAll'.
     * 
     * @param nodesWithoutinventoryAtAll the value of field
     * 'nodesWithoutinventoryAtAll'.
     */
    public void setNodesWithoutinventoryAtAll(final Integer nodesWithoutinventoryAtAll) {
        this.nodesWithoutinventoryAtAll = nodesWithoutinventoryAtAll;
    }

    /**
     * Sets the value of field 'nodesWithoutinventoryAtReportDate'.
     * 
     * @param nodesWithoutinventoryAtReportDate the value of field
     * 'nodesWithoutinventoryAtReportDate'.
     */
    public void setNodesWithoutinventoryAtReportDate(final Integer nodesWithoutinventoryAtReportDate) {
        this.nodesWithoutinventoryAtReportDate = nodesWithoutinventoryAtReportDate;
    }

    /**
     * Sets the value of field 'totalNodes'.
     * 
     * @param totalNodes the value of field 'totalNodes'.
     */
    public void setTotalNodes(final Integer totalNodes) {
        this.totalNodes = totalNodes;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof GroupSet)) {
            return false;
        }
        GroupSet castOther = (GroupSet) other;
        return Objects.equals(groupSetName, castOther.groupSetName) && Objects.equals(totalNodes, castOther.totalNodes)
                && Objects.equals(nodesMatching, castOther.nodesMatching)
                && Objects.equals(nodesWithoutinventoryAtAll, castOther.nodesWithoutinventoryAtAll)
                && Objects.equals(nodesWithoutinventoryAtReportDate, castOther.nodesWithoutinventoryAtReportDate)
                && Objects.equals(nbisinglenodeList, castOther.nbisinglenodeList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupSetName, totalNodes, nodesMatching, nodesWithoutinventoryAtAll,
                nodesWithoutinventoryAtReportDate, nbisinglenodeList);
    }

}
