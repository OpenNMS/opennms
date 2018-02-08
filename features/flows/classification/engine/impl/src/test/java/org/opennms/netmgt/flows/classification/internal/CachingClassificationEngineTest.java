/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.internal;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.persistence.api.ProtocolType;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;

import com.google.common.collect.Lists;

public class CachingClassificationEngineTest {

    @Test
    public void verifyCaching() {
        // Create caching engine and spy o original engine
        final DefaultClassificationEngine originalEngine = new DefaultClassificationEngine(() -> Lists.newArrayList(new Rule("TEST", "0-10000")));
        final ClassificationEngine classificationEngine = Mockito.spy(originalEngine);
        final ClassificationEngine cachingEngine = new CachingClassificationEngine(classificationEngine);

        // Create Request
        final ClassificationRequest request = new ClassificationRequest("Default", 80, "192.168.2.1", ProtocolType.TCP);

        // Verify
        final String classification1 = cachingEngine.classify(request);
        final String classification2 = cachingEngine.classify(request);
        Assert.assertEquals("TEST", classification1);
        Assert.assertEquals(classification1, classification2);

        // Should only be invoked once
        Mockito.verify(classificationEngine, Mockito.times(1)).classify(Mockito.any(ClassificationRequest.class));
    }

    @Test
    public void verifyReloading() {
        final List<Rule> rules = new ArrayList<>();

        // Create caching engine and spy o original engine
        final DefaultClassificationEngine originalEngine = new DefaultClassificationEngine(() -> rules);
        final ClassificationEngine classificationEngine = Mockito.spy(originalEngine);
        final ClassificationEngine cachingEngine = new CachingClassificationEngine(classificationEngine);

        // Verify no rule defined
        final ClassificationRequest request = new ClassificationRequest("Default", 80, "192.168.2.1", ProtocolType.TCP);
        Assert.assertEquals(null, cachingEngine.classify(request));

        // Add rule and reload
        rules.add(new Rule("TEST", "0-10000"));
        cachingEngine.reload();

        // Verify rule is now defined
        Assert.assertEquals("TEST", cachingEngine.classify(request));
    }
}