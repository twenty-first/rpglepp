package io.github.twentyfirst.rpglepp.parser;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.twentyfirst.rpglepp.RpgleppParser.CommentContext;
import io.github.twentyfirst.rpglepp.RpgleppParser.ConditionContext;
import io.github.twentyfirst.rpglepp.RpgleppParser.CopyContext;
import io.github.twentyfirst.rpglepp.RpgleppParser.DefineContext;
import io.github.twentyfirst.rpglepp.RpgleppParser.Else_Context;
import io.github.twentyfirst.rpglepp.RpgleppParser.ElseifContext;
import io.github.twentyfirst.rpglepp.RpgleppParser.EndifContext;
import io.github.twentyfirst.rpglepp.RpgleppParser.EofContext;
import io.github.twentyfirst.rpglepp.RpgleppParser.EofDirContext;
import io.github.twentyfirst.rpglepp.RpgleppParser.EolContext;
import io.github.twentyfirst.rpglepp.RpgleppParser.If_Context;
import io.github.twentyfirst.rpglepp.RpgleppParser.InstructionContext;
import io.github.twentyfirst.rpglepp.RpgleppParser.LineContext;
import io.github.twentyfirst.rpglepp.RpgleppParser.PrefixContext;
import io.github.twentyfirst.rpglepp.RpgleppParser.UndefineContext;
import io.github.twentyfirst.rpglepp.RpgleppParserBaseListener;
import io.github.twentyfirst.rpglepp.api.RpgleppErrorListener;
import io.github.twentyfirst.rpglepp.api.SourceFile;

public class RpgleppRewriter extends RpgleppParserBaseListener {

	private Logger log = LoggerFactory.getLogger(getClass());
	
    private TokenStreamRewriter rewriter;
    private RpgleppPreprocessor preprocessor;
    private RpgleppErrorListener listener;

    private Set<String> defines;
    private Deque<Condition> conditions = new ArrayDeque<Condition>();
    private Token inactiveStart;
    
    private String copyText = null;
    private boolean hasLineNumber = false;
    private String pad = null;
    private boolean deleteLine = false;
    private Token eofDir;

    public RpgleppRewriter(TokenStream tokenStream, Set<String> defines, 
    		RpgleppPreprocessor preprocessor, RpgleppErrorListener listener) {
        rewriter = new TokenStreamRewriter(tokenStream);
        this.preprocessor = preprocessor;
        this.listener = listener;
        this.defines = defines != null ? defines : new HashSet<>();
    }

    @Override
	public void exitPrefix(PrefixContext ctx) {
    	if ( inactiveStart == null ) {
        	if ( ctx.LINE_NUMBER() != null ) {
        		rewriter.delete(ctx.LINE_NUMBER().getSymbol());
        		hasLineNumber = true;
        	}
        	if ( ctx.END_SOURCE_DIR() != null && ctx.END_SOURCE_DIR().getText().length() < 6 ) {
        		String repl = StringUtils.rightPad(ctx.END_SOURCE_DIR().getText(), 6);
        		rewriter.replace(ctx.END_SOURCE_DIR().getSymbol(), repl);
        	}
    	}
	}

	@Override
	public void exitInstruction(InstructionContext ctx) {
    	if ( inactiveStart == null ) {
    		if ( ctx.BAD_INSTRUCTION() != null ) {
    			String repl = StringUtils.replaceChars(ctx.BAD_INSTRUCTION().getText(), "\u00A3\u00A7\u00C2", "LSA");
    			rewriter.replace(ctx.BAD_INSTRUCTION().getSymbol(), repl);
    		}
    	}
	}

	@Override
	public void exitComment(CommentContext ctx) {
    	if ( inactiveStart == null ) {
    		if ( ctx.BAD_COMMENT() != null ) {
    			String repl = StringUtils.replaceEach(ctx.BAD_COMMENT().getText(), 
    					new String[] { "\u00A3", "\u00A7" }, 
    					new String[] { "Pound", "Para" });
    			rewriter.replace(ctx.BAD_COMMENT().getSymbol(), repl);
    		}
    	}
	}

	@Override
    public void exitCopy(CopyContext ctx) {
    	if ( inactiveStart == null ) {
            RpgleppPreprocessor pp = preprocessor.make();
            SourceFile copyBook = pp.getReader().read(ctx.member().getText());
            copyText = pp.preprocess(copyBook, defines);
    	}
    }

    @Override
	public void exitIf_(If_Context ctx) {
    	conditions.push(new Condition(ctx, defines, 
    			conditions.size() == 0 || conditions.getFirst().isActive()));
    	if ( ! conditions.getFirst().isActive() ) {
    		if ( inactiveStart == null ) {
    			inactiveStart = ctx.getStart();
    		}
    	}
    	deleteLine = true;    		
	}

