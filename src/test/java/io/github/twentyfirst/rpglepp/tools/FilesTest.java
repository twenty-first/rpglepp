package io.github.twentyfirst.rpglepp.tools;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

public class FilesTest {

    @Test
    public void readResourceTest() throws IOException {
    	String s = Files.readFile("logback.xml", Arrays.asList("classpath:"));
    	assertTrue(s.indexOf("twentyfirst") != -1);
    }
}
