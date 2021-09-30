/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.feedback;

import java.util.List;

import org.opennms.features.apilayer.utils.InterfaceMapper;
import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.features.situationfeedback.api.AlarmFeedback;
import org.opennms.features.situationfeedback.api.AlarmFeedbackListener;
import org.osgi.framework.BundleContext;

public class AlarmFeedbackListenerManager extends InterfaceMapper<org.opennms.integration.api.v1.feedback.AlarmFeedbackListener, AlarmFeedbackListener> {

    public AlarmFeedbackListenerManager(BundleContext bundleContext) {
        super(AlarmFeedbackListener.class, bundleContext);
    }

    @Override
    public AlarmFeedbackListener map(org.opennms.integration.api.v1.feedback.AlarmFeedbackListener ext) {
        return new AlarmFeedbackListener() {
            @Override
            public void handleAlarmFeedback(List<AlarmFeedback> alarmFeedback) {
                alarmFeedback.stream()
                        .map(ModelMappers::toFeedback)
                        .forEach(ext::onFeedback);
            }
        };
    }
}
