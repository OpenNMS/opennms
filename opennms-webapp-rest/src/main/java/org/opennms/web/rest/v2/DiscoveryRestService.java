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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.ExcludeRange;
import org.opennms.netmgt.config.discovery.ExcludeUrl;
import org.opennms.netmgt.config.discovery.IncludeRange;
import org.opennms.netmgt.config.discovery.IncludeUrl;
import org.opennms.netmgt.config.discovery.Specific;
import org.opennms.netmgt.discovery.DiscoveryTaskExecutor;
import org.opennms.web.api.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for submitting discovery tasks
 *
 * @author Christian Pape
 */
@Component
@Path("discovery")
@Transactional
@Tag(name = "Discovery", description = "Discovery API")
public class DiscoveryRestService {

    private static final Logger LOG = LoggerFactory.getLogger(org.opennms.web.rest.v2.DiscoveryRestService.class);

    private static final String OVERRIDE_LOCATION =
            "Monitoring location to probe from, overriding the configuration-level `location`.";

    private static final String OVERRIDE_RETRIES =
            "ICMP retry count for this target, overriding the configuration-level `retries`.";

    private static final String OVERRIDE_TIMEOUT =
            "ICMP timeout in milliseconds for this target, overriding the configuration-level `timeout`.";

    private static final String OVERRIDE_FOREIGN_SOURCE =
            "Requisition to attribute nodes discovered from this target to, overriding the configuration-level `foreignSource`.";

    private static final String URL_CONTENT = """
            Location of a newline-separated address list, as a URL. `file:` URLs read from the OpenNMS
            host's filesystem and `http:`/`https:` URLs are fetched by the OpenNMS host.""";

    @XmlRootElement(name = "discoveryConfiguration")
    public static class DiscoveryConfigurationDTO {

        @XmlRootElement
        public static class SpecificDTO {
            @Schema(description = "The single address to probe. Named `content`, not `address`.", example = "192.0.2.1")
            private String content;

            @Schema(description = OVERRIDE_LOCATION, example = "Default")
            private String location;

            @Schema(description = OVERRIDE_RETRIES, example = "1")
            private Integer retries;

            @Schema(description = OVERRIDE_TIMEOUT, example = "2000")
            private Long timeout;

            @Schema(description = OVERRIDE_FOREIGN_SOURCE, example = "Routers")
            private String foreignSource;

            public SpecificDTO() {
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }

            public String getLocation() {
                return location;
            }

            public void setLocation(String location) {
                this.location = location;
            }

            public Integer getRetries() {
                return retries;
            }

            public void setRetries(Integer retries) {
                this.retries = retries;
            }

            public Long getTimeout() {
                return timeout;
            }

            public void setTimeout(Long timeout) {
                this.timeout = timeout;
            }

            public String getForeignSource() {
                return foreignSource;
            }

            public void setForeignSource(String foreignSource) {
                this.foreignSource = foreignSource;
            }
        }

        @XmlRootElement
        public static class IncludeRangeDTO {
            @Schema(description = OVERRIDE_LOCATION, example = "Default", defaultValue = "Default")
            private String location = "Default";

            @Schema(description = OVERRIDE_RETRIES, example = "1", defaultValue = "1")
            private Integer retries = 1;

            @Schema(description = OVERRIDE_TIMEOUT, example = "2000", defaultValue = "2000")
            private Long timeout = 2000l;

            @Schema(description = OVERRIDE_FOREIGN_SOURCE, example = "Routers")
            private String foreignSource;

            @Schema(description = "First address of the range, inclusive.", example = "192.0.2.10")
            private String begin;

            @Schema(description = "Last address of the range, inclusive.", example = "192.0.2.20")
            private String end;

            public IncludeRangeDTO() {
            }

            public String getLocation() {
                return location;
            }

            public void setLocation(String location) {
                this.location = location;
            }

            public Integer getRetries() {
                return retries;
            }

            public void setRetries(Integer retries) {
                this.retries = retries;
            }

            public Long getTimeout() {
                return timeout;
            }

            public void setTimeout(Long timeout) {
                this.timeout = timeout;
            }

            public String getForeignSource() {
                return foreignSource;
            }

            public void setForeignSource(String foreignSource) {
                this.foreignSource = foreignSource;
            }

            public String getBegin() {
                return begin;
            }

            public void setBegin(String begin) {
                this.begin = begin;
            }

            public String getEnd() {
                return end;
            }

            public void setEnd(String end) {
                this.end = end;
            }
        }

        @XmlRootElement
        public static class ExcludeRangeDTO {
            @Schema(description = "First address to exclude, inclusive.", example = "192.0.2.15")
            private String begin;

            @Schema(description = "Last address to exclude, inclusive.", example = "192.0.2.16")
            private String end;

