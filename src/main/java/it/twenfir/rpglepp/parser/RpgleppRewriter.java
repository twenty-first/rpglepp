package it.twenfir.rpglepp.parser;

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

import it.twenfir.rpglepp.RpgleppParser.CommentContext;
import it.twenfir.rpglepp.RpgleppParser.ConditionContext;
import it.twenfir.rpglepp.RpgleppParser.CopyContext;
import it.twenfir.rpglepp.RpgleppParser.DefineContext;
import it.twenfir.rpglepp.RpgleppParser.Else_Context;
import it.twenfir.rpglepp.RpgleppParser.ElseifContext;
import it.twenfir.rpglepp.RpgleppParser.EndifContext;
import it.twenfir.rpglepp.RpgleppParser.EofContext;
import it.twenfir.rpglepp.RpgleppParser.EofDirContext;
import it.twenfir.rpglepp.RpgleppParser.EolContext;
import it.twenfir.rpglepp.RpgleppParser.If_Context;
import it.twenfir.rpglepp.RpgleppParser.InstructionContext;
import it.twenfir.rpglepp.RpgleppParser.LineContext;
import it.twenfir.rpglepp.RpgleppParser.PrefixContext;
import it.twenfir.rpglepp.RpgleppParser.UndefineContext;
import it.twenfir.rpglepp.api.RpgleppErrorListener;
import it.twenfir.rpglepp.api.SourceFile;
import it.twenfir.rpglepp.RpgleppParserBaseListener;

public class RpgleppRewriter extends RpgleppParserBaseListener {

	enum State { ACTIVE, CONDITION, EOF }
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
    private TokenStreamRewriter rewriter;
    private RpgleppPreprocessor preprocessor;
    private RpgleppErrorListener listener;

    private Set<String> defines;
    private Deque<Condition> conditions = new ArrayDeque<Condition>();
    private Token inactiveStart;
    private State state = State.ACTIVE;
    private String copyText = null;
    private boolean hasLineNumber = false;
    private String pad = null;
    private boolean deleteCurrentLine = false;
    private Token currentLineStart;
    private Token previousLineEnd;

    public RpgleppRewriter(TokenStream tokenStream, Set<String> defines, 
    		RpgleppPreprocessor preprocessor, RpgleppErrorListener listener) {
        rewriter = new TokenStreamRewriter(tokenStream);
        this.preprocessor = preprocessor;
        this.listener = listener;
        this.defines = defines != null ? defines : new HashSet<>();
    }

    @Override
	public void exitPrefix(PrefixContext ctx) {
    	if ( state == State.ACTIVE ) {
        	if ( ctx.LINE_NUMBER() != null ) {
        		rewriter.delete(ctx.LINE_NUMBER().getSymbol());
        		hasLineNumber = true;
        	}
        	if ( ctx.BAD_PREFIX() != null ) {
//    			listener.preprocessingError(ctx.getStart().getLine(), 
//    					ctx.getStart().getCharPositionInLine(), 
//    					"Invalid character in prefix");			
    			String repl = StringUtils.replaceChars(ctx.BAD_PREFIX().getText(), 
//    					"\u00A3\u00A7\u00C2", "LSA");
    					"\u0082\u00A3\u00A7\u00C2", "    ");
    			rewriter.replace(ctx.BAD_PREFIX().getSymbol(), repl);
        	}
        	if ( ctx.END_SOURCE_DIR() != null && ctx.END_SOURCE_DIR().getText().length() < 6 ) {
        		String repl = StringUtils.rightPad(ctx.END_SOURCE_DIR().getText(), 6);
        		rewriter.replace(ctx.END_SOURCE_DIR().getSymbol(), repl);
        	}
    	}
	}

	@Override
	public void exitInstruction(InstructionContext ctx) {
    	if ( state == State.ACTIVE ) {
    		if ( ctx.BAD_INSTRUCTION() != null ) {
    			listener.preprocessingError(ctx.getStart().getLine(), 
    					ctx.getStart().getCharPositionInLine(), 
    					"Invalid character in instruction");			
//    			String repl = StringUtils.replaceChars(ctx.BAD_INSTRUCTION().getText(), 
//    					"\u00A3\u00A7\u00C2", "LSA");
//    			String repl = StringUtils.replaceChars(ctx.BAD_INSTRUCTION().getText(), 
//    					"\u00A3\u00A7\u00C2", "   ");
//    			rewriter.replace(ctx.BAD_INSTRUCTION().getSymbol(), repl);
    		}
    	}
	}

	@Override
	public void exitComment(CommentContext ctx) {
    	if ( state == State.ACTIVE ) {
    		if ( ctx.BAD_COMMENT() != null ) {
//    			listener.preprocessingError(ctx.getStart().getLine(), 
//    					ctx.getStart().getCharPositionInLine(), 
//    					"Invalid character in comment");
//    			String repl = StringUtils.replaceEach(ctx.BAD_COMMENT().getText(), 
//    					new String[] { "\u00A3", "\u00A7" }, 
//    					new String[] { "Pound", "Para" });
    			String repl = ctx.BAD_COMMENT().getText();    				
    			int i = repl.indexOf('*');
    			if ( i != -1 ) {
        			repl = " " + repl.substring(i);    				
    			}
    			repl = StringUtils.replaceChars(repl, "\u0082\u00A3\u00A7\u00C2", "    ");
    			rewriter.replace(ctx.BAD_COMMENT().getSymbol(), repl);
    		}
    	}
	}

	@Override
    public void exitCopy(CopyContext ctx) {
    	if ( state == State.ACTIVE ) {
            RpgleppPreprocessor pp = preprocessor.make();
            SourceFile copyBook = pp.getReader().read(ctx.member().getText());
            copyText = pp.preprocess(copyBook, defines);
    	}
    }

