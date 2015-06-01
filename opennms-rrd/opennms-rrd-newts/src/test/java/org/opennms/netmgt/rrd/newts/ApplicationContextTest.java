package org.opennms.netmgt.rrd.newts;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.cassandraunit.dataset.CQLDataSet;
import org.cassandraunit.dataset.cql.FileCQLDataSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.support.ConditionalNewtsDaoContext;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.newts.cassandra.AbstractCassandraTestCase;
import org.opennms.newts.persistence.cassandra.CassandraSampleRepository;
import org.opennms.newts.cassandra.Schema;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-empty.xml",
})
@JUnitConfigurationEnvironment
public class ApplicationContextTest extends AbstractCassandraTestCase {

    @Test
    public void canLoadTheApplicationContext() {
        System.setProperty("org.opennms.newts.config.hostname", CASSANDRA_HOST);
        System.setProperty("org.opennms.newts.config.port", Integer.toString(CASSANDRA_PORT));
        System.setProperty("org.opennms.newts.config.compression", CASSANDRA_COMPRESSION);
        System.setProperty("org.opennms.newts.config.keyspace", CASSANDRA_KEYSPACE);
        System.setProperty(ConditionalNewtsDaoContext.RRD_STRATEGY_CLASS_PROPERTY_NAME, ConditionalNewtsDaoContext.NEWTS_RRD_STRATEGY_CLASS_NAME);

        try(ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "/META-INF/opennms/applicationContext-soa.xml",
                "/META-INF/opennms/component-dao-ext.xml",
                "/META-INF/opennms/component-rrd.xml")) {
            NewtsRrdStrategy newtsRrdStrategy = (NewtsRrdStrategy) context.getBean("newtsRrdStrategy");
            assertNotNull(newtsRrdStrategy);
        }
    }

    @Override
    public CQLDataSet getDataSet() {
        try {
            final Schema searchSchema = new org.opennms.newts.cassandra.search.Schema();
            final Schema samplesSchema = new org.opennms.newts.persistence.cassandra.Schema();
            final List<Schema> schemas = Lists.newArrayList(searchSchema, samplesSchema);

            //  Concatenate the schema strings
            String schemasString = "";
            for (Schema schema : schemas) {
                schemasString += IOUtils.toString(schema.getInputStream());
            }

            // Replace the placeholders
            schemasString = schemasString.replace(KEYSPACE_PLACEHOLDER, CASSANDRA_KEYSPACE);

            // Split the resulting script back into lines
            String lines[] = schemasString.split("\\r?\\n");

            // Remove duplicate CREATE KEYSPACE statements;
            StringBuffer sb = new StringBuffer();            
            boolean foundCreateKeyspace = false;
            boolean skipNextLine = false;
            for (String line : lines) {
                if (line.startsWith("CREATE KEYSPACE")) {
                    if (!foundCreateKeyspace) {
                        foundCreateKeyspace = true;
                        sb.append(line);
                        sb.append("\n");
                    } else {
                        skipNextLine = true;
                    }
                } else if (skipNextLine) {
                    skipNextLine = false;
                } else {
                    sb.append(line);
                    sb.append("\n");
                }
            }

            // Write the results to disk
            File schemaFile = File.createTempFile("schema-", ".cql", new File("target"));
            schemaFile.deleteOnExit();
            Files.write(sb.toString(), schemaFile, Charsets.UTF_8);
            return new FileCQLDataSet(schemaFile.getAbsolutePath(), false, true, CASSANDRA_KEYSPACE);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
