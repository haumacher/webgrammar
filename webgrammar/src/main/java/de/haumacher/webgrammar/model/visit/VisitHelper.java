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
package de.haumacher.webgrammar.model.visit;

import java.util.Collection;

import de.haumacher.webgrammar.model.Alternative;
import de.haumacher.webgrammar.model.Annotation;
import de.haumacher.webgrammar.model.Concat;
import de.haumacher.webgrammar.model.Constant;
import de.haumacher.webgrammar.model.ContextFreeRule;
import de.haumacher.webgrammar.model.Epsilon;
import de.haumacher.webgrammar.model.Expression;
import de.haumacher.webgrammar.model.Grammar;
import de.haumacher.webgrammar.model.GrammarNode;
import de.haumacher.webgrammar.model.Iteration;
import de.haumacher.webgrammar.model.Name;
import de.haumacher.webgrammar.model.NonTerminal;
import de.haumacher.webgrammar.model.Optional;
import de.haumacher.webgrammar.model.QName;
import de.haumacher.webgrammar.model.Regexp;
import de.haumacher.webgrammar.model.RegexpAlphabet;
import de.haumacher.webgrammar.model.RegexpAlternative;
import de.haumacher.webgrammar.model.RegexpIdentifier;
import de.haumacher.webgrammar.model.RegexpOneOrMore;
import de.haumacher.webgrammar.model.RegexpOptional;
import de.haumacher.webgrammar.model.RegexpSequence;
import de.haumacher.webgrammar.model.RegexpString;
import de.haumacher.webgrammar.model.RegexpZeroOrMore;
import de.haumacher.webgrammar.model.TokenRule;


public class VisitHelper {

	public static <R,A> R visit(GrammarVisitor<R, A> v, GrammarNode expr, A arg) {
		if (expr instanceof Expression) {
			return visitExpression(v, (Expression) expr, arg);
		}
		if (expr instanceof Regexp) {
			return visitRegexp(v, (Regexp) expr, arg);
		}
		if (expr instanceof Grammar) {
			return v.visit((Grammar) expr, arg);
		}
		if (expr instanceof ContextFreeRule) {
			return v.visit((ContextFreeRule) expr, arg);
		}
		if (expr instanceof TokenRule) {
			return v.visit((TokenRule) expr, arg);
		}
		if (expr instanceof Annotation) {
			return v.visit((Annotation) expr, arg);
		}
		if (expr instanceof Name) {
			return v.visit((Name) expr, arg);
		}
		if (expr instanceof QName) {
			return v.visit((QName) expr, arg);
		}
		throw cannotHandle(expr);
	}
	
	public static <R,A> R visitExpression(ExpressionVisitor<R, A> v, Expression expr, A arg) {
		if (expr instanceof Concat) {
			return v.visit((Concat) expr, arg);
		}
		if (expr instanceof Alternative) {
			return v.visit((Alternative) expr, arg);
		}
		if (expr instanceof Constant) {
			return v.visit((Constant) expr, arg);
		}
		if (expr instanceof Epsilon) {
			return v.visit((Epsilon) expr, arg);
		}
		if (expr instanceof NonTerminal) {
			return v.visit((NonTerminal) expr, arg);
		}
		if (expr instanceof Optional) {
			return v.visit((Optional) expr, arg);
		}
		if (expr instanceof Iteration) {
			return v.visit((Iteration) expr, arg);
		}
		throw cannotHandle(expr);
	}

	public static <R,A> R visitRegexp(RegexpVisitor<R,A> v, Regexp expr, A arg) {
		if (expr instanceof RegexpAlphabet) {
			return v.visit((RegexpAlphabet) expr, arg);
		}
		if (expr instanceof RegexpAlternative) {
			return v.visit((RegexpAlternative) expr, arg);
		}
		if (expr instanceof RegexpSequence) {
			return v.visit((RegexpSequence) expr, arg);
		}
		if (expr instanceof RegexpIdentifier) {
			return v.visit((RegexpIdentifier) expr, arg);
		}
		if (expr instanceof RegexpString) {
			return v.visit((RegexpString) expr, arg);
		}
		if (expr instanceof RegexpOptional) {
			return v.visit((RegexpOptional) expr, arg);
		}
		if (expr instanceof RegexpOneOrMore) {
			return v.visit((RegexpOneOrMore) expr, arg);
		}
		if (expr instanceof RegexpZeroOrMore) {
			return v.visit((RegexpZeroOrMore) expr, arg);
		}
		throw cannotHandle(expr);
	}

	private static UnsupportedOperationException cannotHandle(GrammarNode expr) {
		return new UnsupportedOperationException("Cannot handle: " + expr);
	}

	public static <R,A> void visitAll(ExpressionVisitor<R, A> v, Collection<Expression> expressions, A arg) {
		for (Expression expr : expressions) {
			visitExpression(v, expr, arg);
		}
	}

}