    @Override
	public void exitIf_(If_Context ctx) {
    	if ( state != State.EOF ) {
        	conditions.push(new Condition(ctx, defines, 
        			conditions.size() == 0 || conditions.getFirst().isActive()));
        	if ( ! conditions.getFirst().isActive() ) {
        		if ( state == State.ACTIVE ) {
        			state = State.CONDITION;
        			inactiveStart = currentLineStart;
        		}
        	}
        	else {
            	deleteCurrentLine = true;    		
        	}
    	}
	}

	@Override
	public void exitElseif(ElseifContext ctx) {
    	if ( state != State.EOF ) {
    		if ( conditions.isEmpty() ) {
    			listener.preprocessingError(ctx.getStart().getLine(), 
    					ctx.getStart().getCharPositionInLine(), "/ELSEIF directive without /IF");
    		}
    		else {
    			conditions.pop();
    	    	conditions.push(new Condition(ctx, defines, 
    	    			conditions.size() == 0 || conditions.getFirst().isActive()));
    	    	if ( conditions.getFirst().isActive() ) {
    	    		if ( state == State.CONDITION ) {
    	    			rewriter.delete(inactiveStart, previousLineEnd);
    	    			inactiveStart = null;
    	    			state = State.ACTIVE;
    	    		}
    	        	deleteCurrentLine = true;
    	    	}
    	    	else {
    	    		if ( state == State.ACTIVE ) {
    	    			inactiveStart = currentLineStart;
    	    			state = State.CONDITION;
    	    		}
    	    	}
    		}
    	}
	}

	@Override
	public void exitElse_(Else_Context ctx) {
    	if ( state != State.EOF ) {
    		if ( conditions.isEmpty() ) {
    			listener.preprocessingError(ctx.getStart().getLine(), 
    					ctx.getStart().getCharPositionInLine(), "/ELSE directive without /IF");
    		}
    		else {
    			Condition prev = conditions.pop();
    			conditions.push(new Condition(prev.getName(), prev.getKind().flip(),
    					( conditions.size() == 0 || conditions.getFirst().isActive() ) && 
    							! prev.isActive()));
    	    	if ( conditions.getFirst().isActive() ) {
    	    		if ( state == State.CONDITION ) {
    	    			rewriter.delete(inactiveStart, previousLineEnd);
    	    			inactiveStart = null;
    	    			state = State.ACTIVE;
    	    		}
    	        	deleteCurrentLine = true;
    	    	}
    	    	else {
    	    		if ( state == State.ACTIVE ) {
    	    			inactiveStart = currentLineStart;
    	    			state = State.CONDITION;
    	    		}
    	    	}
    		}
    	}
	}

	@Override
	public void exitEndif(EndifContext ctx) {
    	if ( state != State.EOF ) {
    		if ( conditions.isEmpty() ) {
    			listener.preprocessingError(ctx.getStart().getLine(), 
    					ctx.getStart().getCharPositionInLine(), "/ENDIF directive without /IF");
    		}
    		else {
    			conditions.pop();
    	    	if ( conditions.isEmpty() || conditions.getFirst().isActive() ) {
    	    		if ( state == State.CONDITION ) {
    	    			rewriter.delete(inactiveStart, previousLineEnd);
    	    			inactiveStart = null;
    	    			state = State.ACTIVE;
    	    		}
    	    	}
            	deleteCurrentLine = true;
    		}
    	}
	}

	@Override
	public void exitDefine(DefineContext ctx) {
    	if ( state == State.ACTIVE ) {
    		if ( defines.contains(ctx.NAME().getText())) {
    			log.warn("Nome " + ctx.NAME().getText() + " gia' definito");
    		}
    		else {
    			defines.add(ctx.NAME().getText());
    		}
        	deleteCurrentLine = true;
    	}
	}

	@Override
	public void exitUndefine(UndefineContext ctx) {
    	if ( state == State.ACTIVE ) {
    		if ( ! defines.contains(ctx.NAME().getText())) {
    			log.warn("Nome " + ctx.NAME().getText() + " non definito");
    		}
    		else {
    			defines.remove(ctx.NAME().getText());
    		}
        	deleteCurrentLine = true;
    	}
	}

	@Override
	public void exitEofDir(EofDirContext ctx) {
    	if ( state != State.EOF ) {
    		if ( inactiveStart == null ) {
    			inactiveStart = currentLineStart;
    			state = State.EOF;
    		}
    	}
	}

	@Override
	public void exitEof(EofContext ctx) {
		if ( state == State.CONDITION ) {
			listener.preprocessingError(ctx.getStart().getLine(), 
					ctx.getStart().getCharPositionInLine(), 
					"/IF or /ELSEIF directive without /ENDIF");			
		}
		else if ( state == State.EOF ) {
			rewriter.delete(inactiveStart, ctx.getStop());
		}
	}

	@Override
	public void exitCondition(ConditionContext ctx) {
    	if ( state == State.ACTIVE ) {
        	if ( ctx.NAME().getText().indexOf('*') != -1 ) {
        		String repl = ctx.NAME().getText().replace("*", "");
        		rewriter.replace(ctx.NAME().getSymbol(), repl);
        	}
    	}
	}


	@Override
    public void enterLine(LineContext ctx) {
		currentLineStart = ctx.getStart();
	}

	@Override
    public void exitLine(LineContext ctx) {
		if ( deleteCurrentLine ) {
			rewriter.delete(ctx.getStart(), ctx.getStop());
			deleteCurrentLine = false;
		}
		else if ( state == State.ACTIVE ) {
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
    
	@Override
	public void exitEol(EolContext ctx) {
    	previousLineEnd = ctx.getStop();
	}
	
	public String getText() {
        return rewriter.getText();
    }
}
