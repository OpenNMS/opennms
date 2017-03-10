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

package org.opennms.netmgt.config.snmpAsset.adapter;

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
 * Class MibObjs.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "mibObjs")
@XmlAccessorType(XmlAccessType.FIELD)
public class MibObjs implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * a MIB object
     */
    @XmlElement(name = "mibObj")
    private List<MibObj> mibObjList = new ArrayList<>();

    /**
     * 
     * 
     * @param vMibObj
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addMibObj(final MibObj vMibObj) throws IndexOutOfBoundsException {
        this.mibObjList.add(vMibObj);
    }

    /**
     * 
     * 
     * @param index
     * @param vMibObj
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addMibObj(final int index, final MibObj vMibObj) throws IndexOutOfBoundsException {
        this.mibObjList.add(index, vMibObj);
    }

    /**
     * Method enumerateMibObj.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<MibObj> enumerateMibObj() {
        return Collections.enumeration(this.mibObjList);
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
        
        if (obj instanceof MibObjs) {
            MibObjs temp = (MibObjs)obj;
            boolean equals = Objects.equals(temp.mibObjList, mibObjList);
            return equals;
        }
        return false;
    }

    /**
     * Method getMibObj.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the MibObj
     * at the given index
     */
    public MibObj getMibObj(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.mibObjList.size()) {
            throw new IndexOutOfBoundsException("getMibObj: Index value '" + index + "' not in range [0.." + (this.mibObjList.size() - 1) + "]");
        }
        
        return (MibObj) mibObjList.get(index);
    }

    /**
     * Method getMibObj.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public MibObj[] getMibObj() {
        MibObj[] array = new MibObj[0];
        return (MibObj[]) this.mibObjList.toArray(array);
    }

    /**
     * Method getMibObjCollection.Returns a reference to 'mibObjList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<MibObj> getMibObjCollection() {
        return this.mibObjList;
    }

    /**
     * Method getMibObjCount.
     * 
     * @return the size of this collection
     */
    public int getMibObjCount() {
        return this.mibObjList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            mibObjList);
        return hash;
    }

    /**
     * Method iterateMibObj.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<MibObj> iterateMibObj() {
        return this.mibObjList.iterator();
    }

    /**
     */
    public void removeAllMibObj() {
        this.mibObjList.clear();
    }

    /**
     * Method removeMibObj.
     * 
     * @param vMibObj
     * @return true if the object was removed from the collection.
     */
    public boolean removeMibObj(final MibObj vMibObj) {
        boolean removed = mibObjList.remove(vMibObj);
        return removed;
    }

    /**
     * Method removeMibObjAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public MibObj removeMibObjAt(final int index) {
        Object obj = this.mibObjList.remove(index);
        return (MibObj) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vMibObj
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setMibObj(final int index, final MibObj vMibObj) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.mibObjList.size()) {
            throw new IndexOutOfBoundsException("setMibObj: Index value '" + index + "' not in range [0.." + (this.mibObjList.size() - 1) + "]");
        }
        
        this.mibObjList.set(index, vMibObj);
    }

    /**
     * 
     * 
     * @param vMibObjArray
     */
    public void setMibObj(final MibObj[] vMibObjArray) {
        //-- copy array
        mibObjList.clear();
        
        for (int i = 0; i < vMibObjArray.length; i++) {
                this.mibObjList.add(vMibObjArray[i]);
        }
    }

    /**
     * Sets the value of 'mibObjList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vMibObjList the Vector to copy.
     */
    public void setMibObj(final List<MibObj> vMibObjList) {
        // copy vector
        this.mibObjList.clear();
        
        this.mibObjList.addAll(vMibObjList);
    }

    /**
     * Sets the value of 'mibObjList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param mibObjList the Vector to set.
     */
    public void setMibObjCollection(final List<MibObj> mibObjList) {
        this.mibObjList = mibObjList;
    }

}
