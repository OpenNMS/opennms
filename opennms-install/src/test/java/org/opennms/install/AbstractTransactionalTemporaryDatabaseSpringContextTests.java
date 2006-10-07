package org.opennms.install;

import java.util.Arrays;
import java.util.LinkedList;

import javax.sql.DataSource;

import org.opennms.netmgt.config.DataSourceFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

public abstract class AbstractTransactionalTemporaryDatabaseSpringContextTests
    extends AbstractTransactionalDataSourceSpringContextTests {
    
    private PopulatedTemporaryDatabaseTestCase m_populatedTempDb;

    @Override
    protected final ConfigurableApplicationContext
            loadContextLocations(String[] locations) {
        if (!PopulatedTemporaryDatabaseTestCase.isEnabled()) {
            setDependencyCheck(false);
            return super.loadContextLocations(new String[0]);
        }
        
        LinkedList<String> newLocations = new LinkedList<String>();
        newLocations.addAll(Arrays.asList(locations));
        newLocations.add("classpath:META-INF/opennms/applicationContext-AbstractTransactionalTemporaryDatabaseSpringContextTests.xml"); 
        return super.loadContextLocations((String[]) newLocations.toArray(new String[0]));
    }
    
    public void setPopulatedTemporaryDatabaseTestCase(PopulatedTemporaryDatabaseTestCase testCase) {
        m_populatedTempDb = testCase;
    }
    
    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();

        setDirty();
        
        if (!PopulatedTemporaryDatabaseTestCase.isEnabled()) {
            PopulatedTemporaryDatabaseTestCase.notifyTestDisabled(getName());
        }
    }
    
    @Override
    protected void onTearDownAfterTransaction() throws Exception {
        if (m_populatedTempDb != null) {
            m_populatedTempDb.tearDown();
        }
    }
}
