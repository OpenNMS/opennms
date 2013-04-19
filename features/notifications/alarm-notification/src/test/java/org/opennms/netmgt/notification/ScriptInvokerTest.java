package org.opennms.netmgt.notification;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ScriptInvokerTest {

	@Test
	public void testScriptInvokerStringStringBooleanIntegerInteger() {
		new ScriptInvoker("alarmxml", "notification", true, 10, 10);
	}

	@Test
	public void testInvokeScript() {
		ScriptInvoker scriptInvoker = new ScriptInvoker("alarmxml",
				"CBU_App_Script.py", true, 5, 1);
		scriptInvoker.invokeScript();
	}

}