            public ExcludeRangeDTO() {
            }

            public String getBegin() {
                return begin;
            }

            public void setBegin(String begin) {
                this.begin = begin;
            }

            public String getEnd() {
                return end;
            }

            public void setEnd(String end) {
                this.end = end;
            }
        }

        @XmlRootElement
        public static class IncludeUrlDTO {
            @Schema(description = URL_CONTENT, example = "file:/opt/opennms/etc/discovery-include.txt")
            private String content;

            @Schema(description = OVERRIDE_LOCATION, example = "Default", defaultValue = "Default")
            private String location = "Default";

            @Schema(description = OVERRIDE_RETRIES, example = "1", defaultValue = "1")
            private Integer retries = 1;

            @Schema(description = OVERRIDE_TIMEOUT, example = "2000", defaultValue = "2000")
            private Long timeout = 2000l;

            @Schema(description = OVERRIDE_FOREIGN_SOURCE, example = "Routers")
            private String foreignSource;

            public IncludeUrlDTO() {
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }

            public String getLocation() {
                return location;
            }

            public void setLocation(String location) {
                this.location = location;
            }

            public Integer getRetries() {
                return retries;
            }

            public void setRetries(Integer retries) {
                this.retries = retries;
            }

            public Long getTimeout() {
                return timeout;
            }

            public void setTimeout(Long timeout) {
                this.timeout = timeout;
            }

            public String getForeignSource() {
                return foreignSource;
            }

            public void setForeignSource(String foreignSource) {
                this.foreignSource = foreignSource;
            }
        }

        @XmlRootElement
        public static class ExcludeUrlDTO {
            @Schema(description = URL_CONTENT, example = "file:/opt/opennms/etc/discovery-exclude.txt")
            private String content;

            @Schema(description = OVERRIDE_LOCATION, example = "Default", defaultValue = "Default")
            private String location = "Default";

            @Schema(description = OVERRIDE_FOREIGN_SOURCE, example = "Routers")
            private String foreignSource;

            public ExcludeUrlDTO() {
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }

            public String getLocation() {
                return location;
            }

            public void setLocation(String location) {
                this.location = location;
            }

            public String getForeignSource() {
                return foreignSource;
            }

            public void setForeignSource(String foreignSource) {
                this.foreignSource = foreignSource;
            }
        }

        @Schema(description = "Default monitoring location for every target that does not name its own.",
                example = "Default", defaultValue = "Default")
        private String location = "Default";

        @Schema(description = "Default ICMP retry count for targets that do not set their own.",
                example = "1", defaultValue = "1")
        private Integer retries = 1;

        @Schema(description = "Default ICMP timeout in milliseconds for targets that do not set their own.",
                example = "2000", defaultValue = "2000")
        private Long timeout = 2000l;

        @Schema(description = "Default requisition to attribute newly discovered nodes to.", example = "Routers")
        private String foreignSource;

        @Schema(description = "How many addresses go into one ping sweep job.", example = "100", defaultValue = "100")
        private Integer chunkSize = 100;

        private List<SpecificDTO> specificDTOList = new ArrayList<>();
        private List<IncludeRangeDTO> includeRangeDTOList = new ArrayList<>();
        private List<ExcludeRangeDTO> excludeRangeDTOList = new ArrayList<>();
        private List<IncludeUrlDTO> includeUrlDTOList = new ArrayList<>();
        private List<ExcludeUrlDTO> excludeUrlDTOList = new ArrayList<>();

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public Integer getRetries() {
            return retries;
        }

        public void setRetries(Integer retries) {
            this.retries = retries;
        }

        public Long getTimeout() {
            return timeout;
        }

        public void setTimeout(Long timeout) {
            this.timeout = timeout;
        }

        public String getForeignSource() {
            return foreignSource;
        }

        public void setForeignSource(String foreignSource) {
            this.foreignSource = foreignSource;
        }

        public Integer getChunkSize() {
            return chunkSize;
        }

        public void setChunkSize(Integer chunkSize) {
            this.chunkSize = chunkSize;
        }

        @XmlElementWrapper(name="specifics")
        @XmlElement(name="specific")
        public List<SpecificDTO> getSpecificDTOList() {
            return specificDTOList;
        }

        public void setSpecificDTOList(List<SpecificDTO> specificDTOList) {
            this.specificDTOList = specificDTOList;
        }

        @XmlElementWrapper(name="includeRanges")
        @XmlElement(name="includeRange")
        public List<IncludeRangeDTO> getIncludeRangeDTOList() {
            return includeRangeDTOList;
        }

        public void setIncludeRangeDTOList(List<IncludeRangeDTO> includeRangeDTOList) {
            this.includeRangeDTOList = includeRangeDTOList;
        }

