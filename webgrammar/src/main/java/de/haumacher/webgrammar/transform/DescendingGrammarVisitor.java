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

import java.util.List;

import de.haumacher.webgrammar.model.Alternative;
import de.haumacher.webgrammar.model.Annotation;
import de.haumacher.webgrammar.model.Concat;
import de.haumacher.webgrammar.model.Constant;
import de.haumacher.webgrammar.model.ContextFreeRule;
import de.haumacher.webgrammar.model.Epsilon;
import de.haumacher.webgrammar.model.Grammar;
import de.haumacher.webgrammar.model.GrammarNode;
import de.haumacher.webgrammar.model.Iteration;
import de.haumacher.webgrammar.model.Name;
import de.haumacher.webgrammar.model.NonTerminal;
import de.haumacher.webgrammar.model.Optional;
import de.haumacher.webgrammar.model.QName;
import de.haumacher.webgrammar.model.RegexpAlphabet;
import de.haumacher.webgrammar.model.RegexpAlternative;
import de.haumacher.webgrammar.model.RegexpIdentifier;
import de.haumacher.webgrammar.model.RegexpOneOrMore;
import de.haumacher.webgrammar.model.RegexpOptional;
import de.haumacher.webgrammar.model.RegexpSequence;
import de.haumacher.webgrammar.model.RegexpString;
import de.haumacher.webgrammar.model.RegexpZeroOrMore;
import de.haumacher.webgrammar.model.TokenRule;
import de.haumacher.webgrammar.model.visit.GrammarVisitor;
import de.haumacher.webgrammar.model.visit.VisitHelper;

public class DescendingGrammarVisitor<R,A> implements GrammarVisitor<R, A> {

	@Override
	public R visit(Grammar expr, A arg) {
		return descend(expr.getRules(), arg);
	}

	@Override
	public R visit(ContextFreeRule expr, A arg) {
		return descend(expr.getExpression(), arg);
	}

	@Override
	public R visit(TokenRule expr, A arg) {
		return descend(expr.getExpression(), arg);
	}

	@Override
	public R visit(Concat expr, A arg) {
		return descend(expr.getExpressions(), arg);
	}

	@Override
	public R visit(Alternative expr, A arg) {
		return descend(expr.getExpressions(), arg);
	}

	@Override
	public R visit(Constant expr, A arg) {
		return null;
	}

	@Override
	public R visit(Epsilon expr, A arg) {
		return null;
	}

	@Override
	public R visit(NonTerminal expr, A arg) {
		return null;
	}

	@Override
	public R visit(Optional expr, A arg) {
		return descend(expr.getExpression(), arg);
	}

	@Override
	public R visit(Iteration expr, A arg) {
		return descend(expr.getExpression(), arg);
	}

	@Override
	public R visit(RegexpAlphabet expr, A arg) {
		return null;
	}

	@Override
	public R visit(RegexpAlternative expr, A arg) {
		return descend(expr.getExpressions(), arg);
	}

	@Override
	public R visit(RegexpSequence expr, A arg) {
		return descend(expr.getExpressions(), arg);
	}

	@Override
	public R visit(RegexpIdentifier expr, A arg) {
		return null;
	}

	@Override
	public R visit(RegexpString expr, A arg) {
		return null;
	}

	@Override
	public R visit(RegexpOptional expr, A arg) {
		return descend(expr.getExpression(), arg);
	}

	@Override
	public R visit(RegexpOneOrMore expr, A arg) {
		return descend(expr.getExpression(), arg);
	}

	@Override
	public R visit(RegexpZeroOrMore expr, A arg) {
		return descend(expr.getExpression(), arg);
	}
	
	@Override
	public R visit(Annotation expr, A arg) {
		return descend(expr.getArguments(), arg);
	}
	
	@Override
	public R visit(Name expr, A arg) {
		return null;
	}
	
	@Override
	public R visit(QName expr, A arg) {
		return descend(expr.getNames(), arg);
	}

	private R descend(List<? extends GrammarNode> nodes, A arg) {
		R result = initial();
		for (GrammarNode node : nodes) {
			result = combine(result, descend(node, arg));
		}
		return result;
	}

	private R descend(GrammarNode node, A arg) {
		return VisitHelper.visit(this, node, arg);
	}

	protected R combine(R r1, R r2) {
		return r2;
	}

	protected R initial() {
		return null;
	}

	
}
