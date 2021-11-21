package io.github.twentyfirst.rpglepp.rpglepp;

import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import io.github.twentyfirst.rpglepp.RpgleppLexer;
import io.github.twentyfirst.rpglepp.RpgleppParser;
import io.github.twentyfirst.rpglepp.api.CopyBookReader;
import io.github.twentyfirst.rpglepp.api.SourceFile;


public class RpgleppPreprocessor {

    private CopyBookReader reader;
    private ANTLRErrorListener listener;

    public RpgleppPreprocessor(CopyBookReader reader) {
        this(reader, null);
    }

    public RpgleppPreprocessor(List<String> copyPath) {
        this(copyPath, null);
    }

    public RpgleppPreprocessor(List<String> copyPath, ANTLRErrorListener listener) {
        this(new DefaultCopyBookReader(copyPath), listener);
    }

    public RpgleppPreprocessor(CopyBookReader reader, ANTLRErrorListener listener) {
        this.listener = listener;
        this.reader = reader;
    }
    
    public String preprocess(SourceFile copyBook) {
        ANTLRInputStream inputStream = new ANTLRInputStream(copyBook.getText());
		RpgleppLexer lexer = new RpgleppLexer(inputStream);
		if ( listener != null ) {
			lexer.removeErrorListeners();
			lexer.addErrorListener(listener);
		}
		RpgleppTokenSource source = new RpgleppTokenSource(lexer);
		TokenStream tokenStream = new CommonTokenStream(source);
        RpgleppRewriter rewriter = new RpgleppRewriter(tokenStream, this);
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
