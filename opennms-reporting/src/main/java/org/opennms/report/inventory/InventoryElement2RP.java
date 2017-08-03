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
 * Class InventoryElement2RP.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "inventoryElement2RP")
@XmlAccessorType(XmlAccessType.FIELD)
public class InventoryElement2RP implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "inventoryMemoryRP")
    private java.util.List<InventoryMemoryRP> inventoryMemoryRPList;

    @XmlElement(name = "inventorySoftwareRP")
    private java.util.List<InventorySoftwareRP> inventorySoftwareRPList;

    @XmlElement(name = "tupleRP")
    private java.util.List<TupleRP> tupleRPList;

    public InventoryElement2RP() {
        this.inventoryMemoryRPList = new java.util.ArrayList<>();
        this.inventorySoftwareRPList = new java.util.ArrayList<>();
        this.tupleRPList = new java.util.ArrayList<>();
    }

    /**
     * 
     * 
     * @param vInventoryMemoryRP
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addInventoryMemoryRP(final InventoryMemoryRP vInventoryMemoryRP) throws IndexOutOfBoundsException {
        this.inventoryMemoryRPList.add(vInventoryMemoryRP);
    }

    /**
     * 
     * 
     * @param index
     * @param vInventoryMemoryRP
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addInventoryMemoryRP(final int index, final InventoryMemoryRP vInventoryMemoryRP) throws IndexOutOfBoundsException {
        this.inventoryMemoryRPList.add(index, vInventoryMemoryRP);
    }

    /**
     * 
     * 
     * @param vInventorySoftwareRP
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addInventorySoftwareRP(final InventorySoftwareRP vInventorySoftwareRP) throws IndexOutOfBoundsException {
        this.inventorySoftwareRPList.add(vInventorySoftwareRP);
    }

    /**
     * 
     * 
     * @param index
     * @param vInventorySoftwareRP
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addInventorySoftwareRP(final int index, final InventorySoftwareRP vInventorySoftwareRP) throws IndexOutOfBoundsException {
        this.inventorySoftwareRPList.add(index, vInventorySoftwareRP);
    }

    /**
     * 
     * 
     * @param vTupleRP
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addTupleRP(final TupleRP vTupleRP) throws IndexOutOfBoundsException {
        this.tupleRPList.add(vTupleRP);
    }

    /**
     * 
     * 
     * @param index
     * @param vTupleRP
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addTupleRP(final int index, final TupleRP vTupleRP) throws IndexOutOfBoundsException {
        this.tupleRPList.add(index, vTupleRP);
    }

    /**
     * Method enumerateInventoryMemoryRP.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<InventoryMemoryRP> enumerateInventoryMemoryRP() {
        return java.util.Collections.enumeration(this.inventoryMemoryRPList);
    }

    /**
     * Method enumerateInventorySoftwareRP.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<InventorySoftwareRP> enumerateInventorySoftwareRP() {
        return java.util.Collections.enumeration(this.inventorySoftwareRPList);
    }

    /**
     * Method enumerateTupleRP.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<TupleRP> enumerateTupleRP() {
        return java.util.Collections.enumeration(this.tupleRPList);
    }

    /**
     * Method getInventoryMemoryRP.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the InventoryMemoryRP at
     * the given index
     */
    public InventoryMemoryRP getInventoryMemoryRP(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.inventoryMemoryRPList.size()) {
            throw new IndexOutOfBoundsException("getInventoryMemoryRP: Index value '" + index + "' not in range [0.." + (this.inventoryMemoryRPList.size() - 1) + "]");
        }
        
        return (InventoryMemoryRP) inventoryMemoryRPList.get(index);
    }

    /**
     * Method getInventoryMemoryRP.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are changing in
     * another thread, we pass a 0-length Array of the correct type into the API
     * call.  This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    public InventoryMemoryRP[] getInventoryMemoryRP() {
        InventoryMemoryRP[] array = new InventoryMemoryRP[0];
        return (InventoryMemoryRP[]) this.inventoryMemoryRPList.toArray(array);
    }

    /**
     * Method getInventoryMemoryRPCollection.Returns a reference to
     * 'inventoryMemoryRPList'. No type checking is performed on any modifications
     * to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<InventoryMemoryRP> getInventoryMemoryRPCollection() {
        return this.inventoryMemoryRPList;
    }

    /**
     * Method getInventoryMemoryRPCount.
     * 
     * @return the size of this collection
     */
    public int getInventoryMemoryRPCount() {
        return this.inventoryMemoryRPList.size();
    }

    /**
     * Method getInventorySoftwareRP.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the InventorySoftwareRP
     * at the given index
     */
    public InventorySoftwareRP getInventorySoftwareRP(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.inventorySoftwareRPList.size()) {
            throw new IndexOutOfBoundsException("getInventorySoftwareRP: Index value '" + index + "' not in range [0.." + (this.inventorySoftwareRPList.size() - 1) + "]");
        }
        
        return (InventorySoftwareRP) inventorySoftwareRPList.get(index);
    }

    /**
     * Method getInventorySoftwareRP.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are changing in
     * another thread, we pass a 0-length Array of the correct type into the API
     * call.  This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    public InventorySoftwareRP[] getInventorySoftwareRP() {
        InventorySoftwareRP[] array = new InventorySoftwareRP[0];
        return (InventorySoftwareRP[]) this.inventorySoftwareRPList.toArray(array);
    }

    /**
     * Method getInventorySoftwareRPCollection.Returns a reference to
     * 'inventorySoftwareRPList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<InventorySoftwareRP> getInventorySoftwareRPCollection() {
        return this.inventorySoftwareRPList;
    }

    /**
     * Method getInventorySoftwareRPCount.
     * 
     * @return the size of this collection
     */
    public int getInventorySoftwareRPCount() {
        return this.inventorySoftwareRPList.size();
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
     * Method getTupleRP.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the TupleRP at the given
     * index
     */
    public TupleRP getTupleRP(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.tupleRPList.size()) {
            throw new IndexOutOfBoundsException("getTupleRP: Index value '" + index + "' not in range [0.." + (this.tupleRPList.size() - 1) + "]");
        }
        
        return (TupleRP) tupleRPList.get(index);
    }

    /**
     * Method getTupleRP.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public TupleRP[] getTupleRP() {
        TupleRP[] array = new TupleRP[0];
        return (TupleRP[]) this.tupleRPList.toArray(array);
    }

    /**
     * Method getTupleRPCollection.Returns a reference to 'tupleRPList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<TupleRP> getTupleRPCollection() {
        return this.tupleRPList;
    }

    /**
     * Method getTupleRPCount.
     * 
     * @return the size of this collection
     */
    public int getTupleRPCount() {
        return this.tupleRPList.size();
    }

    /**
     * Method iterateInventoryMemoryRP.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<InventoryMemoryRP> iterateInventoryMemoryRP() {
        return this.inventoryMemoryRPList.iterator();
    }

    /**
     * Method iterateInventorySoftwareRP.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<InventorySoftwareRP> iterateInventorySoftwareRP() {
        return this.inventorySoftwareRPList.iterator();
    }

    /**
     * Method iterateTupleRP.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<TupleRP> iterateTupleRP() {
        return this.tupleRPList.iterator();
    }

    /**
     */
    public void removeAllInventoryMemoryRP() {
        this.inventoryMemoryRPList.clear();
    }

    /**
     */
    public void removeAllInventorySoftwareRP() {
        this.inventorySoftwareRPList.clear();
    }

    /**
     */
    public void removeAllTupleRP() {
        this.tupleRPList.clear();
    }

    /**
     * Method removeInventoryMemoryRP.
     * 
     * @param vInventoryMemoryRP
     * @return true if the object was removed from the collection.
     */
    public boolean removeInventoryMemoryRP(final InventoryMemoryRP vInventoryMemoryRP) {
        boolean removed = inventoryMemoryRPList.remove(vInventoryMemoryRP);
        return removed;
    }

    /**
     * Method removeInventoryMemoryRPAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public InventoryMemoryRP removeInventoryMemoryRPAt(final int index) {
        Object obj = this.inventoryMemoryRPList.remove(index);
        return (InventoryMemoryRP) obj;
    }

    /**
     * Method removeInventorySoftwareRP.
     * 
     * @param vInventorySoftwareRP
     * @return true if the object was removed from the collection.
     */
    public boolean removeInventorySoftwareRP(final InventorySoftwareRP vInventorySoftwareRP) {
        boolean removed = inventorySoftwareRPList.remove(vInventorySoftwareRP);
        return removed;
    }

    /**
     * Method removeInventorySoftwareRPAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public InventorySoftwareRP removeInventorySoftwareRPAt(final int index) {
        Object obj = this.inventorySoftwareRPList.remove(index);
        return (InventorySoftwareRP) obj;
    }

    /**
     * Method removeTupleRP.
     * 
     * @param vTupleRP
     * @return true if the object was removed from the collection.
     */
    public boolean removeTupleRP(final TupleRP vTupleRP) {
        boolean removed = tupleRPList.remove(vTupleRP);
        return removed;
    }

    /**
     * Method removeTupleRPAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public TupleRP removeTupleRPAt(final int index) {
        Object obj = this.tupleRPList.remove(index);
        return (TupleRP) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vInventoryMemoryRP
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setInventoryMemoryRP(final int index, final InventoryMemoryRP vInventoryMemoryRP) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.inventoryMemoryRPList.size()) {
            throw new IndexOutOfBoundsException("setInventoryMemoryRP: Index value '" + index + "' not in range [0.." + (this.inventoryMemoryRPList.size() - 1) + "]");
        }
        
        this.inventoryMemoryRPList.set(index, vInventoryMemoryRP);
    }

    /**
     * 
     * 
     * @param vInventoryMemoryRPArray
     */
    public void setInventoryMemoryRP(final InventoryMemoryRP[] vInventoryMemoryRPArray) {
        //-- copy array
        inventoryMemoryRPList.clear();
        
        for (int i = 0; i < vInventoryMemoryRPArray.length; i++) {
                this.inventoryMemoryRPList.add(vInventoryMemoryRPArray[i]);
        }
    }

    /**
     * Sets the value of 'inventoryMemoryRPList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vInventoryMemoryRPList the Vector to copy.
     */
    public void setInventoryMemoryRP(final java.util.List<InventoryMemoryRP> vInventoryMemoryRPList) {
        // copy vector
        this.inventoryMemoryRPList.clear();
        
        this.inventoryMemoryRPList.addAll(vInventoryMemoryRPList);
    }

    /**
     * Sets the value of 'inventoryMemoryRPList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param inventoryMemoryRPList the Vector to set.
     */
    public void setInventoryMemoryRPCollection(final java.util.List<InventoryMemoryRP> inventoryMemoryRPList) {
        this.inventoryMemoryRPList = inventoryMemoryRPList;
    }

    /**
     * 
     * 
     * @param index
     * @param vInventorySoftwareRP
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setInventorySoftwareRP(final int index, final InventorySoftwareRP vInventorySoftwareRP) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.inventorySoftwareRPList.size()) {
            throw new IndexOutOfBoundsException("setInventorySoftwareRP: Index value '" + index + "' not in range [0.." + (this.inventorySoftwareRPList.size() - 1) + "]");
        }
        
        this.inventorySoftwareRPList.set(index, vInventorySoftwareRP);
    }

    /**
     * 
     * 
     * @param vInventorySoftwareRPArray
     */
    public void setInventorySoftwareRP(final InventorySoftwareRP[] vInventorySoftwareRPArray) {
        //-- copy array
        inventorySoftwareRPList.clear();
        
        for (int i = 0; i < vInventorySoftwareRPArray.length; i++) {
                this.inventorySoftwareRPList.add(vInventorySoftwareRPArray[i]);
        }
    }

    /**
     * Sets the value of 'inventorySoftwareRPList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vInventorySoftwareRPList the Vector to copy.
     */
    public void setInventorySoftwareRP(final java.util.List<InventorySoftwareRP> vInventorySoftwareRPList) {
        // copy vector
        this.inventorySoftwareRPList.clear();
        
        this.inventorySoftwareRPList.addAll(vInventorySoftwareRPList);
    }

    /**
     * Sets the value of 'inventorySoftwareRPList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param inventorySoftwareRPList the Vector to set.
     */
    public void setInventorySoftwareRPCollection(final java.util.List<InventorySoftwareRP> inventorySoftwareRPList) {
        this.inventorySoftwareRPList = inventorySoftwareRPList;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * 
     * 
     * @param index
     * @param vTupleRP
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setTupleRP(final int index, final TupleRP vTupleRP) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.tupleRPList.size()) {
            throw new IndexOutOfBoundsException("setTupleRP: Index value '" + index + "' not in range [0.." + (this.tupleRPList.size() - 1) + "]");
        }
        
        this.tupleRPList.set(index, vTupleRP);
    }

    /**
     * 
     * 
     * @param vTupleRPArray
     */
    public void setTupleRP(final TupleRP[] vTupleRPArray) {
        //-- copy array
        tupleRPList.clear();
        
        for (int i = 0; i < vTupleRPArray.length; i++) {
                this.tupleRPList.add(vTupleRPArray[i]);
        }
    }

    /**
     * Sets the value of 'tupleRPList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vTupleRPList the Vector to copy.
     */
    public void setTupleRP(final java.util.List<TupleRP> vTupleRPList) {
        // copy vector
        this.tupleRPList.clear();
        
        this.tupleRPList.addAll(vTupleRPList);
    }

    /**
     * Sets the value of 'tupleRPList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param tupleRPList the Vector to set.
     */
    public void setTupleRPCollection(final java.util.List<TupleRP> tupleRPList) {
        this.tupleRPList = tupleRPList;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof InventoryElement2RP)) {
            return false;
        }
        InventoryElement2RP castOther = (InventoryElement2RP) other;
        return Objects.equals(name, castOther.name)
                && Objects.equals(inventoryMemoryRPList, castOther.inventoryMemoryRPList)
                && Objects.equals(inventorySoftwareRPList, castOther.inventorySoftwareRPList)
                && Objects.equals(tupleRPList, castOther.tupleRPList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, inventoryMemoryRPList, inventorySoftwareRPList, tupleRPList);
    }

}
