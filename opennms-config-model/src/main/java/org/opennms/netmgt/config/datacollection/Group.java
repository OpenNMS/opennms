/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.datacollection;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

/**
 * a MIB object group
 */

@XmlRootElement(name="group", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"name", "ifType", "mibObj", "includeGroup"})
@ValidateUsing("datacollection-config.xsd")
public class Group implements Serializable {
    private static final long serialVersionUID = 8424897884942605338L;

    private static final MibObj[] EMPTY_MIBOBJ_ARRAY = new MibObj[0];
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * group name
     */
    private String m_name;

    /**
     * Interface type. Indicates the interface types from
     *  which the groups MIB objects are to be collected. Supports
     *  individual ifType values or comma-separated list of ifType
     * values in
     *  addition to "all" and "ignore" key words. For example: "6"
     * indicates
     *  that OIDs from this MIB group are to be collected only for
     * ethernet
     *  interfaces (ifType = 6) "6,22" indicates that OIDs from
     * this MIB
     *  group are to be collected only for ethernet and serial
     * interfaces
     *  "all" indicates that the OIDs from this MIB group are to be
     *  collected for all interfaces regardless of ifType "ignore"
     * indicates
     *  that OIDs from this MIB group are node-level objects.
     * Sample ifType
     *  descriptions/values: (Refer to
     *  http://www.iana.org/assignments/ianaiftype-mib for a
     * comprehensive
     *  list.); ethernetCsmacd 6; iso8825TokenRing 9; fddi 15; sdlc
     * 17;
     *  basicISDN 20; primaryISDN 21; propPointToPointSerial 22;
     * ppp 23; atm
     *  37; sonet 39; opticalChannel 195
     */
    private String m_ifType;

    /**
     * a MIB object
     */
    private List<MibObj> m_mibObjects = new ArrayList<MibObj>();

    /**
     * sub group
     */
    private List<String> m_includeGroups = new ArrayList<String>();

    public Group() {
        super();
    }

    public Group(final String name) {
        super();
        m_name = name;
    }

    /**
     * 
     * 
     * @param includeGroup
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeGroup(final String includeGroup) throws IndexOutOfBoundsException {
        m_includeGroups.add(includeGroup);
    }

    /**
     * 
     * 
     * @param index
     * @param includeGroup
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeGroup(final int index, final String includeGroup) throws IndexOutOfBoundsException {
        m_includeGroups.add(index, includeGroup.intern());
    }

    /**
     * 
     * 
     * @param mibObj
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMibObj(final MibObj mibObj) throws IndexOutOfBoundsException {
        m_mibObjects.add(mibObj);
    }

    /**
     * 
     * 
     * @param index
     * @param mibObj
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMibObj(final int index, final MibObj mibObj) throws IndexOutOfBoundsException {
        m_mibObjects.add(index, mibObj);
    }

    /**
     * Method enumerateIncludeGroup.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<String> enumerateIncludeGroup() {
        return Collections.enumeration(m_includeGroups);
    }

    /**
     * Method enumerateMibObj.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<MibObj> enumerateMibObj() {
        return Collections.enumeration(m_mibObjects);
    }

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;
        
        if (obj instanceof Group) {
        
            final Group temp = (Group)obj;
            if (m_name != null) {
                if (temp.m_name == null) return false;
                else if (!(m_name.equals(temp.m_name))) 
                    return false;
            }
            else if (temp.m_name != null)
                return false;
            if (m_ifType != null) {
                if (temp.m_ifType == null) return false;
                else if (!(m_ifType.equals(temp.m_ifType))) 
                    return false;
            }
            else if (temp.m_ifType != null)
                return false;
            if (m_mibObjects != null) {
                if (temp.m_mibObjects == null) return false;
                else if (!(m_mibObjects.equals(temp.m_mibObjects))) 
                    return false;
            }
            else if (temp.m_mibObjects != null)
                return false;
            if (m_includeGroups != null) {
                if (temp.m_includeGroups == null) return false;
                else if (!(m_includeGroups.equals(temp.m_includeGroups))) 
                    return false;
            }
            else if (temp.m_includeGroups != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'ifType'. The field 'ifType' has
     * the following description: Interface type. Indicates the
     * interface types from
     *  which the groups MIB objects are to be collected. Supports
     *  individual ifType values or comma-separated list of ifType
     * values in
     *  addition to "all" and "ignore" key words. For example: "6"
     * indicates
     *  that OIDs from this MIB group are to be collected only for
     * ethernet
     *  interfaces (ifType = 6) "6,22" indicates that OIDs from
     * this MIB
     *  group are to be collected only for ethernet and serial
     * interfaces
     *  "all" indicates that the OIDs from this MIB group are to be
     *  collected for all interfaces regardless of ifType "ignore"
     * indicates
     *  that OIDs from this MIB group are node-level objects.
     * Sample ifType
     *  descriptions/values: (Refer to
     *  http://www.iana.org/assignments/ianaiftype-mib for a
     * comprehensive
     *  list.); ethernetCsmacd 6; iso8825TokenRing 9; fddi 15; sdlc
     * 17;
     *  basicISDN 20; primaryISDN 21; propPointToPointSerial 22;
     * ppp 23; atm
     *  37; sonet 39; opticalChannel 195
     * 
     * @return the value of field 'IfType'.
     */
    @XmlAttribute(name="ifType", required=true)
    public String getIfType() {
        return m_ifType;
    }

