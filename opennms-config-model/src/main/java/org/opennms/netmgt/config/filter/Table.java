/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.filter;


import java.util.ArrayList;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "table")
@XmlAccessorType(XmlAccessType.FIELD)
public class Table implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_VISIBLE = "true";
    private static final String DEFAULT_KEY = "secondary";

    @XmlAttribute(name = "visible")
    private String visible;

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "key")
    private String key;

    @XmlElement(name = "join")
    private java.util.List<Join> joinList = new ArrayList<>();

    @XmlElement(name = "column", required = true)
    private java.util.List<Column> columnList = new ArrayList<>();

    public Table() {
    }

    /**
     * 
     * 
     * @param vColumn
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addColumn(final Column vColumn) throws IndexOutOfBoundsException {
        this.columnList.add(vColumn);
    }

    /**
     * 
     * 
     * @param index
     * @param vColumn
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addColumn(final int index, final Column vColumn) throws IndexOutOfBoundsException {
        this.columnList.add(index, vColumn);
    }

    /**
     * 
     * 
     * @param vJoin
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addJoin(final Join vJoin) throws IndexOutOfBoundsException {
        this.joinList.add(vJoin);
    }

    /**
     * 
     * 
     * @param index
     * @param vJoin
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addJoin(final int index, final Join vJoin) throws IndexOutOfBoundsException {
        this.joinList.add(index, vJoin);
    }

    /**
     * Method enumerateColumn.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<Column> enumerateColumn() {
        return java.util.Collections.enumeration(this.columnList);
    }

    /**
     * Method enumerateJoin.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<Join> enumerateJoin() {
        return java.util.Collections.enumeration(this.joinList);
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
        
        if (obj instanceof Table) {
            Table temp = (Table)obj;
            boolean equals = Objects.equals(temp.visible, visible)
                && Objects.equals(temp.name, name)
                && Objects.equals(temp.key, key)
                && Objects.equals(temp.joinList, joinList)
                && Objects.equals(temp.columnList, columnList);
            return equals;
        }
        return false;
    }

    /**
     * Method getColumn.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Column at the
     * given index
     */
    public Column getColumn(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.columnList.size()) {
            throw new IndexOutOfBoundsException("getColumn: Index value '" + index + "' not in range [0.." + (this.columnList.size() - 1) + "]");
        }
        
        return (Column) columnList.get(index);
    }

    /**
     * Method getColumn.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Column[] getColumn() {
        Column[] array = new Column[0];
        return (Column[]) this.columnList.toArray(array);
    }

    /**
     * Method getColumnCollection.Returns a reference to 'columnList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Column> getColumnCollection() {
        return this.columnList;
    }

    /**
     * Method getColumnCount.
     * 
     * @return the size of this collection
     */
    public int getColumnCount() {
        return this.columnList.size();
    }

    /**
     * Method getJoin.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Join at the given
     * index
     */
    public Join getJoin(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.joinList.size()) {
            throw new IndexOutOfBoundsException("getJoin: Index value '" + index + "' not in range [0.." + (this.joinList.size() - 1) + "]");
        }
        
        return (Join) joinList.get(index);
    }

    /**
     * Method getJoin.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Join[] getJoin() {
        Join[] array = new Join[0];
        return (Join[]) this.joinList.toArray(array);
    }

    /**
     * Method getJoinCollection.Returns a reference to 'joinList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Join> getJoinCollection() {
        return this.joinList;
    }

    /**
     * Method getJoinCount.
     * 
     * @return the size of this collection
     */
    public int getJoinCount() {
        return this.joinList.size();
    }

    /**
     * Returns the value of field 'key'.
     * 
     * @return the value of field 'Key'.
     */
    public String getKey() {
        return this.key != null ? this.key : DEFAULT_KEY;
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value of field 'visible'.
     * 
     * @return the value of field 'Visible'.
     */
    public String getVisible() {
        return this.visible != null ? this.visible : DEFAULT_VISIBLE;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            visible, 
            name, 
            key, 
            joinList, 
            columnList);
        return hash;
    }

    /**
     * Method iterateColumn.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<Column> iterateColumn() {
        return this.columnList.iterator();
    }

    /**
     * Method iterateJoin.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<Join> iterateJoin() {
        return this.joinList.iterator();
    }

    /**
     */
    public void removeAllColumn() {
        this.columnList.clear();
    }

    /**
     */
    public void removeAllJoin() {
        this.joinList.clear();
    }

    /**
     * Method removeColumn.
     * 
     * @param vColumn
     * @return true if the object was removed from the collection.
     */
    public boolean removeColumn(final Column vColumn) {
        boolean removed = columnList.remove(vColumn);
        return removed;
    }

    /**
     * Method removeColumnAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Column removeColumnAt(final int index) {
        Object obj = this.columnList.remove(index);
        return (Column) obj;
    }

    /**
     * Method removeJoin.
     * 
     * @param vJoin
     * @return true if the object was removed from the collection.
     */
    public boolean removeJoin(final Join vJoin) {
        boolean removed = joinList.remove(vJoin);
        return removed;
    }

    /**
     * Method removeJoinAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Join removeJoinAt(final int index) {
        Object obj = this.joinList.remove(index);
        return (Join) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vColumn
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setColumn(final int index, final Column vColumn) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.columnList.size()) {
            throw new IndexOutOfBoundsException("setColumn: Index value '" + index + "' not in range [0.." + (this.columnList.size() - 1) + "]");
        }
        
        this.columnList.set(index, vColumn);
    }

    /**
     * 
     * 
     * @param vColumnArray
     */
    public void setColumn(final Column[] vColumnArray) {
        //-- copy array
        columnList.clear();
        
        for (int i = 0; i < vColumnArray.length; i++) {
                this.columnList.add(vColumnArray[i]);
        }
    }

    /**
     * Sets the value of 'columnList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vColumnList the Vector to copy.
     */
    public void setColumn(final java.util.List<Column> vColumnList) {
        // copy vector
        this.columnList.clear();
        
        this.columnList.addAll(vColumnList);
    }

    /**
     * Sets the value of 'columnList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param columnList the Vector to set.
     */
    public void setColumnCollection(final java.util.List<Column> columnList) {
        this.columnList = columnList == null? new ArrayList<>() : columnList;
    }

    /**
     * 
     * 
     * @param index
     * @param vJoin
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setJoin(final int index, final Join vJoin) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.joinList.size()) {
            throw new IndexOutOfBoundsException("setJoin: Index value '" + index + "' not in range [0.." + (this.joinList.size() - 1) + "]");
        }
        
        this.joinList.set(index, vJoin);
    }

    /**
     * 
     * 
     * @param vJoinArray
     */
    public void setJoin(final Join[] vJoinArray) {
        //-- copy array
        joinList.clear();
        
        for (int i = 0; i < vJoinArray.length; i++) {
                this.joinList.add(vJoinArray[i]);
        }
    }

    /**
     * Sets the value of 'joinList' by copying the given Vector. All elements will
     * be checked for type safety.
     * 
     * @param vJoinList the Vector to copy.
     */
    public void setJoin(final java.util.List<Join> vJoinList) {
        // copy vector
        this.joinList.clear();
        
        this.joinList.addAll(vJoinList);
    }

    /**
     * Sets the value of 'joinList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param joinList the Vector to set.
     */
    public void setJoinCollection(final java.util.List<Join> joinList) {
        this.joinList = joinList == null? new ArrayList<>() : joinList;
    }

    /**
     * Sets the value of field 'key'.
     * 
     * @param key the value of field 'key'.
     */
    public void setKey(final String key) {
        this.key = key;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("'name' is a required attribute!");
        }
        this.name = name;
    }

    /**
     * Sets the value of field 'visible'.
     * 
     * @param visible the value of field 'visible'.
     */
    public void setVisible(final String visible) {
        this.visible = visible;
    }

}
