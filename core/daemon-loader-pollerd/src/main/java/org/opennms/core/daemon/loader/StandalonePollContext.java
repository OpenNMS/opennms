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

import org.opennms.netmgt.poller.DefaultPollContext;

/**
 * Standalone PollContext that skips AsyncPollingEngine initialization.
 *
 * In standalone daemon containers, resilience4j bundles may not be wired
 * to opennms-services at class-load time. The AsyncPollingEngine (which
 * uses resilience4j Bulkhead) is not needed for local polling — polls
 * execute directly via LocalPollerClient without async bulkhead control.
 */
public class StandalonePollContext extends DefaultPollContext {

    @Override
    public void afterPropertiesSet() {
        // Skip AsyncPollingEngine creation — not needed for standalone polling.
        // The parent creates new AsyncPollingEngine(...) which requires
        // resilience4j-bulkhead, unavailable in the minimal OSGi container.
    }
}
