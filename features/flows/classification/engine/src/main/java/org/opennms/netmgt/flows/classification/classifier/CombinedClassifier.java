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

package org.opennms.netmgt.flows.classification.classifier;

import java.util.Arrays;
import java.util.List;

import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.matcher.IpMatcher;
import org.opennms.netmgt.flows.classification.matcher.Matcher;

public class CombinedClassifier implements Classifier {

    private final List<Matcher> matchers;
    private final String application;

    public CombinedClassifier(String application, Matcher... matchers) {
        this(application, Arrays.asList(matchers));
    }

    public CombinedClassifier(String application, List<Matcher> matchers) {
        if (matchers == null || matchers.size() == 0) {
            throw new IllegalArgumentException("At least 1 matcher should be provided, but received null or 0");
        }
        this.matchers = matchers;
        this.application = application;
    }

    @Override
    public int getPriority() {
        return matchers.size();
    }

    @Override
    public String classify(ClassificationRequest request) {
        boolean matches = true;
        for (Matcher m : matchers) {
            matches = matches && m.matches(request);
            if (!matches) {
                return null;
            }
        }
        return application;
    }

    @Override
    public boolean hasIpMatcher() {
        return matchers.stream().anyMatch(m -> m instanceof IpMatcher);
    }
}
