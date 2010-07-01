package org.opennms.sms.reflector.commands.internal;

import java.io.PrintStream;
import org.opennms.sms.ping.SmsPinger;
import org.apache.felix.shell.Command;

/**
 * <p>SmsPingCommand class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SmsPingCommand implements Command {

	/** {@inheritDoc} */
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

	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return "smsPing";
	}

	/**
	 * <p>getShortDescription</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getShortDescription() {
		return "Initiates an smsPing to the desired phonenumber";
	}

	/**
	 * <p>getUsage</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getUsage() {
		return "smsPing <phoneNumber>";
	}

}
