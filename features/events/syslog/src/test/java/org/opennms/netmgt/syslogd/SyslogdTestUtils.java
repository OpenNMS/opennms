/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

    public static ByteBuffer toByteBuffer(String string) {
        return toByteBuffer(string, StandardCharsets.US_ASCII);
    }

    public static ByteBuffer toByteBuffer(String string, Charset charset) {
        return ByteBuffer.wrap(string.getBytes(charset));
    }
}
