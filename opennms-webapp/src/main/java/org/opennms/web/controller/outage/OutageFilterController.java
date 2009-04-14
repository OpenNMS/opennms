package org.opennms.web.controller.outage;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.WebSecurityUtils;
import org.opennms.web.filter.Filter;
import org.opennms.web.outage.Outage;
import org.opennms.web.outage.OutageQueryParms;
import org.opennms.web.outage.OutageType;
import org.opennms.web.outage.OutageUtil;
import org.opennms.web.outage.SortStyle;
import org.opennms.web.outage.WebOutageRepository;
import org.opennms.web.outage.filter.OutageCriteria;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * A controller that handles querying the outages table by using filters to create an
 * outage list and and then forwards that outage list to a JSP for display.
 * 
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
public class OutageFilterController extends AbstractController implements InitializingBean {
    public static final int DEFAULT_MULTIPLE = 0;

    private String m_successView;

    private Integer m_defaultShortLimit;

    private Integer m_defaultLongLimit;
    
    private OutageType m_defaultOutageType = OutageType.CURRENT;

    private SortStyle m_defaultSortStyle = SortStyle.ID;

    private WebOutageRepository m_webOutageRepository;
    


    /**
     * Parses the query string to determine what types of filters to use
     * (for example, what to filter on or sort by), then does the database query
     * (through the OutageFactory) and then forwards the results to a JSP for
     * display.
     * 
     * <p>
     * Sets request attributes for the forwardee JSP (or whatever gets called).
     * </p>
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

        // handle the outage type parameter
        String outageTypeString = request.getParameter("outtype");
        OutageType outageType = m_defaultOutageType;
        if (outageTypeString != null) {
            OutageType temp = OutageType.getOutageType(outageTypeString);
            if (temp != null) {
                outageType = temp;
            } else {
                try {
                    // handle old URLs which used numeric constants
                    int outageTypeInt = Integer.parseInt(outageTypeString);
                    outageType = OutageType.values()[outageTypeInt - 1];
                } catch (Exception e) {
                    // nothing else to try, leave it at the default
                }
            }
        }

        // handle the filter parameters
        String[] filterStrings = request.getParameterValues("filter");
        List<Filter> filterList = new ArrayList<Filter>();
        if (filterStrings != null) {
            for (String filterString : filterStrings) {
                Filter filter = OutageUtil.getFilter(filterString);
                if (filter != null) {
                    filterList.add(filter);
                }
            }
        }

        // handle the optional limit parameter
        String limitString = request.getParameter("limit");
        int limit = "long".equals(display) ? getDefaultLongLimit() : getDefaultShortLimit();

        if (limitString != null) {
            try {
                limit = WebSecurityUtils.safeParseInt(limitString);
            } catch (NumberFormatException e) {
                // do nothing, the default is aready set
            }
        }

        // handle the optional multiple parameter
        String multipleString = request.getParameter("multiple");
        int multiple = DEFAULT_MULTIPLE;
        if (multipleString != null) {
            try {
                multiple = WebSecurityUtils.safeParseInt(multipleString);
            } catch (NumberFormatException e) {
            }
        }

        // put the parameters in a convenient struct
        
        Filter[] filters = filterList.toArray(new Filter[0]);
        
        OutageQueryParms parms = new OutageQueryParms();
        parms.outageType = outageType;
        parms.filters = filterList;
        parms.limit = limit;
        parms.multiple =  multiple;
        parms.sortStyle = sortStyle;
        
        OutageCriteria queryCriteria = new OutageCriteria(filters, sortStyle, outageType, limit, limit * multiple);
        OutageCriteria countCriteria = new OutageCriteria(outageType, filters);

        Outage[] outages = m_webOutageRepository.getMatchingOutages(queryCriteria);
        
        // get the total outage count
        int outageCount = m_webOutageRepository.countMatchingOutages(countCriteria);
        
        ModelAndView modelAndView = new ModelAndView(getSuccessView());
        modelAndView.addObject("outages", outages);
        modelAndView.addObject("outageCount", outageCount);
        modelAndView.addObject("parms", parms);
        return modelAndView;

    }

    private Integer getDefaultShortLimit() {
        return m_defaultShortLimit;
    }

    public void setDefaultShortLimit(Integer limit) {
        m_defaultShortLimit = limit;
    }

    private Integer getDefaultLongLimit() {
        return m_defaultLongLimit;
    }

    public void setDefaultLongLimit(Integer limit) {
        m_defaultLongLimit = limit;
    }

    private String getSuccessView() {
        return m_successView;
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }
    
    public void setWebOutageRepository(WebOutageRepository webOutageRepository) {
        m_webOutageRepository = webOutageRepository;
    }

    public void afterPropertiesSet() {
        Assert.notNull(m_defaultShortLimit, "property defaultShortLimit must be set to a value greater than 0");
        Assert.isTrue(m_defaultShortLimit > 0, "property defaultShortLimit must be set to a value greater than 0");
        Assert.notNull(m_defaultLongLimit, "property defaultLongLimit must be set to a value greater than 0");
        Assert.isTrue(m_defaultLongLimit > 0, "property defaultLongLimit must be set to a value greater than 0");
        Assert.notNull(m_successView, "property successView must be set");
        Assert.notNull(m_webOutageRepository, "webOutageRepository must be set");
    }

    public OutageType getDefaultOutageType() {
        return m_defaultOutageType;
    }

    public void setDefaultOutageType(OutageType defaultOutageType) {
        m_defaultOutageType = defaultOutageType;
    }

    public SortStyle getDefaultSortStyle() {
        return m_defaultSortStyle;
    }

    public void setDefaultSortStyle(SortStyle defaultSortStyle) {
        m_defaultSortStyle = defaultSortStyle;
    }

}