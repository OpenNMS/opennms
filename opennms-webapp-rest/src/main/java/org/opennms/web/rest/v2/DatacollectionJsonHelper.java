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
package org.opennms.web.rest.v2;

import com.fasterxml.jackson.core.type.TypeReference;
import org.opennms.netmgt.collection.api.Parameter;
import org.opennms.netmgt.config.datacollection.IpList;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.MibObjProperty;

import java.util.List;

/**
 * Delegates to {@link org.opennms.netmgt.config.api.DatacollectionJsonHelper}.
 * Kept for backward compatibility with existing code in opennms-webapp-rest.
 */
public final class DatacollectionJsonHelper {

    private DatacollectionJsonHelper() {
    }

    public static String toJson(Object value) {
        return org.opennms.netmgt.config.api.DatacollectionJsonHelper.toJson(value);
    }

    public static IpList fromJsonToIpList(String json) {
        return org.opennms.netmgt.config.api.DatacollectionJsonHelper.fromJsonToIpList(json);
    }

    public static List<Parameter> fromJsonToParameters(String json) {
        return org.opennms.netmgt.config.api.DatacollectionJsonHelper.fromJsonToParameters(json);
    }

    public static List<MibObj> fromJsonToMibObjs(String json) {
        return org.opennms.netmgt.config.api.DatacollectionJsonHelper.fromJsonToMibObjs(json);
    }

    public static List<MibObjProperty> fromJsonToProperties(String json) {
        return org.opennms.netmgt.config.api.DatacollectionJsonHelper.fromJsonToProperties(json);
    }

    public static <T> T fromJson(final String json, final TypeReference<T> typeRef) {
        return org.opennms.netmgt.config.api.DatacollectionJsonHelper.fromJson(json, typeRef);
    }
}
