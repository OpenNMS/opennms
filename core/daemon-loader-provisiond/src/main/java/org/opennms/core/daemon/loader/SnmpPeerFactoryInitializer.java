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
package org.opennms.core.daemon.loader;

import org.opennms.netmgt.config.SnmpPeerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Initializes SnmpPeerFactory from within the daemon-loader's classloader.
 * This avoids the MethodInvokingFactoryBean classloader issue in OSGi.
 */
public class SnmpPeerFactoryInitializer implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpPeerFactoryInitializer.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        LOG.info("Initializing SnmpPeerFactory from daemon-loader classloader");
        SnmpPeerFactory.init();
    }

    public SnmpPeerFactory getInstance() {
        return SnmpPeerFactory.getInstance();
    }
}
