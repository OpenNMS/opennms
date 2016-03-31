/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.cassandraunit;

import com.datastax.driver.core.CloseFuture;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.CQLDataSet;
import org.cassandraunit.dataset.cql.FileCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.opennms.newts.cassandra.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Provision an instance of Cassandra with the Newts keyspace when the {@link JUnitNewtsCassandra}
 * annotation is present. 
 *
 * @author jwhite
 */
public class JUnitNewtsCassandraExecutionListener extends AbstractTestExecutionListener {

    private static final Logger LOG = LoggerFactory.getLogger(JUnitNewtsCassandraExecutionListener.class);

    private static final String KEYSPACE_PLACEHOLDER = "$KEYSPACE$";

    private CassandraCQLUnit m_cassandraUnit;
    private boolean m_initialized = false;
    private Session m_session;
    private Cluster m_cluster;

    /**
     * We currently use a newer driver than the one associated with the
     * cassandra-unit package and need to override the load() method
     * to make things work.
     *
     * This shouldn't be necessary when upgrading to cassandra-unit >= 3.0.0
     *
     * @author jwhite
     */
    private static class MyCassandraCQLUnit extends CassandraCQLUnit {
        private final CQLDataSet dataSet;

        public MyCassandraCQLUnit(CQLDataSet dataSet, String configurationFileName) {
            super(dataSet, configurationFileName);
            this.dataSet = dataSet;
        }

        @Override
        protected void load() {
            String hostIp = EmbeddedCassandraServerHelper.getHost();
            int port = EmbeddedCassandraServerHelper.getNativeTransportPort();
            cluster = new Cluster.Builder().addContactPoint(hostIp).withPort(port).build();
            session = cluster.connect();
            CQLDataLoader dataLoader = new CQLDataLoader(session);
            dataLoader.load(dataSet);
            session = dataLoader.getSession();
        }
    }

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        JUnitNewtsCassandra config = findNewtsCassandraAnnotation(testContext);
        if (config == null) {
            return;
        }

        m_cassandraUnit = new MyCassandraCQLUnit(getDataSet(config.keyspace()), config.configurationFileName());

        if (!m_initialized) {
            m_cassandraUnit.before();
            m_session = m_cassandraUnit.session;
            m_cluster = m_cassandraUnit.cluster;
            m_initialized = true;
        }
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        JUnitNewtsCassandra config = findNewtsCassandraAnnotation(testContext);
        if (config == null) {
            return;
        }

        if(m_session!=null){
            LOG.debug("session shutdown");
            CloseFuture closeFuture = m_session.closeAsync();
            closeFuture.force();
        }
        if (m_cluster != null) {
            LOG.debug("cluster shutdown");
            m_cluster.close();
        }
    }

    private static JUnitNewtsCassandra findNewtsCassandraAnnotation(TestContext testContext) {
        Method testMethod = testContext.getTestMethod();
        if (testMethod != null) {
            JUnitNewtsCassandra config = testMethod.getAnnotation(JUnitNewtsCassandra.class);
            if (config != null) {
                return config;
            }
        }

        Class<?> testClass = testContext.getTestClass();
        return testClass.getAnnotation(JUnitNewtsCassandra.class);
    }

    public static CQLDataSet getDataSet(String keyspace) {
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
            schemasString = schemasString.replace(KEYSPACE_PLACEHOLDER, keyspace);

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
            return new FileCQLDataSet(schemaFile.getAbsolutePath(), false, true, keyspace);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
