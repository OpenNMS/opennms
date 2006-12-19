
import org.springframework.util.*;
import org.opennms.netmgt.model.*;
import org.opennms.netmgt.poller.*;
import org.opennms.netmgt.poller.monitors.*;
import org.opennms.tools.groovy.*;

	  
	  ManuallyMonitoredService monSvc = new ManuallyMonitoredService(ipAddr:'209.34.247.247');	  
	  
	  String pageSequenceConfig = """
    <page-sequence>
      <page path="/opennms" port="8080" successMatch="Password" />
      <page path="/opennms/j_acegi_security_check"  port="8080" method="POST" failureMatch="(?s)Your log-in attempt failed.*Reason: ([^&lt;]*)" failureMessage="Login in Failed: ${1}" successMatch="Log out">
        <parameter key="j_username" value="demo"/>
        <parameter key="j_password" value="demo"/>
      </page>
      <page path="/opennms/event/index.jsp" port="8080" successMatch="Event Queries" />
      <page path="/opennms/j_acegi_logout" port="8080" successMatch="logged off" />
    </page-sequence>
    """
    
	  Map parms = [retry:'1', timeout:'2000', 'page-sequence':pageSequenceConfig];
	  
	  PageSequenceMonitor monitor = new PageSequenceMonitor();
	  
	  monitor.initialize(parms);
	  monitor.initialize(monSvc);
	  PollStatus status = monitor.poll(monSvc, parms);
	  println status;
	  

