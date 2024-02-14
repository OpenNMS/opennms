/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.util.ilr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import static org.junit.Assert.*;


public class RegularExpressionTest {
	@Test
	public void testParseTimestamp (){
		String regex =  "\\s*(\\d+)-(\\d+)-(\\d+)\\s*(\\d+):(\\d+):(\\d+),(\\d+)";
		Pattern timestamp = Pattern.compile(regex);
		Matcher timestampMatcher = timestamp.matcher("2010-06-01 13:53:41,062 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: scheduleExistingInterfaces: begin");
		assertEquals(false,timestampMatcher.matches());
		timestampMatcher.reset();
		assertEquals(true,timestampMatcher.find());
	}
	@Test
	public void testSaveTimestamp() {
		String regex =  "\\s*(\\d+)-(\\d+)-(\\d+)\\s*(\\d+):(\\d+):(\\d+),(\\d+)";
		String sucess = "";
		String fail = "failure";
		Pattern timestamp = Pattern.compile(regex);
		Matcher timestampMatcher = timestamp.matcher("2010-06-01 13:53:41,062 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: scheduleExistingInterfaces: begin");
		if (timestampMatcher.find()){
			 sucess += timestampMatcher.group(); 
		}else {
			sucess += fail;
		}
		assertEquals("2010-06-01 13:53:41,062", sucess);
	}
	
	@Test
	public void testSeparateDate () {
		String regex =  "\\s*(\\d+)-(\\d+)-(\\d+)\\s*(\\d+):(\\d+):(\\d+),(\\d+)";
		String sucess = "";
		String fail = "failure";
		Pattern timestamp = Pattern.compile(regex);
		Matcher timestampMatcher = timestamp.matcher("2010-06-01 13:53:41,062 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: scheduleExistingInterfaces: begin");
		if (timestampMatcher.find()){
			 sucess += timestampMatcher.group(); 
		}else {
			sucess += fail;
		}
		assertEquals("2010-06-01 13:53:41,062", sucess);
		assertEquals(timestampMatcher.group(1), "2010");
		assertEquals(timestampMatcher.group(2), "06");
		assertEquals(timestampMatcher.group(3), "01");
		assertEquals(timestampMatcher.group(4), "13");
		assertEquals(timestampMatcher.group(5), "53");
		assertEquals(timestampMatcher.group(6), "41");
		assertEquals(timestampMatcher.group(7), "062");
		
		
	}
	@Test
	public void testParseType (){
		String regex =  "\\s*(\\D+)";
		Pattern type = Pattern.compile(regex);
		Matcher typeMatcher = type.matcher("2010-06-01 13:53:41,062 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: scheduleExistingInterfaces: begin");
		assertEquals(false,typeMatcher.matches());
		typeMatcher.reset();
		assertEquals(true,typeMatcher.find());
	}
	@Test
	public void testSaveType() {
		String regex =  "\\s(\\D+)\\s";
		String sucess = "";
		String fail = "failure";
		Pattern type = Pattern.compile(regex);
		Matcher typeMatcher = type.matcher("2010-06-01 13:53:41,062 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: scheduleExistingInterfaces: begin");
		if (typeMatcher.find()){
			 sucess += typeMatcher.group(); 
		}else {
			sucess += fail;
		}
		assertEquals(" DEBUG ", sucess);
		
	}
	@Test
	public void testParseThread (){
		String regex =  "\\s*\\[(\\D+)-(\\d+)\\s+(\\D+)-(\\D+)\\d\\]"; 
		Pattern thread = Pattern.compile(regex);
		Matcher threadMatcher = thread.matcher("2010-06-01 13:53:41,062 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: scheduleExistingInterfaces: begin");
		assertEquals(false,threadMatcher.matches());
		threadMatcher.reset();
		assertEquals(true,threadMatcher.find());
	}
	@Test
	public void testSaveThread() {
		String regex =  "\\[(\\D+)-(\\d+)\\s+(\\D+)-(\\D+)\\d\\]";
		String sucess = "";
		String fail = "failure";
		Pattern thread = Pattern.compile(regex);
		Matcher threadMatcher = thread.matcher("2010-06-01 13:53:41,062 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: scheduleExistingInterfaces: begin");
		if (threadMatcher.find()){
			 sucess += threadMatcher.group(); 
		}else {
			sucess += fail;
		}
		assertEquals("[CollectdScheduler-50 Pool-fiber0]", sucess);
	}
	@Test
	public void testParseEvent (){
		String regex =  "\\s*(\\D+):(\\D+):\\s+(\\D+)";
		Pattern event = Pattern.compile(regex);
		Matcher eventMatcher = event.matcher("2010-06-01 13:53:41,062 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: scheduleExistingInterfaces: begin");
		assertEquals(false,eventMatcher.matches());
		eventMatcher.reset();
		assertEquals(true,eventMatcher.find());
	}
	@Test
	public void testSaveEvent() {
		String regex =  "\\s(\\D+):(\\D+):\\s+(\\D+)";
		String sucess = "";
		String fail = "failure";
		Pattern event = Pattern.compile(regex);
		Matcher eventMatcher = event.matcher("2010-06-01 13:53:41,062 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: scheduleExistingInterfaces: begin");
		if (eventMatcher.find()){
			 sucess += eventMatcher.group(); 
		}else {
			sucess += fail;
		}
		assertEquals(" Collectd: scheduleExistingInterfaces: begin", sucess);
	}
	
}
