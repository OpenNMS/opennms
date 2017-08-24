/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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
