package org.opennms.features.topology.ssh.internal;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestTerminal {

	Terminal term;
	Terminal term_large;
	String[] ucLetters = {"A", "B", "C", "D", "F", "H"};
	String[] lcLetters = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"};
	String[] esc_first4 = {"P", "Q", "R", "S"};
	String[] esc_last8 = {"15~", "17~", "18~", "19~", "20~", "21~", "23~", "24~"};
	String[] esc_nums = {"5~", "6~", "2~", "3~"};
	
	@Before
	public void setUp() throws Exception {
		term = new Terminal();
		term_large = new Terminal(100, 30);
	}

	@Test
	public void testDump() {
		String expected = "";
		String tags = "<div><pre class='term'>";
		try {
			expected = term.dump();
			assertTrue(expected.contains(tags));
			
			term.write("& < >");
			expected = term.dump();
			assertTrue(expected.contains("&amp; &lt; &gt;"));
			
			term.write("\u001b[?5h");
			expected = term.dump();
			assertTrue(expected.contains("&amp; &lt; &gt;"));
			
			term.write("\u001b[4m");
			term.write("underlinedText");
			expected = term.dump();
			assertTrue(expected.contains("underlinedText"));
			
			term.write("\u001b[1m");
			term.write("boldedText");
			expected = term.dump();
			assertTrue(expected.contains("boldedText"));
		} catch (Exception e) {
			fail("Dump should not have thrown an exception");
		}
	}
	
	@Test
	public void testPipe_A() {
		String expected = "A";
		String input = "A";
		assertEquals(expected, term.pipe(input));
	}
	
	@Test
	public void testPipe_2tildas() {
		String expected = "~";
		String input = "~~";
		assertEquals(expected, term.pipe(input));
	}
	
	@Test
	public void testPipe_control_ucLetters() {
		String expected = "\u001b[";
		String input = "~";
		for (int i = 0; i < ucLetters.length; i++) {
			assertEquals(expected + ucLetters[i], term.pipe(input + ucLetters[i]));
		}
	}
	
	@Test
	public void testPipe_control_lcLetters() {
		String expected = "\u001bO";
		String input = "~";
		int i;
		for (i = 0; i < 4; i++) {
			if (i < lcLetters.length && i < esc_first4.length) {
				assertEquals(expected + esc_first4[i], term.pipe(input + lcLetters[i]));
			}
		}
		expected = "\u001b[";
		for (int j = 0; j < lcLetters.length; j++) {
			if (j < esc_last8.length) {
				assertEquals(expected + esc_last8[j], term.pipe(input + lcLetters[j+i]));
			}
		}
	}
	
	@Test
	public void testPipe_control_nums() {
		String expected = "\u001b[";
		String input = "~";
		for (int i = 0; i < 4; i++) {
			assertEquals(expected + esc_nums[i], term.pipe(input + (i+1)));
		}
		
		expected = "";
		input = "~5";
		assertEquals(expected, term.pipe(input));
	}
	
	@Test
	public void testPipe_cm_ucLetters() {
		String expected = "\u001bO";
		String input = "~";
		term.write("\u001b[?1h");
		for (int i = 0; i < ucLetters.length; i++) {
			assertEquals(expected + ucLetters[i], term.pipe(input + ucLetters[i]));
		}
	}
	
	@Test
	public void testPipe_cm_lcLetters() {
		String expected = "\u001bO";
		String input = "~";
		term.write("\u001b[?1h");
		int i;
		for (i = 0; i < 4; i++) {
			if (i < lcLetters.length && i < esc_first4.length) {
				assertEquals(expected + esc_first4[i], term.pipe(input + lcLetters[i]));
			}
		}
		expected = "\u001b[";
		for (int j = 0; j < lcLetters.length; j++) {
			if (j < esc_last8.length) {
				assertEquals(expected + esc_last8[j], term.pipe(input + lcLetters[j+i]));
			}
		}
	}
	
	@Test
	public void testPipe_cm_nums() {
		String expected = "\u001b[";
		String input = "~";
		term.write("\u001b[?1h");
		for (int i = 0; i < 4; i++) {
			assertEquals(expected + esc_nums[i], term.pipe(input + (i+1)));
		}
		expected = "";
		input = "~5";
		assertEquals(expected, term.pipe(input));
	}
	
	@Test
	public void testPipe_cm_tilda() {
		String expected = "~";
		String input = "~~";
		term.write("\u001b[?1h");
		assertEquals(expected, term.pipe(input));
	}

	
	@Test
	public void testPipe_backspaceMode_on() {
		String expected = "" + (char)8;
		String input = "" + (char)127;
		term.write("\u001b[?67h");
		assertArrayEquals(expected.getBytes(), term.pipe(input).getBytes());
	}
	
	@Test
	public void testPipe_backspaceMode_off() {
		String expected = "" + (char)127;
		String input = "" + (char)127;
		assertArrayEquals(expected.getBytes(), term.pipe(input).getBytes());
	}
	
	@Test
	public void testPipe_newlineMode_on() {
		String expected = "" + (char)13 + (char)10;
		String input = "" + (char)13;
		term.write("\u001b[20h");
		assertArrayEquals(expected.getBytes(), term.pipe(input).getBytes());
		
		expected = "B";
		input = "B";
		term.write("\u001b[20h");
		assertArrayEquals(expected.getBytes(), term.pipe(input).getBytes());
	}
	
	@Test
	public void testPipe_newlineMode_off() {
		String expected = "" + (char)13;
		String input = "" + (char)13;
		assertArrayEquals(expected.getBytes(), term.pipe(input).getBytes());
	}
	
	@Test
	public void testRead() {
		assertEquals("", term.read());
		term.write("\u001b[c");
		assertEquals("\u001b[?1;2c", term.read());
	}
	
	@Test
	public void testSetSize() {
		assertFalse(term.setSize(1, 1));
		assertFalse(term.setSize(1, 3));
		assertFalse(term.setSize(3, 1));
		assertFalse(term.setSize(257, 257));
		assertFalse(term.setSize(50, 257));
		assertFalse(term.setSize(257, 50));
		assertTrue(term.setSize(100, 100));
	}
	
	@Test
	public void testToString() {
		StringBuilder expected = new StringBuilder();
		for (int i = 0; i < 24; i++) {
			for (int j = 0; j < 80; j++) {
				expected.append(" ");
			}
			expected.append("\n");
		}
		assertArrayEquals(expected.toString().getBytes(), term.toString().getBytes());
		
		expected = new StringBuilder();
		expected.append("this thing better work");
		int remainingLine = 80 - expected.length();
		for (int i = 0; i < remainingLine; i++) {
			expected.append(" ");
		}
		expected.append("\n");
		for (int i = 0; i < 23; i++) {
			for (int j = 0; j < 80; j++) {
				expected.append(" ");
			}
			expected.append("\n");
		}
		term.write("this thing better work");
		assertArrayEquals(expected.toString().getBytes(), term.toString().getBytes());
	}
	
	@Test
	public void testWrite() {

	}

}
