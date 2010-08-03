package edu.ncsu.pdgrenon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Collector {

    public static final String SERVICE_TITLE_FORMAT = "%-40s%20s%15s%25s%15s%25s%15s%20s%25s\n";
    public static final String SERVICE_DATA_FORMAT = "%-40s%20s%15s%25s%15.1f%25s%15.1f%20s%25s\n";

	private Set<String> m_threads = new HashSet<String>();
	
	private LogMessage m_firstMessage;
	private LogMessage m_lastMessage;
	
	private Map<String, ServiceCollector> m_serviceCollectors = new HashMap<String, ServiceCollector>();

	public void addLog(String logMessage) {
		LogMessage msg = BaseLogMessage.create(logMessage);
		if (msg != null) {
			if (m_firstMessage == null && msg.isBeginMessage()) {
				m_firstMessage = msg;
			}
			if (msg.isEndMessage()) {
				m_lastMessage = msg;
			}
			getServiceCollector(msg.getServiceID()).addMessage(msg);
			m_threads.add(msg.getThread());
		}
		
	}
	public Date getStartTime() {
		LogMessage firstValidLine = getFirstValidLogMessage();
		return firstValidLine.getDate();	
			
	}
	public LogMessage getFirstValidLogMessage (){
		return m_firstMessage;
	}
	public LogMessage getLastValidLogMessage () {
		return m_lastMessage;
	}
	public Date getEndTime() {
		LogMessage logMessage = getLastValidLogMessage();
		return logMessage.getDate();
	}
	public long getDuration() {
		return this.getEndTime().getTime()-this.getStartTime().getTime();
	}
	public int getServiceCount() {
		return getServices().size();
	}

	public Set<String> getServices() {
		return m_serviceCollectors.keySet();
	}
	
	public Collection<ServiceCollector> getServiceCollectors() {
		Comparator<ServiceCollector> c = new Comparator<ServiceCollector>() {

			public int compare(ServiceCollector o1, ServiceCollector o2) {
				Long a = Long.valueOf(o1.getAverageCollectionTime());
				Long b = Long.valueOf(o2.getAverageCollectionTime());
				return b.compareTo(a);
			}
		};
		
		ArrayList<ServiceCollector> collectors = new ArrayList<ServiceCollector>(m_serviceCollectors.values());
	
		Collections.sort(collectors, c);
		return collectors;
	}
	
	public int getThreadCount() {
		return m_threads.size();
	}
	public int getCollectionsPerService(String serviceID) {
		return getServiceCollector(serviceID).getCollectionCount();
	}
	
	public long getAverageCollectionTimePerService(String serviceID) {
		return getServiceCollector(serviceID).getAverageCollectionTime();
	}

	public long getTotalCollectionTimePerService(String serviceID) {
		return getServiceCollector(serviceID).getTotalCollectionTime();
	}
	private ServiceCollector getServiceCollector(String serviceID) {
		ServiceCollector serviceCollector = m_serviceCollectors.get(serviceID);
		if (serviceCollector == null) {
			serviceCollector = new ServiceCollector(serviceID);
			m_serviceCollectors.put(serviceID, serviceCollector);
		}
		return serviceCollector;
	}

	public void readLogMessagesFromFile(String fileName) throws IOException {
		File logFile = new File(fileName);
		BufferedReader r = new BufferedReader(new FileReader(logFile));	
		String logMessage = r.readLine();
		while(logMessage != null){
			this.addLog(logMessage);
			logMessage = r.readLine();
		}
		r.close();
	}
	public void printGlobalStats(PrintWriter out) {
		SimpleDateFormat f = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss,S");
		out.println("Start Time: " + f.format(this.getStartTime()));
		out.println("End Time: " + f.format(this.getEndTime()));
		out.println("Duration: "+ Collector.formatDuration(this.getDuration()));
		out.println("Total Services: "+this.getServiceCount());
		out.println("Threads Used: " + this.getThreadCount());
	}
	public static String formatDuration(long millis) {
		if (millis==0) {
			return "0s";
		}
		boolean force = false;
		StringBuilder buf = new StringBuilder();
		if (force || millis >= (1000*3600*24)) {
			long d = millis/(1000*3600*24);
			buf.append(d);
			buf.append("d");
			millis%=(1000*3600*24);
			force = millis!=0;
		}
		if (force || millis >= (1000*3600)) {
			long h = millis/(1000*3600);
			buf.append(h);
			buf.append("h");
			millis%=(1000*3600);
			force = millis!=0;
		}
		if (force || millis >= 60000) {
			long m = millis/60000;
			buf.append(m);
			buf.append("m");
			millis %= 60000;
			force=millis!=0;
		}
		if (millis!=0) {
			long s = millis/1000;
			buf.append(s);
			if(millis%1000 !=0) {
				buf.append(".");
				buf.append(String.format("%03d", millis%1000));
			}
			buf.append("s");
		}
		return buf.toString();
		
	}
	private void printServiceStats(ServiceCollector serviceCollector, PrintWriter out) {
		out.printf(SERVICE_DATA_FORMAT, serviceCollector.getServiceID(), 
			   Collector.formatDuration(serviceCollector.getAverageCollectionTime()), serviceCollector.getCollectionCount(), 
			   Collector.formatDuration(serviceCollector.getAverageSuccessfulCollectionTime()), serviceCollector.getSuccessPercentage(), 
			   Collector.formatDuration(serviceCollector.getAverageErrorCollectionTime()), serviceCollector.getErrorPercentage(),
			   Collector.formatDuration(serviceCollector.getAverageTimeBetweenCollections()),
			   Collector.formatDuration(serviceCollector.getTotalCollectionTime()));
	}
//	Service               Avg Collect Time  Avg Persist Time  Avg Time between Collects # Collections Total Collection Time Total Persist Time
//	19/172.10.1.21/SNMP       13.458s             .002s              5m27s                    3                 45.98s           .010s
	public void printServiceHeader(PrintWriter out) {
		out.printf(SERVICE_TITLE_FORMAT, "Service", "Avg Collect Time", "# Collections",  "Avg Success Time", "% Success", "Avg Error Time", "% Errors", "Avg Time Between", "Total Collection Time");
		
	}
	public void printReport(PrintWriter out) {
		this.printGlobalStats(out);
		out.println();
		this.printServiceHeader(out);
		for(ServiceCollector serviceCollector : getServiceCollectors()) {
			this.printServiceStats(serviceCollector, out);
		}
	}
	public void printServiceStats(String serviceID, PrintWriter out) {
		ServiceCollector collector = getServiceCollector(serviceID);
		if (collector != null) {
			printServiceStats(collector, out);
		}
		
	}
}
