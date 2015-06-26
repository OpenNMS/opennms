/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.gwt.web.ui.asset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.gwt.web.ui.asset.server.AssetServiceImpl;
import org.opennms.gwt.web.ui.asset.shared.AssetCommand;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.api.Authentication;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-mockDao.xml",
		"classpath*:/META-INF/opennms/component-dao.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class AssetServiceImplTest implements InitializingBean {

	@Autowired
	private DistPollerDao m_distPollerDao;

	@Autowired
	private NodeDao m_nodeDao;

	@Autowired
	private AssetRecordDao m_assetRecordDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;

	// private SecurityContextService m_securityContextService;

	private final GrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority(Authentication.ROLE_ADMIN);
	
	/*
	private final GrantedAuthority ROLE_PROVISION = new GrantedAuthorityImpl(Authentication.ROLE_PROVISION);
	private final GrantedAuthority ROLE_USER = new GrantedAuthorityImpl(Authentication.ROLE_USER);
	*/

	private final String USERNAME = "opennms";

	private final String PASS = "r0c|<Z";
	
	private User validAdmin;
	
	/*
	private User invalidAdmin;
	
	private User validProvision;
	
	private User invalidProvision;
	
	private User validUser;
	
	private User invalidUser;
	
	private User validPower;
	
	private User invalidPower;
	*/
	
	private org.springframework.security.core.Authentication m_auth;

	private SecurityContext m_context;

	@Override
	public void afterPropertiesSet() throws Exception {
	    org.opennms.core.spring.BeanUtils.assertAutowiring(this);
	}

	@Before
	public void setUp() {
		m_databasePopulator.populateDatabase();
		m_context = new SecurityContextImpl();
		
		validAdmin = new User(USERNAME, PASS, true, true, true, true,
				Arrays.asList(new GrantedAuthority[] { ROLE_ADMIN }));
		
		/*
		invalidAdmin = new User(USERNAME, PASS, true, true, true, true,
				new GrantedAuthority[] { ROLE_ADMIN });
		
		validProvision = new User(USERNAME, PASS, true, true, true, true,
				new GrantedAuthority[] { ROLE_PROVISION });
		invalidProvision = new User(USERNAME, PASS, true, true, true, true,
				new GrantedAuthority[] { ROLE_PROVISION });
		
		validUser = new User(USERNAME, PASS, true, true, true, true,
				new GrantedAuthority[] { ROLE_USER });
		invalidUser = new User(USERNAME, PASS, true, true, true, true,
				new GrantedAuthority[] { ROLE_USER });

		validPower = new User(USERNAME, PASS, true, true, true, true,
				new GrantedAuthority[] { ROLE_ADMIN, ROLE_PROVISION });
		invalidPower = new User(USERNAME, PASS, true, true, true, true,
				new GrantedAuthority[] { ROLE_USER, ROLE_PROVISION });
				*/

		m_auth = new PreAuthenticatedAuthenticationToken(validAdmin, new Object());
		m_context.setAuthentication(m_auth);
		SecurityContextHolder.setContext(m_context);
		// m_securityContextService = new SpringSecurityContextService();
	}

	@After
	public void tearDown() {
		for (final OnmsNode node : m_nodeDao.findAll()) {
			m_nodeDao.delete(node);
		}
		m_nodeDao.flush();
	}

	@Test
	public void testCreateAndGets() {
		OnmsNode onmsNode = new OnmsNode(m_distPollerDao.load("localhost"));
		onmsNode.setLabel("myNode");
		m_nodeDao.save(onmsNode);
		OnmsAssetRecord assetRecord = onmsNode.getAssetRecord();
		assetRecord.setAssetNumber("imported-id: 7");
		m_assetRecordDao.update(assetRecord);
		m_assetRecordDao.flush();

		// Test findAll method
		Collection<OnmsAssetRecord> assetRecords = m_assetRecordDao.findAll();
		assertEquals(7, assetRecords.size());

		// Test countAll method
		assertEquals(7, m_assetRecordDao.countAll());
	}

	@Test
	public void testAssetServiceImpl() {
		OnmsNode onmsNode = new OnmsNode(m_distPollerDao.load("localhost"));
		onmsNode.setLabel("myNode");
		m_nodeDao.save(onmsNode);
		OnmsAssetRecord assetRecord = onmsNode.getAssetRecord();
		assetRecord.setAssetNumber("imported-id: " + onmsNode.getId());
		assetRecord.setAdmin("supermario");
		assetRecord.getGeolocation().setAddress1("my address");
		assetRecord.getGeolocation().setZip("myzip");
		m_assetRecordDao.update(assetRecord);
		m_assetRecordDao.flush();

		onmsNode = new OnmsNode(m_distPollerDao.load("localhost"));
		onmsNode.setLabel("myNode2");
		m_nodeDao.save(onmsNode);
		assetRecord = onmsNode.getAssetRecord();
		assetRecord.setAssetNumber("imported-id: 23");
		assetRecord.setAdmin("mediummario");
                assetRecord.getGeolocation().setAddress1("youraddress");
		assetRecord.getGeolocation().setZip("yourzip");
		m_assetRecordDao.update(assetRecord);
		m_assetRecordDao.flush();

		AssetServiceImpl assetServiceImpl = new AssetServiceImpl();
		assetServiceImpl.setNodeDao(m_nodeDao);
		assetServiceImpl.setAssetRecordDao(m_assetRecordDao);

		System.out.println("AssetCommand: "
				+ assetServiceImpl.getAssetByNodeId(onmsNode.getId()).toString());
		System.out.println("Suggestions: "
				+ assetServiceImpl.getAssetSuggestions());
		assertTrue("Test save or update by admin.", assetServiceImpl.getAssetByNodeId(onmsNode.getId()).getAllowModify());
	}

