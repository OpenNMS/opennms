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

	public boolean isPlatformIPv6Ready() {
			if (Platform.isWindows()) {
				String[] requiredHotfixes = { "978338", "947369" };

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
				}

				// Windows XP
				if (version < 6.0f) {
					boolean ok = true;
					final String productName = getStringFromRegistry("ProductName");
					if (architecture.equals("x86")) {
						if (productName == null || !productName.contains("Server 2003")) {
							error(null, "OpenNMS does not support 32-bit Windows XP.");
							ok = false;
						}
					}

					if (servicePack < 2) {
						error(null, "OpenNMS requires Service Pack 2 or higher on Windows XP.");
						ok = false;
					}

					for (final String hotfix : requiredHotfixes) {
						if (!keyExists(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Hotfix\\KB" + hotfix)) {
							error(null, "OpenNMS requires HotFix KB" + hotfix + " to be installed for IPv6 to function properly on Windows XP or Windows Server 2003." +
									"  To install it, see the following link: http://support.microsoft.com/?kbid=" + hotfix);
							ok = false;
						}
					}
					if (!ok) {
						error(null, "One or more HotFixes was not found.  Please install them and try again.");
						return false;
					}
				}
				debug(null, "Windows NT version %.1f, build %d (service pack %d)", version, build, servicePack);
				return true;
			}
		return false;
	}

	private static void error(final Throwable t, final String format, final Object... args) {
		System.err.println(String.format("ERROR: " + format, args));
		if (t != null) {
			t.printStackTrace();
		}
//		LogUtils.errorf(this, t, format, args);
	}

	private static void warn(final Throwable t, final String format, final Object... args) {
		System.err.println(String.format("WARNING: " + format, args));
		if (t != null) {
			t.printStackTrace();
		}
//		LogUtils.errorf(this, t, format, args);
	}

	private static void debug(final Throwable t, final String format, final Object... args) {
		System.err.println(String.format("DEBUG: " + format, args));
		if (t != null) {
			t.printStackTrace();
		}
//		LogUtils.debugf(this, t, format, args);
	}

	private String getStringFromRegistry(final String key) {
		try {
			return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, REGISTRY_CURRENTVERSION, key);
		} catch (final Throwable t) {
			warn(t, "Unable to retrieve the value for %s\\%s", REGISTRY_CURRENTVERSION, key);
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
