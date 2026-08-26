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
package org.opennms.web.rest.v1;

import java.net.InetAddress;

import javax.annotation.PreDestroy;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpConfigAccessService;
import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.svclayer.model.SnmpInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * REST service to the OpenNMS SNMP configuration <code>snmp-config.xml</code>
 * </p>
 * <p>
 * This current implementation setting and getting all parameters which are in
 * snmp-config.xml<br/>
 * <br/>
 * <b>Be aware</b> that setting the SNMP configuration for a rage of IPs is
 * currently not supported by this REST service!
 * </p>
 * 
 * <p>
 * The implementation only supports a PUT request because it is an implied
 * "Update" of the configuration since it requires an IP address and all IPs
 * have a default configuration. This request is is passed to the factory for
 * optimization of the configuration store:<code>snmp-config.xml</code>.
 * </p>
 * <p>
 * Example 1: Change SNMP configuration. 
 * </p>
 * 
 * <pre>
 * curl -v -X PUT -H "Content-Type: application/xml" \
 *      -H "Accept: application/xml" \
 *      -d "&lt;snmp-info&gt;
 *          &lt;community&gt;yRuSonoZ&lt;/community&gt;
 *          &lt;port&gt;161&lt;/port&gt;
 *          &lt;retries&gt;1&lt;/retries&gt;
 *          &lt;timeout&gt;2000&lt;/timeout&gt;
 *          &lt;version&gt;v2c&lt;/version&gt;
 *          &lt;/snmp-info&gt;" \
 *      -u admin:admin http://localhost:8980/opennms/rest/snmpConfig/10.1.1.1
 * </pre>
 * <p>
 * Example 2: Query SNMP community string.
 * </p>
 * 
 * <pre>
 * curl -v -X GET -u admin:admin http://localhost:8980/opennms/rest/snmpConfig/10.1.1.1
 * </pre>
 *
 * @deprecated This v1 SNMP Config Rest API is deprecated as it is being replaced by the new v2 Rest API.
 * It will be removed in a future release.
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Component("snmpConfigRestService")
@Path("snmpConfig")
@Tag(name = "SnmpConfig", description = """
        SNMP configuration API over `snmp-config.xml`. Deprecated in favour of the v2 SNMP configuration API
        and slated for removal.

        `GET` reports the *effective* configuration for one address: the most specific matching definition
        merged over the file's defaults. It always answers, because every address has a default, so a 200
        does not mean a definition exists for that address.

        `PUT` takes a single address or a dash-separated range (`10.1.1.1-10.1.1.20`), in which case the
        definition is written for the whole range. The factory then merges and optimises the definitions in
        the file, so the entry that ends up stored is not necessarily the one that was sent.

        Writes affect live polling of the addresses they cover. There is no delete operation.""")
@Transactional
@Deprecated(forRemoval = true)
public class SnmpConfigRestService extends OnmsRestService {

    @Autowired
    private SnmpConfigAccessService m_accessService;

    @PreDestroy
    protected void tearDown() {
        if (m_accessService != null) {
            m_accessService.flushAll();
        }
    }

