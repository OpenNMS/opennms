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

package org.opennms.netmgt.config.notificationCommands;

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
 * Class Command.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "command")
@XmlAccessorType(XmlAccessType.FIELD)
public class Command implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_BINARY = "true";

    @XmlAttribute(name = "binary")
    private String binary;

    @XmlElement(name = "name", required = true)
    private String name;

    @XmlElement(name = "execute", required = true)
    private String execute;

    @XmlElement(name = "comment")
    private String comment;

    @XmlElement(name = "contact-type")
    private String contactType;

    @XmlElement(name = "argument")
    private List<Argument> argumentList = new ArrayList<>();

    public Command() { }

    /**
     * 
     * 
     * @param vArgument
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addArgument(final Argument vArgument) throws IndexOutOfBoundsException {
        this.argumentList.add(vArgument);
    }

    /**
     * 
     * 
     * @param index
     * @param vArgument
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addArgument(final int index, final Argument vArgument) throws IndexOutOfBoundsException {
        this.argumentList.add(index, vArgument);
    }

    /**
     * Method enumerateArgument.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Argument> enumerateArgument() {
        return Collections.enumeration(this.argumentList);
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
        
        if (obj instanceof Command) {
            Command temp = (Command)obj;
            boolean equals = Objects.equals(temp.binary, binary)
                && Objects.equals(temp.name, name)
                && Objects.equals(temp.execute, execute)
                && Objects.equals(temp.comment, comment)
                && Objects.equals(temp.contactType, contactType)
                && Objects.equals(temp.argumentList, argumentList);
            return equals;
        }
        return false;
    }

    /**
     * Method getArgument.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the
     * Argument at the given index
     */
    public Argument getArgument(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.argumentList.size()) {
            throw new IndexOutOfBoundsException("getArgument: Index value '" + index + "' not in range [0.." + (this.argumentList.size() - 1) + "]");
        }
        
        return (Argument) argumentList.get(index);
    }

    /**
     * Method getArgument.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Argument[] getArgument() {
        Argument[] array = new Argument[0];
        return (Argument[]) this.argumentList.toArray(array);
    }

    /**
     * Method getArgumentCollection.Returns a reference to 'argumentList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Argument> getArgumentCollection() {
        return this.argumentList;
    }

    /**
     * Method getArgumentCount.
     * 
     * @return the size of this collection
     */
    public int getArgumentCount() {
        return this.argumentList.size();
    }

    /**
     * Returns the value of field 'binary'.
     * 
     * @return the value of field 'Binary'.
     */
    public String getBinary() {
        return this.binary != null ? this.binary : DEFAULT_BINARY;
    }

    /**
     * Returns the value of field 'comment'.
     * 
     * @return the value of field 'Comment'.
     */
    public Optional<String> getComment() {
        return Optional.ofNullable(this.comment);
    }

    /**
     * Returns the value of field 'contactType'.
     * 
     * @return the value of field 'ContactType'.
     */
    public Optional<String> getContactType() {
        return Optional.ofNullable(this.contactType);
    }

    /**
     * Returns the value of field 'execute'.
     * 
     * @return the value of field 'Execute'.
     */
    public String getExecute() {
        return this.execute;
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
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            binary, 
            name, 
            execute, 
            comment, 
            contactType, 
            argumentList);
        return hash;
    }

    /**
     * Method iterateArgument.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Argument> iterateArgument() {
        return this.argumentList.iterator();
    }

    /**
     */
    public void removeAllArgument() {
        this.argumentList.clear();
    }

    /**
     * Method removeArgument.
     * 
     * @param vArgument
     * @return true if the object was removed from the collection.
     */
    public boolean removeArgument(final Argument vArgument) {
        boolean removed = argumentList.remove(vArgument);
        return removed;
    }

    /**
     * Method removeArgumentAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Argument removeArgumentAt(final int index) {
        Object obj = this.argumentList.remove(index);
        return (Argument) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vArgument
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setArgument(final int index, final Argument vArgument) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.argumentList.size()) {
            throw new IndexOutOfBoundsException("setArgument: Index value '" + index + "' not in range [0.." + (this.argumentList.size() - 1) + "]");
        }
        
        this.argumentList.set(index, vArgument);
    }

    /**
     * 
     * 
     * @param vArgumentArray
     */
    public void setArgument(final Argument[] vArgumentArray) {
        //-- copy array
        argumentList.clear();
        
        for (int i = 0; i < vArgumentArray.length; i++) {
                this.argumentList.add(vArgumentArray[i]);
        }
    }

    /**
     * Sets the value of 'argumentList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vArgumentList the Vector to copy.
     */
    public void setArgument(final List<Argument> vArgumentList) {
        // copy vector
        this.argumentList.clear();
        
        this.argumentList.addAll(vArgumentList);
    }

    /**
     * Sets the value of 'argumentList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param argumentList the Vector to set.
     */
    public void setArgumentCollection(final List<Argument> argumentList) {
        this.argumentList = argumentList == null? new ArrayList<>() : argumentList;
    }

    /**
     * Sets the value of field 'binary'.
     * 
     * @param binary the value of field 'binary'.
     */
    public void setBinary(final String binary) {
        this.binary = binary;
    }

    /**
     * Sets the value of field 'comment'.
     * 
     * @param comment the value of field 'comment'.
     */
    public void setComment(final String comment) {
        this.comment = comment;
    }

    /**
     * Sets the value of field 'contactType'.
     * 
     * @param contactType the value of field 'contactType'.
     */
    public void setContactType(final String contactType) {
        this.contactType = contactType;
    }

    /**
     * Sets the value of field 'execute'.
     * 
     * @param execute the value of field 'execute'.
     */
    public void setExecute(final String execute) {
        if (execute == null) {
            throw new IllegalArgumentException("Execute is a required field!");
        }
        this.execute = execute;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name is a required field!");
        }
        this.name = name;
    }

}
