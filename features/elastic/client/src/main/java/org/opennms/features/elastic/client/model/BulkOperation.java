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
package org.opennms.features.elastic.client.model;

/**
 * Represents a single operation in a bulk request.
 */
public class BulkOperation {
    
    public enum Type {
        INDEX, UPDATE, DELETE
    }
    
    private final Type type;
    private final String index;
    private final String id;
    private final Object source;
    
    public BulkOperation(Type type, String index, String id, Object source) {
        this.type = type;
        this.index = index;
        this.id = id;
        this.source = source;
    }
    
    public static BulkOperation index(String index, String id, Object source) {
        return new BulkOperation(Type.INDEX, index, id, source);
    }
    
    public static BulkOperation update(String index, String id, Object source) {
        return new BulkOperation(Type.UPDATE, index, id, source);
    }
    
    public static BulkOperation delete(String index, String id) {
        return new BulkOperation(Type.DELETE, index, id, null);
    }
    
    public Type getType() {
        return type;
    }
    
    public String getIndex() {
        return index;
    }
    
    public String getId() {
        return id;
    }
    
    public Object getSource() {
        return source;
    }
}