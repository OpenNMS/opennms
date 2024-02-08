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
package org.opennms.netmgt.vmmgr;

import javax.management.ObjectInstance;

import org.opennms.netmgt.config.service.Service;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
class InvokerResult {
    private Service m_service;
    private ObjectInstance m_mbean;
    private Object m_result;
    private Throwable m_throwable;
    
    /**
     * <p>Constructor for InvokerResult.</p>
     *
     * @param service a {@link org.opennms.netmgt.config.service.Service} object.
     * @param mbean a {@link javax.management.ObjectInstance} object.
     * @param result a {@link java.lang.Object} object.
     * @param throwable a {@link java.lang.Throwable} object.
     */
    public InvokerResult(Service service, ObjectInstance mbean, Object result, Throwable throwable) {
        m_service = service;
        m_mbean = mbean;
        m_result = result;
        m_throwable = throwable;
    }
    
    /**
     * <p>getMbean</p>
     *
     * @return a {@link javax.management.ObjectInstance} object.
     */
    public ObjectInstance getMbean() {
        return m_mbean;
    }
    
    /**
     * <p>getResult</p>
     *
     * @return a {@link java.lang.Object} object.
     */
    public Object getResult() {
        return m_result;
    }
    
    /**
     * <p>getThrowable</p>
     *
     * @return a {@link java.lang.Throwable} object.
     */
    public Throwable getThrowable() {
        return m_throwable;
    }

    /**
     * <p>getService</p>
     *
     * @return a {@link org.opennms.netmgt.config.service.Service} object.
     */
    public Service getService() {
        return m_service;
    }

}
