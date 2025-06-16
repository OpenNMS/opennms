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
package org.opennms.features.deviceconfig.sshscripting.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.opennms.features.deviceconfig.sshscripting.impl.SshScriptingServiceImpl.matchAndConsume;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

public class SshScriptingServiceImplTest {

    @Test
    public void testMatchAndConsume() throws Exception {
        var bytes = new byte[] { 0, 1, 2, 3 };
        {
            var bos = new ByteArrayOutputStream();
            bos.write(bytes);
            assertThat(matchAndConsume(bos, bytes), is(true));
            assertThat(bos.size(), is(0));
        }
        {
            var bos = new ByteArrayOutputStream();
            bos.write(bytes);
            assertThat(matchAndConsume(bos, new byte[] { 0, 1 }), is(true));
            assertThat(bos.size(), is(2));
            assertThat(bos.toByteArray()[0], is((byte)2));
            assertThat(bos.toByteArray()[1], is((byte)3));
        }
        {
            var bos = new ByteArrayOutputStream();
            bos.write(bytes);
            assertThat(matchAndConsume(bos, new byte[] { 1, 2 }), is(true));
            assertThat(bos.size(), is(1));
            assertThat(bos.toByteArray()[0], is((byte)3));
        }
        {
            var bos = new ByteArrayOutputStream();
            bos.write(bytes);
            assertThat(matchAndConsume(bos, new byte[] { 2, 3 }), is(true));
            assertThat(bos.size(), is(0));
        }
        {
            var bos = new ByteArrayOutputStream();
            bos.write(new byte[] { 1, 1, 1, 1, 1 });
            assertThat(matchAndConsume(bos, new byte[] { 1, 1 }), is(true));
            assertThat(bos.size(), is(3));
            assertThat(matchAndConsume(bos, new byte[] { 1, 1 }), is(true));
            assertThat(bos.size(), is(1));
            assertThat(matchAndConsume(bos, new byte[] { 1, 1 }), is(false));
        }
    }
}
