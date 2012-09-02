package org.opennms.features.topology.ssh.internal;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TerminalTest {

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
			assertTrue(expected.contains(tags)); //testing default html tags from empty dump

			term.write("& < >");
			expected = term.dump();
			assertTrue(expected.contains("&amp; &lt; &gt;")); //testing html-escaped characters

			term.write("\u001b[?5h");
			expected = term.dump();
			assertTrue(expected.contains("&amp; &lt; &gt;")); //testing html-escaped characters

			term.write("\u001b[4m");
			term.write("underlinedText");
			expected = term.dump();
			assertTrue(expected.contains("underlinedText")); //testing underlined texted

			term.write("\u001b[1m");
			term.write("boldedText");
			expected = term.dump();
			assertTrue(expected.contains("boldedText")); //testing bolded text
		} catch (Exception e) {
			fail("Dump should not have thrown an exception");
		}
	}

	@Test
	public void testPipe_A() {
		String expected = "A";
		String input = "A";
		assertEquals(expected, term.pipe(input)); //testing normal character writing
	}

	@Test
	public void testPipe_2tildas() {
		String expected = "~";
		String input = "~~";
		assertEquals(expected, term.pipe(input)); //testing 2 consecutive tilda's
	}

	@Test
	public void testPipe_control_ucLetters() {
		String expected = "\u001b[";
		String input = "~";
		for (int i = 0; i < ucLetters.length; i++) {
			//testing upper-case control sequences
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
				//testing lower-case control sequences from a - d
				assertEquals(expected + esc_first4[i], term.pipe(input + lcLetters[i]));
			}
		}
		expected = "\u001b[";
		for (int j = 0; j < lcLetters.length; j++) {
			if (j < esc_last8.length) {
				//testing lower-case control sequences from e - l
				assertEquals(expected + esc_last8[j], term.pipe(input + lcLetters[j+i]));
			}
		}
	}

	@Test
	public void testPipe_control_nums() {
		String expected = "\u001b[";
		String input = "~";
		for (int i = 0; i < 4; i++) {
			//testing numeric control sequences from 1 - 4
			assertEquals(expected + esc_nums[i], term.pipe(input + (i+1)));
		}

		expected = "";
		input = "~5";
		assertEquals(expected, term.pipe(input)); //testing non-existent control sequence
	}

	@Test
	public void testPipe_cm_ucLetters() {
		String expected = "\u001bO";
		String input = "~";
		term.write("\u001b[?1h");
		for (int i = 0; i < ucLetters.length; i++) {
			//testing cursor-mode upper-case control sequences
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
				//testing cursor-mode lower-case control sequences from a - d
				assertEquals(expected + esc_first4[i], term.pipe(input + lcLetters[i]));
			}
		}
		expected = "\u001b[";
		for (int j = 0; j < lcLetters.length; j++) {
			if (j < esc_last8.length) {
				//testing cursor-mode lower-case control sequences from e - l
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
			//testing cursor-mode numeric control sequences from 1 - 4
			assertEquals(expected + esc_nums[i], term.pipe(input + (i+1)));
		}
		expected = "";
		input = "~5";
		assertEquals(expected, term.pipe(input)); //testing non-existence cursor-mode numeric control sequence
	}

	@Test
	public void testPipe_cm_tilda() {
		String expected = "~";
		String input = "~~";
		term.write("\u001b[?1h");
		assertEquals(expected, term.pipe(input)); //testing cursor-mode tilda control sequence
	}


	@Test
	public void testPipe_backspaceMode_on() {
		String expected = "" + (char)8;
		String input = "" + (char)127;
		term.write("\u001b[?67h");
		//testing a backspace char while in backspace-mode
		assertArrayEquals(expected.getBytes(), term.pipe(input).getBytes());
	}

	@Test
	public void testPipe_backspaceMode_off() {
		String expected = "" + (char)127;
		String input = "" + (char)127;
		//testing a backspace char while not in backspace-mode
		assertArrayEquals(expected.getBytes(), term.pipe(input).getBytes());
	}

	@Test
	public void testPipe_newlineMode_on() {
		String expected = "" + (char)13 + (char)10;
		String input = "" + (char)13;
		term.write("\u001b[20h");
		//testing a carriage return while in newline-mode
		assertArrayEquals(expected.getBytes(), term.pipe(input).getBytes());

		expected = "B";
		input = "B";
		term.write("\u001b[20h");
		//testing a normal char while in newline-mode
		assertArrayEquals(expected.getBytes(), term.pipe(input).getBytes());
	}

	@Test
	public void testPipe_newlineMode_off() {
		String expected = "" + (char)13;
		String input = "" + (char)13;
		//testing a carriage return while not in newline-mode
		assertArrayEquals(expected.getBytes(), term.pipe(input).getBytes());
	}

	@Test
	public void testRead() {
		assertEquals("", term.read()); //testing a read from an empty term
		term.write("\u001b[c");
		assertEquals("\u001b[?1;2c", term.read()); //testing a read after a request has been made
	}

	@Test
	public void testSetSize() {
		//testing various term sizes around the boundary limits
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
		//testing an empty term with 80x24 space chars
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
		//testing a term with text written in it
		assertArrayEquals(expected.toString().getBytes(), term.toString().getBytes());
	}

	@Test
	public void testWrite() {
		term.write("\u001b[r"); //enables scrolling of entire screen
		term.write("\u001b[T"); //scrolls screen down 1 line
		term.write("\u001b[S"); //scrolls screen up 1 line
		term.write("" + (char)10); //writing a newline char
		term.write("" + (char)13); //writing a carriage return
		term.write("\u001b[20h"); //on newline-mode
		term.write("" + (char)10); //writing a newline char
		term.write("" + (char)13); //writing a carriage return
		term.write("" + 'a'); //writing a normal char
		term.write("\u001b[" + (char)24); //writing parse-reset sequence
		term.write("\u001b[" + (char)25); //checking non-existent sequence between 24 and 26
		term.write("\u001b[" + (char)26); //writing parse-reset sequence
		term.write("\u001b[?3h"); //132 char mode on
		term.write("\u001b[?3l"); //80 char mode on
		term.write("\u001b[?40h"); //insert mode on
		term.write("\u001b[?3h"); //132 char mode on while insert mode is on
		term.write("\u001b[?3l"); //80 char mode on while insert mode is on
		term.write("\u001b[4h"); //insert/replace mode on
		term.write("\u001b[?6h"); //origin mode on
		term.write("\u001b[?6l"); //origin mode off
		term.write("\u001b[?7h"); //auto-wrap mode on
		term.write("\u001b[?25h"); //text-cursor enabled
		term.write("\u001b[?47h"); //graphic rotated print mode on
		term.write("\u001b[?68h"); //keyboard usage on
		term.write("\u001b#8"); //fill screen with E
		term.write("" + (char)14); //shift out
		term.write("" + (char)15); //shift in
		assertTrue(term.write("\u001b[!p")); //test passes if the term writes all the above lines successfully
	}

	@Test
	public void testWrite_1byteModes() {
		for (int i = 0x0040; i <= 0x78; i++) {
			//testing CSI sequences with 1 byte
			term.write("\u001b[" + (char)i);
		}

		for (int i = 0x0036; i <= 0x7E; i++) {
			//testing escape sequences with 1 byte
			term.write("\u001b" + (char)i);
		}
	}

	@Test
	public void testWrite_2byteModes() {
		for (int i = 0x23; i <= 0x29; i++) {
			for (int j = 0x30; j <= 0x42; j++) {
				//testing escape sequences with 2 bytes
				term.write("\u001b" + (char)i + (char)j);
			}
		}

		for (int i = 0x20; i <= 0x24; i++) {
			for (int j = 0x40; j <= 0x77; j++) {
				//testing CSI sequences with 2 bytes
				term.write("\u001b[" + (char)i + (char)j);
			}
		}
	}

	@Test
	public void testCSI_SGR() {
		//testing different text colors and styles
		term.write("\u001b[7;m");
		term.write("\u001b[8;m");
		term.write("\u001b[24;m");
		term.write("\u001b[27;m");
		term.write("\u001b[28;m");
		term.write("\u001b[30;m");
		term.write("\u001b[39;m");
		term.write("\u001b[40;m");
		term.write("\u001b[49;m");
		term.write("\u001b[<8;m");
	}
	
	@Test
	public void testCSI_DSR() {
		//testing different Device Status Reports (DSR)
		term.write("\u001b[5n");
		term.write("\u001b[6n");
		term.write("\u001b[7n");
		term.write("\u001b[8n");
		term.write("\u001b[?6n");
		term.write("\u001b[?15n");
		term.write("\u001b[?25n");
		term.write("\u001b[?26n");
		term.write("\u001b[?53n");
	}
	
	@Test
	public void testCSI_DECREQTPARM() {
		//testing different Report Terminal Parameters
		term.write("\u001b[0x");
		term.write("\u001b[1x");
	}
	
	@Test
	public void testCSI_ED() {
		//testing the erase display sequence
		term.write("\u001b[1J");
		term.write("\u001b[2J");
	}
	
	@Test
	public void testCSI_DA() {
		//testing the Device Attributes
		term.write("\u001b[>0c");
	}
	
	@Test
	public void testCSI_TBC() {
		//testing the Tabulation Clear sequence
		term.write("\u001b[3g");
		term.write("\u001b[4g");
	}
	
	@Test
	public void testCSI_EL() {
		//testing the erase line sequence
		term.write("\u001b[1K");
		term.write("\u001b[2K");
	}
	
	@Test
	public void testCSI_CTC() {
		//testing the Cursor Tabulation Control
		term.write("\u001b[5W");
		term.write("\u001b[6W");
	}
	
	@Test
	public void testCSI_DECSTBM() {
		//testing the Top and Bottom margins
		term.write("\u001b[?6h");
		term.write("\u001b[" + (char)0x72);
	}
	
	@Test
	public void testCSI_CUP() {
		//testing the Cursor up sequence
		term.write("\u001b[?6h");
		term.write("\u001b[" + (char)0x48);
	}
	
	@Test
	public void testDUMB_ECHO() {
		//testing chars that fall through to the dumb_echo method
		term.write("" + (char)16);
		term.write("" + (char)8);
		term.write("" + (char)9);
	}
	
	@Test
	public void testState_Str() {
		//testing different sequences that change the state to Str
		term.write("\u001bP!");
		term.write("\u001bP" + (char)31);
	}

}
