package org.opennms.bootstrap;

public class InstallerBootstrap extends Bootstrap {
	public static void main(String[] args) throws Exception {
		executeClass("org.opennms.install.Installer", "main", args, false);
	}
}