    /**
     * Method getIncludeGroup.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getIncludeGroup(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_includeGroups.size()) {
            throw new IndexOutOfBoundsException("getIncludeGroup: Index value '" + index + "' not in range [0.." + (m_includeGroups.size() - 1) + "]");
        }
        return m_includeGroups.get(index);
    }

    /**
     * Method getIncludeGroup.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    @XmlElement(name="includeGroup")
    public String[] getIncludeGroup() {
        return m_includeGroups.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Method getIncludeGroupCollection.Returns a reference to
     * '_includeGroupList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getIncludeGroupCollection() {
        return m_includeGroups;
    }

    /**
     * Method getIncludeGroupCount.
     * 
     * @return the size of this collection
     */
    public int getIncludeGroupCount() {
        return m_includeGroups.size();
    }

    /**
     * Method getMibObj.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * MibObj at the
     * given index
     */
    public MibObj getMibObj(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_mibObjects.size()) {
            throw new IndexOutOfBoundsException("getMibObj: Index value '" + index + "' not in range [0.." + (m_mibObjects.size() - 1) + "]");
        }
        return m_mibObjects.get(index);
    }

    /**
     * Method getMibObj.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    @XmlElement(name="mibObj")
    public MibObj[] getMibObj() {
        return m_mibObjects.toArray(EMPTY_MIBOBJ_ARRAY);
    }

    /**
     * Method getMibObjCollection.Returns a reference to
     * '_mibObjList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<MibObj> getMibObjCollection() {
        return m_mibObjects;
    }

    /**
     * Method getMibObjCount.
     * 
     * @return the size of this collection
     */
    public int getMibObjCount() {
        return m_mibObjects.size();
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the
     * following description: group name
     * 
     * @return the value of field 'Name'.
     */
    @XmlAttribute(name="name", required=true)
    public String getName() {
        return m_name;
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;
        
        if (m_name != null) {
           result = 37 * result + m_name.hashCode();
        }
        if (m_ifType != null) {
           result = 37 * result + m_ifType.hashCode();
        }
        if (m_mibObjects != null) {
           result = 37 * result + m_mibObjects.hashCode();
        }
        if (m_includeGroups != null) {
           result = 37 * result + m_includeGroups.hashCode();
        }
        
        return result;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    @Deprecated
    public boolean isValid() {
        try {
            validate();
        } catch (final ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * Method iterateIncludeGroup.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<String> iterateIncludeGroup() {
        return m_includeGroups.iterator();
    }

    /**
     * Method iterateMibObj.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<MibObj> iterateMibObj() {
        return m_mibObjects.iterator();
    }

    /**
     * 
     * 
     * @param out
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    @Deprecated
    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws java.io.IOException if an IOException occurs during
     * marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    @Deprecated
    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    public void removeAllIncludeGroup() {
        m_includeGroups.clear();
    }

    public void removeAllMibObj() {
        m_mibObjects.clear();
    }

    /**
     * Method removeIncludeGroup.
     * 
     * @param includeGroup
     * @return true if the object was removed from the collection.
     */
    public boolean removeIncludeGroup(final String includeGroup) {
        return m_includeGroups.remove(includeGroup);
    }

    /**
     * Method removeIncludeGroupAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeIncludeGroupAt(final int index) {
        return m_includeGroups.remove(index);
    }

    /**
     * Method removeMibObj.
     * 
     * @param mibObj
     * @return true if the object was removed from the collection.
     */
    public boolean removeMibObj(final MibObj mibObj) {
        return m_mibObjects.remove(mibObj);
    }

    /**
     * Method removeMibObjAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public MibObj removeMibObjAt(final int index) {
        return m_mibObjects.remove(index);
    }

    /**
     * Sets the value of field 'ifType'. The field 'ifType' has the
     * following description: Interface type. Indicates the
     * interface types from
     *  which the groups MIB objects are to be collected. Supports
     *  individual ifType values or comma-separated list of ifType
     * values in
     *  addition to "all" and "ignore" key words. For example: "6"
     * indicates
     *  that OIDs from this MIB group are to be collected only for
     * ethernet
     *  interfaces (ifType = 6) "6,22" indicates that OIDs from
     * this MIB
     *  group are to be collected only for ethernet and serial
     * interfaces
     *  "all" indicates that the OIDs from this MIB group are to be
     *  collected for all interfaces regardless of ifType "ignore"
     * indicates
     *  that OIDs from this MIB group are node-level objects.
     * Sample ifType
     *  descriptions/values: (Refer to
     *  http://www.iana.org/assignments/ianaiftype-mib for a
     * comprehensive
     *  list.); ethernetCsmacd 6; iso8825TokenRing 9; fddi 15; sdlc
     * 17;
     *  basicISDN 20; primaryISDN 21; propPointToPointSerial 22;
     * ppp 23; atm
     *  37; sonet 39; opticalChannel 195
     * 
     * @param ifType the value of field 'ifType'.
     */
    public void setIfType(final String ifType) {
        m_ifType = ifType.intern();
    }

    /**
     * 
     * 
     * @param index
     * @param includeGroup
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setIncludeGroup(final int index, final String includeGroup) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_includeGroups.size()) {
            throw new IndexOutOfBoundsException("setIncludeGroup: Index value '" + index + "' not in range [0.." + (m_includeGroups.size() - 1) + "]");
        }
        m_includeGroups.set(index, includeGroup.intern());
    }

    /**
     * 
     * 
     * @param includeGroups
     */
    public void setIncludeGroup(final String[] includeGroups) {
        m_includeGroups.clear();
        for (int i = 0; i < includeGroups.length; i++) {
                m_includeGroups.add(includeGroups[i].intern());
        }
    }

    /**
     * Sets the value of '_includeGroupList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param includeGroups the Vector to copy.
     */
    public void setIncludeGroup(final List<String> includeGroups) {
        m_includeGroups.clear();
        for (final String includeGroup : includeGroups) {
            m_includeGroups.add(includeGroup.intern());
        }
    }

    /**
     * Sets the value of '_includeGroupList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param includeGroups the Vector to set.
     */
    public void setIncludeGroupCollection(final List<String> includeGroups) {
        for (int i = 0; i < includeGroups.size(); i++) {
            includeGroups.set(i, includeGroups.get(i).intern());
        }
        m_includeGroups = includeGroups;
    }

    /**
     * 
     * 
     * @param index
     * @param mibObj
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setMibObj(final int index, final MibObj mibObj) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_mibObjects.size()) {
            throw new IndexOutOfBoundsException("setMibObj: Index value '" + index + "' not in range [0.." + (m_mibObjects.size() - 1) + "]");
        }
        m_mibObjects.set(index, mibObj);
    }

    /**
     * 
     * 
     * @param mibObjs
     */
    public void setMibObj(final MibObj[] mibObjs) {
        m_mibObjects.clear();
        for (int i = 0; i < mibObjs.length; i++) {
                m_mibObjects.add(mibObjs[i]);
        }
    }

    /**
     * Sets the value of '_mibObjList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param mibObjs the Vector to copy.
     */
    public void setMibObj(final List<MibObj> mibObjs) {
        m_mibObjects.clear();
        m_mibObjects.addAll(mibObjs);
    }

    /**
     * Sets the value of '_mibObjList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param mibObjs the Vector to set.
     */
    public void setMibObjCollection(final List<MibObj> mibObjs) {
        m_mibObjects = mibObjs;
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the
     * following description: group name
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        m_name = name.intern();
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * Group
     */
    @Deprecated
    public static Group unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Group) Unmarshaller.unmarshal(Group.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    @Deprecated
    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

}
