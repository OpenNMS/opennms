/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
