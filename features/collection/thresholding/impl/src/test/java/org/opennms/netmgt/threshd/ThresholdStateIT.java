/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.threshd;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.mate.api.Scope;
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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-testPostgresBlobStore.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class ThresholdStateIT {
    @Autowired
    private BlobStore blobStore;

    private final ThresholdingSession thresholdingSession = MockSession.getSession();
    private ThresholdStateMonitor monitor;
    private final Scope scope = mock(Scope.class);

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("org.opennms.rrd.strategyClass", "org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy");
    }

    @Before
    public void setup() {
        monitor = new BlobStoreAwareMonitor(blobStore);
        when(thresholdingSession.getThresholdStateMonitor()).thenReturn(monitor);
        when(thresholdingSession.getBlobStore()).thenReturn(blobStore);
        when(scope.get(new ContextKey("requisition", "value"))).thenReturn(Optional.of(new Scope.ScopeValue(Scope.ScopeName.DEFAULT, "99.0")));
        when(scope.get(new ContextKey("requisition", "rearm"))).thenReturn(Optional.of(new Scope.ScopeValue(Scope.ScopeName.DEFAULT, "0.5")));
        when(scope.get(new ContextKey("requisition", "trigger"))).thenReturn(Optional.of(new Scope.ScopeValue(Scope.ScopeName.DEFAULT, "2")));
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
        ThresholdEvaluatorState.ThresholdValues thresholdValues = getWrapper().interpolateThresholdValues(scope);

        ThresholdEvaluatorState.Status status = item.evaluate(100.0, thresholdValues, null);
        assertEquals("first threshold evaluation status", ThresholdEvaluatorState.Status.NO_CHANGE, status);

        // re-initialize the item to simulate another node taking over processing of this threshold and creating a new
        // threshold object to do so
        item = new ThresholdEvaluatorHighLow.ThresholdEvaluatorStateHighLow(getWrapper(), thresholdingSession);
        
        status = item.evaluate(100.0, thresholdValues, null);
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
        ThresholdEvaluatorState.ThresholdValues thresholdValues = getWrapper().interpolateThresholdValues(scope);
        item.evaluate(100.0, thresholdValues, null);
        item.evaluate(100.0, thresholdValues, null);
        
        // Verify that only one fetch was performed
        assertThat(fetchesPerformed.get(), equalTo(1));
        
        // Now simulate being on Sentinel in distributed and redo the evaluations
        when(thresholdingSession.isDistributed()).thenReturn(true);
        fetchesPerformed.set(0);
        item.evaluate(100.0, thresholdValues, null);
        item.evaluate(100.0, thresholdValues, null);

        // Verify that multiple fetches were performed
        assertThat(fetchesPerformed.get(), greaterThan(1));
    }
    
    @Test
    public void canRetriggerAfterClear() {
        ThresholdEvaluatorState item = new ThresholdEvaluatorHighLow.ThresholdEvaluatorStateHighLow(getWrapper(),
                thresholdingSession);

        // Two evaluations exceeding the threshold should trigger
        ThresholdEvaluatorState.ThresholdValues thresholdValues = getWrapper().interpolateThresholdValues(scope);
        ThresholdEvaluatorState.Status status = item.evaluate(100.0, thresholdValues, null);
        assertEquals("first threshold evaluation status", ThresholdEvaluatorState.Status.NO_CHANGE, status);
        status = item.evaluate(100.0, thresholdValues, null);
        assertEquals("second threshold evaluation status", ThresholdEvaluatorState.Status.TRIGGERED, status);

        // A third evaluation exceeding the threshold should result in no change since the evaluator is already
        // triggered
        status = item.evaluate(100.0, thresholdValues, null);
        assertEquals("third threshold evaluation status", ThresholdEvaluatorState.Status.NO_CHANGE, status);

        // After performing a state clear we should now see the threshold get triggered again if we exceed the threshold
        // twice again
        monitor.reinitializeStates();
        item.evaluate(100.0, thresholdValues, null);
        status = item.evaluate(100.0, thresholdValues, null);
        assertEquals("third threshold evaluation status", ThresholdEvaluatorState.Status.TRIGGERED, status);
    }

    private ThresholdConfigWrapper getWrapper() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("${requisition:value|0}");
        threshold.setRearm("${requisition:rearm|0}");
        threshold.setTrigger("${requisition:trigger|0}");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        
        return wrapper;
    }
}
