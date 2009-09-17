/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: September 17, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.vmmgr;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

import org.apache.log4j.Logger;

/**
 * This class is used to replace the stdout and stderr PrintStreams in Java's System class so that they
 * can be redirected to the Logger API.  
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class CaptchaStds {
    
    public static final Logger captchaLogger = Logger.getLogger("OpenNMS.Stds");
    
    public static void captchaStdOut() {
        System.setErr(wrapStdWithLog(System.err));
        System.setOut(wrapStdWithLog(System.out));
    }
    
    private static PrintStream wrapStdWithLog(final PrintStream s) {
        
        return new PrintStream(s) {
            
            StringBuffer sb = new StringBuffer();

            
            @Override
            public void flush() {
                sb = new StringBuffer();
            }
            
            @Override
            public PrintStream format(Locale l, String format, Object... args) {
                return super.format(l, format, args);
            }
            
            @Override
            public void print(boolean b) {
                sb.append(b);
            }
            
            @Override
            public void print(char[] s) {
                sb.append(s);
            }
            
            @Override
            public void print(float f) {
                sb.append(f);
            }
            
            @Override
            public void print(double d) {
                sb.append(d);
            }
            
            @Override
            public void print(int i) {
                sb.append(i);
            }
            
            @Override
            public void print(char c) {
                sb.append(c);
            }
            
            @Override
            public void print(long l) {
                sb.append(l);
            }
            
            @Override
            public void print(Object obj) {
                sb.append(obj);
            }
            
            @Override
            public void print(String s) {
                sb.append(s);
            }
            
            @Override
            public PrintStream printf(Locale l, String format, Object... args) {
                return super.printf(l, format, args);
            }
            
            @Override
            public PrintStream printf(String format, Object... args) {
                return super.printf(format, args);
            }
            
            @Override
            public void println() {
                captchaLogger.debug(sb.toString());
                this.flush();
            }
            
            @Override
            public void println(boolean x) {
                sb.append(x);
                captchaLogger.debug(sb.toString());
                this.flush();
            }
            
            @Override
            public void println(char x) {
                sb.append(x);
                captchaLogger.debug(sb.toString());
                this.flush();
            }
            
            @Override
            public void println(char[] x) {
                sb.append(x);
                captchaLogger.debug(sb.toString());
                this.flush();
            }
            
            @Override
            public void println(double x) {
                sb.append(x);
                captchaLogger.debug(sb.toString());
                this.flush();
            }
            
            @Override
            public void println(float x) {
                sb.append(x);
                captchaLogger.debug(sb.toString());
                this.flush();
            }
            
            @Override
            public void println(int x) {
                sb.append(x);
                captchaLogger.debug(sb.toString());
                this.flush();
            }
            
            @Override
            public void println(long x) {
                sb.append(x);
                captchaLogger.debug(sb.toString());
                this.flush();
            }
            
            @Override
            public void println(Object x) {
                sb.append(x);
                captchaLogger.debug(sb.toString());
                this.flush();
            }
            
            @Override
            public void println(String x) {
                sb.append(x);
                captchaLogger.debug(sb.toString());
                this.flush();
            }
            
            @Override
            public PrintStream append(char c) {
                return super.append(c);
            }
            
            @Override
            public PrintStream append(CharSequence csq) {
                return super.append(csq);
            }
            
            @Override
            public PrintStream append(CharSequence csq, int start, int end) {
                return super.append(csq, start, end);
            }
            
            @Override
            public boolean checkError() {
                return false;
            }
            
            @Override
            public void close() {
            }
            
            @Override
            public PrintStream format(String format, Object... args) {
                return super.format(format, args);
            }

            //FIXME there is probably some contract for this call that makes sense to be implemented
            @Override
            protected void setError() {
            }

            //no references from java.io package to this so I'm not conncerned with handling it in any special way
            @Override
            public void write(byte[] b) throws IOException {
                super.write(b);
            }
            
            //no references from java.io package to this so I'm not conncerned with handling it in any special way
            @Override
            public void write(byte[] buf, int off, int len) {
                super.write(buf, off, len);
            }
            
            //no references from java.io package to this so I'm not conncerned with handling it in any special way
            @Override
            public void write(int b) {
                super.write(b);
            }
            
        };
    }
}
