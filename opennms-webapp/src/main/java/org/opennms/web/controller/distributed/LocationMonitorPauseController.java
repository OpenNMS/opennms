/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.distributed;

import org.opennms.web.svclayer.DistributedPollerService;
import org.opennms.web.svclayer.model.LocationMonitorIdCommand;
import org.opennms.web.validator.LocationMonitorIdValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * <p>LocationMonitorPauseController class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Controller
@RequestMapping("/admin/distributed/locationMonitorPause.htm")
public class LocationMonitorPauseController {

    @Autowired
    private DistributedPollerService m_distributedPollerService;

    @Autowired
    private LocationMonitorIdValidator m_validator;

    private static final String SUCCESS_VIEW = "redirect:/distributed/locationMonitorList.htm";

    private static final String ERROR_VIEW = "distributed/error";

    @RequestMapping(method={ RequestMethod.GET, RequestMethod.POST })
    public String handle(@ModelAttribute("command") LocationMonitorIdCommand cmd, BindingResult errors) {
        m_validator.validate(cmd, errors);

        if (!errors.hasErrors()) {
            m_distributedPollerService.pauseLocationMonitor(cmd, errors);
        }

        if (errors.hasErrors()) {
            return ERROR_VIEW;
        } else {
            return SUCCESS_VIEW;
        }
    }
}
