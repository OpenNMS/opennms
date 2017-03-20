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

package org.opennms.netmgt.config.kscReports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class Report.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "Report")
@XmlAccessorType(XmlAccessType.FIELD)
public class Report implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "id")
    private Integer id;

    @XmlAttribute(name = "title", required = true)
    private String title;

    @XmlAttribute(name = "show_timespan_button")
    private Boolean show_timespan_button;

    @XmlAttribute(name = "show_graphtype_button")
    private Boolean show_graphtype_button;

    @XmlAttribute(name = "graphs_per_line")
    private Integer graphs_per_line;

    @XmlElement(name = "Graph")
    private List<Graph> graphList = new ArrayList<>();

    /**
     * 
     * 
     * @param vGraph
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addGraph(final Graph vGraph) throws IndexOutOfBoundsException {
        this.graphList.add(vGraph);
    }

    /**
     * 
     * 
     * @param index
     * @param vGraph
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addGraph(final int index, final Graph vGraph) throws IndexOutOfBoundsException {
        this.graphList.add(index, vGraph);
    }

    /**
     */
    public void deleteGraphsPerLine() {
        this.graphs_per_line= null;
    }

    /**
     */
    public void deleteId() {
        this.id= null;
    }

    /**
     */
    public void deleteShowGraphtypeButton() {
        this.show_graphtype_button= null;
    }

    /**
     */
    public void deleteShowTimespanButton() {
        this.show_timespan_button= null;
    }

    /**
     * Method enumerateGraph.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Graph> enumerateGraph() {
        return Collections.enumeration(this.graphList);
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
        
        if (obj instanceof Report) {
            Report temp = (Report)obj;
            boolean equals = Objects.equals(temp.id, id)
                && Objects.equals(temp.title, title)
                && Objects.equals(temp.show_timespan_button, show_timespan_button)
                && Objects.equals(temp.show_graphtype_button, show_graphtype_button)
                && Objects.equals(temp.graphs_per_line, graphs_per_line)
                && Objects.equals(temp.graphList, graphList);
            return equals;
        }
        return false;
    }

    /**
     * Method getGraph.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Graph at the
     * given index
     */
    public Graph getGraph(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.graphList.size()) {
            throw new IndexOutOfBoundsException("getGraph: Index value '" + index + "' not in range [0.." + (this.graphList.size() - 1) + "]");
        }
        
        return (Graph) graphList.get(index);
    }

    /**
     * Method getGraph.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Graph[] getGraph() {
        Graph[] array = new Graph[0];
        return (Graph[]) this.graphList.toArray(array);
    }

    /**
     * Method getGraphCollection.Returns a reference to 'graphList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Graph> getGraphCollection() {
        return this.graphList;
    }

    /**
     * Method getGraphCount.
     * 
     * @return the size of this collection
     */
    public int getGraphCount() {
        return this.graphList.size();
    }

    /**
     * Returns the value of field 'graphs_per_line'.
     * 
     * @return the value of field 'Graphs_per_line'.
     */
    public Optional<Integer> getGraphsPerLine() {
        return Optional.ofNullable(this.graphs_per_line);
    }

    /**
     * Returns the value of field 'id'.
     * 
     * @return the value of field 'Id'.
     */
    public Optional<Integer> getId() {
        return Optional.ofNullable(this.id);
    }

    /**
     * Returns the value of field 'show_graphtype_button'.
     * 
     * @return the value of field 'Show_graphtype_button'.
     */
    public Optional<Boolean> getShowGraphtypeButton() {
        return Optional.ofNullable(this.show_graphtype_button);
    }

    /**
     * Returns the value of field 'show_timespan_button'.
     * 
     * @return the value of field 'Show_timespan_button'.
     */
    public Optional<Boolean> getShowTimespanButton() {
        return Optional.ofNullable(this.show_timespan_button);
    }

    /**
     * Returns the value of field 'title'.
     * 
     * @return the value of field 'Title'.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Method hasGraphs_per_line.
     * 
     * @return true if at least one Graphs_per_line has been added
     */
    public boolean hasGraphsPerLine() {
        return this.graphs_per_line != null;
    }

    /**
     * Method hasId.
     * 
     * @return true if at least one Id has been added
     */
    public boolean hasId() {
        return this.id != null;
    }

    /**
     * Method hasShow_graphtype_button.
     * 
     * @return true if at least one Show_graphtype_button has been added
     */
    public boolean hasShowGraphtypeButton() {
        return this.show_graphtype_button != null;
    }

    /**
     * Method hasShow_timespan_button.
     * 
     * @return true if at least one Show_timespan_button has been added
     */
    public boolean hasShowTimespanButton() {
        return this.show_timespan_button != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            id, 
            title, 
            show_timespan_button, 
            show_graphtype_button, 
            graphs_per_line, 
            graphList);
        return hash;
    }

    /**
     * Returns the value of field 'show_graphtype_button'.
     * 
     * @return the value of field 'Show_graphtype_button'.
     */
    public Boolean isShowGraphtypeButton() {
        return this.show_graphtype_button;
    }

    /**
     * Returns the value of field 'show_timespan_button'.
     * 
     * @return the value of field 'Show_timespan_button'.
     */
    public Boolean isShowTimespanButton() {
        return this.show_timespan_button;
    }

    /**
     * Method iterateGraph.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Graph> iterateGraph() {
        return this.graphList.iterator();
    }

    /**
     */
    public void removeAllGraph() {
        this.graphList.clear();
    }

    /**
     * Method removeGraph.
     * 
     * @param vGraph
     * @return true if the object was removed from the collection.
     */
    public boolean removeGraph(final Graph vGraph) {
        boolean removed = graphList.remove(vGraph);
        return removed;
    }

    /**
     * Method removeGraphAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Graph removeGraphAt(final int index) {
        Object obj = this.graphList.remove(index);
        return (Graph) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vGraph
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setGraph(final int index, final Graph vGraph) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.graphList.size()) {
            throw new IndexOutOfBoundsException("setGraph: Index value '" + index + "' not in range [0.." + (this.graphList.size() - 1) + "]");
        }
        
        this.graphList.set(index, vGraph);
    }

    /**
     * 
     * 
     * @param vGraphArray
     */
    public void setGraph(final Graph[] vGraphArray) {
        //-- copy array
        graphList.clear();
        
        for (int i = 0; i < vGraphArray.length; i++) {
                this.graphList.add(vGraphArray[i]);
        }
    }

    /**
     * Sets the value of 'graphList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vGraphList the Vector to copy.
     */
    public void setGraph(final List<Graph> vGraphList) {
        // copy vector
        this.graphList.clear();
        
        this.graphList.addAll(vGraphList);
    }

    /**
     * Sets the value of 'graphList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param graphList the Vector to set.
     */
    public void setGraphCollection(final List<Graph> graphList) {
        this.graphList = graphList;
    }

    /**
     * Sets the value of field 'graphs_per_line'.
     * 
     * @param graphs_per_line the value of field 'graphs_per_line'.
     */
    public void setGraphsPerLine(final Integer graphs_per_line) {
        this.graphs_per_line = graphs_per_line;
    }

    /**
     * Sets the value of field 'id'.
     * 
     * @param id the value of field 'id'.
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * Sets the value of field 'show_graphtype_button'.
     * 
     * @param show_graphtype_button the value of field 'show_graphtype_button'.
     */
    public void setShowGraphtypeButton(final Boolean show_graphtype_button) {
        this.show_graphtype_button = show_graphtype_button;
    }

    /**
     * Sets the value of field 'show_timespan_button'.
     * 
     * @param show_timespan_button the value of field 'show_timespan_button'.
     */
    public void setShowTimespanButton(final Boolean show_timespan_button) {
        this.show_timespan_button = show_timespan_button;
    }

    /**
     * Sets the value of field 'title'.
     * 
     * @param title the value of field 'title'.
     */
    public void setTitle(final String title) {
        if (title == null) {
            throw new IllegalArgumentException("'title' is a required attribute!");
        }
        this.title = title;
    }

}
