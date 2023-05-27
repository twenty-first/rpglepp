package it.twenfir.rpglepp.api;

import org.antlr.v4.runtime.ANTLRErrorListener;

public interface RpgleppErrorListener extends ANTLRErrorListener {
	void preprocessingError(int line, int charPositionInLine, String msg);
	void setFileName(String fileName);
	String getFileName();
}
