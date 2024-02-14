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
package org.opennms.netmgt.provision.service.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.provision.service.ProvisionService;

public abstract class ImportOperation {
    private static final Logger LOG = LoggerFactory.getLogger(ImportOperation.class);
    
    private final ProvisionService m_provisionService;

    /**
     *  Enum to differentiate the type of import operation.
     */
    public static enum OperationType {

        INSERT,
        UPDATE,
        DELETE;
    }
    /**
     * <p>Constructor for ImportOperation.</p>
     *
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     */
    public ImportOperation(ProvisionService provisionService) {
        m_provisionService = provisionService;
    }


    /**
     * <p>scan</p>
     */
    public abstract void scan();

    /**
     * <p>getProvisionService</p>
     *
     * @return the provisionService
     */
    protected ProvisionService getProvisionService() {
        return m_provisionService;
    }

    /**
     * <p>doPersist</p>
     */
    protected abstract void doPersist();

    public abstract OperationType getOperationType();


    /**
     * <p>persist</p>
     */
    public void persist() {
    
        final ImportOperation oper = this;
    
        LOG.info("Persist: {}", oper);
    
        doPersist();
    	
    
        LOG.info("Clear cache: {}", this);
    
        // clear the cache to we don't use up all the memory
    	getProvisionService().clearCache();
    }


}
