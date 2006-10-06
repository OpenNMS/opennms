package org.opennms.install;

import java.util.Arrays;
import java.util.LinkedList;

import javax.sql.DataSource;

import org.opennms.netmgt.config.DataSourceFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

public abstract class AbstractTransactionalTemporaryDatabaseSpringContextTests
    extends AbstractTransactionalDataSourceSpringContextTests {
    
    private PopulatedTemporaryDatabaseTestCaseTest m_populatedTempDb;
    
    public AbstractTransactionalTemporaryDatabaseSpringContextTests() {
        m_populatedTempDb = new PopulatedTemporaryDatabaseTestCaseTest();
    }

    @Override
    protected final ConfigurableApplicationContext
            loadContextLocations(String[] locations) {
        LinkedList<String> newLocations = new LinkedList<String>();
        newLocations.addAll(Arrays.asList(locations));
        newLocations.add("classpath:META-INF/opennms/applicationContext-AbstractTransactionalTemporaryDatabaseSpringContextTests.xml"); 
        return super.loadContextLocations((String[]) newLocations.toArray(new String[0]));
    }
    
    protected boolean isDisabledInThisEnvironment(String testMethodName) {
        return m_populatedTempDb.isDisabledInThisEnvironment(testMethodName);
    }
    
    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        
        try {
            m_populatedTempDb.setUp();
        } catch (Exception e) {
            m_populatedTempDb.fail("setUp failed on " + m_populatedTempDb.getClass().getName(), e);
        }
        
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (!(dataSource instanceof TemporaryDatabaseDataSource)) {
            throw new Exception("dataSource is not an instance of "
                                + "TemporaryDatabaseDataSource");
        }
        
        TemporaryDatabaseDataSource temporaryDatabaseDataSource =
            (TemporaryDatabaseDataSource) dataSource;
        
        temporaryDatabaseDataSource.setTemporaryDatabaseTestCase(m_populatedTempDb);
        DataSourceFactory.setInstance(dataSource);
    }
    
    @Override
    protected void onTearDownAfterTransaction() throws Exception {
        m_populatedTempDb.tearDown();
    }
}
