package it.twenfir.rpglepp.api;

import org.antlr.v4.runtime.ANTLRErrorListener;

public interface RpgleppErrorListener extends ANTLRErrorListener {
	public void preprocessingError(int line, int charPositionInLine, String msg);
}
