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
package org.opennms.smoketest.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.opennms.smoketest.utils.DevDebugUtils.CONTAINER_HOST_M2_SYS_PROP;

import java.util.concurrent.Callable;

import org.junit.Test;

public class DevDebugUtilsTest {

    @Test
    public void canConvertToContainerAccessibleUrl() {
        withClearedContainerHost(() -> {
            assertThat(DevDebugUtils.convertToContainerAccessibleUrl("http://127.0.0.1:39995/path?query=x", "opennms", 8980),
                    equalTo("http://opennms:8980/path?query=x"));
            return null;
        });

        withClearedContainerHost(() -> {
            System.setProperty(CONTAINER_HOST_M2_SYS_PROP, "beer");
            assertThat(DevDebugUtils.convertToContainerAccessibleUrl("http://127.0.0.1:39995/path?query=x", "opennms", 8980),
                    equalTo("http://beer:39995/path?query=x"));
            return null;
        });
    }

    private static void withClearedContainerHost(Callable<Void> callable) {
        final String existingContainerHost = System.getProperty(CONTAINER_HOST_M2_SYS_PROP);
        try {
            System.clearProperty(CONTAINER_HOST_M2_SYS_PROP);
            callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (existingContainerHost != null) {
                System.setProperty(CONTAINER_HOST_M2_SYS_PROP, existingContainerHost);
            }
        }
    }
}
