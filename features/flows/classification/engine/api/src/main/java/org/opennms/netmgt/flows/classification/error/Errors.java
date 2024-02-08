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
package org.opennms.netmgt.flows.classification.error;

import org.opennms.netmgt.flows.classification.persistence.api.Rule;

// Pre-defined ErrorTemplate messages.
public interface Errors {

    ErrorTemplate GROUP_READ_ONLY = new ErrorTemplate("group.readonly", "The group is read only. It cannot be altered.");
    ErrorTemplate GROUP_DUPLICATE_RULE = new ErrorTemplate("rule.duplicate", "A rule with the same definition already exists");
    ErrorTemplate GROUP_NAME_NOT_UNIQUE = new ErrorTemplate("group.name.duplicate", "A group with name ''{0}'' already exists");
    ErrorTemplate GROUP_NOT_FOUND = new ErrorTemplate("group.not found", "A group with name ''{0}'' does not exist");

    ErrorTemplate RULE_NAME_IS_REQUIRED = new ErrorTemplate("rule.name.required", "No name provided. Please provide a name.");
    ErrorTemplate RULE_NO_DEFINITIONS = new ErrorTemplate("rule.nodefinitions", "You must provide at least one definition for src/dst port, src/dst address, protocol or exporter filter");
    ErrorTemplate RULE_GROUP_IS_REQUIRED = new ErrorTemplate("rule.group.required", "Group is required");

    ErrorTemplate RULE_PROTOCOL_DOES_NOT_EXIST = new ErrorTemplate("rule.protocol.doesnotexist", "The defined protocol ''{0}'' does not exist");
    ErrorTemplate RULE_PROTOCOL_IS_REQUIRED = new ErrorTemplate("rule.protocol.required", "Protocol is required");

    ErrorTemplate RULE_PORT_NO_WILDCARD = new ErrorTemplate("rule.port.nowildcard", "Wildcard is not supported as port definition");
    ErrorTemplate RULE_PORT_DEFINITION_NOT_VALID = new ErrorTemplate("rule.port.definition.invalid", "Please provide a valid port definition. Allowed values are numbers between " + Rule.MIN_PORT_VALUE + " and " + Rule.MAX_PORT_VALUE + ". A range can be provided, e.g. \"4000-5000\", multiple values are allowed, e.g. \"80,8080\"");
    ErrorTemplate RULE_PORT_IS_REQUIRED = new ErrorTemplate("rule.port.required", "Please provide a value.");
    ErrorTemplate RULE_PORT_RANGE_BOUNDS_NOT_VALID = new ErrorTemplate("rule.port.range.bounds.invalid", "The first value of the range must be lower than the second value.");
    ErrorTemplate RULE_PORT_VALUE_NOT_IN_RANGE = new ErrorTemplate("rule.port.value.notinrange", "Range must be between {0} and {1}.");

    ErrorTemplate RULE_EXPORTER_FILTER_INVALID = new ErrorTemplate("rule.filter.invalid", "The provided filter ''{0}'' is invalid: {1}");

    ErrorTemplate RULE_IP_ADDRESS_INVALID = new ErrorTemplate("rule.ipaddress.invalid", "The provided IP Address ''{0}'' is not valid.");
    ErrorTemplate RULE_IP_ADDRESS_RANGE_INVALID = new ErrorTemplate("rule.ipaddress.range.invalid", "The provided IP Address range ''{0}'' is not valid. A valid range expression may be: 10.0.0.0-10.0.0.255");
    ErrorTemplate RULE_IP_ADDRESS_RANGE_BEGIN_END_INVALID = new ErrorTemplate("rule.ipaddress.range.begin-end.invalid", "The beginning of IP Address range ''{0}'' must come before end of IP Address range ''{1}''");
    ErrorTemplate RULE_IP_ADDRESS_RANGE_CIDR_NOT_SUPPORTED = new ErrorTemplate("rule.ipaddress.range.cidr.not-support", "Ranged values may not contain a CIDR expression");
    ErrorTemplate RULE_IP_ADDRESS_INVALID_CIDR_EXPRESSION = new ErrorTemplate("rule.ipaddress.cidr-expression.invalid", "The provided input ''{0}'' is not a valid CIDR expression: ''{1}''");

    ErrorTemplate CSV_TOO_FEW_COLUMNS = new ErrorTemplate("csv.toofewcolumns", "The provided rule ''{0}'' cannot be parsed. Expected columns {2} but received {3}.");
    ErrorTemplate CSV_IMPORT_FAILED = new ErrorTemplate( "csv.unknown.error", "An unexpected ErrorTemplate occurred while parsing the CSV: {0}.");
}
