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

import static org.ops4j.pax.exam.CoreOptions.maven;

import org.opennms.core.test.karaf.KarafTestCase;
import org.ops4j.pax.exam.options.MavenUrlReference;

/**
 * This base class uses the OpenNMS-modified Karaf container
 * from container/karaf instead of a vanilla Karaf container.
 */
public class OnmsKarafTestCase extends KarafTestCase {

	/**
	 * Use the OpenNMS-modified Karaf container.
	 */
	@Override
	protected MavenUrlReference getFrameworkUrl() {
		return maven()
				.groupId("org.opennms.container")
				.artifactId("org.opennms.container.karaf")
				.type("tar.gz")
				.version("22.0.0-SNAPSHOT");
	}
}
