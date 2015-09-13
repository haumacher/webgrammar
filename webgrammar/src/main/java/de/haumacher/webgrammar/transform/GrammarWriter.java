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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.haumacher.webgrammar.model.Alternative;
import de.haumacher.webgrammar.model.CharRange;
import de.haumacher.webgrammar.model.Concat;
import de.haumacher.webgrammar.model.Constant;
import de.haumacher.webgrammar.model.Expression;
import de.haumacher.webgrammar.model.Grammar;
import de.haumacher.webgrammar.model.GrammarNode;
import de.haumacher.webgrammar.model.Name;
import de.haumacher.webgrammar.model.NaryRegexp;
import de.haumacher.webgrammar.model.Optional;
import de.haumacher.webgrammar.model.QName;
import de.haumacher.webgrammar.model.Regexp;
import de.haumacher.webgrammar.model.RegexpAlphabet;
import de.haumacher.webgrammar.model.RegexpAlternative;
import de.haumacher.webgrammar.model.RegexpOneOrMore;
import de.haumacher.webgrammar.model.RegexpOptional;
import de.haumacher.webgrammar.model.RegexpSequence;
import de.haumacher.webgrammar.model.RegexpString;
import de.haumacher.webgrammar.model.RegexpZeroOrMore;
import de.haumacher.webgrammar.model.Rule;
import de.haumacher.webgrammar.model.UnaryExpression;
import de.haumacher.webgrammar.model.UnaryRegexp;
import de.haumacher.webgrammar.model.visit.GrammarVisitor;
import de.haumacher.webgrammar.model.visit.VisitHelper;

public abstract class GrammarWriter extends SourceWriter implements GrammarVisitor<Void, Void> {

	private Grammar _grammar;

	private Map<String, Rule> _rules;

	public GrammarWriter(OutputStreamWriter out) {
		super(out);
	}

	public void visit(Grammar expr) {
		_grammar = expr;
		visit(expr, null);
		flush();
		_grammar = null;
	}

	@Override
	public Void visit(RegexpAlphabet expr, Void arg) {
		if (expr.isNegative()) {
			write("~");
		}
		write("[");
		boolean first = true;
		for (CharRange range : expr.getRanges()) {
			if (first) {
				first = false;
			} else {
				write(", ");
			}
			transform(range);
		}
		write("]");
		return null;
	}

	private void transform(CharRange range) {
		write("\"");
		write(range.getFirst());
		write("\"");
		if (range.getLast() != null) {
			write("-");
			write("\"");
			write(range.getLast());
			write("\"");
		}
	}

	@Override
	public Void visit(RegexpAlternative expr, Void arg) {
		nl();
		write("(");
		writeSingleIndent();
		indent();
		boolean first = true;
		for (Regexp choice : expr.getExpressions()) {
			if (first) {
				first = false;
			} else {
				nl();
				write("| ");
			}
			descend(choice);
		}
		nl();
		unindent();
		write(")");
		return null;
	}

	@Override
	public Void visit(RegexpSequence expr, Void arg) {
		descendSequence(expr);
		return null;
	}

	private void descendSequence(NaryRegexp expr) {
		boolean first = true;
		for (Regexp step : expr.getExpressions()) {
			if (first) {
				first = false;
			} else {
				write(" ");
			}
			descend(step);
		}
	}

	@Override
	public Void visit(RegexpString expr, Void arg) {
		write("\"");
		write(expr.getContent());
		write("\"");
		return null;
	}

	@Override
	public Void visit(RegexpOptional expr, Void arg) {
		write("(");
		visitUnaryRegexp(expr);
		write(")?");
		return null;
	}

	@Override
	public Void visit(RegexpOneOrMore expr, Void arg) {
		write("(");
		visitUnaryRegexp(expr);
		write(")+");
		return null;
	}

	@Override
	public Void visit(RegexpZeroOrMore expr, Void arg) {
		write("(");
		visitUnaryRegexp(expr);
		write(")*");
		return null;
	}

	@Override
	public Void visit(Concat expr, Void arg) {
		for (Expression sub : expr.getExpressions()) {
			descend(sub);
			write(" ");
		}
		return null;
	}

	@Override
	public Void visit(Alternative expr, Void arg) {
		List<Expression> expressions = expr.getExpressions();
		boolean first = true;
		indent();
		nl();
		write("( ");
		for (Expression choice : expressions) {
			if (first) {
				first = false;
			} else {
				write("| ");
			}
			descend(choice);
			nl();
		}
		line(")");
		unindent();
		return null;
	}
	
	@Override
	public Void visit(Constant expr, Void arg) {
		write("\"");
		write(expr.getContent());
		write("\"");
		return null;
	}

	@Override
	public Void visit(Optional expr, Void arg) {
		write("(");
		visitUnaryExpression(expr);
		write(")?");
		return null;
	}

	@Override
	public Void visit(QName expr, Void arg) {
		boolean first = true;
		for (Name name : expr.getNames()) {
			if (first) {
				first = false;
			} else {
				write(".");
			}
			descend(name);
		}
		return null;
	}
	
	@Override
	public Void visit(Name expr, Void arg) {
		write(expr.getValue());
		return null;
	}

	public Grammar getGrammar() {
		return _grammar;
	}
	
	public Rule getRule(String name) {
		if (_rules == null) {
			_rules = indexRules(getGrammar());
		}
		return _rules.get(name);
	}

	private Map<String, Rule> indexRules(Grammar grammar) {
		Map<String, Rule> rules = new HashMap<>();
		for (Rule rule : grammar.getRules()) {
			rules.put(rule.getName(), rule);
		}
		return rules;
	}
	
	protected final void visitUnaryExpression(UnaryExpression expr) {
		descend(expr.getExpression());
	}
	
	protected final void visitUnaryRegexp(UnaryRegexp expr) {
		descend(expr.getExpression());
	}
	
	protected final Void descend(GrammarNode expr) {
		return VisitHelper.visit(this, expr, null);
	}
	
}
