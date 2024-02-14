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
package org.opennms.smoketest.sentinel;


import static org.opennms.smoketest.utils.KarafShellUtils.awaitHealthCheckSucceeded;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.junit.SentinelTests;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Category(SentinelTests.class)
public class SentinelSshIT {

    private static final Logger LOG = LoggerFactory.getLogger(SentinelSshIT.class);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.SENTINEL;

    @Test
    public void testSshHealthOnSentinel(){
        //Test for no exception to occur
        LOG.info("Waiting for Sentinel ssh health check...");
        awaitHealthCheckSucceeded(stack.sentinel());
    }
}
