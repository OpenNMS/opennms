/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.test;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Constructor;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This JUnit test simply checks for public constructors on the list of classes
 * that are returned by the {@link #getClasses()} method.
 *
 * @author Seth
 * @version $Id: $
 */
public abstract class PublicConstructorTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(PublicConstructorTest.class);

	/**
	 * <p>testPublicConstructors</p>
	 *
	 * @throws java.lang.NoSuchMethodException if any.
	 * @throws java.lang.Exception if any.
	 */
	@Test
	public void testPublicConstructors() throws NoSuchMethodException, Exception {
		for (Class<? extends Object> clazz : getClasses()) {
			try {
				Constructor<? extends Object> constructor = clazz.getConstructor();
				assertNotNull(constructor);
				System.out.println("Found public constructor on class: " + clazz.getName());
			} catch (final Exception e) {
			    LOG.warn("unable to locate constructor on class: {}", clazz.getName(), e);
				throw e;
			}
		}
	}

	/**
	 * This method should return a list of classes that you wish to inspect for public
	 * constructors.
	 *
	 * @return a {@link java.util.List} object.
	 * @throws java.lang.Exception if any.
	 */
	protected abstract List<Class<? extends Object>> getClasses() throws Exception;
}
