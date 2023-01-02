package it.twenfir.rpglepp.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import it.twenfir.rpglepp.api.SourceFile;

public class RpgleppPreprocessorIntegrationTests {

	RpgleppPreprocessor preprocessor(List<String> copyPath) {
		return new RpgleppPreprocessor(new DefaultCopyBookReader(copyPath));
	}

	String pad(String in) {
		if ( Character.isDigit(in.charAt(0)) ) {
			in = in.substring(12);
		}
		int i = in.indexOf('\r');
		if ( i == -1 ) {
			i = in.indexOf('\n');
		}
		return StringUtils.rightPad(in.substring(0, i), 80) + in.substring(i);
	}
	
    @Test
    public void bootsrap() {
        String source = "      *\n";
        RpgleppPreprocessor pp = preprocessor(Collections.emptyList());
        String out = pp.preprocess(new SourceFile("DUMMY.RPGLE", source), null);
        assertEquals(pad(source), out);
    }

    @Test
    public void simpleCopy() {
        String source = "      /COPY LIBRARY/FILE,COPYBOOK\n";
        RpgleppPreprocessor pp = preprocessor(Arrays.asList("test/copy"));
        String out = pp.preprocess(new SourceFile("DUMMY.RPGLE", source), null);
        assertTrue(!out.isEmpty() && out.charAt(6) == '*');
    }

	@Test
	public void commentPrefixedBySpec() {
	    String source = "     C*\n";
	    RpgleppPreprocessor pp = preprocessor(Collections.emptyList());
	    String out = pp.preprocess(new SourceFile("DUMMY.RPGLE", source), null);
	    assertEquals(pad(source), out);
	}

	@Test
	public void windowsLineEndings() {
	    String source = "      *\r\n";
	    RpgleppPreprocessor pp = preprocessor(Collections.emptyList());
	    String out = pp.preprocess(new SourceFile("DUMMY.RPGLE", source), null);
	    assertEquals(pad(source), out);
	}

	@Test
	public void correctLineLength() {
	    String source = "000600050331     djNHField         c                   CONST(1)\n";
	    RpgleppPreprocessor pp = preprocessor(Collections.emptyList());
	    String out = pp.preprocess(new SourceFile("DUMMY.RPGLE", source), null);
	    assertEquals(pad(source), out);
	}

	@Test
	public void emptyLineWithNumber() {
	    String source = "000600050331\n";
	    RpgleppPreprocessor pp = preprocessor(Collections.emptyList());
	    String out = pp.preprocess(new SourceFile("DUMMY.RPGLE", source), null);
	    assertEquals(pad(source), out);
	}

	@Test
	public void define() {
	    String source = "      /DEFINE NAME\n";
	    RpgleppPreprocessor pp = preprocessor(Collections.emptyList());
	    Set<String> defines = new HashSet<>();
	    pp.preprocess(new SourceFile("DUMMY.RPGLE", source), defines);
	    assertTrue(defines.contains("NAME"));
	}

	@Test
	public void undefine() {
	    String source = "      /UNDEFINE NAME\n";
	    RpgleppPreprocessor pp = preprocessor(Collections.emptyList());
	    Set<String> defines = new HashSet<>();
	    defines.add("NAME");
	    pp.preprocess(new SourceFile("DUMMY.RPGLE", source), defines);
	    assertTrue(defines.isEmpty());
	}

	@Test
	public void defineUndefine() {
	    String source = "      /DEFINE NAME\n      /UNDEFINE NAME\n";
	    RpgleppPreprocessor pp = preprocessor(Collections.emptyList());
	    Set<String> defines = new HashSet<>();
	    pp.preprocess(new SourceFile("DUMMY.RPGLE", source), defines);
	    assertTrue(defines.isEmpty());
	}

	@Test
	public void padPrefix() {
		String source = "DF00   DCL-PROC proc;\n" +
		"|\n" +
		"|        DCL-F  F       EXTDESC('F')\n" +
		"|                       EXTFILE(*EXTDESC)\n" +
		"|                       QUALIFIED;\n" +
		"|\n" +
		"\n";
		
	    RpgleppPreprocessor pp = preprocessor(Collections.emptyList());
	    String out = pp.preprocess(new SourceFile("DUMMY.RPGLE", source), null);
		out.lines().forEachOrdered(l -> {
			assertTrue(l.charAt(l.length() - 1) != '|');
		});
	}
}
