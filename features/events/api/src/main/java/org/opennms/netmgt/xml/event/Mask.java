/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.xml.event;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.opennms.netmgt.events.api.model.IMask;

import java.io.Serializable;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.Hidden;

/**
 * The Mask for event configuration: The mask contains one
 *  or more 'maskelements' which uniquely identify an event. This
 * can only
 *  include elements from the following subset: uei, source, host,
 * snmphost,
 *  nodeid, interface, service, id(SNMP EID), specific, generic,
 *  community
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="mask")
@XmlAccessorType(XmlAccessType.FIELD)
//@ValidateUsing("event.xsd")
public class Mask implements Serializable {
	private static final long serialVersionUID = 6553429078798831778L;

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

	/**
     * The mask element
     */
	@XmlElement(name="maskelement", required=true, nillable = false)
	@Size(min=1)
	@Valid
    private java.util.List<org.opennms.netmgt.xml.event.Maskelement> _maskelementList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Mask() {
        super();
        this._maskelementList = new java.util.ArrayList<>();
    }

    public static Mask copyFrom(IMask source) {
        if (source == null) {
            return null;
        }

        Mask mask = new Mask();
        mask.getMaskelementCollection().addAll(
                source.getMaskelementCollection().stream().map(Maskelement::copyFrom).collect(Collectors.toList()));
        return mask;
    }

      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vMaskelement
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMaskelement(
            final org.opennms.netmgt.xml.event.Maskelement vMaskelement)
    throws java.lang.IndexOutOfBoundsException {
        this._maskelementList.add(vMaskelement);
    }

    /**
     * 
     * 
     * @param index
     * @param vMaskelement
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMaskelement(
            final int index,
            final org.opennms.netmgt.xml.event.Maskelement vMaskelement)
    throws java.lang.IndexOutOfBoundsException {
        this._maskelementList.add(index, vMaskelement);
    }

    /**
     * Method enumerateMaskelement.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.xml.event.Maskelement> enumerateMaskelement(
    ) {
        return java.util.Collections.enumeration(this._maskelementList);
    }

    /**
     * Method getMaskelement.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.xml.event.Maskelement at the given index
     */
    public org.opennms.netmgt.xml.event.Maskelement getMaskelement(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._maskelementList.size()) {
            throw new IndexOutOfBoundsException("getMaskelement: Index value '" + index + "' not in range [0.." + (this._maskelementList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.xml.event.Maskelement) _maskelementList.get(index);
    }

    /**
     * Method getMaskelement.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.xml.event.Maskelement[] getMaskelement(
    ) {
        org.opennms.netmgt.xml.event.Maskelement[] array = new org.opennms.netmgt.xml.event.Maskelement[0];
        return (org.opennms.netmgt.xml.event.Maskelement[]) this._maskelementList.toArray(array);
    }

    /**
     * Method getMaskelementCollection.Returns a reference to
     * '_maskelementList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.xml.event.Maskelement> getMaskelementCollection(
    ) {
        return this._maskelementList;
    }

    /**
     * Method getMaskelementCount.
     * 
     * @return the size of this collection
     */
    public int getMaskelementCount(
    ) {
        return this._maskelementList.size();
    }

    /**
     * Method iterateMaskelement.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.xml.event.Maskelement> iterateMaskelement(
    ) {
        return this._maskelementList.iterator();
    }

    /**
     */
    public void removeAllMaskelement(
    ) {
        this._maskelementList.clear();
    }

    /**
     * Method removeMaskelement.
     * 
     * @param vMaskelement
     * @return true if the object was removed from the collection.
     */
    public boolean removeMaskelement(
            final org.opennms.netmgt.xml.event.Maskelement vMaskelement) {
        return _maskelementList.remove(vMaskelement);
    }

    /**
     * Method removeMaskelementAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.xml.event.Maskelement removeMaskelementAt(
            final int index) {
        return this._maskelementList.remove(index);
    }

    /**
     * 
     * 
     * @deprecated
     * @param index
     * @param vMaskelement
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    @Hidden
    public void setMaskelement(
            final int index,
            final org.opennms.netmgt.xml.event.Maskelement vMaskelement)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._maskelementList.size()) {
            throw new IndexOutOfBoundsException("setMaskelement: Index value '" + index + "' not in range [0.." + (this._maskelementList.size() - 1) + "]");
        }
        
        this._maskelementList.set(index, vMaskelement);
    }

    /**
     * 
     * @deprecated
     * @param vMaskelementArray
     */
    @Hidden
    public void setMaskelement(
            final org.opennms.netmgt.xml.event.Maskelement[] vMaskelementArray) {
        //-- copy array
        _maskelementList.clear();
        
        for (int i = 0; i < vMaskelementArray.length; i++) {
                this._maskelementList.add(vMaskelementArray[i]);
        }
    }

    /**
     * Sets the value of '_maskelementList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vMaskelementList the Vector to copy.
     */
    public void setMaskelement(
            final java.util.List<org.opennms.netmgt.xml.event.Maskelement> vMaskelementList) {
        // copy vector
        this._maskelementList.clear();
        
        this._maskelementList.addAll(vMaskelementList);
    }

    /**
     * Sets the value of '_maskelementList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param maskelementList the Vector to set.
     */
    @Hidden
    public void setMaskelementCollection(
            final java.util.List<org.opennms.netmgt.xml.event.Maskelement> maskelementList) {
        this._maskelementList = maskelementList;
    }

        @Override
    public String toString() {
    	return new OnmsStringBuilder(this).toString();
    }
}
