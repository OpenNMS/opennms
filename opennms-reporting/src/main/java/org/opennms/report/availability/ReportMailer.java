package org.opennms.report.availability;

import java.io.IOException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.JavaMailer;
import org.opennms.netmgt.utils.JavaMailerException;

public class ReportMailer {
	
	private static final String LOG4J_CATEGORY = "OpenNMS.Report";
	
	private Category log;
	
	private String m_filename;
	
	private String m_address;
	
	
	public ReportMailer() {
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(AvailabilityReport.class);
	}
	
	
	public ReportMailer(String address, String filename) {
		this.m_address = address;
		this.m_filename = filename;
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(AvailabilityReport.class);
	}
	
	public void send() throws IOException {
        if (m_filename == null || m_address == null) {
            throw new IllegalArgumentException("Cannot take null paramters.");
        }

        try {
            JavaMailer jm = new JavaMailer();
            jm.setTo(m_address);
            jm.setSubject("OpenNMS Availability Report");
            jm.setFileName(m_filename);
            jm.setMessageText("Availability Report Mailed from JavaMailer class.");
            jm.mailSend();
        } catch (JavaMailerException e) {
            log.error("Caught JavaMailer exception sending file: " + m_filename, e);
            throw new IOException("Error sending file: " + m_filename);
        }
    }

	public String getAddress() {
		return m_address;
	}

	public void setAddress(String address) {
		this.m_address = address;
	}

	public String getFilename() {
		return m_filename;
	}

	public void setFilename(String filename) {
		this.m_filename = filename;
	}

}
