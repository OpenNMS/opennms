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
package org.opennms.install;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinReg.HKEY;

public class IPv6Validator {
	private static final String REGISTRY_CURRENTVERSION = "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion";
	Pattern SERVICE_PACK_PATTERN = Pattern.compile("Service Pack (\\d+)");

    /**
     * Checks if the platform is IPv6 ready.
     * For non windows platforms it always returns true.
     * For windows platforms it is version dependant.
     *
     * The following table assigns the internal microsoft version to the windows release.
     *
     * Operating system        Version number
     * -----------------       --------------
     * Windows 8.1                 6.3
     * Windows Server 2012 R2      6.3
     * Windows 8                   6.2
     * Windows Server 2012         6.2
     * Windows 7                   6.1
     * Windows Server 2008 R2      6.1
     * Windows Server 2008         6.0
     * Windows Vista               6.0
     * Windows Server 2003 R2      5.2
     * Windows Server 2003         5.2
     * Windows XP 64-Bit Edition   5.2
     * Windows XP                  5.1
     * Windows 2000                5.0
     *
     * Source: http://msdn.microsoft.com/en-us/library/windows/desktop/ms724832(v=vs.85).aspx
     *
     * @return true if Platform is not windows or if the provided windows system supports IPv6.
     */
    public boolean isPlatformIPv6Ready() {
		boolean ok = true;
		if (Platform.isWindows()) {
			debug(null, "Looks like we're on Windows...  Validating.");

			final String architecture = System.getenv("PROCESSOR_ARCHITECTURE");
			final String rawVersion = getStringFromRegistry("CurrentVersion");
			if (rawVersion == null) {
				error(null, "Unable to determine Windows version from the registry.");
				return false;
			}
			final Float version = Float.valueOf(rawVersion);
			if (version < 5.1f) {
				error(null, "You must be running at least Windows Vista (32-bit) or Windows XP Professional Service Pack 2 (64-bit), but Windows kernel version is only %.1f!", version);
				return false;
			}
			final String rawBuild = getStringFromRegistry("CurrentBuildNumber");
			Integer build = 0;
			if (rawBuild != null) {
				build = Integer.valueOf(rawBuild);
			}
			final String csdVersion = getStringFromRegistry("CSDVersion");
			Integer servicePack = 0;
			if (csdVersion != null) {
				final Matcher m = SERVICE_PACK_PATTERN.matcher(csdVersion);
				if (m.matches()) {
					servicePack = Integer.valueOf(m.group(1));
				}
				
				if (servicePack > 0) {
					debug(null, "Found Service Pack " + servicePack);
				}
			}

			// Windows XP
			if (version < 6.0f) {
				final String productName = getStringFromRegistry("ProductName");
				
				// Can't do XP on x86, unless it's Server 2003
				if (architecture.equals("x86")) {
					if (productName == null || !productName.contains("Server 2003")) {
						error(null, "OpenNMS does not support 32-bit Windows XP.");
						ok = false;
					}
				}

				// No matter what, we need Service Pack 2 on XP
				if (servicePack < 2) {
					error(null, "OpenNMS requires Service Pack 2 or higher on Windows XP and Windows Server 2003.");
					ok = false;
				}

				if (ok) {
					// If we get this far, make sure the hotfixes are installed
					for (final String hotfix : new String[] { "978338", "947369" }) {
						if (!checkHotfix(hotfix, "Windows XP or Windows Server 2003")) {
                            warn(null, "Hotfix %s was not found.", hotfix);
                            warn(null, "Go to http://support.microsoft.com/kb/%s and install it", hotfix);
							ok = false;
						}
					}
					if (!ok) {
						error(null, "One or more HotFixes was not found.  Please install them and try again.");
						return false;
					}
				}
			}
			
			// Windows Vista and Windows Server 2008
			if (version < 6.1f) {
				if (servicePack < 1) {
					error(null, "OpenNMS requires Service Pack 1 or higher on Windows Vista and Windows Server 2008.");
					ok = false;
				}
			}
			debug(null, "Windows NT Version %.1f, Build %d (Service Pack %d)", version, build, servicePack);
		}
        debug(null, "Platform is IPv6 ready: %s", ok);
		return ok;
	}

	private boolean checkHotfix(final String hotfix, final String os) {
		if (!keyExists(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Hotfix\\KB" + hotfix)) {
			error(null, "OpenNMS requires HotFix KB"
				+ hotfix
				+ " to be installed for IPv6 to function properly on " + os + "."
				+ "  To install it, see the following link: http://support.microsoft.com/?kbid="
				+ hotfix);
			return false;
		} else {
			debug(null, "Found HotFix KB" + hotfix);
		}
		return true;
	}
	private static void error(final Throwable t, final String format,
			final Object... args) {
		System.err.println(String.format("ERROR: " + format, args));
		if (t != null) {
			t.printStackTrace();
		}
	}

	private static void warn(final Throwable t, final String format, final Object... args) {
		System.err.println(String.format("WARNING: " + format, args));
		if (t != null) {
			t.printStackTrace();
		}
	}

	private static void debug(final Throwable t, final String format, final Object... args) {
		System.err.println(String.format("DEBUG: " + format, args));
		if (t != null) {
			t.printStackTrace();
		}
	}

	private String getStringFromRegistry(final String key) {
		try {
			return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, REGISTRY_CURRENTVERSION, key);
		} catch (final Throwable t) {
            // NMS-7053: we only want to warn the user, but do not scare him with an exception
			warn(null, "Unable to retrieve the value for %s\\%s", REGISTRY_CURRENTVERSION, key);
			return null;
		}
	}

	public boolean keyExists(final HKEY root, final String key) {
		return Advapi32Util.registryKeyExists(root, key);
	}

	public static void main(final String... args) throws IOException {
		final IPv6Validator checker = new IPv6Validator();
		debug(null, "ready = " + checker.isPlatformIPv6Ready());
	}
}
