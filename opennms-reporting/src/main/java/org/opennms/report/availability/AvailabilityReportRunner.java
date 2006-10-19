package org.opennms.report.availability;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.report.availability.render.ReportRenderException;
import org.opennms.report.availability.render.ReportRenderer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

public class AvailabilityReportRunner {

	private static final String LOG4J_CATEGORY = "OpenNMS.Report";

	private static final String HTML_FORMAT = "HTML";

	private static final String SVG_FORMAT = "SVG";

	private static final String PDF_FORMAT = "PDF";

	private static AvailabilityCalculator calculator;

	private static ReportRenderer renderer;
	
	public static void main(String args[]) {

		String startMonth = System.getProperty("startMonth");
		String startDate = System.getProperty("startDate");
		String startYear = System.getProperty("startYear");

		if ((startMonth == null) && (startDate == null) && (startYear == null)) {
			Date date = new Date();
			String defaultMonth = new SimpleDateFormat("MM").format(date);
			String defaultDay = new SimpleDateFormat("dd").format(date);
			String defaultYear = new SimpleDateFormat("yyyy").format(date);
			System.out.println("running report with date of (yyyy/mm/dd) " + defaultYear +  defaultMonth + defaultDay );
			doReport(defaultYear, defaultMonth, defaultDay);
		} else if (isValidDate(startDate, startMonth, startYear)) {
			System.out.println("running report with valid user date of (yyyy/mm/dd) " + startYear +  startMonth + startDate );
			doReport(startDate, startMonth, startYear);
		} else 
			System.out.println("Oops, invalid date entered, cannot run report");

	}

	public static void doReport(String startYear, String startMonth,
			String startDate) {

		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		Category log = ThreadCategory.getInstance(AvailabilityReport.class);
		ClassPathResource resource = new ClassPathResource("META-INF/opennms/applicationContext-reporting.xml");
		BeanFactory bf = new XmlBeanFactory(resource);
	
		// properties required for calculating the availability data and
		// marshalling to XML

		String logoURL = System.getProperty("image");
		String categoryName = System.getProperty("catName");
		if (categoryName == null || categoryName.equals(""))
			categoryName = "all";

		String monthFormat = System.getProperty("monthFormat");

		if (monthFormat == null || monthFormat.equals("")
				|| monthFormat.equals("classic")) {
			calculator = (AvailabilityCalculator) bf.getBean("classicAvailabilityCalculator");
		} else {
			calculator = (AvailabilityCalculator) bf.getBean("calendarAvailabilityCalculator");
		}

		calculator.setCalendar(new GregorianCalendar());
		calculator.setCategoryName(categoryName);
		calculator.setLogoURL(logoURL);
		calculator.setStartMonth(startMonth);
		calculator.setStartDate(startDate);
		calculator.setStartYear(startYear);

		String format = System.getProperty("format");
		
		if (format == null || format.equals(SVG_FORMAT)) {
			log.debug("report will be rendered as PDF with embedded SVG");
			renderer = (ReportRenderer) bf.getBean("svgReportRenderer");
			//renderer.setOutputFileName("svg-" + categoryName + "-adhoc.pdf");
			calculator.setReportFormat(SVG_FORMAT);
		} else if (format.equals(PDF_FORMAT)) {
			log.debug("report will be rendered as PDF");
			renderer = (ReportRenderer) bf.getBean("pdfReportRenderer");
			//renderer.setOutputFileName(categoryName + "-adhoc.pdf");
			calculator.setReportFormat(PDF_FORMAT);
		} else {
			log.debug("report will be rendered as html");
			renderer = (ReportRenderer) bf.getBean("htmlReportRenderer");
			//renderer.setOutputFileName(categoryName + "-adhoc.html");
			calculator.setReportFormat(HTML_FORMAT);
		}

		try {
			//String xmlFileName = categoryName + "-adhoc.xml";
			calculator.calculate();
			//calculator.setOutputFileName(xmlFileName);
			calculator.writeXML();
			//renderer.setInputFileName(xmlFileName);
			renderer.render();
		} catch (AvailabilityCalculationException ce) {
			log.fatal("Unable to calculate report data ", ce);
		} catch (ReportRenderException re) {
			log.fatal("Unable to render report ", re);
		}
	}

	private static boolean isValidDate(String day, String month, String year) {

		String month2;
		String day2;

		String format = new String("MM/dd/yy");

		if (month.length() == 1)
			month2 = new String("0" + month);
		else
			month2 = new String(month);

		if (day.length() == 1)
			day2 = new String("0" + day);
		else
			day2 = new String(day);

		String date = new String(month2 + "/" + day2 + "/" + year);

		try {
			Date dateSimple = new SimpleDateFormat(format).parse(date);
			Format formatter = new SimpleDateFormat(format);
			if (!date.equals(formatter.format(dateSimple))) {
				return false;
			}
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

}
