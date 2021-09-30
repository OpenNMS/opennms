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

package org.opennms.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.attrsummary.Summary;
import org.opennms.web.svclayer.model.SummarySpecification;
import org.opennms.web.svclayer.rrd.RrdSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;

/**
 * <p>RrdSummaryController class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
@Controller
@RequestMapping("/summary/results.htm")
public class RrdSummaryController {

    private static class MarshalledView extends AbstractView {
        public String getContentType() { return "text/xml"; }

        @Override
        protected void renderMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
            Assert.notNull(model.get("summary"), "summary must not be null.. unable to marshall xml");
            JaxbUtils.marshal(model.get("summary"), response.getWriter());
        }
    }

    @Autowired
    private RrdSummaryService m_rrdSummaryService;

    private ModelAndView getSummary(final SummarySpecification spec) {
        final Summary summary = m_rrdSummaryService.getSummary(spec);
        return new ModelAndView(new MarshalledView(), "summary", summary);
    }

    /** {@inheritDoc} */
    @RequestMapping(method={ RequestMethod.GET, RequestMethod.POST })
    public ModelAndView processFormSubmission(final HttpServletResponse response, final SummarySpecification command) {
        final ModelAndView summary = getSummary((SummarySpecification)command);
        response.setContentType(summary.getView().getContentType());
        return summary;
    }

    public void setRrdSummaryService(RrdSummaryService rrdSummaryService) {
        m_rrdSummaryService = rrdSummaryService;
    }
}
