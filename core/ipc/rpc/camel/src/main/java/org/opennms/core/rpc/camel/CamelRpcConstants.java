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
package org.opennms.core.rpc.camel;

public interface CamelRpcConstants {
    static final String JMS_QUEUE_PREFIX = "RPC";
    static final String JMS_QUEUE_NAME_HEADER = "JmsQueueName";
    static final String JMS_REPLY_TO_QUEUE_NAME_HEADER = "JmsReplyToQueueName";
    static final String CAMEL_JMS_REQUEST_TIMEOUT_HEADER = "CamelJmsRequestTimeout";
    static final String JMS_SYSTEM_ID_HEADER = "SystemId";
    static final String JMS_TRACING_INFO = "RpcTracingInfo";
}
