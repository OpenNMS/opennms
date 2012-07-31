package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class HibernateCriteriaConverterTest implements InitializingBean {
    @Autowired
    DatabasePopulator m_populator;
    
    @Autowired
    NodeDao m_nodeDao;

    private static boolean m_populated = false;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
    	MockLogAppender.setupLogging(true);
        try {
            if (!m_populated) {
                m_populator.populateDatabase();
            }
        } catch (final Throwable e) {
            e.printStackTrace(System.err);
        } finally {
            m_populated = true;
        }
    }

	@Test
	public void testNodeQuery() throws Exception {
		List<OnmsNode> nodes;

		// first, try with OnmsCriteria
		final OnmsCriteria crit = new OnmsCriteria(OnmsNode.class);
		crit.add(org.hibernate.criterion.Restrictions.isNotNull("id"));
		nodes = m_nodeDao.findMatching(crit);
		assertEquals(6, nodes.size());

		// then the same with the builder
		final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
		cb.isNotNull("id");
		nodes = m_nodeDao.findMatching(cb.toCriteria());
		assertEquals(6, nodes.size());
		
		cb.eq("label", "node1").join("ipInterfaces", "ipInterface").eq("ipInterface.ipAddress", "192.168.1.1");
		nodes = m_nodeDao.findMatching(cb.toCriteria());
		assertEquals(1, nodes.size());
	}

    @Test
    public void testNodeIlikeQuery() {
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.isNotNull("id").eq("label", "node1").alias("ipInterfaces", "ipInterface", JoinType.LEFT_JOIN).ilike("ipInterface.ipAddress", "1%");
        final List<OnmsNode> nodes = m_nodeDao.findMatching(cb.toCriteria());
        assertEquals(3, nodes.size());
    }

	@Test
	@Transactional
	public void testDistinctQuery() {
		List<OnmsNode> nodes = null;

		final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
		cb.isNotNull("id").distinct();
		cb.eq("label", "node1").join("ipInterfaces", "ipInterface", JoinType.LEFT_JOIN).eq("ipInterface.ipAddress", "192.168.1.1");

		nodes = m_nodeDao.findMatching(cb.toCriteria());
		assertEquals(1, nodes.size());
		assertEquals(Integer.valueOf(1), nodes.get(0).getId());
	}
}
