package org.opennms.netmgt.rrd.newts;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.cassandraunit.JUnitNewtsCassandra;
import org.cassandraunit.JUnitNewtsCassandraExecutionListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.newts.api.Timestamp;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@TestExecutionListeners({
    JUnitNewtsCassandraExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-rrd.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
        "org.opennms.newts.config.hostname=" + NewtsRrdStrategyIT.CASSANDRA_HOST,
        "org.opennms.newts.config.port=" + NewtsRrdStrategyIT.CASSANDRA_PORT,
        "org.opennms.newts.config.keyspace=" + NewtsRrdStrategyIT.NEWTS_KEYSPACE,
        "org.opennms.newts.config.max_batch_delay=0", // No delay
        "org.opennms.rrd.strategyClass=org.opennms.netmgt.rrd.newts.NewtsRrdStrategy",
        "org.opennms.rrd.usequeue=false"
})
@JUnitNewtsCassandra(
        host=NewtsRrdStrategyIT.CASSANDRA_HOST,
        port=NewtsRrdStrategyIT.CASSANDRA_PORT,
        keyspace=NewtsRrdStrategyIT.NEWTS_KEYSPACE
)
public class NewtsRrdStrategyIT {

    protected static final String CASSANDRA_HOST = "localhost";
    protected static final int CASSANDRA_PORT = 9043;
    protected static final String NEWTS_KEYSPACE = "newts";

    @Autowired
    private RrdStrategy<RrdDef, RrdDb> m_rrdStrategy;

    @Autowired
    private ResourceStorageDao m_resourceStorageDao;

    @Test
    public void createOpenUpdateCloseRead() throws Exception {
        String rrdBaseDir = System.getProperty("rrd.base.dir");

        // Go through the life-cycle of creating and updating an .rrd file
        RrdDataSource ds1 = new RrdDataSource("x", "GAUGE", 900, "0", "100");
        RrdDataSource ds2 = new RrdDataSource("y", "GAUGE", 900, "0", "100");
        RrdDef def = m_rrdStrategy.createDefinition("test", rrdBaseDir + "/snmp/1", "loadavg", 1,
                Lists.newArrayList(ds1, ds2),
                Lists.newArrayList("RRA:AVERAGE:0.5:1:1000"));

        Map<String, String> attributes = Maps.newHashMap();
        attributes.put("!key!", "#value#");
        m_rrdStrategy.createFile(def, attributes);

        // Add metrics to the file we created above
        String fileName = rrdBaseDir + "/snmp/1/loadavg.newts";
        RrdDb db = m_rrdStrategy.openFile(fileName);

        long timestampInSeconds = Timestamp.now().asSeconds();
        m_rrdStrategy.updateFile(db, "test", String.format("%d:U:1.0", timestampInSeconds));
        m_rrdStrategy.updateFile(db, "test", String.format("%d:1.0:2.0", 1 + timestampInSeconds));
        m_rrdStrategy.updateFile(db, "test", String.format("%d:2.0:3.0", 2 + timestampInSeconds));
        m_rrdStrategy.closeFile(db);

        // Wait for the results to be flushed
        Thread.sleep(5000);

        // Verify the results
        double delta = 0.001;
        assertEquals(2.0, m_rrdStrategy.fetchLastValueInRange(fileName, "x", 1, 60*60*1000), delta);
        assertEquals(3.0, m_rrdStrategy.fetchLastValueInRange(fileName, "y", 1, 60*60*1000), delta);

        ResourcePath resourcePath = ResourcePath.get("snmp", "1");
        Map<String, String> metaDataAttributes = m_resourceStorageDao.getMetaData(resourcePath);
        assertEquals("#value#", metaDataAttributes.get("!key!"));
    }
}
