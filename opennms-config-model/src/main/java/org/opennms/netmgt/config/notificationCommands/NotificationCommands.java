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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the notificationCommands.xml
 *  configuration file.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "notification-commands")
@XmlAccessorType(XmlAccessType.FIELD)
public class NotificationCommands implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "header", required = true)
    private Header header;

    @XmlElement(name = "command", required = true)
    private List<Command> commandList = new ArrayList<>();

    public NotificationCommands() { }

    /**
     * 
     * 
     * @param vCommand
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addCommand(final Command vCommand) throws IndexOutOfBoundsException {
        this.commandList.add(vCommand);
    }

    /**
     * 
     * 
     * @param index
     * @param vCommand
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addCommand(final int index, final Command vCommand) throws IndexOutOfBoundsException {
        this.commandList.add(index, vCommand);
    }

    /**
     * Method enumerateCommand.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Command> enumerateCommand() {
        return Collections.enumeration(this.commandList);
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
        
        if (obj instanceof NotificationCommands) {
            NotificationCommands temp = (NotificationCommands)obj;
            boolean equals = Objects.equals(temp.header, header)
                && Objects.equals(temp.commandList, commandList);
            return equals;
        }
        return false;
    }

    /**
     * Method getCommand.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the
     * Command at the given index
     */
    public Command getCommand(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.commandList.size()) {
            throw new IndexOutOfBoundsException("getCommand: Index value '" + index + "' not in range [0.." + (this.commandList.size() - 1) + "]");
        }
        
        return (Command) commandList.get(index);
    }

    /**
     * Method getCommand.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Command[] getCommand() {
        Command[] array = new Command[0];
        return (Command[]) this.commandList.toArray(array);
    }

    /**
     * Method getCommandCollection.Returns a reference to 'commandList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Command> getCommandCollection() {
        return this.commandList;
    }

    /**
     * Method getCommandCount.
     * 
     * @return the size of this collection
     */
    public int getCommandCount() {
        return this.commandList.size();
    }

    /**
     * Returns the value of field 'header'.
     * 
     * @return the value of field 'Header'.
     */
    public Header getHeader() {
        return this.header;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            header, 
            commandList);
        return hash;
    }

    /**
     * Method iterateCommand.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Command> iterateCommand() {
        return this.commandList.iterator();
    }

    /**
     */
    public void removeAllCommand() {
        this.commandList.clear();
    }

    /**
     * Method removeCommand.
     * 
     * @param vCommand
     * @return true if the object was removed from the collection.
     */
    public boolean removeCommand(final Command vCommand) {
        boolean removed = commandList.remove(vCommand);
        return removed;
    }

    /**
     * Method removeCommandAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Command removeCommandAt(final int index) {
        Object obj = this.commandList.remove(index);
        return (Command) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vCommand
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setCommand(final int index, final Command vCommand) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.commandList.size()) {
            throw new IndexOutOfBoundsException("setCommand: Index value '" + index + "' not in range [0.." + (this.commandList.size() - 1) + "]");
        }
        
        this.commandList.set(index, vCommand);
    }

    /**
     * 
     * 
     * @param vCommandArray
     */
    public void setCommand(final Command[] vCommandArray) {
        //-- copy array
        commandList.clear();
        
        for (int i = 0; i < vCommandArray.length; i++) {
                this.commandList.add(vCommandArray[i]);
        }
    }

    /**
     * Sets the value of 'commandList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vCommandList the Vector to copy.
     */
    public void setCommand(final List<Command> vCommandList) {
        // copy vector
        this.commandList.clear();
        
        this.commandList.addAll(vCommandList);
    }

    /**
     * Sets the value of 'commandList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param commandList the Vector to set.
     */
    public void setCommandCollection(final List<Command> commandList) {
        this.commandList = commandList == null? new ArrayList<>() : commandList;
    }

    /**
     * Sets the value of field 'header'.
     * 
     * @param header the value of field 'header'.
     */
    public void setHeader(final Header header) {
        this.header = header;
    }

}
