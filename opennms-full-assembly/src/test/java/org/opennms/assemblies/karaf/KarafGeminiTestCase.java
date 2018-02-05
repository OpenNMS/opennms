/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.assemblies.karaf;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;

import java.util.List;

import org.opennms.core.test.karaf.KarafTestCase;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.configs.FeaturesCfg;

/**
 * <p>This test case removes {@code aries-blueprint} from {@code featuresBoot} and adds:</p>
 * <ul>
 * <li>spring/4.2.9.RELEASE_1</li>
 * <li>gemini-blueprint</li>
 * </ul>
 * <p>This will allow all subsequent bundles to bootstrap via Spring contexts that are
 * annotated in their {@code <Spring-Context>} {@code MANIFEST.MF} entries.</p>
 * 
 * @author Seth
 */
public class KarafGeminiTestCase extends KarafTestCase {

    @Configuration
    @Override
    public Option[] config() {
        List<Option> config = configAsList();

        final String version = getOpenNMSVersion();

        // Set opennms.home to a directory where we copy the filtered directory from
        // opennms-base-assembly inside our maven POM
        //config.add(editConfigurationFilePut("etc/system.properties", "opennms.home", ConfigurationTestUtils.getDaemonEtcDirectory().getParentFile().getAbsolutePath()));
        //config.add(editConfigurationFilePut("etc/system.properties", "opennms.home", "../../../../opennms-base-assembly/src/main/filtered"));
        config.add(editConfigurationFilePut("etc/system.properties", "opennms.home", "../../gemini"));

        // Remove aries-blueprint and shell-compat from featuresBoot
        // Put framework and wrap first... I ran into wrap URL problems
        // without this
        //config.add(editConfigurationFilePut(FeaturesCfg.BOOT, "instance,package,log,ssh,aries-blueprint,framework,system,eventadmin,feature,shell,management,service,jaas,shell-compat,deployer,diagnostic,wrap,bundle,config,kar"));
        config.add(editConfigurationFilePut(FeaturesCfg.BOOT, "(framework,wrap),instance,package,log,ssh,system,eventadmin,feature,shell,management,service,jaas,deployer,diagnostic,bundle,config,kar"));

        // Add gemini-blueprint to the featuresBoot
        config.add(features(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("spring-legacy"),"spring/4.2.9.RELEASE_1"));
        config.add(features(maven().groupId("org.opennms.karaf").artifactId("opennms").version(version).type("xml").classifier("features"),"gemini-blueprint"));

        return config.toArray(new Option[config.size()]);
    }
}
