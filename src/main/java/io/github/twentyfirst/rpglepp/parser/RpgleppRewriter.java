package io.github.twentyfirst.rpglepp.parser;

import java.util.Set;

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
import io.github.twentyfirst.rpglepp.api.SourceFile;

public class RpgleppRewriter extends RpgleppParserBaseListener {

	private Logger log = LoggerFactory.getLogger(getClass());
	
    private TokenStreamRewriter rewriter;
    private RpgleppPreprocessor preprocessor;
    private Set<String> defines;
    
    private String copyText = null;
    private boolean hasLineNumber = false;
    private String pad = null;
    private boolean deleteLine = false;

    public RpgleppRewriter(TokenStream tokenStream, Set<String> defines, 
    		RpgleppPreprocessor preprocessor) {
        rewriter = new TokenStreamRewriter(tokenStream);
        this.preprocessor = preprocessor;
        this.defines = defines;
    }

    @Override
	public void exitPrefix(PrefixContext ctx) {
    	if ( ctx.LINE_NUMBER() != null ) {
    		rewriter.delete(ctx.LINE_NUMBER().getSymbol());
    		hasLineNumber = true;
    	}
    	if ( ctx.END_SOURCE_DIR() != null && ctx.END_SOURCE_DIR().getText().length() < 6 ) {
    		String repl = StringUtils.rightPad(ctx.END_SOURCE_DIR().getText(), 6);
    		rewriter.replace(ctx.END_SOURCE_DIR().getSymbol(), repl);
    	}
	}

	@Override
	public void exitInstruction(InstructionContext ctx) {
		if ( ctx.BAD_INSTRUCTION() != null ) {
			String repl = StringUtils.replaceChars(ctx.BAD_INSTRUCTION().getText(), "\u00A3\u00A7\u00C2", "LSA");
			rewriter.replace(ctx.BAD_INSTRUCTION().getSymbol(), repl);
		}
	}

	@Override
	public void exitComment(CommentContext ctx) {
		if ( ctx.BAD_COMMENT() != null ) {
			String repl = StringUtils.replaceEach(ctx.BAD_COMMENT().getText(), 
					new String[] { "\u00A3", "\u00A7" }, 
					new String[] { "Pound", "Para" });
			rewriter.replace(ctx.BAD_COMMENT().getSymbol(), repl);
		}
	}

	@Override
    public void exitCopy(CopyContext ctx) {
        RpgleppPreprocessor pp = preprocessor.make();
        SourceFile copyBook = pp.getReader().read(ctx.member().getText());
        copyText = pp.preprocess(copyBook, defines);
    }

    @Override
	public void exitIf_(If_Context ctx) {
    	deleteLine = true;
	}

	@Override
	public void exitElseif(ElseifContext ctx) {
    	deleteLine = true;
	}

	@Override
	public void exitElse_(Else_Context ctx) {
    	deleteLine = true;
	}

	@Override
	public void exitEndif(EndifContext ctx) {
    	deleteLine = true;
	}

	@Override
	public void exitDefine(DefineContext ctx) {
		if ( defines.contains(ctx.NAME().getText())) {
			log.warn("Nome " + ctx.NAME().getText() + " gia' definito");
		}
		else {
			defines.add(ctx.NAME().getText());
		}
	}

	@Override
	public void exitUndefine(UndefineContext ctx) {
		if ( ! defines.contains(ctx.NAME().getText())) {
			log.warn("Nome " + ctx.NAME().getText() + " non definito");
		}
		else {
			defines.remove(ctx.NAME().getText());
		}
	}

	@Override
	public void exitEofDir(EofDirContext ctx) {
		// TODO Auto-generated method stub
		super.exitEofDir(ctx);
	}

	@Override
	public void exitEof(EofContext ctx) {
		// TODO Auto-generated method stub
		super.exitEof(ctx);
	}

	@Override
	public void exitCondition(ConditionContext ctx) {
    	if ( ctx.NAME().getText().indexOf('*') != -1 ) {
    		String repl = ctx.NAME().getText().replace("*", "");
    		rewriter.replace(ctx.NAME().getSymbol(), repl);
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
