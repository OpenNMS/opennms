/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.config.threshd.ThresholdType;
import org.opennms.netmgt.threshd.api.ThresholdStateMonitor;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-testPostgresBlobStore.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class ThresholdStateIT {
    @Autowired
    private BlobStore blobStore;

    private final ThresholdingSession thresholdingSession = MockSession.getSession();
    private ThresholdStateMonitor monitor;

    @Before
    public void setup() {
        monitor = new BlobStoreAwareMonitor(blobStore);
        when(thresholdingSession.getThresholdStateMonitor()).thenReturn(monitor);
        when(thresholdingSession.getBlobStore()).thenReturn(blobStore);
    }
    
    @After
    public void cleanup() {
        blobStore.truncateContext(AbstractThresholdEvaluatorState.THRESHOLDING_KV_CONTEXT);
    }
    
    @Test
    public void canResumeWithState() {
        // The following test simulates Sentinel A thresholding a high-low threshold with an exceeded value then going
        // down
        // Sentinel B then evaluates the same threshold with another exceeded value and since it retrieved state first
        // it correctly sees that the threshold has been exceeded twice and triggers the threshold
        ThresholdEvaluatorState item = new ThresholdEvaluatorHighLow.ThresholdEvaluatorStateHighLow(getWrapper(),
                thresholdingSession);

        ThresholdEvaluatorState.Status status = item.evaluate(100.0);
        assertEquals("first threshold evaluation status", ThresholdEvaluatorState.Status.NO_CHANGE, status);

        // re-initialize the item to simulate another node taking over processing of this threshold and creating a new
        // threshold object to do so
        item = new ThresholdEvaluatorHighLow.ThresholdEvaluatorStateHighLow(getWrapper(), thresholdingSession);
        
        status = item.evaluate(100.0);
        assertEquals("second threshold evaluation status", ThresholdEvaluatorState.Status.TRIGGERED, status);
    }
    
    @Test
    public void onlyAlwaysFetchesWhenDistributed() {
        // This test needs to use a non-default blobstore so we can count blobstore operations
        BlobStore mockBlobStore = mock(BlobStore.class);
        when(thresholdingSession.getBlobStore()).thenReturn(mockBlobStore);
        // We also need to explicitly clear the existing serdes that are in use since they will have been built using
        // the other blobstore impl
        AbstractThresholdEvaluatorState.clearSerdesMap();
        
        // Set up the mock so that any type of fetch operation will increment a counter
        AtomicInteger fetchesPerformed = new AtomicInteger(0);
        when(mockBlobStore.getLastUpdated(anyString(), anyString())).then((Answer<Long>) invocationOnMock -> {
            fetchesPerformed.incrementAndGet();
            return 0L;
        });
        when(mockBlobStore.get(anyString(), anyString())).then((Answer<byte[]>) invocationOnMock -> {
            fetchesPerformed.incrementAndGet();
            return new byte[0];
        });
        when(mockBlobStore.getIfStale(anyString(), anyString(), anyLong())).then((Answer<byte[]>) invocationOnMock -> {
            fetchesPerformed.incrementAndGet();
            return new byte[0];
        });
        when(thresholdingSession.getBlobStore()).thenReturn(mockBlobStore);

        // Now evaluate a threshold multiple times
        ThresholdEvaluatorState item = new ThresholdEvaluatorHighLow.ThresholdEvaluatorStateHighLow(getWrapper(),
                thresholdingSession);
        item.evaluate(100.0);
        item.evaluate(100.0);
        
        // Verify that only one fetch was performed
        assertThat(fetchesPerformed.get(), equalTo(1));
        
        // Now simulate being on Sentinel in distributed and redo the evaluations
        when(thresholdingSession.isDistributed()).thenReturn(true);
        fetchesPerformed.set(0);
        item.evaluate(100.0);
        item.evaluate(100.0);

        // Verify that multiple fetches were performed
        assertThat(fetchesPerformed.get(), greaterThan(1));
    }
    
    @Test
    public void canRetriggerAfterClear() {
        ThresholdEvaluatorState item = new ThresholdEvaluatorHighLow.ThresholdEvaluatorStateHighLow(getWrapper(),
                thresholdingSession);

        // Two evaluations exceeding the threshold should trigger
        ThresholdEvaluatorState.Status status = item.evaluate(100.0);
        assertEquals("first threshold evaluation status", ThresholdEvaluatorState.Status.NO_CHANGE, status);
        status = item.evaluate(100.0);
        assertEquals("second threshold evaluation status", ThresholdEvaluatorState.Status.TRIGGERED, status);

        // A third evaluation exceeding the threshold should result in no change since the evaluator is already
        // triggered
        status = item.evaluate(100.0);
        assertEquals("third threshold evaluation status", ThresholdEvaluatorState.Status.NO_CHANGE, status);

        // After performing a state clear we should now see the threshold get triggered again if we exceed the threshold
        // twice again
        monitor.reinitializeStates();
        item.evaluate(100.0);
        status = item.evaluate(100.0);
        assertEquals("third threshold evaluation status", ThresholdEvaluatorState.Status.TRIGGERED, status);
    }

    private ThresholdConfigWrapper getWrapper() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(2);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        
        return wrapper;
    }
}
