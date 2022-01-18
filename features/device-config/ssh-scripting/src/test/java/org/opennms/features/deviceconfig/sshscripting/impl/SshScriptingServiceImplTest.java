/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
