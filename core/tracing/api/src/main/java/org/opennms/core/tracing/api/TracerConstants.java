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
package org.opennms.core.tracing.api;

public interface TracerConstants {

    // Common tags for RPC.
    public static final String TAG_SYSTEM_ID = "systemId";
    public static final String TAG_LOCATION = "location";
    public static final String TAG_TIMEOUT = "timeout";
    public static final String TAG_RPC_FAILED = "failed";
    public static final String TAG_MESSAGE_SIZE = "messageSize";
    public static final String TAG_SOURCE_ADDRESS = "sourceAddress";
    public static final String TAG_TOPIC = "topic";
    public static final String TAG_THREAD = "thread";
}
