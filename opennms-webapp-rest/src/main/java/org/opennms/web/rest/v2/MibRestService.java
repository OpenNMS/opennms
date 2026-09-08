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

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.opennms.web.rest.v2.api.MibRestApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class MibRestService implements MibRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(MibRestService.class);

    @Autowired
    private MibFileService mibFileService;

    void setMibFileService(MibFileService mibFileService) {
        this.mibFileService = mibFileService;
    }

    @Override
    public Response listMibFiles() {
        final Map<String, Object> response = new LinkedHashMap<>();
        response.put(MibFileService.PENDING, mibFileService.listMibFiles(MibFileService.PENDING));
        response.put(MibFileService.COMPILED, mibFileService.listMibFiles(MibFileService.COMPILED));
        return Response.ok(response).build();
    }

    @Override
    public Response uploadMibFiles(final List<Attachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "No files provided in the 'upload' part"))
                    .build();
        }
        final List<Map<String, Object>> successList = new ArrayList<>();
        final List<Map<String, Object>> errorList = new ArrayList<>();
        for (final Attachment attachment : attachments) {
            final String fileName = attachment.getContentDisposition() == null
                    ? null
                    : attachment.getContentDisposition().getParameter("filename");
            try (InputStream stream = attachment.getDataHandler().getInputStream()) {
                mibFileService.saveUpload(fileName, stream);
                final Map<String, Object> success = new HashMap<>();
                success.put("file", fileName);
                successList.add(success);
            } catch (Exception e) {
                LOG.warn("Failed to upload MIB file {}", fileName, e);
                final Map<String, Object> error = new HashMap<>();
                error.put("file", fileName);
                error.put("error", e.getMessage());
                errorList.add(error);
            }
        }
        final Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", successList);
        response.put("errors", errorList);
        return Response.ok(response).build();
    }

    @Override
    public Response getMibFileContent(final String dir, final String name) {
        try {
            return Response.ok(mibFileService.readMibFile(dir, name)).build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @Override
    public Response updatePendingMibFile(final String name, final String content) {
        try {
            mibFileService.updatePendingMibFile(name, content);
            return Response.ok(Map.of("file", name)).build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @Override
    public Response deleteMibFile(final String dir, final String name) {
        try {
            mibFileService.deleteMibFile(dir, name);
            return Response.noContent().build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @Override
    public Response compileMibFile(final String name, final boolean overwrite) {
        try {
            return Response.ok(mibFileService.compile(name, overwrite)).build();
        } catch (MibFileService.MibExistsException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("mibName", e.getMibName(), "targetFile", e.getTargetFile(), "error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @Override
    public Response generateEvents(final String name, final String ueiBase) {
        try {
            return Response.ok(mibFileService.generateEvents(name, ueiBase)).build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @Override
    public Response generateDataCollection(final String name) {
        try {
            return Response.ok(mibFileService.generateDataCollection(name)).build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    @Override
    public Response generateGraphTemplates(final String name, final boolean dryRun) {
        try {
            return Response.ok(mibFileService.generateGraphTemplates(name, dryRun)).build();
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    private Response errorResponse(final Exception e) {
        final Response.Status status;
        if (e instanceof FileNotFoundException) {
            status = Response.Status.NOT_FOUND;
        } else if (e instanceof IllegalArgumentException) {
            status = Response.Status.BAD_REQUEST;
        } else {
            LOG.error("MIB compiler operation failed", e);
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }
        // force JSON: endpoints that produce text/plain would otherwise have no
        // message body writer for the error map and turn this into a 500
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()))
                .build();
    }
}
