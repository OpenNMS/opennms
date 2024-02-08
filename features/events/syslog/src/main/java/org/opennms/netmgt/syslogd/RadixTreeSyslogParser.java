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
package org.opennms.netmgt.syslogd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.SyslogdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This parser reads a set of grok patterns that are stored in the 
 * <i>grok-patterns.txt</i> classpath resource and uses the patterns to
 * construct a syslog message parser.
 * 
 * @author Seth
 */
public class RadixTreeSyslogParser extends SyslogParser {

	private static final Logger LOG = LoggerFactory.getLogger(RadixTreeSyslogParser.class);

	private static final Pattern STRUCTURED_DATA = Pattern.compile("^(?:\\[.*?\\])*(?: \uFEFF?(.*?))?$");

	private static RadixTreeParser radixParser = new RadixTreeParser();

	static {
		try {
			File configFile = ConfigFileConstants.getFile(ConfigFileConstants.SYSLOGD_GROK_PATTERNS_FILE_NAME);
			if (configFile.exists() && configFile.isFile()) {
				try (Reader reader = new FileReader(configFile)) {
					new BufferedReader(reader).lines().forEach(pattern -> {
						// Ignore comments and blank lines
						if (pattern == null || pattern.trim().length() == 0 || pattern.trim().startsWith("#")) {
							return;
						}
						radixParser.teach(GrokParserStageSequenceBuilder.parseGrok(pattern).toArray(new ParserStage[0]));
					});
				}

				if (radixParser.size() == 0) {
					LOG.warn("{} has no grok patterns, check the content of {}, using default grok pattern set", RadixTreeSyslogParser.class.getSimpleName(), ConfigFileConstants.getFileName(ConfigFileConstants.SYSLOGD_GROK_PATTERNS_FILE_NAME));
					teachDefaultPatterns();
				}
			} else {
				teachDefaultPatterns();
			}
		} catch (FileNotFoundException e) {
			teachDefaultPatterns();
		} catch (IOException e) {
			LOG.warn("Unexpeceted exception while reading {}, using default grok pattern set", ConfigFileConstants.getFileName(ConfigFileConstants.SYSLOGD_GROK_PATTERNS_FILE_NAME), e);
			teachDefaultPatterns();
		}


		// After we have taught all of the patterns to the parser, perform
		// edge compression to optimize the tree
		radixParser.performEdgeCompression();
	}

	private static final void teachDefaultPatterns() {
		new BufferedReader(new InputStreamReader(RadixTreeSyslogParser.class.getClassLoader().getResourceAsStream("org/opennms/netmgt/syslogd/grok-patterns.txt"))).lines().forEach(pattern -> {
			// Ignore comments and blank lines
			if (pattern == null || pattern.trim().length() == 0 || pattern.trim().startsWith("#")) {
				return;
			}
			radixParser.teach(GrokParserStageSequenceBuilder.parseGrok(pattern).toArray(new ParserStage[0]));
		});
	}

	public RadixTreeSyslogParser(SyslogdConfig config, ByteBuffer syslogString) {
		super(config, syslogString);
	}

	public static RadixTreeParser getRadixParser() {
		return radixParser;
	}

	public static void setRadixParser(RadixTreeParser radixParser) {
		RadixTreeSyslogParser.radixParser = radixParser;
	}

	/**
	 * Since this parser does not rely on a regex expression match for its initial
	 * parsing, always return true.
	 */
	@Override
	public boolean find() {
		return true;
	}

	@Override
	public SyslogMessage parse() {
		SyslogMessage retval = radixParser.parse(getText()).join();

		if (retval != null) {
			// Trim off the RFC 5424 structured data to emulate the behavior of the legacy parser (for now)
			String message = retval.getMessage();
			if (message != null && message.startsWith("[")) {
				Matcher matcher = STRUCTURED_DATA.matcher(message);
				if (matcher.find()) {
					String newMessage = matcher.group(1);
					retval.setMessage(newMessage == null ? null : newMessage);
				}
			}
			setTimezoneIfNeeded(retval);
			setYearIfNeeded(retval);
		}

		return retval;
	}

	private void setYearIfNeeded(SyslogMessage message) {
	    boolean hasTimeinformation =
	            message.getMonth() != null ||
	            message.getDayOfMonth() != null ||
	            message.getHourOfDay() != null ||
	            message.getMinute() != null ||
	            message.getSecond() != null ||
	            message.getMillisecond() != null;
	    if (hasTimeinformation && message.getYear() == null) {
	    	SyslogYearCompleter.complete(message);
	    }
	}

	private void setTimezoneIfNeeded(SyslogMessage message){
        boolean hasTimeinformation = // to no break logic in ConvertToEvent
		        message.getYear() != null ||
				message.getMonth() != null ||
				message.getDayOfMonth() != null ||
				message.getHourOfDay() != null ||
				message.getMinute() != null ||
				message.getSecond() != null ||
				message.getMillisecond() != null;

		ZoneId timeZone = message.getZoneId();
		if(timeZone == null && hasTimeinformation && getConfig().getTimeZone() == null){
			message.setZoneId(ZoneId.systemDefault());
		} else if (timeZone == null && hasTimeinformation && getConfig().getTimeZone() != null){
			message.setZoneId(getConfig().getTimeZone().toZoneId());
		}
	}
}
