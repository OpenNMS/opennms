package org.opennms.netmgt.notification;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

public class ScriptInvokerTest {

	@Test
	public void testScriptInvokerStringStringBooleanIntegerInteger() {
		new ScriptInvoker("alarmxml", "notification", "16", true, 10, 10);
	}

	@Test
	public void testInvokeScript() {
		ScriptInvoker scriptInvoker = new ScriptInvoker("alarmxml","CBU_App_Script.py",
				"15", true, 5, 1);
		scriptInvoker.invokeScript();
	}

}
