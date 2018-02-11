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
 * Class GroupXSet.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "groupXSet")
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupXSet implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "groupXSetName", required = true)
    private String groupXSetName;

    @XmlElement(name = "totalNodes")
    private Integer totalNodes;

    @XmlElement(name = "nodesMatching")
    private Integer nodesMatching;

    @XmlElement(name = "nodesWithoutconfigurationAtAll")
    private Integer nodesWithoutconfigurationAtAll;

    @XmlElement(name = "nodesWithoutconfigurationAtReportDate")
    private Integer nodesWithoutconfigurationAtReportDate;

    @XmlElement(name = "nodeSet")
    private List<NodeSet> nodeSetList;

    public GroupXSet() {
        this.nodeSetList = new ArrayList<>();
    }

    /**
     * 
     * 
     * @param vNodeSet
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addNodeSet(final NodeSet vNodeSet) throws IndexOutOfBoundsException {
        this.nodeSetList.add(vNodeSet);
    }

    /**
     * 
     * 
     * @param index
     * @param vNodeSet
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addNodeSet(final int index, final NodeSet vNodeSet) throws IndexOutOfBoundsException {
        this.nodeSetList.add(index, vNodeSet);
    }

    /**
     */
    public void deleteNodesMatching() {
        this.nodesMatching= null;
    }

    /**
     */
    public void deleteNodesWithoutconfigurationAtAll() {
        this.nodesWithoutconfigurationAtAll= null;
    }

    /**
     */
    public void deleteNodesWithoutconfigurationAtReportDate() {
        this.nodesWithoutconfigurationAtReportDate= null;
    }

    /**
     */
    public void deleteTotalNodes() {
        this.totalNodes= null;
    }

    /**
     * Method enumerateNodeSet.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<NodeSet> enumerateNodeSet() {
        return Collections.enumeration(this.nodeSetList);
    }

    /**
     * Returns the value of field 'groupXSetName'.
     * 
     * @return the value of field 'GroupXSetName'.
     */
    public String getGroupXSetName() {
        return this.groupXSetName;
    }

    /**
     * Method getNodeSet.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the NodeSet at the
     * given index
     */
    public NodeSet getNodeSet(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.nodeSetList.size()) {
            throw new IndexOutOfBoundsException("getNodeSet: Index value '" + index + "' not in range [0.." + (this.nodeSetList.size() - 1) + "]");
        }
        
        return (NodeSet) nodeSetList.get(index);
    }

    /**
     * Method getNodeSet.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public NodeSet[] getNodeSet() {
        NodeSet[] array = new NodeSet[0];
        return (NodeSet[]) this.nodeSetList.toArray(array);
    }

    /**
     * Method getNodeSetCollection.Returns a reference to 'nodeSetList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<NodeSet> getNodeSetCollection() {
        return this.nodeSetList;
    }

    /**
     * Method getNodeSetCount.
     * 
     * @return the size of this collection
     */
    public int getNodeSetCount() {
        return this.nodeSetList.size();
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
     * Returns the value of field 'nodesWithoutconfigurationAtAll'.
     * 
     * @return the value of field 'NodesWithoutconfigurationAtAll'.
     */
    public Integer getNodesWithoutconfigurationAtAll() {
        return this.nodesWithoutconfigurationAtAll;
    }

    /**
     * Returns the value of field 'nodesWithoutconfigurationAtReportDate'.
     * 
     * @return the value of field 'NodesWithoutconfigurationAtReportDate'.
     */
    public Integer getNodesWithoutconfigurationAtReportDate() {
        return this.nodesWithoutconfigurationAtReportDate;
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
     * Method hasNodesWithoutconfigurationAtAll.
     * 
     * @return true if at least one NodesWithoutconfigurationAtAll has been added
     */
    public boolean hasNodesWithoutconfigurationAtAll() {
        return this.nodesWithoutconfigurationAtAll != null;
    }

    /**
     * Method hasNodesWithoutconfigurationAtReportDate.
     * 
     * @return true if at least one NodesWithoutconfigurationAtReportDate has been
     * added
     */
    public boolean hasNodesWithoutconfigurationAtReportDate() {
        return this.nodesWithoutconfigurationAtReportDate != null;
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
     * Method iterateNodeSet.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<NodeSet> iterateNodeSet() {
        return this.nodeSetList.iterator();
    }

    /**
     */
    public void removeAllNodeSet() {
        this.nodeSetList.clear();
    }

    /**
     * Method removeNodeSet.
     * 
     * @param vNodeSet
     * @return true if the object was removed from the collection.
     */
    public boolean removeNodeSet(final NodeSet vNodeSet) {
        boolean removed = nodeSetList.remove(vNodeSet);
        return removed;
    }

    /**
     * Method removeNodeSetAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public NodeSet removeNodeSetAt(final int index) {
        Object obj = this.nodeSetList.remove(index);
        return (NodeSet) obj;
    }

    /**
     * Sets the value of field 'groupXSetName'.
     * 
     * @param groupXSetName the value of field 'groupXSetName'.
     */
    public void setGroupXSetName(final String groupXSetName) {
        this.groupXSetName = groupXSetName;
    }

    /**
     * 
     * 
     * @param index
     * @param vNodeSet
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setNodeSet(final int index, final NodeSet vNodeSet) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.nodeSetList.size()) {
            throw new IndexOutOfBoundsException("setNodeSet: Index value '" + index + "' not in range [0.." + (this.nodeSetList.size() - 1) + "]");
        }
        
        this.nodeSetList.set(index, vNodeSet);
    }

    /**
     * 
     * 
     * @param vNodeSetArray
     */
    public void setNodeSet(final NodeSet[] vNodeSetArray) {
        //-- copy array
        nodeSetList.clear();
        
        for (int i = 0; i < vNodeSetArray.length; i++) {
                this.nodeSetList.add(vNodeSetArray[i]);
        }
    }

    /**
     * Sets the value of 'nodeSetList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vNodeSetList the Vector to copy.
     */
    public void setNodeSet(final List<NodeSet> vNodeSetList) {
        // copy vector
        this.nodeSetList.clear();
        
        this.nodeSetList.addAll(vNodeSetList);
    }

    /**
     * Sets the value of 'nodeSetList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param nodeSetList the Vector to set.
     */
    public void setNodeSetCollection(final List<NodeSet> nodeSetList) {
        this.nodeSetList = nodeSetList;
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
     * Sets the value of field 'nodesWithoutconfigurationAtAll'.
     * 
     * @param nodesWithoutconfigurationAtAll the value of field
     * 'nodesWithoutconfigurationAtAll'.
     */
    public void setNodesWithoutconfigurationAtAll(final Integer nodesWithoutconfigurationAtAll) {
        this.nodesWithoutconfigurationAtAll = nodesWithoutconfigurationAtAll;
    }

    /**
     * Sets the value of field 'nodesWithoutconfigurationAtReportDate'.
     * 
     * @param nodesWithoutconfigurationAtReportDate the value of field
     * 'nodesWithoutconfigurationAtReportDate'.
     */
    public void setNodesWithoutconfigurationAtReportDate(final Integer nodesWithoutconfigurationAtReportDate) {
        this.nodesWithoutconfigurationAtReportDate = nodesWithoutconfigurationAtReportDate;
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
        if (!(other instanceof GroupXSet)) {
            return false;
        }
        GroupXSet castOther = (GroupXSet) other;
        return Objects.equals(groupXSetName, castOther.groupXSetName)
                && Objects.equals(totalNodes, castOther.totalNodes)
                && Objects.equals(nodesMatching, castOther.nodesMatching)
                && Objects.equals(nodesWithoutconfigurationAtAll, castOther.nodesWithoutconfigurationAtAll)
                && Objects.equals(nodesWithoutconfigurationAtReportDate,
                        castOther.nodesWithoutconfigurationAtReportDate)
                && Objects.equals(nodeSetList, castOther.nodeSetList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupXSetName, totalNodes, nodesMatching, nodesWithoutconfigurationAtAll,
                nodesWithoutconfigurationAtReportDate, nodeSetList);
    }

}
