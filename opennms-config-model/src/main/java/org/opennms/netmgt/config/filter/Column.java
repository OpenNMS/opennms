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
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "column")
@XmlAccessorType(XmlAccessType.FIELD)
public class Column implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_VISISBLE = "true";

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "sql-type")
    private String sqlType;

    @XmlAttribute(name = "java-type")
    private String javaType;

    @XmlAttribute(name = "visible")
    private String visible;

    @XmlElement(name = "alias")
    private java.util.List<Alias> aliasList = new ArrayList<>();

    @XmlElement(name = "constraint")
    private java.util.List<Constraint> constraintList = new ArrayList<>();

    public Column() {
    }

    /**
     * 
     * 
     * @param vAlias
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addAlias(final Alias vAlias) throws IndexOutOfBoundsException {
        this.aliasList.add(vAlias);
    }

    /**
     * 
     * 
     * @param index
     * @param vAlias
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addAlias(final int index, final Alias vAlias) throws IndexOutOfBoundsException {
        this.aliasList.add(index, vAlias);
    }

    /**
     * 
     * 
     * @param vConstraint
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addConstraint(final Constraint vConstraint) throws IndexOutOfBoundsException {
        this.constraintList.add(vConstraint);
    }

    /**
     * 
     * 
     * @param index
     * @param vConstraint
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addConstraint(final int index, final Constraint vConstraint) throws IndexOutOfBoundsException {
        this.constraintList.add(index, vConstraint);
    }

    /**
     * Method enumerateAlias.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<Alias> enumerateAlias() {
        return java.util.Collections.enumeration(this.aliasList);
    }

    /**
     * Method enumerateConstraint.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<Constraint> enumerateConstraint() {
        return java.util.Collections.enumeration(this.constraintList);
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
        
        if (obj instanceof Column) {
            Column temp = (Column)obj;
            boolean equals = Objects.equals(temp.name, name)
                && Objects.equals(temp.sqlType, sqlType)
                && Objects.equals(temp.javaType, javaType)
                && Objects.equals(temp.visible, visible)
                && Objects.equals(temp.aliasList, aliasList)
                && Objects.equals(temp.constraintList, constraintList);
            return equals;
        }
        return false;
    }

    /**
     * Method getAlias.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Alias at the
     * given index
     */
    public Alias getAlias(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.aliasList.size()) {
            throw new IndexOutOfBoundsException("getAlias: Index value '" + index + "' not in range [0.." + (this.aliasList.size() - 1) + "]");
        }
        
        return (Alias) aliasList.get(index);
    }

    /**
     * Method getAlias.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Alias[] getAlias() {
        Alias[] array = new Alias[0];
        return (Alias[]) this.aliasList.toArray(array);
    }

    /**
     * Method getAliasCollection.Returns a reference to 'aliasList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Alias> getAliasCollection() {
        return this.aliasList;
    }

    /**
     * Method getAliasCount.
     * 
     * @return the size of this collection
     */
    public int getAliasCount() {
        return this.aliasList.size();
    }

    /**
     * Method getConstraint.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Constraint at the
     * given index
     */
    public Constraint getConstraint(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.constraintList.size()) {
            throw new IndexOutOfBoundsException("getConstraint: Index value '" + index + "' not in range [0.." + (this.constraintList.size() - 1) + "]");
        }
        
        return (Constraint) constraintList.get(index);
    }

    /**
     * Method getConstraint.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Constraint[] getConstraint() {
        Constraint[] array = new Constraint[0];
        return (Constraint[]) this.constraintList.toArray(array);
    }

    /**
     * Method getConstraintCollection.Returns a reference to 'constraintList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Constraint> getConstraintCollection() {
        return this.constraintList;
    }

    /**
     * Method getConstraintCount.
     * 
     * @return the size of this collection
     */
    public int getConstraintCount() {
        return this.constraintList.size();
    }

    /**
     * Returns the value of field 'javaType'.
     * 
     * @return the value of field 'JavaType'.
     */
    public Optional<String> getJavaType() {
        return Optional.ofNullable(this.javaType);
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
     * Returns the value of field 'sqlType'.
     * 
     * @return the value of field 'SqlType'.
     */
    public Optional<String> getSqlType() {
        return Optional.ofNullable(this.sqlType);
    }

    /**
     * Returns the value of field 'visible'.
     * 
     * @return the value of field 'Visible'.
     */
    public String getVisible() {
        return this.visible != null ? this.visible : DEFAULT_VISISBLE;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            name, 
            sqlType, 
            javaType, 
            visible, 
            aliasList, 
            constraintList);
        return hash;
    }

    /**
     * Method iterateAlias.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<Alias> iterateAlias() {
        return this.aliasList.iterator();
    }

    /**
     * Method iterateConstraint.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<Constraint> iterateConstraint() {
        return this.constraintList.iterator();
    }

    /**
     * Method removeAlias.
     * 
     * @param vAlias
     * @return true if the object was removed from the collection.
     */
    public boolean removeAlias(final Alias vAlias) {
        boolean removed = aliasList.remove(vAlias);
        return removed;
    }

    /**
     * Method removeAliasAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Alias removeAliasAt(final int index) {
        Object obj = this.aliasList.remove(index);
        return (Alias) obj;
    }

    /**
     */
    public void removeAllAlias() {
        this.aliasList.clear();
    }

    /**
     */
    public void removeAllConstraint() {
        this.constraintList.clear();
    }

    /**
     * Method removeConstraint.
     * 
     * @param vConstraint
     * @return true if the object was removed from the collection.
     */
    public boolean removeConstraint(final Constraint vConstraint) {
        boolean removed = constraintList.remove(vConstraint);
        return removed;
    }

    /**
     * Method removeConstraintAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Constraint removeConstraintAt(final int index) {
        Object obj = this.constraintList.remove(index);
        return (Constraint) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vAlias
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setAlias(final int index, final Alias vAlias) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.aliasList.size()) {
            throw new IndexOutOfBoundsException("setAlias: Index value '" + index + "' not in range [0.." + (this.aliasList.size() - 1) + "]");
        }
        
        this.aliasList.set(index, vAlias);
    }

    /**
     * 
     * 
     * @param vAliasArray
     */
    public void setAlias(final Alias[] vAliasArray) {
        //-- copy array
        aliasList.clear();
        
        for (int i = 0; i < vAliasArray.length; i++) {
                this.aliasList.add(vAliasArray[i]);
        }
    }

    /**
     * Sets the value of 'aliasList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vAliasList the Vector to copy.
     */
    public void setAlias(final java.util.List<Alias> vAliasList) {
        // copy vector
        this.aliasList.clear();
        
        this.aliasList.addAll(vAliasList);
    }

    /**
     * Sets the value of 'aliasList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param aliasList the Vector to set.
     */
    public void setAliasCollection(final java.util.List<Alias> aliasList) {
        this.aliasList = aliasList == null? new ArrayList<>() : aliasList;
    }

    /**
     * 
     * 
     * @param index
     * @param vConstraint
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setConstraint(final int index, final Constraint vConstraint) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.constraintList.size()) {
            throw new IndexOutOfBoundsException("setConstraint: Index value '" + index + "' not in range [0.." + (this.constraintList.size() - 1) + "]");
        }
        
        this.constraintList.set(index, vConstraint);
    }

    /**
     * 
     * 
     * @param vConstraintArray
     */
    public void setConstraint(final Constraint[] vConstraintArray) {
        //-- copy array
        constraintList.clear();
        
        for (int i = 0; i < vConstraintArray.length; i++) {
                this.constraintList.add(vConstraintArray[i]);
        }
    }

    /**
     * Sets the value of 'constraintList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vConstraintList the Vector to copy.
     */
    public void setConstraint(final java.util.List<Constraint> vConstraintList) {
        // copy vector
        this.constraintList.clear();
        
        this.constraintList.addAll(vConstraintList);
    }

    /**
     * Sets the value of 'constraintList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param constraintList the Vector to set.
     */
    public void setConstraintCollection(final java.util.List<Constraint> constraintList) {
        this.constraintList = constraintList == null? new ArrayList<>() : constraintList;
     }

    /**
     * Sets the value of field 'javaType'.
     * 
     * @param javaType the value of field 'javaType'.
     */
    public void setJavaType(final String javaType) {
        this.javaType = javaType;
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
     * Sets the value of field 'sqlType'.
     * 
     * @param sqlType the value of field 'sqlType'.
     */
    public void setSqlType(final String sqlType) {
        this.sqlType = sqlType;
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
