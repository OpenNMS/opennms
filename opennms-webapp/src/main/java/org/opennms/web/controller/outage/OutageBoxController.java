package org.opennms.web.controller.outage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.outage.OutageSummary;
import org.opennms.web.outage.WebOutageRepository;
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
    public static final int ROWS = 12;

    private WebOutageRepository m_webOutageRepository;
    private String m_successView;
    
    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int rows = Integer.getInteger("opennms.nodesWithOutagesCount", ROWS);
        final String parm = request.getParameter("outageCount");
        if (parm != null) {
            try {
                rows = Integer.valueOf(parm);
            } catch (NumberFormatException e) {
                // ignore, and let it fall back to the defaults
            }
        }
        OutageSummary[] summaries = m_webOutageRepository.getCurrentOutages(rows);
        int outagesRemaining = (m_webOutageRepository.countCurrentOutages() - summaries.length);

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