        @XmlElementWrapper(name="excludeRanges")
        @XmlElement(name="excludeRange")
        public List<ExcludeRangeDTO> getExcludeRangeDTOList() {
            return excludeRangeDTOList;
        }

        public void setExcludeRangeDTOList(List<ExcludeRangeDTO> excludeRangeDTOList) {
            this.excludeRangeDTOList = excludeRangeDTOList;
        }

        @XmlElementWrapper(name="includeUrls")
        @XmlElement(name="includeUrl")
        public List<IncludeUrlDTO> getIncludeUrlDTOList() {
            return includeUrlDTOList;
        }

        public void setIncludeUrlDTOList(List<IncludeUrlDTO> includeUrlDTOList) {
            this.includeUrlDTOList = includeUrlDTOList;
        }

        @XmlElementWrapper(name="excludeUrls")
        @XmlElement(name="excludeUrl")
        public List<ExcludeUrlDTO> getExcludeUrlDTOList() {
            return excludeUrlDTOList;
        }

        public void setExcludeUrlDTOList(List<ExcludeUrlDTO> excludeUrlDTOList) {
            this.excludeUrlDTOList = excludeUrlDTOList;
        }
    }

    @Autowired
    ServiceRegistry serviceRegistry;

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Submit a one-off discovery scan",
            description = """
        Runs a ping sweep now, using the configuration in the body only. Nothing is written to
        `discovery-configuration.xml` and the scheduled discovery configuration is untouched.

        The call returns as soon as the sweep has been handed to the discovery task executor, so a 200
        means the request was accepted, not that any address answered. Addresses that respond raise
        `newSuspect` events. There is no job id and no way to poll this operation for progress.

        Targets come from five collections, all optional: `specifics` (single addresses), `includeRanges`
        and `excludeRanges` (inclusive address ranges), and `includeUrls`/`excludeUrls` (addresses read
        from a URL). Each entry may override the top-level `location`, `retries`, `timeout` and
        `foreignSource`. A body with no targets at all is accepted and sweeps nothing.

        Requires `ROLE_PROVISION` or `ROLE_ADMIN`, checked in the handler on top of the Spring Security
        rule for `/api/v2/**`.

        The JSON member names are the wrapper names (`specifics`, `includeRanges`, `excludeRanges`,
        `includeUrls`, `excludeUrls`), not the Java property names. An unrecognised JSON member is
        rejected with a 500 carrying the Jackson message.""",
            operationId = "submitDiscoveryScan")
    @RequestBody(required = true, description = "The one-off discovery configuration to run.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DiscoveryConfigurationDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "location": "Default",
                      "retries": 1,
                      "timeout": 2000,
                      "foreignSource": "Routers",
                      "chunkSize": 10,
                      "specifics": [
                        {"content": "192.0.2.1", "location": "Default", "retries": 1, "timeout": 2000}
                      ],
                      "includeRanges": [
                        {"begin": "192.0.2.10", "end": "192.0.2.20", "location": "Default", "retries": 1, "timeout": 2000}
                      ],
                      "excludeRanges": [
                        {"begin": "192.0.2.15", "end": "192.0.2.16"}
                      ],
                      "includeUrls": [],
                      "excludeUrls": []
                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = DiscoveryConfigurationDTO.class),
                            examples = @ExampleObject(value = """
                    <discoveryConfiguration>
                      <location>Default</location>
                      <retries>1</retries>
                      <timeout>2000</timeout>
                      <foreignSource>Routers</foreignSource>
                      <chunkSize>10</chunkSize>
                      <specifics>
                        <specific>
                          <content>192.0.2.1</content>
                          <location>Default</location>
                          <retries>1</retries>
                          <timeout>2000</timeout>
                        </specific>
                      </specifics>
                      <includeRanges>
                        <includeRange>
                          <begin>192.0.2.10</begin>
                          <end>192.0.2.20</end>
                        </includeRange>
                      </includeRanges>
                    </discoveryConfiguration>"""))
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The sweep was handed to the discovery task executor. No body."),
            @ApiResponse(responseCode = "403", description = """
                    The caller holds neither `ROLE_PROVISION` nor `ROLE_ADMIN`. The shipped security
                    rules gate `POST /api/v2/discovery` on the same two roles, so in a stock deployment
                    the container answers first with its HTML error page and the in-handler plain-text
                    message below is unreachable; it appears only under customized security rules.""",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "The PROVISION or ADMIN role is required to submit a discovery scan."))),
            @ApiResponse(responseCode = "500", description = """
                    Either the body could not be bound (an unrecognised JSON member reports the Jackson
                    error) or no `DiscoveryTaskExecutor` is registered, in which case the body is
                    empty.""",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unrecognized field \"specificDTOList\" (Class org.opennms.web.rest.v2.DiscoveryRestService$DiscoveryConfigurationDTO), not marked as ignorable")))
    })
    public Response scan(@Context final SecurityContext securityContext, DiscoveryConfigurationDTO discoveryConfigurationDTO) {

        // Discovery is a provisioning operation: its include/exclude URLs can read local files
        // or fetch arbitrary URLs. Restrict to provisioning/admin roles rather than allowing any
        // ROLE_REST user (this mirrors the spring-security rule for /api/v2/discovery).
        if (!securityContext.isUserInRole(Authentication.ROLE_PROVISION)
                && !securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            return Response.status(Status.FORBIDDEN)
                    .entity("The PROVISION or ADMIN role is required to submit a discovery scan.").build();
        }

        DiscoveryConfiguration discoveryConfiguration = getDiscoveryConfig(discoveryConfigurationDTO);

        DiscoveryTaskExecutor discoveryTaskExecutor = serviceRegistry.findProvider(DiscoveryTaskExecutor.class);

        if (discoveryTaskExecutor != null) {
            discoveryTaskExecutor.handleDiscoveryTask(discoveryConfiguration);
        } else {
            LOG.warn("No DiscoveryTaskExecutor service is available");

            return Response.serverError().build();
        }

        return Response.ok().build();
    }

    private DiscoveryConfiguration getDiscoveryConfig(DiscoveryConfigurationDTO discoveryConfigurationDTO) {
        DiscoveryConfiguration discoveryConfiguration = new DiscoveryConfiguration();

        discoveryConfiguration.setTimeout(discoveryConfigurationDTO.getTimeout());
        discoveryConfiguration.setRetries(discoveryConfigurationDTO.getRetries());
        discoveryConfiguration.setForeignSource(discoveryConfigurationDTO.getForeignSource());
        discoveryConfiguration.setLocation(discoveryConfigurationDTO.getLocation());
        discoveryConfiguration.setChunkSize(discoveryConfigurationDTO.getChunkSize());

        for(DiscoveryConfigurationDTO.SpecificDTO specificDTO : discoveryConfigurationDTO.getSpecificDTOList()) {
            Specific specific = new Specific();
            specific.setAddress(specificDTO.getContent());
            specific.setTimeout(specificDTO.getTimeout());
            specific.setRetries(specificDTO.getRetries());
            specific.setForeignSource(specificDTO.getForeignSource());
            specific.setLocation(specificDTO.getLocation());
            discoveryConfiguration.addSpecific(specific);
        }

        for(DiscoveryConfigurationDTO.IncludeUrlDTO includeUrlDTO : discoveryConfigurationDTO.getIncludeUrlDTOList()){
            IncludeUrl includeUrl = new IncludeUrl();
            includeUrl.setUrl(includeUrlDTO.getContent());
            includeUrl.setTimeout(includeUrlDTO.getTimeout());
            includeUrl.setRetries(includeUrlDTO.getRetries());
            includeUrl.setForeignSource(includeUrlDTO.getForeignSource());
            includeUrl.setLocation(includeUrlDTO.getLocation());
            discoveryConfiguration.addIncludeUrl(includeUrl);
        }

        for(DiscoveryConfigurationDTO.ExcludeUrlDTO excludeUrlDTO : discoveryConfigurationDTO.getExcludeUrlDTOList()){
            ExcludeUrl excludeUrl = new ExcludeUrl();
            excludeUrl.setUrl(excludeUrlDTO.getContent());
            excludeUrl.setForeignSource(excludeUrlDTO.getForeignSource());
            excludeUrl.setLocation(excludeUrlDTO.getLocation());
            discoveryConfiguration.addExcludeUrl(excludeUrl);
        }

        for(DiscoveryConfigurationDTO.IncludeRangeDTO includeRangeDTO : discoveryConfigurationDTO.getIncludeRangeDTOList()){
            IncludeRange includeRange = new IncludeRange();
            includeRange.setBegin(includeRangeDTO.getBegin());
            includeRange.setEnd(includeRangeDTO.getEnd());
            includeRange.setTimeout(includeRangeDTO.getTimeout());
            includeRange.setRetries(includeRangeDTO.getRetries());
            includeRange.setForeignSource(includeRangeDTO.getForeignSource());
            includeRange.setLocation(includeRangeDTO.getLocation());
            discoveryConfiguration.addIncludeRange(includeRange);
        }

        for(DiscoveryConfigurationDTO.ExcludeRangeDTO excludeRangeDTO : discoveryConfigurationDTO.getExcludeRangeDTOList()) {
            ExcludeRange excludeRange = new ExcludeRange();
            excludeRange.setBegin(excludeRangeDTO.getBegin());
            excludeRange.setEnd(excludeRangeDTO.getEnd());
            discoveryConfiguration.addExcludeRange(excludeRange);
        }

        return discoveryConfiguration;
    }
}
