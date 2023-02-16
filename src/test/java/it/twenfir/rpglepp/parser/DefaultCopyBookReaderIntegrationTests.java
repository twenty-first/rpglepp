package it.twenfir.rpglepp.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import it.twenfir.rpglepp.api.SourceFile;

public class DefaultCopyBookReaderIntegrationTests {
    
    @Test
    public void traditionalName() throws FileNotFoundException {
        DefaultCopyBookReader dcbr = new DefaultCopyBookReader(Arrays.asList("test/copy"));
        SourceFile copy = dcbr.read("LIBRARY/MEMBER,COPYBOOK");
        assertTrue(!copy.getText().isEmpty());
    }
    
    @Test
    public void ifsName() throws FileNotFoundException {
        DefaultCopyBookReader dcbr = new DefaultCopyBookReader(Arrays.asList("test/copy"));
        SourceFile copy = dcbr.read("/LIBRARY/MEMBER/COPYBOOK");
        assertTrue(!copy.getText().isEmpty());
    }
    
    @Test
    public void ifsNameWithExtension() throws FileNotFoundException {
        DefaultCopyBookReader dcbr = new DefaultCopyBookReader(Arrays.asList("test/copy"));
        SourceFile copy = dcbr.read("/usr/include/sqlda.sql");
        assertTrue(!copy.getText().isEmpty());
    }
}
