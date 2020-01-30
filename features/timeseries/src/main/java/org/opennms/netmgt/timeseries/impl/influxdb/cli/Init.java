/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.impl.influxdb.cli;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.domain.OnboardingRequest;

import okhttp3.OkHttpClient;

public class Init implements Command {

    private static final String OPENNMS_DATA_SOURCE_NAME = "opennms";

    @Option(name = "-h", aliases = {"--help"}, help = true)
    private boolean showHelp = false;

    @Option(name = "-b", aliases = {"--bucket"}, usage = "Specifies which bucket to create, default: opennms.")
    private String configBucket = "opennms";

    @Option(name = "-o", aliases = {"--organization"}, usage = "Specifies which organization to create, default: opennms.")
    private String configOrg = "opennms";

    @Option(name = "-l", aliases = {"--link"}, usage = "The url to InfluxDB, default: opennms.")
    private String configUrl = "http://localhost:9999";

    @Option(name = "-u", aliases = {"--user"}, usage = "The user name for InfluxDB, default: opennms.")
    private String configUser = "opennms";

    @Option(name = "-p", aliases = {"--password"}, usage = "The password for InfluxDB, default: opennms.")
    private String configPassword = "password";

    @Override
    public void execute() throws Exception {
        if (showHelp) {
            System.out.println("Usage: $OPENNMS_HOME/bin/influxdb init");
            CmdLineParser parser = new CmdLineParser(new Init());
            parser.printUsage(System.out);
            return;
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .bucket(configBucket)
                .connectionString(configUrl)
                .org(configOrg)
                .url(configUrl)
                .okHttpClient(builder)
                // .authenticateToken(configToken.toCharArray())
                .build();
        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(options);


        System.out.println("Checking preconditions");
        if(!influxDBClient.isOnboardingAllowed()) {
            System.out.println("Onboarding via api is not allowed, please set it up manually. Bye!");
        }
        System.out.println("Preconditions: ok");
        System.out.println(String.format("Create account with user=%s, organization=%s, bucket=%s on url=%s", configUser, configOrg, configBucket, configUrl));
        OnboardingRequest request = new OnboardingRequest()
                .bucket(configBucket)
                .org(configOrg)
                .username(configUser)
                .password(configPassword);
        influxDBClient.onBoarding(request);
        System.out.println("Create account: ok. Enjoy!");
    }
}
