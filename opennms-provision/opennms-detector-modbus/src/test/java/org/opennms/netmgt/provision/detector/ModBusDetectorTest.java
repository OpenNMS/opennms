package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.modbus.ModBusDetector;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/detectors.xml" })

public class ModBusDetectorTest implements ApplicationContextAware {
	ModBusDetector m_detector;
	private ApplicationContext m_applicationContext;

	// to really test functionality, this test needs to pull up a modbus server which returns the
	// expected value (and a false value)
	
	@Test
	public void testDetector() {
		m_detector = getDetector(ModBusDetector.class);
		assertNotNull(m_detector);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		m_applicationContext = applicationContext;
		// TODO Auto-generated method stub

	}

	private ModBusDetector getDetector(
			Class<? extends ServiceDetector> detectorClass) {
		Object bean = m_applicationContext.getBean(detectorClass.getName());
		assertNotNull(bean);
		assertTrue(detectorClass.isInstance(bean));
		return (ModBusDetector) bean;
	}
}
