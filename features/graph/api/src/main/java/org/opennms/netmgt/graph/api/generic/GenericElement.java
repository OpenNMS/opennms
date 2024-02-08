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
package org.opennms.netmgt.graph.api.generic;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.graph.api.validation.NamespaceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

public abstract class GenericElement {

    private final static Logger LOG = LoggerFactory.getLogger(GenericElement.class);

    protected final Map<String, Object> properties;

   /**
    * All values of properties need to be immutable.
    */
    protected GenericElement(Map<String, Object> properties) {
        Objects.requireNonNull(properties);
        this.properties = ImmutableMap.copyOf(properties);
        new NamespaceValidator().validate(getNamespace());
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
    	protected final NamespaceValidator namespaceValidator = new NamespaceValidator();
    	
        protected GenericElementBuilder() {}
    	
        public T id(String id) {
            property(GenericProperties.ID, id);
        	return (T) this;
        }
        
        public T label(String label){
            property(GenericProperties.LABEL, label);
        	return (T) this;
        }
        
        public T namespace(String namespace) {
            Objects.requireNonNull(namespace, "namespace cannot be null.");
            property(GenericProperties.NAMESPACE, namespace);
        	return (T) this;
        }

        public T property(String name, Object value) {
            if(name == null || value == null) {
                LOG.debug("Property name ({}) or value ({}) is null => ignoring it.", name, value);
                return (T) this;
            }
            // Ensure the namespace is valid before changing it
            if (GenericProperties.NAMESPACE.equals(name)) {
                namespaceValidator.validate((String) value);
            }
            properties.put(name, value);
            return (T) this;
        }
        
        public T properties(Map<String, Object> properties) {
            Objects.requireNonNull(properties, "properties cannot be null");
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                property(entry.getKey(), entry.getValue());
            }
            return (T) this;
        }

        public String getNamespace() {
            return (String) properties.get(GenericProperties.NAMESPACE);
        }

        public String getId() {
            return (String) properties.get(GenericProperties.ID);
        }
    }
}