//	@Test
//	public void successAllowModifyAssetByAdmin() {
//		AssetServiceImpl assetServiceImpl = new AssetServiceImpl();
//		assetServiceImpl.setNodeDao(m_nodeDao);
//		assetServiceImpl.setAssetRecordDao(m_assetRecordDao);
//		m_auth = new PreAuthenticatedAuthenticationToken(
//				validAdmin, new Object());
//		m_context.setAuthentication(m_auth);
//		SecurityContextHolder.setContext(m_context);
//		m_securityContextService = new SpringSecurityContextService();
//		assertTrue("Test save or update by admin.", assetServiceImpl.getAssetByNodeId(7).getAllowModify());
//	}
//
//	@Test
//	public void failAllowModifyAssetByAdmin() {
//		AssetServiceImpl assetServiceImpl = new AssetServiceImpl();
//		assetServiceImpl.setNodeDao(m_nodeDao);
//		assetServiceImpl.setAssetRecordDao(m_assetRecordDao);
//		m_auth = new PreAuthenticatedAuthenticationToken(
//				invalidAdmin, new Object());
//		m_context.setAuthentication(m_auth);
//		SecurityContextHolder.setContext(m_context);
//		m_securityContextService = new SpringSecurityContextService();
//		assertFalse("Test save or update by admin.", assetServiceImpl.getAssetByNodeId(7).getAllowModify());
//	}
	
	
	@Test
	public void testSaveOrUpdate() {
		OnmsNode onmsNode = new OnmsNode(m_distPollerDao.load("localhost"));
		onmsNode.setLabel("myNode");
		m_nodeDao.save(onmsNode);
		OnmsAssetRecord assetRecord = onmsNode.getAssetRecord();
		assetRecord.setAssetNumber("imported-id: " + onmsNode.getId());
		assetRecord.setAdmin("supermario");
		assetRecord.setLastModifiedDate(new Date());
		assetRecord.getGeolocation().setAddress1("220 Chatham Business Drive");
                assetRecord.getGeolocation().setCity("Pittsboro");
                assetRecord.getGeolocation().setState("NC");
                assetRecord.getGeolocation().setZip("27312");
                assetRecord.getGeolocation().setCountry("US");
                assetRecord.getGeolocation().setLatitude(35.717582f);
                assetRecord.getGeolocation().setLongitude(-79.161800f);
		m_assetRecordDao.update(assetRecord);
		m_assetRecordDao.flush();

		AssetCommand assetCommand = new AssetCommand();
		BeanUtils.copyProperties(assetRecord, assetCommand);

		System.out.println("AssetCommand (Source): " + assetCommand);
		System.out.println("Asset to Save (Target): " + assetRecord);

		AssetServiceImpl assetServiceImpl = new AssetServiceImpl();
		assetServiceImpl.setNodeDao(m_nodeDao);
		assetServiceImpl.setAssetRecordDao(m_assetRecordDao);
		System.out.println();
		assertTrue(assetServiceImpl.saveOrUpdateAssetByNodeId(onmsNode.getId(), assetCommand));
		
		OnmsAssetRecord updated = m_assetRecordDao.get(assetRecord.getId());
		assertEquals(assetRecord.getGeolocation().getAddress1(), updated.getGeolocation().getAddress1());
                assertEquals(assetRecord.getGeolocation().getState(), updated.getGeolocation().getState());
                assertEquals(assetRecord.getGeolocation().getCity(), updated.getGeolocation().getCity());
                assertEquals(assetRecord.getGeolocation().getZip(), updated.getGeolocation().getZip());
                assertEquals(assetRecord.getGeolocation().getCountry(), updated.getGeolocation().getCountry());
                assertEquals(assetRecord.getGeolocation().getLongitude(), updated.getGeolocation().getLongitude());
                assertEquals(assetRecord.getGeolocation().getLatitude(), updated.getGeolocation().getLatitude());
	}

	@Test
	public void testAssetSuggestion() {
		OnmsNode onmsNode = new OnmsNode(m_distPollerDao.load("localhost"));
		onmsNode.setLabel("your Node");
		onmsNode.setSysObjectId("mySysOid");
		m_nodeDao.save(onmsNode);
		OnmsAssetRecord assetRecord = onmsNode.getAssetRecord();
		assetRecord.setAssetNumber("imported-id: 666");
		assetRecord.setAdmin("medium mario");
		assetRecord.setLastModifiedDate(new Date());
		assetRecord.getGeolocation().setZip("his zip");
		m_assetRecordDao.update(assetRecord);
		m_assetRecordDao.flush();

		onmsNode = new OnmsNode(m_distPollerDao.load("localhost"));
		onmsNode.setLabel("his Node");
		m_nodeDao.save(onmsNode);
		assetRecord = onmsNode.getAssetRecord();
		assetRecord.setAssetNumber("imported-id: 999");
		assetRecord.setAdmin("super mario");
		assetRecord.setLastModifiedDate(new Date());
		assetRecord.getGeolocation().setZip("your zip");
		m_assetRecordDao.update(assetRecord);
		m_assetRecordDao.flush();

		AssetServiceImpl assetServiceImpl = new AssetServiceImpl();
		assetServiceImpl.setNodeDao(m_nodeDao);
		assetServiceImpl.setAssetRecordDao(m_assetRecordDao);
		System.out.println("Asset: " + assetServiceImpl.getAssetByNodeId(onmsNode.getId()));
	}

	@Test
	public void testBasicBeanSanitizer() {
		CommandBeanMockup bean = new CommandBeanMockup();
		bean = (CommandBeanMockup) AssetServiceImpl
				.sanitizeBeanStringProperties(bean, null);

		assertTrue("Script property is sanitized",
				WebSecurityUtils.sanitizeString("<script>foo</script>", false)
						.equals(bean.getScript()));
		assertTrue("Script property is not sanitized with Html allowed",
				!WebSecurityUtils.sanitizeString("<script>foo</script>", true)
						.equals(bean.getScript()));

		assertTrue("HtmlTable is sanitized and html removed", WebSecurityUtils
				.sanitizeString("<table>", false).equals(bean.getHtmlTable()));
		assertTrue(
				"Not, HtmlTable is sanitized with Html allowed",
				!WebSecurityUtils.sanitizeString("<table>", true).equals(
						bean.getHtmlTable()));
	}

	@Test
	public void testBeanSanitizerWithHtmlAllowList() {
		CommandBeanMockup bean = new CommandBeanMockup();
		HashSet<String> set = new HashSet<String>();
		set.add("htmltable");
		bean = (CommandBeanMockup) AssetServiceImpl
				.sanitizeBeanStringProperties(bean, set);

		assertTrue("Script property is sanitized no Html allowed",
				WebSecurityUtils.sanitizeString("<script>foo</script>", false)
						.equals(bean.getScript()));
		assertTrue("Not, Script property is sanitized with Html allowed",
				!WebSecurityUtils.sanitizeString("<script>foo</script>", true)
						.equals(bean.getScript()));

		assertTrue(
				"HtmlTable is sanitzied with Html allowed so, no changes",
				WebSecurityUtils.sanitizeString("<table>", true).equals(
						bean.getHtmlTable()));
		assertTrue(
				"Not, HtmlTable is sanitized and html removed",
				!WebSecurityUtils.sanitizeString("<table>", false).equals(
						bean.getHtmlTable()));
	}
}
