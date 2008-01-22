package org.opennms.web.element;

import org.opennms.core.resource.Vault;
import org.opennms.netmgt.dao.db.PopulatedTemporaryDatabaseTestCase;

public class NetworkElementFactoryTest extends PopulatedTemporaryDatabaseTestCase {
    @Override
    protected void setUp() throws Exception {
        setSetupIpLike(true);
        
        super.setUp();
        
        Vault.setDataSource(getDataSource());
    }
    
    public void testGetNodesWithIpLikeOneInterface() throws Exception {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime, nodeType) VALUES (1, now(), 'A')");
        jdbcTemplate.update("INSERT INTO ipInterface (nodeId, ipAddr, isManaged) VALUES (1, '1.1.1.1', 'M')");
        
        assertEquals("node count in DB", 1, jdbcTemplate.queryForInt("SELECT count(*) FROM node"));
        assertEquals("ipInterface count in DB", 1, jdbcTemplate.queryForInt("SELECT count(*) FROM ipInterface"));
        
        Node[] nodes = NetworkElementFactory.getNodesWithIpLike("*.*.*.*");
        assertEquals("node count", 1, nodes.length);
    }
    
    // bug introduced in revision 2932
    public void testGetNodesWithIpLikeTwoInterfaces() throws Exception {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime, nodeType) VALUES (1, now(), 'A')");
        jdbcTemplate.update("INSERT INTO ipInterface (nodeId, ipAddr, isManaged) VALUES (1, '1.1.1.1', 'M')");
        jdbcTemplate.update("INSERT INTO ipInterface (nodeId, ipAddr, isManaged) VALUES (1, '1.1.1.2', 'M')");
        
        assertEquals("node count in DB", 1, jdbcTemplate.queryForInt("SELECT count(*) FROM node"));
        assertEquals("ipInterface count in DB", 2, jdbcTemplate.queryForInt("SELECT count(*) FROM ipInterface"));

        Node[] nodes = NetworkElementFactory.getNodesWithIpLike("*.*.*.*");
        assertEquals("node count", 1, nodes.length);
    }
}
