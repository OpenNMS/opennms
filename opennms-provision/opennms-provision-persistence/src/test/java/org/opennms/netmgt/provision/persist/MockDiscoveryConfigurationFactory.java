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
package org.opennms.netmgt.provision.persist;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.config.api.DiscoveryConfigurationFactory;
import org.opennms.netmgt.config.discovery.Detector;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.model.discovery.IPPollAddress;
import org.opennms.netmgt.model.discovery.IPPollRange;

public class MockDiscoveryConfigurationFactory implements DiscoveryConfigurationFactory {

    public MockDiscoveryConfigurationFactory() {
    }

    @Override
    public DiscoveryConfiguration getConfiguration() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<IPPollAddress> getURLSpecifics() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<IPPollRange> getRanges() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<IPPollAddress> getSpecifics() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public boolean isExcluded(InetAddress address, String location) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public String getForeignSource(InetAddress address) {
        return null;
    }

    @Override
    public long getIntraPacketDelay() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public double getPacketsPerSecond() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Iterator<IPPollAddress> getExcludingIterator(Iterator<IPPollAddress> it) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Iterable<IPPollAddress> getConfiguredAddresses() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<Detector> getListOfDetectors(InetAddress inetAddress, String location) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public long getRestartSleepTime() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public long getInitialSleepTime() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
