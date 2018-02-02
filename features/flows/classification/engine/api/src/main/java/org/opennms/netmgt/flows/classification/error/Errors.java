/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.error;

// Pre-defined error messagegs.
public interface Errors {

    Error RULE_NAME_IS_REQUIRED = new Error(ErrorContext.Name,"rule.name.required", "No name provided. Please provide a name.");
    Error GROUP_DUPLICATE_RULE = new Error(ErrorContext.Name, "rule.duplicate", "A rule with the same definition already exists");
    Error RULE_NO_DEFINITIONS = new Error(ErrorContext.Entity, "rule.nodefinitions", "You must provide at least one definition for port, ip address or protocol");

    Error RULE_PROTOCOL_DOES_NOT_EXIST = new Error(ErrorContext.Protocol, "rule.protocol.doesnotexist", "The defined protocol ''{0}'' does not exist");
    Error RULE_PROTOCOL_IS_REQUIRED = new Error(ErrorContext.Protocol, "rule.protocol.required", "Protocol is required");

    Error RULE_PORT_NO_WILDCARD = new Error(ErrorContext.Port, "rule.port.nowildcard", "Wildcard is not supported as port definition");
    Error RULE_PORT_DEFINITION_NOT_VALID = new Error(ErrorContext.Port, "rule.port.definition.invalid", "Please provide a valid port definition. Allowed values are numbers between 0 and 65536. A range can be provided, e.g. \"4000-5000\", multiple values are allowed, e.g. \"80,8080\"");
    Error RULE_PORT_IS_REQUIRED = new Error(ErrorContext.Port,"rule.port.required", "Please provide a value.");
    Error RULE_PORT_RANGE_BOUNDS_NOT_VALID = new Error(ErrorContext.Port,"rule.port.range.bounds.invalid", "The first value of the range must be lower than the second value.");
    Error RULE_PORT_VALUE_NOT_IN_RANGE = new Error(ErrorContext.Port,"rule.port.value.notinrange", "Range must be between {0} and {1}.");

    Error RULE_IP_ADDRESS_INVALID = new Error(ErrorContext.IpAddress,"rule.ipaddress.invalid", "The provided IP Address ''{0}'' is not valid.");

    Error CSV_TOO_FEW_COLUMNS = new Error(ErrorContext.Entity, "csv.toofewcolumns", "The provided rule ''{0}'' cannot be parsed. Expected columns {2} but received {3}.");
    Error CSV_IMPORT_FAILED = new Error(ErrorContext.Entity, "csv.unknown.error", "An unexpected error occurred while parsing the CSV: {0}.");
}
