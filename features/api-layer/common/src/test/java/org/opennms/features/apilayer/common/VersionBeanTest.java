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
package org.opennms.features.apilayer.common;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.Test;
import org.opennms.integration.api.v1.runtime.Version;

public class VersionBeanTest {

    @Test
    public void canParseVersion() {
        Version versionA = new VersionBean("23.0.0-SNAPSHOT");
        assertThat(versionA.getMajor(), equalTo(23));
        assertThat(versionA.getMinor(), equalTo(0));
        assertThat(versionA.getPatch(), equalTo(0));
        assertThat(versionA.isSnapshot(), equalTo(true));

        Version versionB = new VersionBean("24.1.1");
        assertThat(versionB.getMajor(), equalTo(24));
        assertThat(versionB.getMinor(), equalTo(1));
        assertThat(versionB.getPatch(), equalTo(1));
        assertThat(versionB.isSnapshot(), equalTo(false));

        Version versionC = new VersionBean("23.0.0-0.20181003.onms2471.features.integration.api.3");
        assertThat(versionC.getMajor(), equalTo(23));
        assertThat(versionC.getMinor(), equalTo(0));
        assertThat(versionC.getPatch(), equalTo(0));
        assertThat(versionC.isSnapshot(), equalTo(true));

        // Test comparable
        assertThat(versionB, greaterThan(versionA));
        assertThat(versionB, greaterThan(versionC));
        assertThat(versionA, equalTo(versionC));
    }
}
