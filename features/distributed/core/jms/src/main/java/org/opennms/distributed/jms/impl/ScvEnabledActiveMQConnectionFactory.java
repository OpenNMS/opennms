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
package org.opennms.distributed.jms.impl;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ActiveMQ component with support for retrieving credentials from  SCV. 
 *
 * @author jwhite
 */
public class ScvEnabledActiveMQConnectionFactory extends ActiveMQConnectionFactory {
    public static final Logger LOG = LoggerFactory.getLogger(ScvEnabledActiveMQConnectionFactory.class);

    public ScvEnabledActiveMQConnectionFactory(String brokerUrl, SecureCredentialsVault scv, String scvAlias) {
        this.setBrokerURL(brokerUrl);
        final Credentials amqCredentials = scv.getCredentials(scvAlias);
        if (amqCredentials == null) {
            LOG.warn("No credentials found in SCV for alias '{}'. Using default credentials.", scvAlias);
            setUserName("admin");
            setPassword("admin");
        } else {
            setUserName(amqCredentials.getUsername());
            setPassword(amqCredentials.getPassword());
        }
    }
}
