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

package org.opennms.netmgt.config.threshd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Grouping of related threshold definitions
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "group")
@XmlAccessorType(XmlAccessType.FIELD)
public class Group implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Group name
     */
    @XmlAttribute(name = "name", required = true)
    private String name;

    /**
     * Full path to the RRD repository where the data is stored
     *  
     */
    @XmlAttribute(name = "rrdRepository", required = true)
    private String rrdRepository;

    /**
     * Threshold definition
     */
    @XmlElement(name = "threshold")
    private List<Threshold> thresholdList = new ArrayList<>();

    /**
     * Expression definition
     */
    @XmlElement(name = "expression")
    private List<Expression> expressionList = new ArrayList<>();

    public Group() { }

    /**
     * 
     * 
     * @param vExpression
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addExpression(final Expression vExpression) throws IndexOutOfBoundsException {
        this.expressionList.add(vExpression);
    }

    /**
     * 
     * 
     * @param index
     * @param vExpression
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addExpression(final int index, final Expression vExpression) throws IndexOutOfBoundsException {
        this.expressionList.add(index, vExpression);
    }

    /**
     * 
     * 
     * @param vThreshold
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addThreshold(final Threshold vThreshold) throws IndexOutOfBoundsException {
        this.thresholdList.add(vThreshold);
    }

    /**
     * 
     * 
     * @param index
     * @param vThreshold
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addThreshold(final int index, final Threshold vThreshold) throws IndexOutOfBoundsException {
        this.thresholdList.add(index, vThreshold);
    }

    /**
     * Method enumerateExpression.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Expression> enumerateExpression() {
        return Collections.enumeration(this.expressionList);
    }

    /**
     * Method enumerateThreshold.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Threshold> enumerateThreshold() {
        return Collections.enumeration(this.thresholdList);
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
        
        if (obj instanceof Group) {
            Group temp = (Group)obj;
            boolean equals = Objects.equals(temp.name, name)
                && Objects.equals(temp.rrdRepository, rrdRepository)
                && Objects.equals(temp.thresholdList, thresholdList)
                && Objects.equals(temp.expressionList, expressionList);
            return equals;
        }
        return false;
    }

    /**
     * Method getExpression.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Expression at
     * the given index
     */
    public Expression getExpression(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.expressionList.size()) {
            throw new IndexOutOfBoundsException("getExpression: Index value '" + index + "' not in range [0.." + (this.expressionList.size() - 1) + "]");
        }
        
        return (Expression) expressionList.get(index);
    }

    /**
     * Method getExpression.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Expression[] getExpression() {
        Expression[] array = new Expression[0];
        return (Expression[]) this.expressionList.toArray(array);
    }

    /**
     * Method getExpressionCollection.Returns a reference to 'expressionList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Expression> getExpressionCollection() {
        return this.expressionList;
    }

    /**
     * Method getExpressionCount.
     * 
     * @return the size of this collection
     */
    public int getExpressionCount() {
        return this.expressionList.size();
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the following
     * description: Group name
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value of field 'rrdRepository'. The field 'rrdRepository' has
     * the following description: Full path to the RRD repository where the data
     * is stored
     *  
     * 
     * @return the value of field 'RrdRepository'.
     */
    public String getRrdRepository() {
        return this.rrdRepository;
    }

    /**
     * Method getThreshold.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Threshold at the
     * given index
     */
    public Threshold getThreshold(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.thresholdList.size()) {
            throw new IndexOutOfBoundsException("getThreshold: Index value '" + index + "' not in range [0.." + (this.thresholdList.size() - 1) + "]");
        }
        
        return (Threshold) thresholdList.get(index);
    }

    /**
     * Method getThreshold.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Threshold[] getThreshold() {
        Threshold[] array = new Threshold[0];
        return (Threshold[]) this.thresholdList.toArray(array);
    }

    /**
     * Method getThresholdCollection.Returns a reference to 'thresholdList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Threshold> getThresholdCollection() {
        return this.thresholdList;
    }

    /**
     * Method getThresholdCount.
     * 
     * @return the size of this collection
     */
    public int getThresholdCount() {
        return this.thresholdList.size();
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
            rrdRepository, 
            thresholdList, 
            expressionList);
        return hash;
    }

    /**
     * Method iterateExpression.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Expression> iterateExpression() {
        return this.expressionList.iterator();
    }

    /**
     * Method iterateThreshold.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Threshold> iterateThreshold() {
        return this.thresholdList.iterator();
    }

    /**
     */
    public void removeAllExpression() {
        this.expressionList.clear();
    }

    /**
     */
    public void removeAllThreshold() {
        this.thresholdList.clear();
    }

    /**
     * Method removeExpression.
     * 
     * @param vExpression
     * @return true if the object was removed from the collection.
     */
    public boolean removeExpression(final Expression vExpression) {
        boolean removed = expressionList.remove(vExpression);
        return removed;
    }

    /**
     * Method removeExpressionAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Expression removeExpressionAt(final int index) {
        Object obj = this.expressionList.remove(index);
        return (Expression) obj;
    }

    /**
     * Method removeThreshold.
     * 
     * @param vThreshold
     * @return true if the object was removed from the collection.
     */
    public boolean removeThreshold(final Threshold vThreshold) {
        boolean removed = thresholdList.remove(vThreshold);
        return removed;
    }

    /**
     * Method removeThresholdAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Threshold removeThresholdAt(final int index) {
        Object obj = this.thresholdList.remove(index);
        return (Threshold) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vExpression
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setExpression(final int index, final Expression vExpression) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.expressionList.size()) {
            throw new IndexOutOfBoundsException("setExpression: Index value '" + index + "' not in range [0.." + (this.expressionList.size() - 1) + "]");
        }
        
        this.expressionList.set(index, vExpression);
    }

    /**
     * 
     * 
     * @param vExpressionArray
     */
    public void setExpression(final Expression[] vExpressionArray) {
        //-- copy array
        expressionList.clear();
        
        for (int i = 0; i < vExpressionArray.length; i++) {
                this.expressionList.add(vExpressionArray[i]);
        }
    }

    /**
     * Sets the value of 'expressionList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vExpressionList the Vector to copy.
     */
    public void setExpression(final List<Expression> vExpressionList) {
        // copy vector
        this.expressionList.clear();
        
        this.expressionList.addAll(vExpressionList);
    }

    /**
     * Sets the value of 'expressionList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param expressionList the Vector to set.
     */
    public void setExpressionCollection(final List<Expression> expressionList) {
        this.expressionList = expressionList;
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the following
     * description: Group name
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the value of field 'rrdRepository'. The field 'rrdRepository' has the
     * following description: Full path to the RRD repository where the data is
     * stored
     *  
     * 
     * @param rrdRepository the value of field 'rrdRepository'.
     */
    public void setRrdRepository(final String rrdRepository) {
        this.rrdRepository = rrdRepository;
    }

    /**
     * 
     * 
     * @param index
     * @param vThreshold
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setThreshold(final int index, final Threshold vThreshold) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.thresholdList.size()) {
            throw new IndexOutOfBoundsException("setThreshold: Index value '" + index + "' not in range [0.." + (this.thresholdList.size() - 1) + "]");
        }
        
        this.thresholdList.set(index, vThreshold);
    }

    /**
     * 
     * 
     * @param vThresholdArray
     */
    public void setThreshold(final Threshold[] vThresholdArray) {
        //-- copy array
        thresholdList.clear();
        
        for (int i = 0; i < vThresholdArray.length; i++) {
                this.thresholdList.add(vThresholdArray[i]);
        }
    }

    /**
     * Sets the value of 'thresholdList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vThresholdList the Vector to copy.
     */
    public void setThreshold(final List<Threshold> vThresholdList) {
        // copy vector
        this.thresholdList.clear();
        
        this.thresholdList.addAll(vThresholdList);
    }

    /**
     * Sets the value of 'thresholdList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param thresholdList the Vector to set.
     */
    public void setThresholdCollection(final List<Threshold> thresholdList) {
        this.thresholdList = thresholdList;
    }

}
