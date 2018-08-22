/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
		}

		return retval;
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
