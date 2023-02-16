package it.twenfir.rpglepp.parser;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.twenfir.antlr.tools.Files;
import it.twenfir.rpglepp.api.CopyBookReader;
import it.twenfir.rpglepp.api.SourceFile;
import it.twenfir.rpglepp.exception.InvalidCopyBookException;
import it.twenfir.rpglepp.exception.MissingCopyBookException;

public class DefaultCopyBookReader implements CopyBookReader {

	private static String[] COPY_EXTENSIONS = new String[] { ".RPGLECOPY", ".RPGLE", ".SQLRPGLE" };

    private static Pattern NAME_PATTERN = Pattern.compile("/?(?:[A-Za-z*]+(?:[,/]))*((?:\\w+)(?:\\.\\w+)?)");

    private List<String> copyBookPath;

    public DefaultCopyBookReader(List<String> copyBookPath) {
        this.copyBookPath = copyBookPath;
    }

    @Override
    public SourceFile read(String copyBookName){
        Matcher m = NAME_PATTERN.matcher(copyBookName);
        if ( ! m.matches() || m.group(1) == null ) {
            throw new InvalidCopyBookException(copyBookName);
        }
        String baseName = null;
        if ( m.group(1).contains(".") ) {
            try {
                String text = Files.readFile(m.group(1), copyBookPath);
                return new SourceFile(m.group(1), text);
            } catch (IOException e) {
            	baseName = m.group(1).substring(0, m.group(1).indexOf('.'));
            }
        }
        else {
            baseName = m.group(1);
        }
        baseName = baseName.toUpperCase();
        for ( String ext: COPY_EXTENSIONS ) {
            try {
                String fileName = baseName + ext;
                String text = Files.readFile(fileName, copyBookPath);
                return new SourceFile(fileName, text);
            } catch (IOException e) {
            }
        }
        throw new MissingCopyBookException(baseName);
    }
    
}
