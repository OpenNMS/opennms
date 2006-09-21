package org.opennms.webstart.poller;

import org.opennms.netmgt.poller.remote.PollerView;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class Main {
		
	public static void main(String[] args) {
		
		String[] configs = {
				"classpath:/META-INF/opennms/applicationContext-ws-gui.xml",
				"classpath:/META-INF/opennms/applicationContext-ws-svclayer.xml"
		};
		
        ApplicationContext ctx = new ClassPathXmlApplicationContext(configs);
        PollerView gui = (PollerView) ctx.getBean("pollerView");
        gui.showView();

	}


}
