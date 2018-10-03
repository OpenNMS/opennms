/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.opennms.web.assets.api.AssetLocator;
import org.opennms.web.assets.api.AssetResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

@Component("webAssetsRestService")
@Path("web-assets")
public class WebAssetsRestService extends OnmsRestService {
    private static final Logger LOG = LoggerFactory.getLogger(WebAssetsRestService.class);

    @Autowired
    private AssetLocator m_assetLocator;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/")
    public List<String> listAssets() {
        return new ArrayList<>(m_assetLocator.getAssets());
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("{assetName}")
    public List<AssetResource> getResources(@PathParam("assetName") final String assetName) {
        final Optional<Collection<AssetResource>> resources = m_assetLocator.getResources(assetName);
        if (!resources.isPresent()) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        return new ArrayList<>(resources.get());
    }

    @GET
    @Path("{assetName}.{type}")
    public Response getResource(@PathParam("assetName") final String assetName, @PathParam("type") final String type) {
        InputStream is = null;

        try {
            Optional<InputStream> resourceInputStream = m_assetLocator.open(assetName, type);
            if (!resourceInputStream.isPresent()) {

                final List<String> split = Arrays.asList(assetName.split("-"));
                if (split.size() > 1) {
                    split.remove(split.size() - 1);
                    final String newAssetName = String.join("-", split);
                    final Optional<AssetResource> newResource = m_assetLocator.getResource(newAssetName, type);
                    LOG.debug("{}.{} not found, found {} instead", assetName, type, newResource);
                    if (newResource.isPresent() && newResource.get().getPath().equals(assetName + "." + type)) {
                        resourceInputStream = m_assetLocator.open(newAssetName, type);
                    }
                }
            }

            if (!resourceInputStream.isPresent()) {
                return Response.status(Status.NOT_FOUND).build();
            }

            is = resourceInputStream.get();
            final byte[] bytes = FileCopyUtils.copyToByteArray(is);
            switch(type) {
            // javascript
            case "js":
                return Response.ok(new String(bytes, StandardCharsets.UTF_8)).type("application/javascript").build();
            case "map":
            case "json":
                return streamResponse(bytes, "application/json");
            // styles
            case "css":
                return Response.ok(new String(bytes, StandardCharsets.UTF_8)).type("text/css").build();
            // images
            case "gif":
                return streamResponse(bytes, "image/gif");
            case "ico":
                return streamResponse(bytes, "image/x-icon");
            case "jpg":
            case "jpeg":
                return streamResponse(bytes, "image/jpeg");
            case "png":
                return streamResponse(bytes, "image/png");
            case "svg":
                return streamResponse(bytes, "image/svg+xml");
            // fonts
            case "eot":
                return streamResponse(bytes, "application/vnd.ms-fontobject");
            case "otf":
            case "ttf":
                return streamResponse(bytes, "application/octet-stream");
            case "woff":
                return streamResponse(bytes, "font/woff");
            case "woff2":
                return streamResponse(bytes, "font/woff2");
            // text
            case "html":
                return streamResponse(bytes, "text/html");
            case "jsp":
            case "md":
            case "sh":
            case "txt":
            case "yml":
                return Response.ok(new String(bytes, StandardCharsets.UTF_8)).type("text/plain").build();
            case "xml":
                return Response.ok(new String(bytes, StandardCharsets.UTF_8)).type("text/xml").build();
            // miscellaneous
            case "hbs":
                return streamResponse(bytes, "text/x-handlebars-template");
            default:
                LOG.warn("Unhandled type, returning as application/octet-stream: {}", type);
                return streamResponse(bytes, "application/octet-stream");
            }
        } catch (final IOException e) {
            LOG.debug("I/O error while reading {}.{}", assetName, type, e);
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity("Resource " + assetName + "/" + type + " exists, but could not be read.\n" + e.getMessage() + "\n" + e.getCause()).build());
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private Response streamResponse(final byte[] bytes, final String mimeType) {
        return Response.status(Status.OK).type(mimeType).entity(new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException, WebApplicationException {
                output.write(bytes);
                output.flush();
            }
        }).build();
    }
}
