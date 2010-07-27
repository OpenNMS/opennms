package edu.ncsu.pdgrenon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;


public class Collector {

	public static final String SERVICE_FORMAT_STRING = "%-28s%20s%15s%25s\n";
	private List<LogMessage> m_messages = new ArrayList<LogMessage>();

	public void addLog(String logMessage) {
		m_messages.add(new LogMessage(logMessage));
	}
	public Date getStartTime() {
		LogMessage firstValidLine = getFirstValidLogMessage();
		return firstValidLine.getDate();	
			
	}
	public LogMessage getFirstValidLogMessage (){
		for(LogMessage logMessage : m_messages ) {
			if(logMessage.isBeginMessage()){
				return logMessage;
			}
		}
		return null;
	}
	public LogMessage getLastValidLogMessage () {
		for(ListIterator<LogMessage> it = m_messages.listIterator(m_messages.size()); it.hasPrevious() ; ) { 
			LogMessage logMessage = it.previous();
			if(logMessage.isEndMessage()){
				return logMessage;
			}
		}
		return null;
	}
	public Date getEndTime() {
		LogMessage logMessage = getLastValidLogMessage();
		return logMessage.getDate();
	}
	public long getDuration() {
		return this.getEndTime().getTime()-this.getStartTime().getTime();
	}
	public int getServiceCount() {
		Set<String> services = getServices();
		return services.size();
	}
	public Set<String> getServices() {
		Set<String> services = new HashSet<String>();
		for(LogMessage logMessage : m_messages) {
			if (logMessage.getServiceID() != null) {
				services.add(logMessage.getServiceID());
			}
		}
		return services;
	}
	public void printMessageTypeCounts(PrintWriter out) {
		int errorCount = 0;
		int beginCount = 0;
		int endCount = 0;
		int persistCount = 0;
		int failure = 0;
		for(LogMessage logMessage : m_messages) {
			if(logMessage.isBeginMessage() && !logMessage.isPersistMessage()) {
				beginCount +=1;
			}else if (logMessage.isCollectorEndMessage() && !logMessage.isPersistMessage()) {
				endCount +=1;
			}else if(logMessage.isPersistMessage()) {
				persistCount +=1;
			}else if(logMessage.isErrorMessage()) {
				errorCount+=1;
			}else{
				failure +=1;
			}
		}
		out.println("Beginning collecting messages during collection: " + beginCount + " Ending collecting messages during collection:  " + endCount +
				" Persisting messages during collection: " + persistCount + " Error messages during collection: " + errorCount+ " failures: " + failure);
	}
	public int getThreadCount() {
		Set<String> threads = new HashSet<String>();
		for(LogMessage logMessage : m_messages) {
			threads.add(logMessage.getThread());
		}
		return threads.size();
	}
	public int collectionsPerService(String serviceID) {
		int count = 0;
		for(LogMessage logMessage : m_messages) {
			if(logMessage.isCollectorBeginMessage() && serviceID.equals(logMessage.getServiceID())) {
				count += 1;
			}
		}
		return count;
	}
	public long averageCollectionTimePerService(String serviceID) {
		long [] begin = new long [this.collectionsPerService(serviceID)];
		long [] end = new long [this.collectionsPerService(serviceID)];
		int i = 0;
		long added = 0;
		long average = 0;
		long [] elapsed = new long[this.collectionsPerService(serviceID)];
		for(LogMessage logMessage : m_messages) {
			if(serviceID.equals(logMessage.getServiceID()) && logMessage.isCollectorBeginMessage()) {
				begin[i] = logMessage.getDate().getTime();
			}
			if(serviceID.equals(logMessage.getServiceID()) && logMessage.isCollectorEndMessage()) {
				end[i] = logMessage.getDate().getTime();
			}		
		}
		for(i = 0 ; i < begin.length && i < end.length ; i++) {
			elapsed[ i] =  end[ i] - begin[i];
			added += elapsed [i];
		}
		if(elapsed.length == 0) {
			average = 0;
		}else if(end[0]==0){
			average = 0;
		}else {
			average = added / elapsed.length;
		}
		return average;
	}
	public long totalCollectionTimePerService(String serviceID) {
		long [] begin = new long [this.collectionsPerService(serviceID)];
		long [] end = new long [this.collectionsPerService(serviceID)];
		int i = 0;
		long added = 0;
		long [] elapsed = new long[this.collectionsPerService(serviceID)];
		for(LogMessage logMessage : m_messages) {
			if(serviceID.equals(logMessage.getServiceID()) && logMessage.isCollectorBeginMessage()) {
				begin[i] = logMessage.getDate().getTime();
			}
			if(serviceID.equals(logMessage.getServiceID()) && logMessage.isCollectorEndMessage()) {
				end[i] = logMessage.getDate().getTime();
			}		
		}
		for(i = 0 ; i < begin.length && i < end.length ; i++) {
			elapsed[ i] =  end[ i] - begin[i];
			added += elapsed [i];
		}
		if(added<0){
			added = 0;
		}
		return added;
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
	public void printServiceStats(String serviceID, PrintWriter out) {
		out.printf(SERVICE_FORMAT_STRING, serviceID, Collector.formatDuration(this.averageCollectionTimePerService(serviceID)), this.collectionsPerService(serviceID), Collector.formatDuration(this.totalCollectionTimePerService(serviceID)));
	}
//	Service               Avg Collect Time  Avg Persist Time  Avg Time between Collects # Collections Total Collection Time Total Persist Time
//	19/172.10.1.21/SNMP       13.458s             .002s              5m27s                    3                 45.98s           .010s
	public void printServiceHeader(PrintWriter out) {
		out.printf(SERVICE_FORMAT_STRING, "Service", "Avg Collect Time", "# Collections", "Total Collection Time");
		
	}
	public void printReport(PrintWriter out) {
		this.printGlobalStats(out);
		out.println();
		this.printServiceHeader(out);
		for(String serviceID : this.getServices()){
			this.printServiceStats(serviceID, out);
		}
	}
}
