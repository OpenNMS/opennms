/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter.stats;

import static org.junit.Assert.assertThat;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class RpkiValidatorTest {


    @Test
    public void testRpkiValidator() {
        RpkiValidatorClient rpkiValidatorClient = new RpkiValidatorClient("localhost");
        URL resourceURL = getClass().getResource("/roas.json");
        try (FileReader reader = new FileReader(resourceURL.toURI().getPath())) {
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(false);
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(jsonReader);
            List<RpkiInfo> rpkiInfoList = rpkiValidatorClient.parseRpkiInfoFromResponse(jsonElement.toString());
            assertThat(rpkiInfoList, Matchers.hasSize(4));
            RpkiInfo rpkiInfo = rpkiInfoList.get(0);
            Assert.assertThat(rpkiInfo.getAsn(), Matchers.isOneOf(2312L, 4323L, 9210L, 3456L));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

}
