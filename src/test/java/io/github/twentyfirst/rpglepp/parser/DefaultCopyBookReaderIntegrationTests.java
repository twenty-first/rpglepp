package io.github.twentyfirst.rpglepp.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import it.twenfir.rpglepp.api.SourceFile;
import it.twenfir.rpglepp.parser.DefaultCopyBookReader;

public class DefaultCopyBookReaderIntegrationTests {
    
    @Test
    public void basic() throws FileNotFoundException {
        DefaultCopyBookReader dcbr = new DefaultCopyBookReader(Arrays.asList("test/copy"));
        SourceFile copy = dcbr.read("LIBRARY/MEMBER,COPYBOOK");
        assertTrue(!copy.getText().isEmpty());
    }
}