    /**
     * <p>getSnmpInfo</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.snmpinfo.SnmpInfo} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{ipAddr}")
    @Operation(
            summary = "Get the effective SNMP configuration for an address",
            description = """
        Return the SNMP agent configuration that would be used for one IP address: the most specific matching
        definition in `snmp-config.xml` merged over the file's defaults.

        Every address resolves to something, so this always answers 200 for a parseable address.

        `community` and `readCommunity` carry the same value. Fields the configuration does not set come back
        null.

        A string that is not a valid IP address fails with 500, not 400.""",
            operationId = "getSnmpInfo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The effective configuration for the address.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = SnmpInfo.class),
                                    examples = @ExampleObject(value = """
                    {
                      "location": null,
                      "port": 161,
                      "version": "v2c",
                      "contextName": null,
                      "timeout": 1800,
                      "retries": 1,
                      "authPassPhrase": null,
                      "privPassPhrase": null,
                      "securityLevel": null,
                      "authProtocol": null,
                      "privProtocol": null,
                      "engineId": null,
                      "contextEngineId": null,
                      "enterpriseId": null,
                      "maxRequestSize": 65535,
                      "maxVarsPerPdu": 10,
                      "maxRepetitions": 2,
                      "ttl": null,
                      "proxyHost": null,
                      "securityName": null,
                      "readCommunity": "public",
                      "writeCommunity": "private",
                      "community": "public"
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = SnmpInfo.class),
                                    examples = @ExampleObject(value = """
                    <snmp-info>
                      <community>public</community>
                      <maxRepetitions>2</maxRepetitions>
                      <maxRequestSize>65535</maxRequestSize>
                      <maxVarsPerPdu>10</maxVarsPerPdu>
                      <port>161</port>
                      <readCommunity>public</readCommunity>
                      <retries>1</retries>
                      <timeout>1800</timeout>
                      <version>v2c</version>
                      <writeCommunity>private</writeCommunity>
                    </snmp-info>"""))
                    }),
            @ApiResponse(responseCode = "500", description = "The path segment is not a valid IP address, or a previous write left the configuration unreadable.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid IPAddress not-an-ip")))
    })
    public SnmpInfo getSnmpInfo(
            @Parameter(description = "Single IP address to resolve. A range is not accepted here.",
                    required = true, example = "192.0.2.10")
            @PathParam("ipAddr") String ipAddr,
            @Parameter(description = "Monitoring location to resolve the configuration for. Defaults to the core location.",
                    example = "Default")
            @QueryParam("location") String location) {
        final InetAddress addr = InetAddressUtils.addr(ipAddr);
        if (addr == null) {
            throw getException(Status.BAD_REQUEST, "Malformed IP Address: {}.", ipAddr);
        }
        final SnmpAgentConfig config = m_accessService.getAgentConfig(addr, location);
        return new SnmpInfo(config);
    }

    /**
     * <p>setSnmpInfo</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @param snmpInfo a {@link org.opennms.web.snmpinfo.SnmpInfo} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{ipAddr}")
    @Operation(
            summary = "Set the SNMP configuration for an address or range",
            description = """
        Write an SNMP definition covering one address, or a dash-separated range such as
        `10.1.1.1-10.1.1.20`. Only the fields present in the body are set; the rest keep the file's defaults.

        This changes how the addresses are polled, and it takes effect without a restart. The definition is
        merged into `snmp-config.xml` by the factory, so an address already covered by a wider definition may
        cause that definition to be split.

        Three body formats are accepted: JSON, XML (`<snmp-info>` with one element per field) and form
        encoding (`community=public&port=161&...`). The field names are the same in all three.

        Values are not validated on the way in. A `version` outside `v1`, `v2c` and `v3` is accepted with 204
        and surfaces only later, as a validation error on the next `GET` for a covered address.""",
            operationId = "setSnmpInfo"
    )
    @RequestBody(
            required = true,
            description = "The fields to set. Omitted fields keep the configuration defaults.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SnmpInfo.class),
                            examples = @ExampleObject(value = """
                    {
                      "community": "public",
                      "port": 161,
                      "retries": 1,
                      "timeout": 1800,
                      "version": "v2c"
                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = SnmpInfo.class),
                            examples = @ExampleObject(value = """
                    <snmp-info>
                      <community>public</community>
                      <port>161</port>
                      <retries>1</retries>
                      <timeout>1800</timeout>
                      <version>v2c</version>
                    </snmp-info>""")),
                    @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                            schema = @Schema(implementation = SnmpInfo.class),
                            examples = @ExampleObject(value = "community=public&port=161&retries=1&timeout=1800&version=v2c"))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The definition was written. This is also the answer for a body containing invalid values."),
            @ApiResponse(responseCode = "500", description = "The address or range could not be parsed, or the configuration could not be written.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't update SNMP configuration for not-an-ip : <cause>")))
    })
    public Response setSnmpInfo(
            @Parameter(description = "Single IP address, or a dash-separated inclusive range such as `10.1.1.1-10.1.1.20`.",
                    required = true, example = "192.0.2.10-192.0.2.20")
            @PathParam("ipAddr") final String ipAddress, final SnmpInfo snmpInfo) {
        writeLock();
        try {
            final SnmpEventInfo eventInfo;
            if (ipAddress.contains("-")) {
                final String[] addrs = SnmpConfigRestService.getAddresses(ipAddress);
                eventInfo = snmpInfo.createEventInfo(addrs[0], addrs[1]);
            } else {
                eventInfo = snmpInfo.createEventInfo(ipAddress);
            }

            m_accessService.define(eventInfo);
            return Response.noContent().build();
        } catch (final Throwable e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't update SNMP configuration for {} : {}", ipAddress, e.getMessage());
        } finally {
            writeUnlock();
        }
    }
   
    /**
     * Updates a specific interface
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Path("{ipAddr}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    // Same path and method as setSnmpInfo, so it would collide in the document; the form-encoded
    // body is documented as one of that operation's request content types instead.
    @Operation(hidden = true)
    public Response updateInterface(@PathParam("ipAddr") final String ipAddress, final MultivaluedMapImpl params) {
        writeLock();
        try {
            final SnmpInfo info = new SnmpInfo();
            setProperties(params, info);
            final SnmpEventInfo eventInfo;
            if (ipAddress.contains("-")) {
                final String[] addrs = SnmpConfigRestService.getAddresses(ipAddress);
                eventInfo = info.createEventInfo(addrs[0], addrs[1]);
            } else {
                eventInfo = info.createEventInfo(ipAddress);
            }
            m_accessService.define(eventInfo);
            return Response.noContent().build();
        } catch (final Throwable e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't update SNMP configuration for {} : {}", ipAddress, e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    protected static String[] getAddresses(final String input) {
        if (input == null || input.trim().isEmpty()) {
            return new String[] { null, null };
        } else {
            return input.trim().split("-", 2);
        }
    }
}
