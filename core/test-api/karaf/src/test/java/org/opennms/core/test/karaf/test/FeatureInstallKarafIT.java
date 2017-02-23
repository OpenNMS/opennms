/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.core.test.karaf.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.karaf.KarafTestCase;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class FeatureInstallKarafIT extends KarafTestCase {

    /**
     * This test attempts to install all features from the standard Karaf
     * features.xml.
     */
    @Test
    public void testInstallAllStandardFeatures() {
        installFeature("admin");
        installFeature("aries-blueprint");
        installFeature("aries-proxy");
        installFeature("blueprint-web");
        installFeature("config");
        installFeature("deployer");
        installFeature("diagnostic");
        installFeature("eventadmin");
        installFeature("features");
        installFeature("http");
        installFeature("http-whiteboard");
        installFeature("jaas");
        installFeature("jasypt-encryption");
        installFeature("jetty");
        installFeature("karaf-framework");
        installFeature("kar");
        installFeature("management");
        installFeature("obr");
        //installFeature("scr");
        installFeature("service-security");
        installFeature("service-wrapper");
        installFeature("shell");
        installFeature("ssh");
        installFeature("war");
        installFeature("webconsole");
        installFeature("wrap");
        installFeature("wrapper");

        System.out.println(executeCommand("features:list -i"));
    }

    /**
     * This test attempts to install all features from the Spring Karaf
     * features.xml.
     */
    @Test
    public void testInstallAllSpringFeatures() {
        installFeature("spring");
        installFeature("spring-aspects");
        installFeature("spring-instrument");
        installFeature("spring-jdbc");
        installFeature("spring-jms");
        installFeature("spring-test");
        installFeature("spring-orm");
        installFeature("spring-oxm");
        installFeature("spring-tx");
        installFeature("spring-web");
        //installFeature("spring-web-portlet");
        //installFeature("spring-websocket");
        //installFeature("spring-security");

        System.out.println(executeCommand("features:list -i"));
    }
}
