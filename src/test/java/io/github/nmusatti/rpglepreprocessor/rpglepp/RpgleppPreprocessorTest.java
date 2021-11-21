package io.github.nmusatti.rpglepreprocessor.rpglepp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import io.github.twentyfirst.rpglepp.api.SourceFile;
import io.github.twentyfirst.rpglepp.rpglepp.DefaultCopyBookReader;
import io.github.twentyfirst.rpglepp.rpglepp.RpgleppPreprocessor;

public class RpgleppPreprocessorTest {

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
        String out = pp.preprocess(new SourceFile("DUMMY.RPGLE", source));
        assertEquals(pad(source), out);
    }

    @Test
    public void simpleCopy() {
        String source = "      /COPY LIBRARY/FILE,COPYBOOK\n";
        RpgleppPreprocessor pp = preprocessor(Arrays.asList("test/copy"));
        String out = pp.preprocess(new SourceFile("DUMMY.RPGLE", source));
        assertTrue(!out.isEmpty() && out.charAt(6) == '*');
    }

	@Test
	public void commentPrefixedBySpec() {
	    String source = "     C*\n";
	    RpgleppPreprocessor pp = preprocessor(Collections.emptyList());
	    String out = pp.preprocess(new SourceFile("DUMMY.RPGLE", source));
	    assertEquals(pad(source), out);
	}

	@Test
	public void windowsLineEndings() {
	    String source = "      *\r\n";
	    RpgleppPreprocessor pp = preprocessor(Collections.emptyList());
	    String out = pp.preprocess(new SourceFile("DUMMY.RPGLE", source));
	    assertEquals(pad(source), out);
	}

	@Test
	public void correctLineLength() {
	    String source = "000600050331     djNHField         c                   CONST(1)\n";
	    RpgleppPreprocessor pp = preprocessor(Collections.emptyList());
	    String out = pp.preprocess(new SourceFile("DUMMY.RPGLE", source));
	    assertEquals(pad(source), out);
	}

	@Test
	public void emptyLineWithNumber() {
	    String source = "000600050331\n";
	    RpgleppPreprocessor pp = preprocessor(Collections.emptyList());
	    String out = pp.preprocess(new SourceFile("DUMMY.RPGLE", source));
	    assertEquals(pad(source), out);
	}
}
