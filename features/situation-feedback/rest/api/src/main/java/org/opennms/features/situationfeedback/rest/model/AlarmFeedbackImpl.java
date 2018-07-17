/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.situationfeedback.rest.model;

import org.opennms.features.situationfeedback.api.AlarmFeedback;

public class AlarmFeedbackImpl implements AlarmFeedback {

    private String situationKey; // Situation ReductionKey

    // this may not be nesc but may prove helpful
    private String situationThumbprint; // thumbprint of situation at time of feedback;
    
    private String alarmKey; // Alarm ReductionKey

    private FeedbackType feedbackType;

    private String reason;

    private String user;

    private long timestamp;

    public enum FeedbackType {
        FALSE_POSITVE, // Alarm does not belong in this Situation
        FALSE_NEGATIVE, // Alarm was missing from this Situation
        CORRECT // Alarm is correctly correlated
    }
}
