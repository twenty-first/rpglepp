package io.github.twentyfirst.rpglepp.parser;

import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import io.github.twentyfirst.rpglepp.RpgleppLexer;
import io.github.twentyfirst.rpglepp.RpgleppParser;
import io.github.twentyfirst.rpglepp.api.CopyBookReader;
import io.github.twentyfirst.rpglepp.api.RpgleppErrorListener;
import io.github.twentyfirst.rpglepp.api.SourceFile;
import it.twenfir.antlr.parser.LoggingTokenSource;


public class RpgleppPreprocessor {

    private CopyBookReader reader;
    private RpgleppErrorListener listener;

    public RpgleppPreprocessor(CopyBookReader reader) {
        this(reader, null);
    }

    public RpgleppPreprocessor(List<String> copyPath) {
        this(copyPath, null);
    }

    public RpgleppPreprocessor(List<String> copyPath, RpgleppErrorListener listener) {
        this(new DefaultCopyBookReader(copyPath), listener);
    }

    public RpgleppPreprocessor(CopyBookReader reader, RpgleppErrorListener listener) {
        this.listener = listener;
        this.reader = reader;
    }
    
    public String preprocess(SourceFile copyBook, Set<String> defines) {
		CodePointCharStream inputStream = CharStreams.fromString(copyBook.getText(), copyBook.getName());
		RpgleppLexer lexer = new RpgleppLexer(inputStream);
		if ( listener != null ) {
			lexer.removeErrorListeners();
			lexer.addErrorListener(listener);
		}
		LoggingTokenSource source = new LoggingTokenSource(lexer);
		TokenStream tokenStream = new CommonTokenStream(source);
        RpgleppRewriter rewriter = new RpgleppRewriter(tokenStream, defines, this, listener);
		RpgleppParser parser = new RpgleppParser(tokenStream);
		if ( listener != null ) {
			parser.removeErrorListeners();
			parser.addErrorListener(listener);
		}
        RuleContext tree = parser.sourceFile();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(rewriter, tree);
        return rewriter.getText();
    }

    RpgleppPreprocessor make() {
    	RpgleppPreprocessor pp = new RpgleppPreprocessor(reader, listener);
    	return pp;
    }

	public CopyBookReader getReader() {
		return reader;
	}
}
