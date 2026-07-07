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
package org.opennms.core.test.junit5;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * JUnit 5 (Jupiter) equivalent of {@link org.opennms.core.test.OpenNMSJUnit4ClassRunner}:
 * runs the test through {@link SpringExtension} and registers the standard OpenNMS
 * {@code TestExecutionListener}s via {@link OpenNMSTestContextBootstrapper} (reflectively,
 * so only the test-api modules actually on the test classpath contribute their listener).
 *
 * <p>Usage is the same as the JUnit 4 harness, just with Jupiter annotations:</p>
 *
 * <pre>
 * &#64;OpenNMSSpringJupiterTest
 * &#64;ContextConfiguration(locations = { ... })
 * &#64;JUnitConfigurationEnvironment
 * &#64;JUnitTemporaryDatabase
 * class MyDaoIT { ... }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(SpringExtension.class)
@BootstrapWith(OpenNMSTestContextBootstrapper.class)
public @interface OpenNMSSpringJupiterTest {
}
