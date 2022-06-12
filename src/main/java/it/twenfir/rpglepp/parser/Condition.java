package it.twenfir.rpglepp.parser;

import java.util.Set;

import it.twenfir.rpglepp.RpgleppParser.ConditionContext;
import it.twenfir.rpglepp.RpgleppParser.ElseifContext;
import it.twenfir.rpglepp.RpgleppParser.If_Context;

class Condition {
	enum Kind {
		DEFINED, UNDEFINED;
		
		Kind flip() {
			switch(this) {
			case DEFINED:
				return UNDEFINED;
			case UNDEFINED:
				return DEFINED;
			default:
				return null;
			}
		}
	}
	
	private String name;
	private Kind kind;
	private boolean active;

	Condition(If_Context ctx, Set<String> defines, boolean active) {
		this(ctx.condition(), defines, active);
	}

	Condition(ElseifContext ctx, Set<String> defines, boolean active) {
		this(ctx.condition(), defines, active);
	}
	
	Condition(ConditionContext ctx, Set<String> defines, boolean active) {
		this(ctx.NAME().getText(), ctx.NOT() != null ? Kind.UNDEFINED : Kind.DEFINED, 
				active && ( ( ctx.NOT() == null ) == ( defines.contains(ctx.NAME().getText()) ) ) );
	}
	
	Condition(String name, Kind kind, boolean active) {
		this.name = name;
		this.kind = kind;
		this.active = active;
	}

	public String getName() {
		return name;
	}

	public Kind getKind() {
		return kind;
	}

	public boolean isActive() {
		return active;
	}

}
