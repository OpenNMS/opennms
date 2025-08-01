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
package org.opennms.web.controller.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.event.Event;
import org.opennms.web.event.WebEventRepository;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.event.filter.EventIdListFilter;
import org.opennms.web.filter.Filter;
import org.opennms.web.notification.AcknowledgeType;
import org.opennms.web.notification.NoticeQueryParms;
import org.opennms.web.notification.NoticeUtil;
import org.opennms.web.notification.Notification;
import org.opennms.web.notification.SortStyle;
import org.opennms.web.notification.WebNotificationRepository;
import org.opennms.web.notification.filter.NotificationCriteria;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>NotificationFilterController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NotificationFilterController extends AbstractController implements InitializingBean {

    /** Constant <code>DEFAULT_MULTIPLE=0</code> */
    public static final int DEFAULT_MULTIPLE = 0;

    private String m_successView;
    private Integer m_defaultShortLimit;
    private Integer m_defaultLongLimit;
    private SortStyle m_defaultSortStyle = SortStyle.ID;
    private AcknowledgeType m_defaultAckType = AcknowledgeType.UNACKNOWLEDGED;

    private WebEventRepository m_webEventRepository;
    private WebNotificationRepository m_webNotificationRepository;
    private NodeDao m_nodeDao;

    /**
     * {@inheritDoc}
     *
     * Parses the query string to determine what types of notification filters to use
     * (for example, what to filter on or sort by), then does the database query
     * and then forwards the results to a JSP for display.
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String display = request.getParameter("display");

        // handle the style sort parameter
        String sortStyleString = request.getParameter("sortby");
        SortStyle sortStyle = m_defaultSortStyle;
        if (sortStyleString != null) {
            SortStyle temp = SortStyle.getSortStyle(sortStyleString);
            if (temp != null) {
                sortStyle = temp;
            }
        }

        // handle the acknowledgment type parameter
        String ackTypeString = request.getParameter("acktype");
        AcknowledgeType ackType = m_defaultAckType;
        if (ackTypeString != null) {
            AcknowledgeType temp = AcknowledgeType.getAcknowledgeType(ackTypeString);
            if (temp != null) {
                ackType = temp;
            }
        }

        // handle the filter parameters
        String[] filterStrings = request.getParameterValues("filter");
        List<Filter> filterList = new ArrayList<>();
        if (filterStrings != null) {
            for (String filterString : filterStrings) {
                Filter filter = NoticeUtil.getFilter(filterString, getServletContext());
                if (filter != null) {
                    filterList.add(filter);
                }
            }
        }

        // Check for a username filter (used on notifications/index.jsp)
        String username = request.getParameter("user");
        if (username != null) {
            Filter filter = NoticeUtil.getFilter("user=" + username, getServletContext());
            if (filter != null) {
                filterList.add(filter);
            }
        }

        // handle the optional limit parameter
        String limitString = request.getParameter("limit");
        int limit = "long".equals(display) ? m_defaultLongLimit : m_defaultShortLimit;

        if (limitString != null) {
            try {
                int newlimit = WebSecurityUtils.safeParseInt(limitString);
                if (newlimit > 0) {
                    limit = newlimit;
                }
            } catch (NumberFormatException e) {
                // do nothing, the default is already set
            }
        }

        // handle the optional multiple parameter
        String multipleString = request.getParameter("multiple");
        int multiple = DEFAULT_MULTIPLE;
        if (multipleString != null) {
            try {
                multiple = Math.max(0, WebSecurityUtils.safeParseInt(multipleString));
            } catch (NumberFormatException e) {
            }
        }

        // put the parameters in a convenient struct
        Filter[] filters = filterList.toArray(new Filter[0]);

        NoticeQueryParms parms = new NoticeQueryParms();
        parms.ackType = ackType;
        parms.display = display;
        parms.filters = filterList;
        parms.limit = limit;
        parms.multiple =  multiple;
        parms.sortStyle = sortStyle;

        NotificationCriteria queryCriteria = new NotificationCriteria(filters, sortStyle, ackType, limit, limit * multiple);
        NotificationCriteria countCriteria = new NotificationCriteria(ackType, filters);

        Notification[] notices = m_webNotificationRepository.getMatchingNotifications(queryCriteria);
        int noticeCount = m_webNotificationRepository.countMatchingNotifications(countCriteria);
        final Map<Integer,String[]> nodeLabels = new HashMap<Integer,String[]>();
        final Map<Integer,String[]> nodeLocations = new HashMap<Integer,String[]>();
        Set<Long> eventIds = new TreeSet<>();

        // really inefficient, is there a better way to do this?
        for (Notification notice : notices) {
            if (notice.getEventId() > 0) {
                eventIds.add(notice.getEventId());
            }
            if (notice.getNodeId() > 0) {
                if (!nodeLabels.containsKey(notice.getNodeId())) {
                    String[] labels = null;
                    String[] locations = null;
                    OnmsNode node = m_nodeDao.get(notice.getNodeId());
                    if (node != null) {
                        String longLabel = node.getLabel();
                        if( longLabel == null ) {
                            labels = new String[] { "&lt;No Node Label&gt;", "&lt;No Node Label&gt;" };
                        } else {
                            if ( longLabel.length() > 32 ) {
                                String shortLabel = longLabel.substring( 0, 31 );
                                labels = new String[] { shortLabel, longLabel };
                            } else {
                                labels = new String[] { longLabel, longLabel };
                            }
                        }

                        if (node.getLocation() != null) {
                            String location = node.getLocation().getLocationName();
                            if ( location == null ) {
                                locations = new String[] { "&lt;No Node Location&gt;", "&lt;No Node Location&gt;" };
                            } else {
                                if ( location.length() > 32 ) {
                                    String shortLocation = location.substring(0, 31) + "&hellip;";
                                    locations = new String[] { shortLocation, location };
                                } else {
                                    locations = new String[] { location, location };
                                }
                            }
                        }
                    }
                    nodeLabels.put( notice.getNodeId(), labels );
                    nodeLocations.put( notice.getNodeId() , locations );
                }
            }
        }

        Map<Long, Event> events = new HashMap<Long, Event>();
        if (eventIds.size() > 0) {
            for (Event e : m_webEventRepository.getMatchingEvents(new EventCriteria(new EventIdListFilter(eventIds)))) {
                events.put(e.getId(), e);
            }
        }

        ModelAndView modelAndView = new ModelAndView(m_successView);
        modelAndView.addObject("notices", notices);
        modelAndView.addObject("noticeCount", noticeCount);
        modelAndView.addObject("nodeLabels", nodeLabels);
        modelAndView.addObject("nodeLocations", nodeLocations);
        modelAndView.addObject("events", events);
        modelAndView.addObject("parms", parms);
        return modelAndView;
    }

    /**
     * <p>setDefaultShortLimit</p>
     *
     * @param limit a {@link java.lang.Integer} object.
     */
    public void setDefaultShortLimit(Integer limit) {
        m_defaultShortLimit = limit;
    }

    /**
     * <p>setDefaultLongLimit</p>
     *
     * @param limit a {@link java.lang.Integer} object.
     */
    public void setDefaultLongLimit(Integer limit) {
        m_defaultLongLimit = limit;
    }

    /**
     * <p>setDefaultSortStyle</p>
     *
     * @param sortStyle a {@link org.opennms.web.notification.SortStyle} object.
     */
    public void setDefaultSortStyle(SortStyle sortStyle) {
        m_defaultSortStyle = sortStyle;
    }

    /**
     * <p>setSuccessView</p>
     *
     * @param successView a {@link java.lang.String} object.
     */
    public void setSuccessView(String successView) {
        m_successView = successView;
    }

    /**
     * <p>setWebEventRepository</p>
     *
     * @param webEventRepository a {@link org.opennms.web.event.WebEventRepository} object.
     */
    public void setWebEventRepository(WebEventRepository webEventRepository) {
        m_webEventRepository = webEventRepository;
    }

    /**
     * <p>setWebNotificationRepository</p>
     *
     * @param webNotificationRepository a {@link org.opennms.web.notification.WebNotificationRepository} object.
     */
    public void setWebNotificationRepository(WebNotificationRepository webNotificationRepository) {
        m_webNotificationRepository = webNotificationRepository;
    }

    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(m_defaultShortLimit, "property defaultShortLimit must be set to a value greater than 0");
        Assert.isTrue(m_defaultShortLimit > 0, "property defaultShortLimit must be set to a value greater than 0");
        Assert.notNull(m_defaultLongLimit, "property defaultLongLimit must be set to a value greater than 0");
        Assert.isTrue(m_defaultLongLimit > 0, "property defaultLongLimit must be set to a value greater than 0");
        Assert.notNull(m_successView, "property successView must be set");
        Assert.notNull(m_webNotificationRepository, "webNotificationRepository must be set");
        Assert.notNull(m_webEventRepository, "webEventRepository must be set");
        Assert.notNull(m_nodeDao, "nodeDao must be set");
    }

}
