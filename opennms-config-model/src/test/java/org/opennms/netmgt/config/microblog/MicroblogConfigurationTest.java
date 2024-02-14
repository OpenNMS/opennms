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
package org.opennms.netmgt.config.microblog;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class MicroblogConfigurationTest extends XmlTestNoCastor<MicroblogConfiguration> {

    public MicroblogConfigurationTest(MicroblogConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/microblog-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfig(),
                "<microblog-configuration default-microblog-profile-name=\"twitter\">\n" +
                "        <microblog-profile\n" + 
                "                name=\"twitter\"\n" + 
                "                service-url=\"https://twitter.com/\"\n" + 
                "                oauth-consumer-key=\"\"\n" + 
                "                oauth-consumer-secret=\"\"\n" + 
                "                oauth-access-token=\"\"\n" + 
                "                oauth-access-token-secret=\"\"\n" + 
                "        />\n" + 
                "</microblog-configuration>"
            }
        });
    }

    private static MicroblogConfiguration getConfig() {
        MicroblogConfiguration config = new MicroblogConfiguration();
        config.setDefaultMicroblogProfileName("twitter");
        
        MicroblogProfile profile = new MicroblogProfile();
        profile.setName("twitter");
        profile.setServiceUrl("https://twitter.com/");
        profile.setOauthConsumerKey("");
        profile.setOauthConsumerSecret("");
        profile.setOauthAccessToken("");
        profile.setOauthAccessTokenSecret("");
        config.addMicroblogProfile(profile);

        return config;
    }
}
