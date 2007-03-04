package org.opennms.dashboard.server;

import java.io.File;
import java.io.Reader;

import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.opennms.dashboard.client.NodeRtc;
import org.opennms.dashboard.client.SurveillanceService;
import org.opennms.dashboard.client.SurveillanceSet;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.SurveillanceViewsFactory;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.test.ConfigurationTestUtils;

public class DefaultSurveillanceServiceIntegrationTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private SurveillanceService m_gwtSurveillanceService;

    public DefaultSurveillanceServiceIntegrationTest() throws Exception {
        File f = new File("src/test/opennms-home");
        System.setProperty("opennms.home", f.getAbsolutePath());
        
        File rrdDir = new File("target/test/opennms-home/share/rrd");
        if (!rrdDir.exists()) {
            rrdDir.mkdirs();
        }
        System.setProperty("distributed.layoutApplicationsVertically", "false");
        
        // FIXME: We should never modify anything under src... this should be under target
        System.setProperty("opennms.logs.dir", "src/test/opennms-home/logs");
        System.setProperty("rrd.base.dir", rrdDir.getAbsolutePath());
        
        Reader schemaReader = ConfigurationTestUtils.getReaderForConfigFile("database-schema.xml");
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(schemaReader));
        
        Reader surveillanceReader = ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/web/svclayer/surveillance-views-gwtSurveillanceService.xml");
        SurveillanceViewsFactory.setInstance(new SurveillanceViewsFactory(surveillanceReader));
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
