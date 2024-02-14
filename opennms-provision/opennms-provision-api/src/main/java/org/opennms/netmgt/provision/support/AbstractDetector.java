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
package org.opennms.netmgt.provision.support;

import org.opennms.netmgt.provision.ServiceDetector;

/**
 * <p>AbstractDetector class.</p>
 *
 * @author ranger
 */
public abstract class AbstractDetector implements ServiceDetector {
    
    private static final int DEFAULT_TIMEOUT = 2000;
    private static final int DEFAULT_RETRIES = 1;
    private int m_port;
    private int m_retries;
    private int m_timeout;
    private String m_ipMatch;
    private String m_serviceName;

    /**
     * <p>Constructor for AbstractDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     */
    protected AbstractDetector(final String serviceName, final int port, final int timeout, final int retries) {
        m_serviceName = serviceName;
        m_port = port;
        m_timeout = timeout;
        m_retries = retries;
    }

    /**
     * <p>Constructor for AbstractDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    protected AbstractDetector(final String serviceName, final int port) {
        this(serviceName, port, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
    }

    /**
     * <p>init</p>
     */
    @Override
    public final void init() {
        if (m_serviceName == null || m_timeout < -1) {
            throw new IllegalStateException(String.format("ServiceName is null or timeout of %d is invalid. Timeout must be > -1", m_timeout));
        }
        onInit();
    }
    
    /**
     * <p>onInit</p>
     */
    protected abstract void onInit();
    
    /**
     * <p>dispose</p>
     */
    @Override
    public abstract void dispose();
    
    /**
     * <p>setPort</p>
     *
     * @param port a int.
     */
    @Override
    public final void setPort(final int port) {
        m_port = port;
    }

    /**
     * <p>getPort</p>
     *
     * @return a int.
     */
    @Override
    public final int getPort() {
        return m_port;
    }

    /**
     * <p>setRetries</p>
     *
     * @param retries a int.
     */
    public void setRetries(final int retries) {
        m_retries = retries;
    }

    /**
     * <p>getRetries</p>
     *
     * @return a int.
     */
    public int getRetries() {
        return m_retries;
    }

    /**
     * <p>setTimeout</p>
     *
     * @param timeout a int.
     */
    @Override
    public final void setTimeout(final int timeout) {
        m_timeout = timeout;
    }

    /**
     * <p>getTimeout</p>
     *
     * @return a int.
     */
    @Override
    public final int getTimeout() {
        return m_timeout;
    }

    /** {@inheritDoc} */
    @Override
    public final void setServiceName(final String serviceName) {
        m_serviceName = serviceName;
    }

    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public final String getServiceName() {
        return m_serviceName;
    }

    /**
     * <p>getIpMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getIpMatch() {
        return m_ipMatch;
    }

    /** {@inheritDoc} */
    @Override
    public void setIpMatch(final String ipMatch) {
        m_ipMatch = ipMatch;
    }

}
