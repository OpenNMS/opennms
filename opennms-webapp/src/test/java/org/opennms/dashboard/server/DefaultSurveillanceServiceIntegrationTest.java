package org.opennms.dashboard.server;

import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.opennms.dashboard.client.NodeRtc;
import org.opennms.dashboard.client.SurveillanceService;
import org.opennms.dashboard.client.SurveillanceSet;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.test.WebAppTestConfigBean;

public class DefaultSurveillanceServiceIntegrationTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private SurveillanceService m_gwtSurveillanceService;

    public DefaultSurveillanceServiceIntegrationTest() throws Exception {
        WebAppTestConfigBean webAppTestConfig = new WebAppTestConfigBean();
        webAppTestConfig.afterPropertiesSet();
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:/META-INF/opennms/applicationContext-dao.xml",
                "classpath:/org/opennms/web/svclayer/applicationContext-svclayer.xml",
                "classpath:/org/opennms/dashboard/applicationContext-svclayer-dashboard-test.xml"
        };
    }
    
    @Override
    protected void onSetUpInTransactionIfEnabled() throws Exception {
        super.onSetUpInTransactionIfEnabled();
        
        /*
         * Since the SecurityContext is stored in a ThreadLocal we need to
         * be sure to clear it before every test.
         */
        SecurityContextHolder.clearContext();
    }
    
    public void testGetRtcForSet() {
        populateSecurityContext();

        NodeRtc[] rtcs = m_gwtSurveillanceService.getRtcForSet(SurveillanceSet.DEFAULT);
        
        assertNotNull("rtcs should not be null", rtcs);
    }
    
    private UserDetails populateSecurityContext() {
        UserDetails details = new User("user", "password", true, true, true, true, new GrantedAuthority[0]);
        Authentication auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return details;
    }

    public SurveillanceService getGwtSurveillanceService() {
        return m_gwtSurveillanceService;
    }

    public void setGwtSurveillanceService(SurveillanceService gwtSurveillanceService) {
        m_gwtSurveillanceService = gwtSurveillanceService;
    }
}
