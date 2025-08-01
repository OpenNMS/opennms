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
package org.opennms.netmgt.graph.domain;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.graph.api.generic.GenericProperties;

public abstract class AbstractDomainElementBuilder<T extends AbstractDomainElementBuilder> {
        protected final Map<String, Object> properties = new HashMap<>();
        
        protected AbstractDomainElementBuilder() {}
        
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
        
        public <V> T property(String name, V value){
            properties.put(name, value);
            return (T) this;
        }
}
