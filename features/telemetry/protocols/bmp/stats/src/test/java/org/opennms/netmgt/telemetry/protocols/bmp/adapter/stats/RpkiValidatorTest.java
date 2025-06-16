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
