package org.opennms.container.web;

import java.util.Arrays;
import java.util.Map;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class OnmsFelixFrameworkFactory implements FrameworkFactory{

	@Override
	public Framework newFramework(Map configProperties) {
		
		configProperties.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, Arrays.asList(new MyActivator()));
		
		return new Felix(configProperties);
	}

}
