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
 * a custom resource type
 */

@XmlRootElement(name="resourceType", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"name", "label", "resourceLabel", "persistenceSelectorStrategy", "storageStrategy"})
@ValidateUsing("datacollection-config.xsd")
public class ResourceType implements Serializable {
    private static final long serialVersionUID = 8938114920180120619L;

    /**
     * resource type name
     */
    private String m_name;

    /**
     * resource type label (this is what users see in the webUI)
     */
    private String m_label;

    /**
     * resource label expression (this is what users see in the
     * webUI for each resource of this type)
     */
    private String m_resourceLabel;

    /**
     * Selects a PersistenceSelectorStrategy that decides which
     * data is persisted and which is not.
     */
    private PersistenceSelectorStrategy m_persistenceSelectorStrategy;

    /**
     * Selects a StorageStrategy that decides where data is stored.
     */
    private StorageStrategy m_storageStrategy;


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
        
        if (obj instanceof ResourceType) {
        
            final ResourceType temp = (ResourceType)obj;
            if (m_name != null) {
                if (temp.m_name == null) return false;
                else if (!(m_name.equals(temp.m_name))) 
                    return false;
            }
            else if (temp.m_name != null)
                return false;
            if (m_label != null) {
                if (temp.m_label == null) return false;
                else if (!(m_label.equals(temp.m_label))) 
                    return false;
            }
            else if (temp.m_label != null)
                return false;
            if (m_resourceLabel != null) {
                if (temp.m_resourceLabel == null) return false;
                else if (!(m_resourceLabel.equals(temp.m_resourceLabel))) 
                    return false;
            }
            else if (temp.m_resourceLabel != null)
                return false;
            if (m_persistenceSelectorStrategy != null) {
                if (temp.m_persistenceSelectorStrategy == null) return false;
                else if (!(m_persistenceSelectorStrategy.equals(temp.m_persistenceSelectorStrategy))) 
                    return false;
            }
            else if (temp.m_persistenceSelectorStrategy != null)
                return false;
            if (m_storageStrategy != null) {
                if (temp.m_storageStrategy == null) return false;
                else if (!(m_storageStrategy.equals(temp.m_storageStrategy))) 
                    return false;
            }
            else if (temp.m_storageStrategy != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'label'. The field 'label' has
     * the following description: resource type label (this is what
     * users see in the webUI)
     * 
     * @return the value of field 'Label'.
     */
    @XmlAttribute(name="label", required=true)
    public String getLabel() {
        return m_label;
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the
     * following description: resource type name
     * 
     * @return the value of field 'Name'.
     */
    @XmlAttribute(name="name", required=true)
    public String getName() {
        return m_name;
    }

    /**
     * Returns the value of field 'persistenceSelectorStrategy'.
     * The field 'persistenceSelectorStrategy' has the following
     * description: Selects a PersistenceSelectorStrategy that
     * decides which data is persisted and which is not.
     * 
     * @return the value of field 'PersistenceSelectorStrategy'.
     */
    @XmlElement(name="persistenceSelectorStrategy")
    public PersistenceSelectorStrategy getPersistenceSelectorStrategy() {
        return m_persistenceSelectorStrategy;
    }

    /**
     * Returns the value of field 'resourceLabel'. The field
     * 'resourceLabel' has the following description: resource
     * label expression (this is what users see in the webUI for
     * each resource of this type)
     * 
     * @return the value of field 'ResourceLabel'.
     */
    @XmlAttribute(name="resourceLabel")
    public String getResourceLabel() {
        return m_resourceLabel;
    }

    /**
     * Returns the value of field 'storageStrategy'. The field
     * 'storageStrategy' has the following description: Selects a
     * StorageStrategy that decides where data is stored.
     * 
     * @return the value of field 'StorageStrategy'.
     */
    @XmlElement(name="storageStrategy")
    public StorageStrategy getStorageStrategy() {
        return m_storageStrategy;
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
        if (m_label != null) {
           result = 37 * result + m_label.hashCode();
        }
        if (m_resourceLabel != null) {
           result = 37 * result + m_resourceLabel.hashCode();
        }
        if (m_persistenceSelectorStrategy != null) {
           result = 37 * result + m_persistenceSelectorStrategy.hashCode();
        }
        if (m_storageStrategy != null) {
           result = 37 * result + m_storageStrategy.hashCode();
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
     * 
     * 
     * @param out
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
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
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    @Deprecated
    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     * Sets the value of field 'label'. The field 'label' has the
     * following description: resource type label (this is what
     * users see in the webUI)
     * 
     * @param label the value of field 'label'.
     */
    public void setLabel(final String label) {
        m_label = label.intern();
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the
     * following description: resource type name
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        m_name = name.intern();
    }

    /**
     * Sets the value of field 'persistenceSelectorStrategy'. The
     * field 'persistenceSelectorStrategy' has the following
     * description: Selects a PersistenceSelectorStrategy that
     * decides which data is persisted and which is not.
     * 
     * @param strategy the value of field
     * 'persistenceSelectorStrategy'.
     */
    public void setPersistenceSelectorStrategy(final PersistenceSelectorStrategy strategy) {
        m_persistenceSelectorStrategy = strategy;
    }

    /**
     * Sets the value of field 'resourceLabel'. The field
     * 'resourceLabel' has the following description: resource
     * label expression (this is what users see in the webUI for
     * each resource of this type)
     * 
     * @param resourceLabel the value of field 'resourceLabel'.
     */
    public void setResourceLabel(final String resourceLabel) {
        m_resourceLabel = resourceLabel.intern();
    }

    /**
     * Sets the value of field 'storageStrategy'. The field
     * 'storageStrategy' has the following description: Selects a
     * StorageStrategy that decides where data is stored.
     * 
     * @param strategy the value of field 'storageStrategy'.
     */
    public void setStorageStrategy(final StorageStrategy strategy) {
        m_storageStrategy = strategy;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * ResourceType
     */
    @Deprecated
    public static ResourceType unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (ResourceType) Unmarshaller.unmarshal(ResourceType.class, reader);
    }

    /**
     * 
     * 
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    @Deprecated
    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

}
