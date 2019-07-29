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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.opennms.features.distributed.kvstore.api.KeyValueStore;
import org.opennms.features.distributed.kvstore.inmemory.InMemoryMapKeyValueStore;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.config.threshd.ThresholdType;
import org.opennms.netmgt.threshd.api.ThresholdingSession;

public class ThresholdStateTest {
    private final KeyValueStore kvStore = new InMemoryMapKeyValueStore(System::currentTimeMillis);
    
    @Test
    public void canResumeWithState() {
        ThresholdingSession thresholdingSession = MockSession.getSession();
        when(thresholdingSession.getKVStore()).thenReturn(kvStore);
        
        // The following test simulates Sentinel A thresholding a high-low threshold with an exceeded value then going
        // down
        // Sentinel B then evaluates the same threshold with another exceeded value and since it retrieved state first
        // it correctly sees that the threshold has been exceeded twice and triggers the threshold
        ThresholdEvaluatorState item = new ThresholdEvaluatorHighLow.ThresholdEvaluatorStateHighLow(getWrapper(),
                MockSession.getSession());

        ThresholdEvaluatorState.Status status = item.evaluate(100.0);
        assertEquals("first threshold evaluation status", ThresholdEvaluatorState.Status.NO_CHANGE, status);

        // re-initialize the item to simulate another node taking over processing of this threshold and creating a new
        // threshold object to do so
        item = new ThresholdEvaluatorHighLow.ThresholdEvaluatorStateHighLow(getWrapper(), MockSession.getSession());
        
        status = item.evaluate(100.0);
        assertEquals("second threshold evaluation status", ThresholdEvaluatorState.Status.TRIGGERED, status);
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
