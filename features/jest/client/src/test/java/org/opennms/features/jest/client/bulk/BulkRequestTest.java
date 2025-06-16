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
package org.opennms.features.jest.client.bulk;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;

public class BulkRequestTest {

    @Test
    public void testSleepTime() {
        for (int i = 0; i< BulkRequest.SLEEP_TIME.length; i++) {
            assertThat(BulkRequest.SLEEP_TIME[i], Matchers.is(BulkRequest.getSleepTime(i)));
        }
        assertThat(BulkRequest.SLEEP_TIME[BulkRequest.SLEEP_TIME.length - 1], Matchers.is(BulkRequest.getSleepTime(100)));
    }

}
