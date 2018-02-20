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

package org.opennms.assemblies.karaf;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

/**
 * <p>This test bootstraps the OpenNMS-modified Karaf container
 * ensuring that:</p>
 * 
 * <ul>
 * <li>The system classpath is sufficient to satisfy the OSGi classpath</li>
 * <li>There are no missing dependencies for the default featuresBoot features</li>
 * </ul>
 * 
 * <p>Even though the system bootstraps, many bundles end up in {@code Failed}
 * or {@code GracePeriod} status because the services that are normally started
 * as part of OpenNMS have not been started.</p>
 * 
 * <p>TODO: Diagnose bundle failures and enhance test to prevent them</p>
 * <p>TODO: Assert that no bundles end up in failure state</p>
 * <p>TODO: Add mock services if necessary to prevent {@code GracePeriod} statuses</p>
 * <p>TODO: Verify availability of services provided by OSGi bundles</p>
 * 
 * <p>Until these TODOs are implemented, we must rely on full-system smoke tests
 * to diagnose failures related to the OSGi wiring inside Karaf.</p>
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class FeaturesBootKarafIT extends OnmsKarafTestCase {

	@Test
	public void testInstallAllOpenNMSFeatures() {
		System.out.println(executeCommand("feature:list -i"));
		System.out.println(executeCommand("list"));
	}
}
