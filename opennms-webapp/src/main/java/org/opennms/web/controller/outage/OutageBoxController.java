package org.opennms.web.controller.outage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.filter.Filter;
import org.opennms.web.outage.OutageSummary;
import org.opennms.web.outage.OutageType;
import org.opennms.web.outage.SortStyle;
import org.opennms.web.outage.WebOutageRepository;
import org.opennms.web.outage.filter.OutageCriteria;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * A controller that handles querying the outages table to create the front-page
 * outage summary box.
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class OutageBoxController extends AbstractController implements InitializingBean {
    /** Constant <code>ROWS=12</code> */
    public static final int ROWS = 12;
    private static final OutageType OUTAGE_TYPE = OutageType.CURRENT;
    private static final SortStyle SORT_STYLE = SortStyle.IFLOSTSERVICE;

    private WebOutageRepository m_webOutageRepository;
    private String m_successView;
    
    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        OutageCriteria queryCriteria = new OutageCriteria(new Filter[]{}, SORT_STYLE, OUTAGE_TYPE, ROWS, 0);
        OutageSummary[] summaries = m_webOutageRepository.getMatchingOutageSummaries(queryCriteria);

        OutageCriteria countCriteria = new OutageCriteria(OUTAGE_TYPE, new Filter[]{});
        int outagesRemaining = (m_webOutageRepository.countMatchingOutageSummaries(countCriteria) - summaries.length);

        ModelAndView modelAndView = new ModelAndView(getSuccessView());
        modelAndView.addObject("summaries", summaries);
        modelAndView.addObject("moreCount", outagesRemaining);
        return modelAndView;

    }

    private String getSuccessView() {
        return m_successView;
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
     * <p>setWebOutageRepository</p>
     *
     * @param webOutageRepository a {@link org.opennms.web.outage.WebOutageRepository} object.
     */
    public void setWebOutageRepository(WebOutageRepository webOutageRepository) {
        m_webOutageRepository = webOutageRepository;
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    public void afterPropertiesSet() {
        Assert.notNull(m_successView, "property successView must be set");
        Assert.notNull(m_webOutageRepository, "webOutageRepository must be set");
    }

}
