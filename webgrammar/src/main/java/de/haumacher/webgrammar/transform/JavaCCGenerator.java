/*
 * Copyright (c) 2015, Bernhard Haumacher. 
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package de.haumacher.webgrammar.transform;

import java.io.OutputStreamWriter;

import de.haumacher.webgrammar.model.Annotation;
import de.haumacher.webgrammar.model.ContextFreeRule;
import de.haumacher.webgrammar.model.Epsilon;
import de.haumacher.webgrammar.model.Expression;
import de.haumacher.webgrammar.model.F;
import de.haumacher.webgrammar.model.Grammar;
import de.haumacher.webgrammar.model.Iteration;
import de.haumacher.webgrammar.model.NonTerminal;
import de.haumacher.webgrammar.model.RegexpIdentifier;
import de.haumacher.webgrammar.model.Rule;
import de.haumacher.webgrammar.model.TokenRule;
import de.haumacher.webgrammar.model.visit.GrammarVisitor;

public class JavaCCGenerator extends GrammarWriter implements GrammarVisitor<Void, Void> {

	public JavaCCGenerator(OutputStreamWriter out) {
		super(out);
	}
	
	@Override
	public Void visit(Grammar expr, Void arg) {
		header();
		
		line("TOKEN :");
		line("{");
		indent();
		boolean first = true;
		for (Rule rule : expr.getRules()) {
			if (rule instanceof TokenRule) {
				if (first) {
					first = false; 
				} else {
					unindent();
					write("|");
					writeSingleIndent();
					indent();
				}
				
				visit((TokenRule) rule, arg);
			}
		}
		unindent();
		line("}");
		nl();
		
		for (Rule rule : expr.getRules()) {
			if (rule instanceof ContextFreeRule) {
				visit((ContextFreeRule) rule, arg);
			}
		}
		flush();
		return null;
	}
	
	@Override
	public Void visit(ContextFreeRule expr, Void arg) {
		write("void ");
		write(ntName(expr.getName()));
		write("() : ");
		nl();
		line("{");
		line("}");
		line("{");
		indent();
		descend(expr.getExpression());
		unindent();
		nl();
		line("}");
		nl();
		return null;
	}
	
	@Override
	public Void visit(TokenRule expr, Void arg) {
		write("< ");
		write(terminalName(expr.getName()));
		write(": ");
		nl();
		indent();
		descend(expr.getExpression());
		nl();
		unindent();
		write(">");
		nl();
		nl();
		return null;
	}
	
	private static String terminalName(String name) {
		return name.replaceAll("(?<=[a-z])(?=[A-Z])", "_").toUpperCase();
	}

	private void header() {
		line("options");
		line("{");
		indent();
		line("JDK_VERSION = \"1.7\";");
		line("STATIC = false;");
		line("UNICODE_INPUT = true;");
		line("JAVA_UNICODE_ESCAPE = false;");
		unindent();
		line("}");
		nl();
		
		line("PARSER_BEGIN(Parser)");
		line("package de.haumacher.webgrammar.webidl.parser;");
		nl();
		line("@SuppressWarnings({ \"javadoc\", \"unused\", \"synthetic-access\" })");
		line("public class Parser {");
		indent();
		unindent();
		line("}");
		nl();
		
		line("PARSER_END(Parser)");
		nl();
	}

	private static String ntName(String name) {
		if (Character.isLowerCase(name.charAt(0))) {
			return quote(name);
		} else {
			return "nt" + quote(name);
		}
	}

	private static String quote(String name) {
		return name.replace('$', '_');
	}

	@Override
	public Void visit(Epsilon expr, Void arg) {
		throw new UnsupportedOperationException("Epsilon productions not supported by JavaCC.");
	}

	@Override
	public Void visit(RegexpIdentifier expr, Void arg) {
		write("< ");
		write(terminalName(expr.getName()));
		write(" >");
		return null;
	}

	@Override
	public Void visit(NonTerminal expr, Void arg) {
		if (getRule(expr.getName()) instanceof TokenRule) {
			write("< ");
			write(terminalName(expr.getName()));
			write(" >");
		} else {
			write(ntName(expr.getName()));
			write("()");
		}
		return null;
	}
	
	public static String ruleName(Rule rule) {
		if (rule instanceof TokenRule) {
			return terminalName(rule.getName());
		} else {
			return ntName(rule.getName());
		}
	}

	@Override
	public Void visit(Iteration expr, Void arg) {
		int min = expr.getMin();
		Expression separator = F.getSeparator(expr);
		while (min > 1) {
			visitUnaryExpression(expr);
			min--;
			
			if (min > 1) {
				if (separator != null) {
					descend(separator);
				}
			}
		}
		write("(");
		if (separator != null) {
			descend(separator);
		}
		visitUnaryExpression(expr);
		write(")");
		write(min == 0 ? "*" : "+");
		return null;
	}
	
	@Override
	public Void visit(Annotation expr, Void arg) {
		return null;
	}
	
}
