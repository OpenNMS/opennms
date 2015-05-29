package org.opennms.netmgt.rrd.newts;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.cassandraunit.dataset.CQLDataSet;
import org.cassandraunit.dataset.cql.FileCQLDataSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.cassandra.AbstractCassandraTestCase;
import org.opennms.newts.persistence.cassandra.Schema;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-empty.xml",
})
@JUnitConfigurationEnvironment
public class NewtsRrdStrategyITest extends AbstractCassandraTestCase {

    private NewtsRrdStrategy newtsRrdStrategy;

    private String opennmsHome;

    @Before
    public void setUp() {
        newtsRrdStrategy = new NewtsRrdStrategy();

        // Point to our test server
        Properties props = new Properties();
        props.put(NewtsUtils.HOSTNAME_PROPERTY, CASSANDRA_HOST);
        props.put(NewtsUtils.PORT_PROPERTY, "" + CASSANDRA_PORT);
        props.put(NewtsUtils.KEYSPACE_PROPERTY, CASSANDRA_KEYSPACE);
        newtsRrdStrategy.setConfigurationProperties(props);

        opennmsHome = System.getProperty("opennms.home");
    }

    @Test
    public void createOpenUpdateCloseRead() throws NumberFormatException, RrdException, InterruptedException {
        // Go through the life-cycle of creating and updating an .rrd file
        RrdDataSource ds1 = new RrdDataSource("x", "GAUGE", 900, "0", "100");
        RrdDataSource ds2 = new RrdDataSource("y", "GAUGE", 900, "0", "100");
        RrdDef def = newtsRrdStrategy.createDefinition("test", opennmsHome + "/share/rrd/snmp/1", "loadavg", 1,
                Lists.newArrayList(ds1, ds2),
                Lists.newArrayList("RRA:AVERAGE:0.5:1:1000"));

        Map<String, String> attributes = Maps.newHashMap();
        attributes.put("key", "value");
        newtsRrdStrategy.createFile(def, attributes);

        // Add metrics to the file we created above
        String fileName = opennmsHome + "/share/rrd/snmp/1/loadavg.newts";
        RrdDb db = newtsRrdStrategy.openFile(fileName);

        long timestampInSeconds = Timestamp.now().asSeconds();
        newtsRrdStrategy.updateFile(db, "test", String.format("%d:U:1.0", timestampInSeconds));
        newtsRrdStrategy.updateFile(db, "test", String.format("%d:1.0:2.0", 1 + timestampInSeconds));
        newtsRrdStrategy.updateFile(db, "test", String.format("%d:2.0:3.0", 2 + timestampInSeconds));
        newtsRrdStrategy.closeFile(db);

        // Wait for the results to be flushed
        Thread.sleep(5000);

        // Verify the results
        double delta = 0.001;
        assertEquals(2.0, newtsRrdStrategy.fetchLastValueInRange(fileName, "x", 1, 60*60*1000), delta);
        assertEquals(3.0, newtsRrdStrategy.fetchLastValueInRange(fileName, "y", 1, 60*60*1000), delta);
    }

    @Override
    public CQLDataSet getDataSet() {
        try {
            final Schema persistenceSchema = new Schema();
            String schema = IOUtils.toString(persistenceSchema.getInputStream());
            schema = schema.replace(KEYSPACE_PLACEHOLDER, CASSANDRA_KEYSPACE);

            File schemaFile = File.createTempFile("schema-", ".cql", new File("target"));
            schemaFile.deleteOnExit();
            Files.write(schema, schemaFile, Charsets.UTF_8);

            return new FileCQLDataSet(schemaFile.getAbsolutePath(), false, true, CASSANDRA_KEYSPACE);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
