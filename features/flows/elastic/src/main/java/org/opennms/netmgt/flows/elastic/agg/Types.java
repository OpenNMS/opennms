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
package org.opennms.netmgt.flows.elastic.agg;

import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.Host;
import org.opennms.netmgt.flows.processing.ConversationKeyUtils;

/**
 * Type definitions that consolidate the logic used to query and
 * parse entities modeled in Elasticsearch with aggregated flow
 * statistics.
 *
 * Types have the following properties:
 *  key: field name used in grouped by statements to aggregate documents that relate to the same entity
 *  toEntity: a function that converts the string based key to the corresponding entity
 *  getOtherEntity: an instance use to represent "other" quantities for which we don't have a key
 *
 * @author jwhite
 */
public class Types {

    private static final String AGG_TOPK = "TOPK";
    private static final String AGG_TOTAL = "TOTAL";

    public static final ApplicationType APPLICATION = new ApplicationType();
    public static final ConversationType CONVERSATION = new ConversationType();
    public static final HostType HOST = new HostType();
    public static final DscpType DSCP = new DscpType();

    public interface Type<T> {
        String getKey();
        T toEntity(String key);
        T getOtherEntity();
        String getAggregationType();
    }

    public static class ApplicationType implements Type<String> {
        public static final String UNKNOWN_APPLICATION_NAME_DISPLAY = "Unknown";
        public static final String OTHER_APPLICATION_NAME_DISPLAY = "Other";

        public String getKey() {
            return "application";
        }

        public String toEntity(String key) {
            if ("__unknown".equals(key)) {
                return UNKNOWN_APPLICATION_NAME_DISPLAY;
            }
            return key;
        }

        @Override
        public String getAggregationType() {
            return AGG_TOPK;
        }

        @Override
        public String getOtherEntity() {
            return OTHER_APPLICATION_NAME_DISPLAY;
        }
    }

    public static class ConversationType implements Type<Conversation> {
        public static final Conversation OTHER = Conversation.forOther().build();

        public String getKey() {
            return "conversation_key";
        }

        public Conversation toEntity(String conversationKey) {
            return Conversation.from(ConversationKeyUtils.fromJsonString(conversationKey)).build();
        }

        @Override
        public String getAggregationType() {
            return AGG_TOPK;
        }

        @Override
        public Conversation getOtherEntity() {
            return OTHER;
        }
    }

    public static class HostType implements Type<Host> {
        public static final Host OTHER = Host.forOther().build();

        public String getKey() {
            return "host_address";
        }

        public Host toEntity(String host) {
            return Host.from(host).build();
        }

        @Override
        public String getAggregationType() {
            return AGG_TOPK;
        }

        @Override
        public Host getOtherEntity() {
            return OTHER;
        }
    }

    public static class DscpType implements Type<String> {
        public static final String OTHER_DSCP_NAME_DISPLAY = "Other";

        public String getKey() {
            return "dscp";
        }

        public String toEntity(String key) {
            return key;
        }

        @Override
        public String getAggregationType() {
            return AGG_TOTAL;
        }

        @Override
        public String getOtherEntity() {
            return OTHER_DSCP_NAME_DISPLAY;
        }
    }

}