	@Override
	public void exitElseif(ElseifContext ctx) {
		if ( conditions.isEmpty() ) {
			listener.preprocessingError(ctx.getStart().getLine(), 
					ctx.getStart().getCharPositionInLine(), "/ELSEIF directive without /IF");
		}
		else {
			conditions.pop();
	    	conditions.push(new Condition(ctx, defines, 
	    			conditions.size() == 0 || conditions.getFirst().isActive()));
	    	if ( conditions.getFirst().isActive() ) {
	    		if ( inactiveStart != null ) {
	    			rewriter.delete(inactiveStart, ctx.getStop());
	    			inactiveStart = null;
	    		}
	    	}
	    	else {
	    		if ( inactiveStart == null ) {
	    			inactiveStart = ctx.getStart();
	    		}
	    	}
		}
    	deleteLine = true;
	}

	@Override
	public void exitElse_(Else_Context ctx) {
		if ( conditions.isEmpty() ) {
			listener.preprocessingError(ctx.getStart().getLine(), 
					ctx.getStart().getCharPositionInLine(), "/ELSE directive without /IF");
		}
		else {
			Condition prev = conditions.pop();
			conditions.push(new Condition(prev.getName(), prev.getKind().flip(),
					( conditions.size() == 0 || conditions.getFirst().isActive() ) && ! prev.isActive()));
	    	if ( conditions.getFirst().isActive() ) {
	    		if ( inactiveStart != null ) {
	    			rewriter.delete(inactiveStart, ctx.getStop());
	    			inactiveStart = null;
	    		}
	    	}
	    	else {
	    		if ( inactiveStart == null ) {
	    			inactiveStart = ctx.getStart();
	    		}
	    	}
		}
    	deleteLine = true;
	}

	@Override
	public void exitEndif(EndifContext ctx) {
		if ( conditions.isEmpty() ) {
			listener.preprocessingError(ctx.getStart().getLine(), 
					ctx.getStart().getCharPositionInLine(), "/ENDIF directive without /IF");
		}
		else {
			conditions.pop();
	    	if ( conditions.isEmpty() || conditions.getFirst().isActive() ) {
	    		if ( inactiveStart != null ) {
	    			rewriter.delete(inactiveStart, ctx.getStop());
	    			inactiveStart = null;
	    		}
	    	}
		}
    	deleteLine = true;
	}

	@Override
	public void exitDefine(DefineContext ctx) {
    	if ( inactiveStart == null ) {
    		if ( defines.contains(ctx.NAME().getText())) {
    			log.warn("Nome " + ctx.NAME().getText() + " gia' definito");
    		}
    		else {
    			defines.add(ctx.NAME().getText());
    		}
    	}
    	deleteLine = true;
	}

	@Override
	public void exitUndefine(UndefineContext ctx) {
    	if ( inactiveStart == null ) {
    		if ( ! defines.contains(ctx.NAME().getText())) {
    			log.warn("Nome " + ctx.NAME().getText() + " non definito");
    		}
    		else {
    			defines.remove(ctx.NAME().getText());
    		}
    	}
    	deleteLine = true;
	}

	@Override
	public void exitEofDir(EofDirContext ctx) {
    	if ( inactiveStart == null ) {
    		if ( eofDir == null ) {
    			eofDir = ctx.getStart();
    		}
    	}
	}

	@Override
	public void exitEof(EofContext ctx) {
		if ( eofDir != null ) {
			rewriter.delete(eofDir, ctx.getStop());
		}
	}

	@Override
	public void exitCondition(ConditionContext ctx) {
    	if ( inactiveStart == null ) {
        	if ( ctx.NAME().getText().indexOf('*') != -1 ) {
        		String repl = ctx.NAME().getText().replace("*", "");
        		rewriter.replace(ctx.NAME().getSymbol(), repl);
        	}
    	}
	}

	@Override
    public void exitLine(LineContext ctx) {
		if ( deleteLine ) {
			rewriter.delete(ctx.getStart(), ctx.getStop());
			deleteLine = false;
		}
		else {
	        if ( copyText != null ) {
	            rewriter.replace(ctx.start, ctx.stop, copyText);
	            copyText = null;
	        }
	        else {
	        	int width = ! hasLineNumber ? 80 : 92;
	        	int len = ctx.getStop().getCharPositionInLine() + ctx.getStop().getText().length();
	        	if ( len < width ) {
	        		pad = StringUtils.rightPad("", width - len);
	        	}
	        	else {
	        		pad = "";
	        	}
	        }
		}
        hasLineNumber = false;
    }
        
    @Override
	public void enterEol(EolContext ctx) {
		rewriter.insertBefore(ctx.getStop(), pad != null ? pad : StringUtils.rightPad("", 80));
		pad = null;
	}
	
	public String getText() {
        return rewriter.getText();
    }
}
