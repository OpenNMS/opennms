package org.opennms.netmgt.dao.support;

import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.ResourceVisitor;
import org.springframework.beans.factory.InitializingBean;

public interface ResourceWalker extends InitializingBean {

	/**
	 * Don't throw any exceptions out of {@link #afterPropertiesSet()}.
	 */
	@Override
	void afterPropertiesSet();

	void walk();

	void setVisitor(ResourceVisitor visitor);

	void setResourceDao(ResourceDao resourceDao);

}
