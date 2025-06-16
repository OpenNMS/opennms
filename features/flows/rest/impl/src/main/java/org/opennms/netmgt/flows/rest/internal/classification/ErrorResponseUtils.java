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
