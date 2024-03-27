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
package org.opennms.netmgt.ticketer.jira.fieldmapper;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *  Wrapper class to help splitting string values, separated by "," and map each split value to its
 *  according (custom) jira field value.
 *
 *  E.g. "label1,label2" becomes ["label1", "label2"], or "label1,label" becomes [{"name":"label1"}, {"name":"label2"}]"
 */
public class ArrayWrapper {

    public Object map(Function<String, Object> itemFunction, String attributeValue) {
        return Arrays.stream(attributeValue.split(","))
                .map(itemFunction::apply)
                .collect(Collectors.toList());
    }
}