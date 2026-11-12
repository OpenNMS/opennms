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
package org.opennms.jicmp.jna;

import java.io.IOException;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * Common base for the JNA-backed {@link NativeDatagramSocket} implementations.
 */
public abstract class AbstractJnaNativeSocket extends NativeDatagramSocket {

    public native String strerror(int errnum);
    public native int setsockopt(int socket, int level, int option_name, Pointer value, int option_len);

    public void allowFragmentation(final int level, final int option_name, final boolean frag) throws IOException {
        final int socket = getSock();
        if (socket < 0) {
            throw new IOException("Invalid socket!");
        }
        final IntByReference dontfragment = new IntByReference(frag == true? 0 : 1);
        try {
            setsockopt(socket, level, option_name, dontfragment.getPointer(), Native.POINTER_SIZE);
        } catch (final LastErrorException e) {
            throw new IOException("setsockopt: " + strerror(e.getErrorCode()));
        }
    }
}
