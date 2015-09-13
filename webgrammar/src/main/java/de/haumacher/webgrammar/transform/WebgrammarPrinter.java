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
import java.util.List;

import de.haumacher.webgrammar.model.Annotatable;
import de.haumacher.webgrammar.model.Annotation;
import de.haumacher.webgrammar.model.Argument;
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

public class WebgrammarPrinter extends GrammarWriter {

	public WebgrammarPrinter(OutputStreamWriter out) {
		super(out);
	}

	@Override
	public Void visit(Grammar expr, Void arg) {
		writeAnnotations(expr);
		line("grammar {");
		indent();
		for (Rule rule : expr.getRules()) {
			descend(rule);
			nl();
		}
		unindent();
		line("}");
		return null;
	}

	@Override
	public Void visit(ContextFreeRule expr, Void arg) {
		writeAnnotations(expr);
		write(expr.getName());
		write(" -> ");
		descend(expr.getExpression());
		write(";");
		nl();
		return null;
	}

	@Override
	public Void visit(TokenRule expr, Void arg) {
		writeAnnotations(expr);
		write(expr.getName());
		write(" = ");
		descend(expr.getExpression());
		write(";");
		nl();
		return null;
	}

	private void writeAnnotations(Annotatable expr) {
		for (Annotation annotation : expr.getAnnotations()) {
			descend(annotation);
		}
		br();
	}
	
	@Override
	public Void visit(Annotation expr, Void arg) {
		br();
		write("@");
		write(expr.getName());
		List<Argument> arguments = expr.getArguments();
		if (!arguments.isEmpty()) {
			write("(");
			indent();
			boolean first = true;
			for (Argument argument : arguments) {
				if (first) {
					first = false;
				} else {
					write(", ");
				}
				descend(argument);
			}
			unindent();
			write(")");
		}
		return null;
	}
	
	@Override
	public Void visit(Epsilon expr, Void arg) {
		write("\\epsilon");
		return null;
	}

	@Override
	public Void visit(NonTerminal expr, Void arg) {
		write(expr.getName());
		return null;
	}

	@Override
	public Void visit(Iteration expr, Void arg) {
		write("(");
		descend(expr.getExpression());
		Expression separator = F.getSeparator(expr);
		if (separator != null) {
			write(", ");
			descend(separator);
		}
		write(")");
		switch (expr.getMin()) {
		case 0: {
			write("*");
			break;
		}
		case 1: {
			write("+");
			break;
		}
		default: {
			write("[");
			write(Integer.toString(expr.getMin()));
			write("...");
			write("]");
			break;
		}
		}
		return null;
	}

	@Override
	public Void visit(RegexpIdentifier expr, Void arg) {
		write(expr.getName());
		return null;
	}


}
