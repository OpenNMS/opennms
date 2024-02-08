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
package org.opennms.netmgt.telemetry.protocols.bmp.adapter.stats;

import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.stats.RouteInfo.isValidIpAddress;

import java.net.URL;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRpkiInfo;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRpkiInfoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * RpkiValidator Client that fetches json response from Rpki Validator Rest API.
 * (https://github.com/RIPE-NCC/rpki-validator-3)
 */
public class RpkiValidatorClient {

    private static final Logger LOG = LoggerFactory.getLogger(RpkiValidatorClient.class);

    private static final Integer DEFAULT_HOUR_OF_THE_DAY = 3;

    private final String rpkiUrl;

    private String authorizationHeader;

    private BmpRpkiInfoDao bmpRpkiInfoDao;

    private String rpkiUsername;

    private String rpkiPassword;

    private Integer hourOfTheDay = DEFAULT_HOUR_OF_THE_DAY;

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("UpdateRpkiInfo-%d")
            .build();

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
            threadFactory);

    public RpkiValidatorClient(String rpkiUrl) {
        this.rpkiUrl = rpkiUrl;
        if (!Strings.isNullOrEmpty(rpkiUsername) && !Strings.isNullOrEmpty(rpkiPassword)) {
            this.authorizationHeader = "Basic " + Base64.getEncoder().encodeToString((rpkiPassword + ":" + rpkiPassword).getBytes());
        }
    }

    public void init() {
        Long hourOfTheDayInMinutes = Utils.getHourOfTheDayInMinutes(hourOfTheDay);
        scheduledExecutorService.scheduleAtFixedRate(this::updateRpkiInfo, hourOfTheDayInMinutes, TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);
    }

    public void destroy() {
        scheduledExecutorService.shutdown();
    }

    private void updateRpkiInfo() {
        String jsonResponse = getJsonRestResponse();
        if (jsonResponse != null) {
            List<RpkiInfo> rpkiInfoList = parseRpkiInfoFromResponse(jsonResponse);
            rpkiInfoList.forEach(rpkiInfo -> {
                BmpRpkiInfo bmpRpkiInfo = buildBmpRpkiValidator(rpkiInfo);
                if (bmpRpkiInfo != null && bmpRpkiInfoDao != null) {
                    try {
                        bmpRpkiInfoDao.saveOrUpdate(bmpRpkiInfo);
                    } catch (Exception e) {
                        LOG.error("Exception while persisting BMP Rpki validator {}", bmpRpkiInfo, e);
                    }
                }
            });
        }
    }

    private BmpRpkiInfo buildBmpRpkiValidator(RpkiInfo rpkiInfo) {
        BmpRpkiInfo bmpRpkiInfo = bmpRpkiInfoDao.findBmpRpkiInfoWith(rpkiInfo.getPrefix(),
                rpkiInfo.getPrefixMaxLen(), rpkiInfo.getAsn());
        if (bmpRpkiInfo == null) {
            bmpRpkiInfo = new BmpRpkiInfo();
            bmpRpkiInfo.setOriginAs(rpkiInfo.getAsn());
            bmpRpkiInfo.setPrefix(rpkiInfo.getPrefix());
            bmpRpkiInfo.setPrefixLenMax(rpkiInfo.getPrefixMaxLen());
        }
        bmpRpkiInfo.setPrefixLen(rpkiInfo.getPrefixLen());
        bmpRpkiInfo.setTimestamp(Date.from(Instant.now()));
        return bmpRpkiInfo;
    }

    @VisibleForTesting
    List<RpkiInfo> parseRpkiInfoFromResponse(String jsonResponse) {
        List<RpkiInfo> rpkiInfos = new ArrayList<>();
        try {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(jsonResponse).getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonArray("roas");

            for (JsonElement jsonElement : jsonArray) {
                try {
                    RpkiInfo rpkiInfo = new RpkiInfo();
                    JsonObject roa = jsonElement.getAsJsonObject();
                    JsonElement asnElement = roa.get("asn");
                    if (asnElement == null) {
                        continue;
                    }
                    Long asn = StringUtils.parseLong(asnElement.getAsString(), null);
                    if (asn == null) {
                        continue;
                    }
                    rpkiInfo.setAsn(asn);
                    JsonElement maxLengthElement = roa.get("maxLength");
                    if (maxLengthElement == null) {
                        continue;
                    }
                    rpkiInfo.setPrefixMaxLen(maxLengthElement.getAsInt());
                    JsonElement prefixElement = roa.get("prefix");
                    if (prefixElement == null) {
                        continue;
                    }
                    String prefixString = prefixElement.getAsString();
                    if (prefixString.contains("/")) {
                        String[] prefixArray = prefixString.split("/", 2);
                        if (isValidIpAddress(prefixArray[0])) {
                            rpkiInfo.setPrefix(prefixArray[0]);
                        }
                        Integer prefixLen = StringUtils.parseInt(prefixArray[1], null);
                        if (prefixLen != null) {
                            rpkiInfo.setPrefixLen(prefixLen);
                        }
                    }
                    rpkiInfos.add(rpkiInfo);
                } catch (Exception e) {
                    // skip element.
                    LOG.warn("Exception while parsing Rpki element {}", jsonElement, e);
                }
            }
        } catch (Exception e) {
            LOG.error("Exception while parsing Rpki Info from json response {}", jsonResponse, e);
        }
        return rpkiInfos;
    }

    private String getJsonRestResponse() {
        try {
            URL url = new URL(this.rpkiUrl);
            final Client client = ClientBuilder.newClient();
            WebTarget target = client.target(url.toString());
            Invocation.Builder builder = target.request();
            if (!Strings.isNullOrEmpty(authorizationHeader)) {
                builder.header("Authorization", authorizationHeader);
            }
            Response response = builder.get();
            return response.readEntity(String.class);
        } catch (Exception e) {
            LOG.error("Exception while fetching response from {}", rpkiUrl, e);
        }
        return null;
    }

    public void setBmpRpkiInfoDao(BmpRpkiInfoDao bmpRpkiInfoDao) {
        this.bmpRpkiInfoDao = bmpRpkiInfoDao;
    }

    public void setRpkiUsername(String rpkiUsername) {
        this.rpkiUsername = rpkiUsername;
    }

    public void setRpkiPassword(String rpkiPassword) {
        this.rpkiPassword = rpkiPassword;
    }

    public void setHourOfTheDay(Integer hourOfTheDay) {
        this.hourOfTheDay = hourOfTheDay;
    }
}
