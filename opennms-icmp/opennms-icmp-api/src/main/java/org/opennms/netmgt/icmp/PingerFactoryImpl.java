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
package org.opennms.netmgt.icmp;

public class PingerFactoryImpl extends AbstractPingerFactory {
    @Override
    public Class<? extends Pinger> getPingerClass() {
        final String pingerClassName = System.getProperty("org.opennms.netmgt.icmp.pingerClass", "org.opennms.netmgt.icmp.best.BestMatchPinger");

        // If the default (0) DSCP pinger has already been initialized, use the
        // same class in case it's been manually overridden with a setInstance()
        // call (ie, in the Remote Poller)
        final Pinger defaultPinger = m_pingers.getIfPresent(1);
        if (defaultPinger != null) {
            return defaultPinger.getClass();
        }

        try {
            return Class.forName(pingerClassName).asSubclass(Pinger.class);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Unable to locate pinger class " + pingerClassName);
        }
    }

}
