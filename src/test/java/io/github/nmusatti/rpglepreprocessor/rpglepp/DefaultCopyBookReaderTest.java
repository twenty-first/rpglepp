package io.github.nmusatti.rpglepreprocessor.rpglepp;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.Arrays;

import org.junit.Test;

import io.github.twentyfirst.rpglepp.api.SourceFile;
import io.github.twentyfirst.rpglepp.rpglepp.DefaultCopyBookReader;

public class DefaultCopyBookReaderTest {
    
    @Test
    public void basic() throws FileNotFoundException {
        DefaultCopyBookReader dcbr = new DefaultCopyBookReader(Arrays.asList("test/copy"));
        SourceFile copy = dcbr.read("LIBRARY/MEMBER,COPYBOOK");
        assertTrue(!copy.getText().isEmpty());
    }
}
