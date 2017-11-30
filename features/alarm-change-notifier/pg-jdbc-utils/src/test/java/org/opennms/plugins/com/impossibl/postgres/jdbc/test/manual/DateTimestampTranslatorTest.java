/**
 * Copyright (c) 2013, impossibl.com
 * Copyright (c) 2016, OpenNMS group inc
 * 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of impossibl.com nor the names of its contributors may
 *    be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.opennms.plugins.com.impossibl.postgres.jdbc.test.manual;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
