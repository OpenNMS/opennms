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
package org.opennms.netmgt.syslogd;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * This class provides utility methods for the Syslogd tests.
 */
public abstract class SyslogdTestUtils {

    public static void startSyslogdGracefully(Syslogd syslogd) {
        syslogd.start();

        /*
         * We MUST sleep for a small period after starting Syslogd
         * so that the SyslogListener thread has time to start and
         * bind to the port. Otherwise, we will get test errors for
         * missing anticipated events, etc.
         */
        try { Thread.sleep(3000); } catch (InterruptedException e) {}
    }
    
    public static void waitForSyslogdToReload() {
        //wait till syslog stops and starts again, no other way to check this
        try { Thread.sleep(6000); } catch (InterruptedException e) {}
    }

    public static ByteBuffer toByteBuffer(String string) {
        return toByteBuffer(string, StandardCharsets.US_ASCII);
    }

    public static ByteBuffer toByteBuffer(String string, Charset charset) {
        return ByteBuffer.wrap(string.getBytes(charset));
    }
}
