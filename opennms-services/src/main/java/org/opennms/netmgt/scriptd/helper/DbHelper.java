package org.opennms.netmgt.scriptd.helper;

import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.NodeDao;
import org.springframework.beans.factory.access.BeanFactoryReference;

public class DbHelper {

	
	public static String getNodeLabel(Integer nodeid) {
    	BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        return BeanUtils.getBean(bf,"nodeDao", NodeDao.class)
        	.get(nodeid).getLabel();
	}
	
}
