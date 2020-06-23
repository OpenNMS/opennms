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

import java.util.Map;

import javax.ws.rs.core.Response;

import org.opennms.netmgt.flows.classification.csv.CsvImportResult;
import org.opennms.netmgt.flows.classification.error.Error;
import org.opennms.netmgt.flows.rest.classification.CsvImportErrorDTO;
import org.opennms.netmgt.flows.rest.classification.ErrorDTO;

public class ErrorResponseUtils {

    public static Response createResponse(Error error) {
        return Response.status(Response.Status.BAD_REQUEST).entity(convert(error)).build();
    }

    public static Response createResponse(CsvImportResult csvImportResult) {
        return Response.status(Response.Status.BAD_REQUEST).entity(convert(csvImportResult)).build();
    }

    private static CsvImportErrorDTO convert(CsvImportResult importResult) {
        final CsvImportErrorDTO errorDTO = new CsvImportErrorDTO();
        errorDTO.setSuccess(importResult.isSuccess());
        if (importResult.getError() != null) {
            errorDTO.setError(convert(importResult.getError()));
        }
        for (Map.Entry<Long, Error> entry : importResult.getErrorMap().entrySet()) {
            errorDTO.addError(entry.getKey(), convert(entry.getValue()));
        }
        return errorDTO;
    }

    private static ErrorDTO convert(Error error) {
        final ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setKey(error.getTemplate().getKey());
        errorDTO.setContext(error.getContext());
        errorDTO.setMessage(error.getFormattedMessage());
        return errorDTO;
    }

}
