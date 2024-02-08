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
        synchronized (this) {
            final ModelAndView summary = getSummary((SummarySpecification) command);
            response.setContentType(summary.getView().getContentType());
            return summary;
        }
    }

    public void setRrdSummaryService(RrdSummaryService rrdSummaryService) {
        m_rrdSummaryService = rrdSummaryService;
    }
}
