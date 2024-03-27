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
package org.opennms.netmgt.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Test;
import org.opennms.netmgt.config.users.User;
import org.opennms.test.DaoTestConfigBean;
import org.springframework.util.FileSystemUtils;

import com.google.common.io.Files;

public class UserFactoryTest {
	@Test(expected=IllegalStateException.class)
	public void testSaveUserNoMD5() throws Exception {
		configureUserFactory(false);
		final User user = new User();
		user.setUserId("test");
		user.setPassword("foo", false);
		final UserManager um = UserFactory.getInstance();
		um.saveUser("test", user);
	}

	@Test(expected=IllegalStateException.class)
	public void testSaveUsersNoMD5() throws Exception {
		configureUserFactory(false);
		final User user1 = new User();
		user1.setUserId("test");
		user1.setPassword("foo", true);

		final User user2 = new User();
		user2.setUserId("test2");
		user2.setPassword("blah", false);
		final UserManager um = UserFactory.getInstance();
		um.saveUsers(Arrays.asList(user1, user2));
	}

	@Test
	public void testSaveUserAllowMD5() throws Exception {
		configureUserFactory(true);
		final User user = new User();
		user.setUserId("test");
		user.setPassword("foo", false);
		final UserManager um = UserFactory.getInstance();
		um.saveUser("test", user);
	}

	@Test
	public void testSaveUsersAllowMD5() throws Exception {
		configureUserFactory(true);
		final User user1 = new User();
		user1.setUserId("test");
		user1.setPassword("foo", true);

		final User user2 = new User();
		user2.setUserId("test2");
		user2.setPassword("blah", false);
		final UserManager um = UserFactory.getInstance();
		um.saveUsers(Arrays.asList(user1, user2));
	}

	private void configureUserFactory(final boolean allowUnsalted) throws Exception {
		final DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
		daoTestConfig.afterPropertiesSet();

		final Path opennmsHome = Paths.get(System.getProperty("opennms.home"));
		final Path tempHome = Files.createTempDir().toPath();

		System.err.println("opennms.home=" + tempHome);
		FileSystemUtils.copyRecursively(opennmsHome.resolve("etc").toFile(), tempHome.resolve("etc").toFile());
		System.setProperty("opennms.home", tempHome.toAbsolutePath().toString());
		System.setProperty(UserFactory.ALLOW_UNSALTED_PROPERTY, String.valueOf(allowUnsalted));
		UserFactory.setInstance(null);
		UserFactory.init();
	}
}
