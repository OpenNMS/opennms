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

import org.opennms.netmgt.flows.classification.persistence.api.Rule;

// Pre-defined ErrorTemplate messages.
public interface Errors {

    ErrorTemplate RULE_NAME_IS_REQUIRED = new ErrorTemplate("rule.name.required", "No name provided. Please provide a name.");
    ErrorTemplate GROUP_DUPLICATE_RULE = new ErrorTemplate("rule.duplicate", "A rule with the same definition already exists");
    ErrorTemplate RULE_NO_DEFINITIONS = new ErrorTemplate("rule.nodefinitions", "You must provide at least one definition for src/dst port, src/dst address, protocol or exporter filter");

    ErrorTemplate RULE_PROTOCOL_DOES_NOT_EXIST = new ErrorTemplate("rule.protocol.doesnotexist", "The defined protocol ''{0}'' does not exist");
    ErrorTemplate RULE_PROTOCOL_IS_REQUIRED = new ErrorTemplate("rule.protocol.required", "Protocol is required");

    ErrorTemplate RULE_PORT_NO_WILDCARD = new ErrorTemplate("rule.port.nowildcard", "Wildcard is not supported as port definition");
    ErrorTemplate RULE_PORT_DEFINITION_NOT_VALID = new ErrorTemplate("rule.port.definition.invalid", "Please provide a valid port definition. Allowed values are numbers between " + Rule.MIN_PORT_VALUE + " and " + Rule.MAX_PORT_VALUE + ". A range can be provided, e.g. \"4000-5000\", multiple values are allowed, e.g. \"80,8080\"");
    ErrorTemplate RULE_PORT_IS_REQUIRED = new ErrorTemplate("rule.port.required", "Please provide a value.");
    ErrorTemplate RULE_PORT_RANGE_BOUNDS_NOT_VALID = new ErrorTemplate("rule.port.range.bounds.invalid", "The first value of the range must be lower than the second value.");
    ErrorTemplate RULE_PORT_VALUE_NOT_IN_RANGE = new ErrorTemplate("rule.port.value.notinrange", "Range must be between {0} and {1}.");

    ErrorTemplate RULE_EXPORTER_FILTER_INVALID = new ErrorTemplate("rule.filter.invalid", "The provided filter ''{0}'' is invalid: {1}");

    ErrorTemplate RULE_IP_ADDRESS_INVALID = new ErrorTemplate("rule.ipaddress.invalid", "The provided IP Address ''{0}'' is not valid.");

    ErrorTemplate CSV_TOO_FEW_COLUMNS = new ErrorTemplate("csv.toofewcolumns", "The provided rule ''{0}'' cannot be parsed. Expected columns {2} but received {3}.");
    ErrorTemplate CSV_IMPORT_FAILED = new ErrorTemplate( "csv.unknown.error", "An unexpected ErrorTemplate occurred while parsing the CSV: {0}.");
}
