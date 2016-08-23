package org.opennms.plugins.com.impossibl.postgres.jdbc.test.manual;

import static org.junit.Assert.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.junit.Test;
import org.opennms.plugins.com.impossibl.postgres.jdbc.TimestampUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//from opennms dependency postgresql-9.3-1100-jdbc4.jar
//import org.postgresql.jdbc2.TimestampUtils;
// alternative is https://github.com/impossibl/pgjdbc-ng/blob/pgjdbc-ng-0.6/src/test/java/com/impossibl/postgres/jdbc/TimestampUtils.java

public class DateTimestampTranslatorTest {
	private static final Logger LOG = LoggerFactory.getLogger(DateTimestampTranslatorTest.class);

	@Test
	public void test() {
		System.out.println("start of test DateTimestampTranslatorTest");

		dbTimeStrToDatetimestr("2016-08-04 16:11:16.01+01");
		dbTimeStrToDatetimestr("2016-08-04 16:12:03.272205+01");
		dbTimeStrToDatetimestr("2016-08-04 17:42:40.418336+01");
		dbTimeStrToDatetimestr("2016-08-08 17:13:10.483+01");

		System.out.println("end of test DateTimestampTranslatorTest");
	}


	public void dbTimeStrToDatetimestr(String dbTimeStr){

		Calendar alarmCreationCal=null;

		try{

			String timeStr=null;
			System.out.println("\n\n                input time="+ dbTimeStr );

			TimestampUtils timestampUtils= new TimestampUtils();
			Timestamp timestamp= timestampUtils.toTimestamp(null, dbTimeStr);
			
			// using DatatypeConverter.printDateTime
			alarmCreationCal=Calendar.getInstance();
			alarmCreationCal.setTime(timestamp);
			timeStr=  DatatypeConverter.printDateTime(alarmCreationCal);
			System.out.println("DatatypeConverter.printDateTime="+timeStr);

			// using simple date format
			final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT); 
			timeStr=  simpleDateFormat.format(timestamp);
			
			System.out.println("simple date format=             "+ timeStr);


		} catch (Exception e){
			e.printStackTrace();
		}
	}

}
