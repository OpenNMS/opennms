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

package org.opennms.netmgt.flows.rest.internal.classification;

import java.util.Objects;

import org.opennms.core.utils.IPLike;
import org.opennms.netmgt.flows.classification.error.ErrorContext;
import org.opennms.netmgt.flows.classification.error.ErrorTemplate;
import org.opennms.netmgt.flows.classification.error.Errors;
import org.opennms.netmgt.flows.classification.exception.ClassificationException;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.rest.classification.ClassificationRequestDTO;

import com.google.common.base.Strings;

public class ClassificationRequestDTOValidator {

    static void validate(ClassificationRequestDTO requestDTO) {
        // Verify Protocol
        if (Strings.isNullOrEmpty(requestDTO.getProtocol())) {
            throw new ClassificationException(ErrorContext.Protocol, Errors.RULE_PROTOCOL_IS_REQUIRED);
        }
        if (Protocols.getProtocol(requestDTO.getProtocol()) == null) {
            throw new ClassificationException(ErrorContext.Protocol, Errors.RULE_PROTOCOL_DOES_NOT_EXIST, requestDTO.getProtocol());
        }

        // Verify Dst
        validatePort(ErrorContext.DstPort, requestDTO.getDstPort());
        validateAddress(ErrorContext.DstAddress, requestDTO.getDstAddress());

        // Verify Src
        validatePort(ErrorContext.SrcPort, requestDTO.getSrcPort());
        validateAddress(ErrorContext.SrcAddress, requestDTO.getSrcAddress());

        // Verify Exporter Address
        validateAddress(ErrorContext.ExporterAddress, requestDTO.getExporterAddress());
    }

    private static void validatePort(String errorContext, String portValue) {
        Objects.requireNonNull(errorContext);

        try {
            int port = Integer.parseInt(portValue);
            if (port < Rule.MIN_PORT_VALUE || port > Rule.MAX_PORT_VALUE) {
                throw new ClassificationException(errorContext, Errors.RULE_PORT_VALUE_NOT_IN_RANGE, Rule.MIN_PORT_VALUE, Rule.MAX_PORT_VALUE);
            }
        } catch (NumberFormatException ex) {
            throw new ClassificationException(
                    errorContext,
                    new ErrorTemplate(null, "The provided port {0} is not a valid number."),
                    portValue);
        }
    }

    private static void validateAddress(String errorContext, String address) {
        Objects.requireNonNull(errorContext);

        try {
            // Matches actually verifies the "pattern" before a match against a concrete ip address is performed
            // As we are only interesting in the validation, we just use "8.8.8.8".
            IPLike.matches("8.8.8.8", address);
        } catch (Exception ex) {
            throw new  ClassificationException(errorContext, Errors.RULE_IP_ADDRESS_INVALID, address);
        }
    }

}
