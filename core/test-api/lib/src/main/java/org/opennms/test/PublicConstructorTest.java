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
