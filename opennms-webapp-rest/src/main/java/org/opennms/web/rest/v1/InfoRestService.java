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

import java.text.ParseException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.resource.Vault;
import org.opennms.core.time.CentralizedDateTimeFormat;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.features.timeformat.api.TimeformatService;
import org.opennms.netmgt.vmmgr.Controller;
import org.opennms.netmgt.vmmgr.StatusGetter;
import org.opennms.web.rest.v1.config.DatetimeformatConfig;
import org.opennms.web.rest.v1.config.TicketerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("infoRestService")
@Path("info")
@Tag(name = "Info", description = "Info API")
@Transactional
public class InfoRestService extends OnmsRestService {
    private static final Logger LOG = LoggerFactory.getLogger(InfoRestService.class);

    private static StatusGetter s_statusGetter;

    @Autowired
    TimeformatService timeformatService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfo(@Context HttpServletRequest httpServletRequest) throws ParseException {
        final SystemInfoUtils sysInfoUtils = new SystemInfoUtils();

        final InfoDTO info = new InfoDTO();
        info.setDisplayVersion(sysInfoUtils.getDisplayVersion());
        info.setVersion(sysInfoUtils.getVersion());
        info.setPackageName(sysInfoUtils.getPackageName());
        info.setPackageDescription(sysInfoUtils.getPackageDescription());
        info.setTicketerConfig(getTicketerConfig());
        info.setDatetimeformatConfig(getDateformatConfig(httpServletRequest.getSession(false)));
        info.setServices(this.getServices());
        return Response.ok().entity(info).build();
    }

    private DatetimeformatConfig getDateformatConfig(HttpSession session) {
        DatetimeformatConfig config = new DatetimeformatConfig();
        config.setZoneId(extractUserTimeZoneId(session));
        config.setDatetimeformat(timeformatService.getFormatPattern());
        return config;
    }

    private ZoneId extractUserTimeZoneId(HttpSession session){
        ZoneId zoneId = null;
        if(session != null){
            zoneId = (ZoneId) session.getAttribute(CentralizedDateTimeFormat.SESSION_PROPERTY_TIMEZONE_ID);
        }
        if(zoneId == null){
            zoneId = ZoneId.systemDefault();
        }
        return zoneId;
    }

    private TicketerConfig getTicketerConfig() {
        final TicketerConfig ticketerConfig = new TicketerConfig();
        ticketerConfig.setEnabled("true".equalsIgnoreCase(Vault.getProperty("opennms.alarmTroubleTicketEnabled")));
        if (ticketerConfig.isEnabled()) {
            ticketerConfig.setPlugin(System.getProperty("opennms.ticketer.plugin"));
        }
        return ticketerConfig;
    }

    private Map<String,String> getServices() {
        if (InfoRestService.s_statusGetter == null) {
            InfoRestService.s_statusGetter = new StatusGetter(new Controller());
        }

        try {
            return s_statusGetter.retrieveStatus();
        } catch (final IllegalStateException e) {
            LOG.warn("Failed to retrieve statuses.  Info will be incomplete.");
        }

        return Collections.emptyMap();
    }

    static void setStatusGetter(final StatusGetter getter) {
        s_statusGetter = getter;
    }
}
