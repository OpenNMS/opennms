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
package org.opennms.netmgt.dao.usermgmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.model.usermgmt.SecurityRole;
import org.opennms.netmgt.model.usermgmt.SecurityRoles;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class SecurityRoleDaoIT implements InitializingBean {

    @Autowired
    private SessionFactory m_sessionFactory;

    @Autowired
    private SecurityRoleDao m_roleDao;

    @Autowired
    private UserAccountDao m_userDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    /** The changelog seeds exactly the roles the Java constants know, with the same descriptions. */
    @Test
    @Transactional
    public void testBuiltinRolesAreSeeded() {
        final List<SecurityRole> builtin = m_roleDao.findBuiltin();
        final Map<String, String> seeded = builtin.stream()
                .collect(Collectors.toMap(SecurityRole::getName, SecurityRole::getDescription, (a, b) -> a, LinkedHashMap::new));
        assertEquals(SecurityRoles.BUILTIN_ROLES, seeded);
        for (final SecurityRole role : builtin) {
            assertTrue(role.isBuiltin());
            assertNotNull(role.getCreatedTime());
        }
        assertTrue(m_roleDao.findUserDefined().isEmpty());
        assertEquals(SecurityRoles.BUILTIN_ROLES.size(), m_roleDao.countAll());
    }

    @Test
    @Transactional
    public void testFindByName() {
        final SecurityRole admin = m_roleDao.findByName(SecurityRoles.ROLE_ADMIN);
        assertNotNull(admin);
        assertTrue(admin.isBuiltin());
        assertNull(m_roleDao.findByName("ROLE_NOPE"));
        assertNull("names are matched exactly", m_roleDao.findByName("role_admin"));
    }

    @Test
    @Transactional
    public void testCreateUserDefinedRole() {
        m_roleDao.save(new SecurityRole(SecurityRoles.toRoleName("operator"), "Operators"));
        m_roleDao.flush();
        m_roleDao.clear();

        final SecurityRole operator = m_roleDao.findByName("ROLE_OPERATOR");
        assertNotNull(operator);
        assertFalse(operator.isBuiltin());
        assertEquals("Operators", operator.getDescription());
        assertNotNull(operator.getCreatedTime());
        assertNotNull(operator.getLastModified());
        assertEquals(List.of(operator), m_roleDao.findUserDefined());
        assertFalse(m_roleDao.findBuiltin().contains(operator));
    }

    @Test
    @Transactional
    public void testBuiltinRoleCannotBeDeleted() {
        final SecurityRole admin = m_roleDao.findByName(SecurityRoles.ROLE_ADMIN);
        assertThrows(IllegalArgumentException.class, () -> m_roleDao.deleteUserDefined(SecurityRoles.ROLE_ADMIN));
        assertThrows(IllegalArgumentException.class, () -> m_roleDao.delete(admin));
        assertThrows(IllegalArgumentException.class, () -> m_roleDao.delete(admin.getId()));
        m_roleDao.flush();
        assertNotNull(m_roleDao.findByName(SecurityRoles.ROLE_ADMIN));
    }

    @Test
    @Transactional
    public void testDeleteUserDefinedRevokesItFromUsers() {
        final SecurityRole operator = new SecurityRole("ROLE_OPERATOR", null);
        m_roleDao.save(operator);
        final UserAccount user = new UserAccount("jane");
        user.getSecurityRoles().add(m_roleDao.findByName(SecurityRoles.ROLE_USER));
        user.getSecurityRoles().add(operator);
        m_userDao.save(user);
        m_userDao.flush();

        assertTrue(m_roleDao.deleteUserDefined("ROLE_OPERATOR"));
        assertFalse("deleting a missing role is a no-op", m_roleDao.deleteUserDefined("ROLE_OPERATOR"));
        m_roleDao.flush();
        m_roleDao.clear();

        assertNull(m_roleDao.findByName("ROLE_OPERATOR"));
        final UserAccount reloaded = m_userDao.findByUsername("jane");
        assertEquals(1, reloaded.getSecurityRoles().size());
        assertTrue(reloaded.hasSecurityRole(SecurityRoles.ROLE_USER));
    }

    @Test
    @Transactional
    public void testRoleNameIsUnique() {
        UserMgmtSql.assertRejected(m_sessionFactory, UserMgmtSql.UNIQUE_VIOLATION,
                "INSERT INTO security_roles (id, name, builtin) VALUES (nextval('security_roles_id_seq'), ?, false)",
                SecurityRoles.ROLE_ADMIN);
    }
}
