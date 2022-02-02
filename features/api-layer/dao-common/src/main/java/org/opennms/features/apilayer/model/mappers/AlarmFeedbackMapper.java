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

package org.opennms.features.apilayer.model.mappers;

import org.mapstruct.Mapper;
import org.opennms.features.situationfeedback.api.AlarmFeedback;
import org.opennms.integration.api.v1.model.immutables.ImmutableAlarmFeedback;

import com.google.common.base.Enums;

@Mapper
public interface AlarmFeedbackMapper {
    ImmutableAlarmFeedback map(AlarmFeedback alarmFeedback);

    default org.opennms.integration.api.v1.model.AlarmFeedback.Type map(AlarmFeedback.FeedbackType feedbackType) {
        return feedbackType == null ? null :
                Enums.getIfPresent(org.opennms.integration.api.v1.model.AlarmFeedback.Type.class,
                        feedbackType.name()).orNull();
    }
}
