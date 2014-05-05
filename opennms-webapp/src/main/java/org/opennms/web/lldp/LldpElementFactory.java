package org.opennms.web.lldp;

import javax.servlet.ServletContext;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.LldpElementDao;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpElement.LldpChassisIdSubType;
import org.opennms.web.api.Util;
import org.opennms.web.element.NetworkElementFactoryInterface;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Transactional(readOnly=true)
public class LldpElementFactory implements InitializingBean, LldpElementFactoryInterface{

	@Autowired
	private LldpElementDao m_lldpElementDao;
	
	@Autowired
	private PlatformTransactionManager m_transactionManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    public static LldpElementFactoryInterface getInstance(ServletContext servletContext) {
        return getInstance(WebApplicationContextUtils.getWebApplicationContext(servletContext));    
    }

    public static LldpElementFactoryInterface getInstance(ApplicationContext appContext) {
    	return appContext.getBean(LldpElementFactoryInterface.class);
    }

    @Override
	public LldpElementNode getLldpElement(int nodeId) {
		return convertFromModel(m_lldpElementDao.findByNodeId(Integer.valueOf(nodeId)));
	}
	
	private LldpElementNode convertFromModel(LldpElement lldp) {
		if (lldp ==  null)
			return null;
		
		LldpElementNode lldpNode = new LldpElementNode();
		lldpNode.setLldpSysIdString(LldpChassisIdSubType.getTypeString(lldp.getLldpChassisIdSubType().getValue())+ " " + lldp.getLldpChassisId());
		lldpNode.setLldpSysName(lldp.getLldpSysname());
		lldpNode.setLldpCreateTime(Util.formatDateToUIString(lldp.getLldpNodeCreateTime()));
		lldpNode.setLldpLastPollTime(Util.formatDateToUIString(lldp.getLldpNodeLastPollTime()));
		
		return lldpNode;
	}

}
