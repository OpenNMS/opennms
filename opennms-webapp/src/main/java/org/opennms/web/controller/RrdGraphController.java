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

import org.opennms.core.utils.StreamUtils;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.util.RrdConvertUtils;
import org.opennms.web.servlet.MissingParameterException;
import org.opennms.web.svclayer.RrdGraphService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

/**
 * <p>RrdGraphController class.</p>
 *
 * Is the front end handler of graph requests.  
 * 
 * Accepts start/end parameters that conform to the "specification" used by rrdfetch, 
 * as defined in it's manpage, or at http://oss.oetiker.ch/rrdtool/doc/rrdfetch.en.html
 * 
 * Or at least, it should.  If it doesn't, write a test and fix the code.
 * 
 * NB; If the start/end are integers, they'll be interpreted as an epoch based timestamp
 * This precludes some of the more compact forms available to rrdtool (e.g. just specifying
 * an hour of the day without am/pm designator.  But there are ways and means of working 
 * around that (specifying the time with hh:mm where mm is 00, or using am/pm; either will 
 * not parse as integers, resulting in evaluation by the rrdtool-alike parser. 
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class RrdGraphController extends AbstractController {
    private RrdGraphService m_rrdGraphService;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String[] requiredParameters = new String[] {
                "resourceId",
                "start",
                "end"
        };
        
        for (String requiredParameter : requiredParameters) {
            if (request.getParameter(requiredParameter) == null) {
                throw new MissingParameterException(requiredParameter,
                                                    requiredParameters);
            }
        }

        ResourceId resourceId = ResourceId.fromString(request.getParameter("resourceId"));
        
        long[] times = this.parseTimes(request);
        
        long startTime = times[0];
        long endTime = times[1];

        InputStream tempIn;
        if ("true".equals(request.getParameter("adhoc"))) {
            String[] adhocRequiredParameters = new String[] {
                    "title",
                    "ds",
                    "agfunction",
                    "color",
                    "dstitle",
                    "style"
            };
            
            for (String requiredParameter : adhocRequiredParameters) {
                if (request.getParameter(requiredParameter) == null) {
                    throw new MissingParameterException(requiredParameter,
                                                        adhocRequiredParameters);
                }
            }

            String title = request.getParameter("title");
            String[] dataSources = request.getParameterValues("ds");
            String[] aggregateFunctions = request.getParameterValues("agfunction");
            String[] colors = request.getParameterValues("color");
            String[] dataSourceTitles = request.getParameterValues("dstitle");
            String[] styles = request.getParameterValues("style");
            
            tempIn = m_rrdGraphService.getAdhocGraph(resourceId,
                                                     title,
                                                     dataSources,
                                                     aggregateFunctions,
                                                     colors,
                                                     dataSourceTitles,
                                                     styles,
                                                     startTime, endTime);
        } else {
            String report = request.getParameter("report");
            if (report == null) {
                throw new MissingParameterException("report");
            }
            
            String width = request.getParameter("width");
            String height = request.getParameter("height");

            tempIn = m_rrdGraphService.getPrefabGraph(resourceId,
                                                      report, startTime, endTime,
                                                      width != null && !width.isEmpty()
                                                        ? Integer.valueOf(width)
                                                        : null,
                                                      height != null && !height.isEmpty()
                                                        ? Integer.valueOf(height)
                                                        : null);
        }

        response.setContentType("image/png");
        
        StreamUtils.streamToStream(tempIn, response.getOutputStream());

        tempIn.close();
                
        return null;
    }
    
    public long[] parseTimes(HttpServletRequest request) {
    	String startTime = request.getParameter("start");
    	String endTime = request.getParameter("end");
    	
    	if(startTime == null || "".equals(startTime)) {
    		startTime = "now - 1day";
    	}
    	
    	if(endTime == null || "".equals(endTime)) {
    		endTime = "now";
    	}
    	boolean startIsInteger = false;
    	boolean endIsInteger = false;
    	long start = 0, end = 0;
    	try {
    		start = Long.valueOf(startTime);
    		startIsInteger = true;
    	} catch (NumberFormatException e) {
    	}
    	
    	try {
    		end = Long.valueOf(endTime);
    		endIsInteger = true;
    	} catch (NumberFormatException e) {
    	}
    	
    	if(endIsInteger && startIsInteger) {
    		return new long[] {start, end};	
    	}
    	
    	//One or both of start/end aren't integers, so we need to do full parsing using TimeParser
    	//But, if one of them *is* an integer, convert from incoming milliseconds to seconds that
    	// is expected for epoch times by TimeParser
    	if(startIsInteger) {
    		//Convert to seconds
    		startTime = ""+(start/1000);
    	}
    	if(endIsInteger) {
    		endTime = "" +(end/1000);
    	}
    	
        try {
        	long[] results = RrdConvertUtils.getTimestamps(startTime, endTime);
        	//Multiply by 1000.  TimeSpec returns timestamps in Seconds, not Milliseconds.  Gah.  
        	results[0] = results[0]*1000;
        	results[1] = results[1]*1000;
        	return results;
		} catch (RrdException e) {
			throw new IllegalArgumentException("Could not parse start '"+ startTime+"' and end '"+endTime+"' as valid time specifications", e);
		}
    }

    /**
     * <p>getRrdGraphService</p>
     *
     * @return a {@link org.opennms.web.svclayer.RrdGraphService} object.
     */
    public RrdGraphService getRrdGraphService() {
        return m_rrdGraphService;
    }

    /**
     * <p>setRrdGraphService</p>
     *
     * @param rrdGraphService a {@link org.opennms.web.svclayer.RrdGraphService} object.
     */
    public void setRrdGraphService(RrdGraphService rrdGraphService) {
        m_rrdGraphService = rrdGraphService;
    }
}
