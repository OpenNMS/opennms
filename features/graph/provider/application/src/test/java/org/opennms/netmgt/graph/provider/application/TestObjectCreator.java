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

package org.opennms.netmgt.graph.provider.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.opennms.netmgt.model.OnmsApplication;

public class TestObjectCreator {

    private static int ids = 0;

    public static OnmsApplication createOnmsApplication() {
        OnmsApplication app = new OnmsApplication();
        app.setId(ids++);
        app.setName(UUID.randomUUID().toString());
        return app;
    }

    public static List<OnmsApplication> createOnmsApplications(int amount) {
        List<OnmsApplication> applications = new ArrayList<>();
        for(int i=0; i<amount; i++) {
            applications.add(createOnmsApplication());
        }
        return applications;
    }
}
