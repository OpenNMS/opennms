/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.alarmd.northbounder.drools;

import java.beans.PropertyEditor;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.springframework.beans.PropertyEditorRegistrySupport;
import org.springframework.context.ApplicationContext;

/**
 * The Class Global.
 */
@XmlRootElement(name="global")
@XmlAccessorType(XmlAccessType.FIELD)
public class Global implements Serializable {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -5037124721934942336L;

    /**
     * The Class SimplePropertyEditorRegistry.
     */
    private static class SimplePropertyEditorRegistry extends PropertyEditorRegistrySupport {
        
        /**
         * Instantiates a new simple property editor registry.
         */
        public SimplePropertyEditorRegistry() {
            registerDefaultEditors();
        }

    }

    /** The m editor registry. */
    @XmlTransient
    private PropertyEditorRegistrySupport m_editorRegistry = new SimplePropertyEditorRegistry();

    /** The name. */
    @XmlAttribute(name="name")
    private String _name;

    /** The type. */
    @XmlAttribute(name="type")
    private String _type;

    /** The value. */
    @XmlAttribute(name="value")
    private String _value;

    /** The reference. */
    @XmlAttribute(name="ref")
    private String _ref;

    /**
     * Instantiates a new global.
     */
    public Global() {}

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return this._name;
    }

    /**
     * Gets the reference.
     *
     * @return the reference
     */
    public String getRef() {
        return this._ref;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return this._type;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return this._value;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(final String name) {
        this._name = name;
    }

    /**
     * Sets the reference.
     *
     * @param ref the new reference
     */
    public void setRef(final String ref) {
        this._ref = ref;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(final String type) {
        this._type = type;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(final String value) {
        this._value = value;
    }

    /**
     * Convert string to.
     *
     * @param <T> the generic type
     * @param value the value
     * @param typeClass the type class
     * @return the t
     */
    public <T> T convertStringTo(String value, Class<T> typeClass) {
        if (typeClass == String.class) {
            return typeClass.cast(value);
        } else {
            try {
                final PropertyEditor editor = getDefaultEditor(typeClass);
                editor.setAsText(value);
                return typeClass.cast(editor.getValue());
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to find a property editor for " + typeClass);
            }
        }
    }

    /**
     * Gets the type class.
     *
     * @return the type class
     */
    public Class<?> getTypeClass() {
        return getType() == null ? Object.class : convertStringTo(getType(), Class.class);
    }

    /**
     * Gets the value as type.
     *
     * @param typeClass the type class
     * @return the value as type
     */
    public Object getValueAsType(Class<?> typeClass) {
        return getValue() == null ? null : convertStringTo(getValue(), typeClass);
    }

    /**
     * Construct value.
     *
     * @param context the context
     * @return the object
     */
    public Object constructValue(final ApplicationContext context) {
        Class<?> typeClass = getTypeClass();

        if (context != null && getRef() != null) {
            return context.getBean(getRef(), typeClass);
        }

        if (getValue() != null){
            return getValueAsType(typeClass);
        }

        throw new IllegalArgumentException("One of either the value or the ref must be specified");
    }

    /**
     * Gets the default editor.
     *
     * @param clazz the clazz
     * @return the default editor
     */
    private PropertyEditor getDefaultEditor(Class<?> clazz) {
        return m_editorRegistry.getDefaultEditor(clazz);
    }

}
