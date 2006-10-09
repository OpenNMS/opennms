package org.opennms.install;

import java.util.Arrays;
import java.util.LinkedList;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

public abstract class AbstractTransactionalTemporaryDatabaseSpringContextTests
    extends AbstractTransactionalDataSourceSpringContextTests {
    
    private PopulatedTemporaryDatabaseTestCase m_populatedTempDb;

    @Override
    protected final ConfigurableApplicationContext
            loadContextLocations(String[] locations) throws Exception {
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
    final protected void onSetUpInTransaction() throws Exception {
        if (!PopulatedTemporaryDatabaseTestCase.isEnabled()) {
            return;
        }
        
        super.onSetUpInTransaction();
        
        onSetUpInTransactionIfEnabled();
    }
    
    protected void onSetUpInTransactionIfEnabled() {
        // Empty by default
    }
    
    @Override
    protected void runTest() throws Throwable {
        setDirty();

        if (!PopulatedTemporaryDatabaseTestCase.isEnabled()) {
            PopulatedTemporaryDatabaseTestCase.notifyTestDisabled(getName());
            return;
        }

        super.runTest();
    }

    @Override
    final protected void onTearDownInTransaction() throws Exception {
        if (!PopulatedTemporaryDatabaseTestCase.isEnabled()) {
            return;
        }
        
        super.onTearDownInTransaction();
        
        onTearDownInTransactionIfEnabled();
    }
    
    protected void onTearDownInTransactionIfEnabled() {
        // Empty by default
    }
    
    @Override
    protected void onTearDownAfterTransaction() throws Exception {
        if (m_populatedTempDb != null) {
            m_populatedTempDb.tearDown();
        }
    }
}
