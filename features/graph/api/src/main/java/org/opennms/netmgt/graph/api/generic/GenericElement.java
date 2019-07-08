/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.api.generic;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

public abstract class GenericElement {
    protected final Map<String, Object> properties;

   /**
    * All values of properties need to be immutable.
    */
    protected GenericElement (Map<String, Object> properties) {
        Objects.requireNonNull(properties);
        this.properties = ImmutableMap.copyOf(properties);
        Objects.requireNonNull(getNamespace(), "namespace cannot be null");
    }

    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }

    public <T> T getProperty(String key, T defaultValue) {
        return (T) properties.getOrDefault(key, defaultValue);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public String getNamespace() {
        return getProperty(GenericProperties.NAMESPACE);
    }

    public String getLabel(){
        return getProperty(GenericProperties.LABEL);
    }

    public String getId() {
        return getProperty(GenericProperties.ID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final GenericElement that = (GenericElement) o;
        return Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(properties);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("properties", properties)
                .toString();
    }
    
    public static abstract class GenericElementBuilder<T extends GenericElementBuilder> {
    	protected final Map<String, Object> properties = new HashMap<>();
    	
        protected GenericElementBuilder() {}
    	
        public T id(String id) {
        	properties.put(GenericProperties.ID, id);
        	return (T) this;
        }
        
        public T label(String label){
        	properties.put(GenericProperties.LABEL, label);
        	return (T) this;
        }
        
        public T namespace(String namespace){
        	properties.put(GenericProperties.NAMESPACE, namespace);
        	return (T) this;
        }
        
        public T property(String name, Object value){
            properties.put(name, value);
            return (T) this;
        }
        
        public T properties(Map<String, Object> properties){
            this.properties.putAll(properties);
            return (T) this;
        }
    }
}
