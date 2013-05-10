/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc. OpenNMS(R) is
 * a registered trademark of The OpenNMS Group, Inc. OpenNMS(R) is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/ Required script will be
 * called from this class with xml as argument.
 */
package org.opennms.netmgt.notification;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.alarmd.api.NorthbounderException;

public class ScriptInvoker {

	private String m_alarmXml;

	private String m_scriptName;

	private boolean m_errorHandling;

	private Integer m_numberOfRetries;

	private Integer m_retryInterval;

	private boolean m_isAlreadyInvoked = false;
	
	private String m_timeoutInSeconds;

	int count = 0;

	/**
	 * 
	 * This constructor is used when only basic filter is present.
	 * 
	 * @param alarmXml
	 * @param scriptName
	 * @param errorHandling
	 * @param numberOfRetries
	 * @param retryInterval
	 */
	public ScriptInvoker(String alarmXml, String scriptName,String timeoutInSeconds,
			boolean errorHandling, Integer numberOfRetries,
			Integer retryInterval) {
		this.m_alarmXml = alarmXml;
		this.m_scriptName = scriptName;
		this.m_errorHandling = errorHandling;
		this.m_numberOfRetries = numberOfRetries;
		this.m_retryInterval = retryInterval;
		this.m_timeoutInSeconds = timeoutInSeconds;
	}

	/**
	 * Script is invoked by this class with xml as argument.
	 * 
	 * @return
	 */
	public boolean invokeScript() throws NorthbounderException {

		LogUtils.debugf(this, "Script " + this.m_scriptName + " to be invoked.");
		if (!m_isAlreadyInvoked && this.m_numberOfRetries != null)
			count = this.m_numberOfRetries;
		String runScript = ConfigFileConstants.getHome()
				+ "/etc/alarm-notification/scripts/" + m_scriptName;
		File file = new File(runScript);
		if (!file.exists()) {
			LogUtils.errorf(this, "File " + runScript + " is not available.");
			return false;
		}

		int mid = m_scriptName.lastIndexOf(".");
		String extn = m_scriptName.substring(mid + 1, m_scriptName.length());
		String cmd = null;
		if (extn.equals("sh"))
			cmd = "bash";
		else if (extn.equals("pl"))
			cmd = "perl";
		else if (extn.equals("py"))
			cmd = "python";
		ProcessBuilder processBuilder = new ProcessBuilder(cmd, runScript,
				this.m_alarmXml);
		processBuilder.redirectErrorStream(true);
		final Process shellProcess;
		try {
			int timeoutInSeconds = 60 * 1000;
			try{
				timeoutInSeconds = Integer.parseInt(m_timeoutInSeconds) * 1000;
			}catch (Exception e) {
				LogUtils.debugf(this,"Exception in the configured timeout for script " + this.m_scriptName);
				timeoutInSeconds = 60*1000;
			}
			shellProcess = processBuilder.start();
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					shellProcess.destroy();
					LogUtils.debugf(this, "Timeout exceeded for the script "
							+ m_scriptName + ". Error handling will not be done.");
				}
			}, timeoutInSeconds);
			//String output = loadStream(shellProcess.getInputStream());
			int shellExitStatus = shellProcess.waitFor();
			timer.cancel();
			LogUtils.debugf(this, "Error status " + shellExitStatus);
			if (shellExitStatus != 0 && m_errorHandling == false && shellExitStatus != 143) {
				LogUtils.debugf(this, "Error while invoking "
						+ this.m_scriptName + " with '" + this.m_alarmXml
						+ "' as argument.But errorhandling is set to false.");
			} else if (shellExitStatus != 0 && m_errorHandling == true
					&& count != 0) {
				LogUtils.debugf(this,
						"Error Handling Enabled.Current retry count is "
								+ count + ".The script will be invoked after "
								+ this.m_retryInterval + " " + " seconds.");
				Thread.sleep(this.m_retryInterval.longValue() * 1000);
				m_isAlreadyInvoked = true;
				count = count - 1;
				invokeScript();
			} else if (shellExitStatus != 0 && m_errorHandling == true
					&& count == 0) {
				LogUtils.debugf(this,
						"No of retry count exceeded. Script will not be invoked again.");
			}
			// errorOutput.close();
			// consoleOutput.close();
			/**
			 * printstacktrace is added as the ScriptInvoker may be called from
			 * drl file too.
			 */
		} catch (IOException ioException) {
			ioException.printStackTrace();
			throw new NorthbounderException(ioException);
		} catch (InterruptedException interruptedException) {
			interruptedException.printStackTrace();
			throw new NorthbounderException(interruptedException);
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new NorthbounderException(exception);
		}
		return true;
	}

	private static String loadStream(InputStream s) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(s));
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			while ((line = br.readLine()) != null)
				sb.append(line).append("\n");
			return sb.toString();

		} finally {
			if (s != null)
				s.close();
			if (br != null)
				br.close();
		}

	}
}
