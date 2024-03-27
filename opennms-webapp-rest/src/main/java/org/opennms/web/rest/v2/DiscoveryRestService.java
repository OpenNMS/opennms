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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.ExcludeRange;
import org.opennms.netmgt.config.discovery.ExcludeUrl;
import org.opennms.netmgt.config.discovery.IncludeRange;
import org.opennms.netmgt.config.discovery.IncludeUrl;
import org.opennms.netmgt.config.discovery.Specific;
import org.opennms.netmgt.discovery.DiscoveryTaskExecutor;
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

    @XmlRootElement(name = "discoveryConfiguration")
    public static class DiscoveryConfigurationDTO {

        @XmlRootElement
        public static class SpecificDTO {
            private String content;
            private String location;
            private Integer retries;
            private Long timeout;
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
            private String location = "Default";
            private Integer retries = 1;
            private Long timeout = 2000l;
            private String foreignSource;
            private String begin;
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
            private String begin;
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
            private String content;
            private String location = "Default";
            private Integer retries = 1;
            private Long timeout = 2000l;
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
            private String content;
            private String location = "Default";
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

        private String location = "Default";
        private Integer retries = 1;
        private Long timeout = 2000l;
        private String foreignSource;
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
    public Response scan(DiscoveryConfigurationDTO discoveryConfigurationDTO) {

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
