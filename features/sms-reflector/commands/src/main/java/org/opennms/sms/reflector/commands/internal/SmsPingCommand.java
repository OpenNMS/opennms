package org.opennms.sms.reflector.commands.internal;

import java.io.PrintStream;
import org.opennms.sms.ping.SmsPinger;
import org.apache.felix.shell.Command;

public class SmsPingCommand implements Command {

	public void execute(String s, PrintStream out, PrintStream err) {
		try {
			String[] command = s.split("\\s");
			String phoneNumber = null;
			if(command.length > 1){
				phoneNumber = command[1];
			}else{
				throw new IllegalArgumentException("You need to have a phone number to ping. Usage smsPing <phoneNumber>");
			}
            Long latency = SmsPinger.ping(phoneNumber);
            
            if(latency == null){
            	out.println("Ping Timedout");
            }else{
            	out.println("Ping roundtrip time: " + latency);
            }
            
        } catch (Exception e) {
        	err.println(e);
        }
	}

	public String getName() {
		return "smsPing";
	}

	public String getShortDescription() {
		return "Initiates an smsPing to the desired phonenumber";
	}

	public String getUsage() {
		return "smsPing <phoneNumber>";
	}

}
