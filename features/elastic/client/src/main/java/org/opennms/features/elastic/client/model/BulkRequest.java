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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bulk request containing multiple operations.
 */
public class BulkRequest {
    
    private final List<BulkOperation> operations = new ArrayList<>();
    private int retryCount = 0;
    private String refresh = null;
    
    public BulkRequest() {
    }
    
    public BulkRequest add(BulkOperation operation) {
        operations.add(operation);
        return this;
    }
    
    public BulkRequest add(BulkOperation.Type type, String index, String id, Object source) {
        operations.add(new BulkOperation(type, index, id, source));
        return this;
    }
    
    public BulkRequest index(String index, String id, Object source) {
        return add(BulkOperation.index(index, id, source));
    }
    
    public BulkRequest update(String index, String id, Object source) {
        return add(BulkOperation.update(index, id, source));
    }
    
    public BulkRequest delete(String index, String id) {
        return add(BulkOperation.delete(index, id));
    }
    
    public List<BulkOperation> getOperations() {
        return operations;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
    
    public String getRefresh() {
        return refresh;
    }
    
    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }
    
    public int size() {
        return operations.size();
    }
    
    public boolean isEmpty() {
        return operations.isEmpty();
    }
    
    public void clear() {
        operations.clear();
    }
}