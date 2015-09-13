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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import de.haumacher.webgrammar.model.Alternative;
import de.haumacher.webgrammar.model.Concat;
import de.haumacher.webgrammar.model.Constant;
import de.haumacher.webgrammar.model.ContextFreeRule;
import de.haumacher.webgrammar.model.Epsilon;
import de.haumacher.webgrammar.model.Expression;
import de.haumacher.webgrammar.model.F;
import de.haumacher.webgrammar.model.Iteration;
import de.haumacher.webgrammar.model.NaryExpression;
import de.haumacher.webgrammar.model.NonTerminal;
import de.haumacher.webgrammar.model.Optional;
import de.haumacher.webgrammar.model.Regexp;
import de.haumacher.webgrammar.model.RegexpAlphabet;
import de.haumacher.webgrammar.model.RegexpAlternative;
import de.haumacher.webgrammar.model.RegexpIdentifier;
import de.haumacher.webgrammar.model.RegexpOneOrMore;
import de.haumacher.webgrammar.model.RegexpOptional;
import de.haumacher.webgrammar.model.RegexpSequence;
import de.haumacher.webgrammar.model.RegexpString;
import de.haumacher.webgrammar.model.RegexpZeroOrMore;
import de.haumacher.webgrammar.model.Rule;
import de.haumacher.webgrammar.model.TokenRule;
import de.haumacher.webgrammar.model.visit.ExpressionVisitor;
import de.haumacher.webgrammar.model.visit.RegexpVisitor;
import de.haumacher.webgrammar.model.visit.VisitHelper;

/**
 * A -> ( "x" | "y" | Z )
 * Z -> ( "z1" | z2 )
 * =>
 * A -> ( "x" | "y" | "z1" | "z2" )
 * Z -> ( "z1" | "z2" )
 * 
 * @author <a href="mailto:http://haumacher.de">Bernhard Haumacher</a>
 * @version
 */
public class ConsolidateEnumProductions extends ExpressionTransformer<Void> {

	public final class LiteralExpander implements ExpressionVisitor<Void, List<List<Expression>>> {
		@Override
		public Void visit(Concat expr, List<List<Expression>> arg) {
			for (Expression part : expr.getExpressions()) {
				VisitHelper.visitExpression(this, part, arg);
			}
			return null;
		}

		@Override
		public Void visit(Alternative expr, List<List<Expression>> prefixes) {
			processAlternatives(expr.getExpressions(), prefixes);
			return null;
		}

		@Override
		public Void visit(Optional expr, List<List<Expression>> prefixes) {
			List<Expression> expressions = new ArrayList<>();
			expressions.add(F.emptyExpression());
			expressions.add(expr.getExpression());
			processAlternatives(expressions, prefixes);
			return null;
		}

		private void processAlternatives(List<Expression> alternatives,
				List<List<Expression>> arg) {
			List<List<Expression>> result = new ArrayList<>();

			for (Expression choice : alternatives) {
				List<List<Expression>> suffixes = copyListList(arg);
				VisitHelper.visitExpression(this, choice, suffixes);
				result.addAll(suffixes);
			}

			arg.clear();
			arg.addAll(result);
		}

		private List<List<Expression>> copyListList(List<List<Expression>> prefixes) {
			List<List<Expression>> result = new ArrayList<>(prefixes.size());
			for (List<Expression> literal : prefixes) {
				result.add(copy(literal));
			}
			return result;
		}

		@Override
		public Void visit(Constant expr, List<List<Expression>> arg) {
			add(arg, expr);
			return null;
		}

		private void add(List<List<Expression>> literals, Expression expr) {
			for (List<Expression> literal : literals) {
				literal.add(copy(expr));
			}
		}

		@Override
		public Void visit(Epsilon expr, List<List<Expression>> arg) {
			return null;
		}

		@Override
		public Void visit(NonTerminal expr, List<List<Expression>> arg) {
			Rule rule = getRule(expr.getName());
			if (rule instanceof ContextFreeRule) {
				VisitHelper.visitExpression(this, ((ContextFreeRule) rule).getExpression(),
						arg);
			} else {
				add(arg, expr);
			}
			return null;
		}

		@Override
		public Void visit(Iteration expr, List<List<Expression>> arg) {
			throw new UnsupportedOperationException();
		}
	}

	enum ExpressionKind {
		LITERAL, LITERALS, OTHER;

		public ExpressionKind max(ExpressionKind other) {
			return values()[Math.max(ordinal(), other.ordinal())];
		}

		public ExpressionKind bothLiteral(ExpressionKind other) {
			return this == LITERAL ? (other == LITERAL ? LITERAL : OTHER) : OTHER;
		}
	}
	
	public class GetKind implements ExpressionVisitor<ExpressionKind, Set<String>>,
			RegexpVisitor<ExpressionKind, Set<String>> {

		@Override
		public ExpressionKind visit(Epsilon expr, Set<String> arg) {
			return ExpressionKind.LITERAL;
		}

		@Override
		public ExpressionKind visit(Constant expr, Set<String> arg) {
			return ExpressionKind.LITERAL;
		}

		@Override
		public ExpressionKind visit(NonTerminal expr, Set<String> arg) {
			// Prevent recursion.
			if (arg.contains(expr.getName())) {
				return ExpressionKind.OTHER;
			}
			arg.add(expr.getName());
			try {
				Rule rule = getRule(expr.getName());
				if (rule instanceof ContextFreeRule) {
					return visit(((ContextFreeRule) rule).getExpression(), arg);
				} else {
					return visit(((TokenRule) rule).getExpression(), arg);
				}
			} finally {
				arg.remove(expr.getName());
			}
		}

		@Override
		public ExpressionKind visit(Concat expr, Set<String> arg) {
			return allConstant(expr, arg);
		}

		@Override
		public ExpressionKind visit(Alternative expr, Set<String> arg) {
			// Not constant but can be inlined into an enum production producing
			// literals.
			return ExpressionKind.LITERALS.max(allConstant(expr, arg));
		}

		private ExpressionKind allConstant(NaryExpression expr, Set<String> arg) {
			ExpressionKind result = ExpressionKind.LITERAL;
			for (Expression part : expr.getExpressions()) {
				ExpressionKind partKind = visit(part, arg);
				result = result.max(partKind);
				if (result == ExpressionKind.OTHER) {
					break;
				}
			}
			return result;
		}

		@Override
		public ExpressionKind visit(Optional expr, Set<String> arg) {
			return ExpressionKind.LITERALS.max(visit(expr.getExpression(), arg));
		}

		@Override
		public ExpressionKind visit(Iteration expr, Set<String> arg) {
			return ExpressionKind.OTHER;
		}

		@Override
		public ExpressionKind visit(RegexpAlphabet expr, Set<String> arg) {
			return ExpressionKind.OTHER;
		}

		@Override
		public ExpressionKind visit(RegexpAlternative expr, Set<String> arg) {
			return ExpressionKind.OTHER;
		}

		@Override
		public ExpressionKind visit(RegexpSequence expr, Set<String> arg) {
			ExpressionKind result = ExpressionKind.LITERAL;
			for (Regexp part : expr.getExpressions()) {
				result = result.bothLiteral(visit(part, arg));
				if (result == ExpressionKind.OTHER) {
					break;
				}
			}
			return result;
		}

		@Override
		public ExpressionKind visit(RegexpIdentifier expr, Set<String> arg) {
			return visit(((TokenRule) getRule(expr.getName())).getExpression(), arg);
		}

		@Override
		public ExpressionKind visit(RegexpString expr, Set<String> arg) {
			return ExpressionKind.LITERAL;
		}

		@Override
		public ExpressionKind visit(RegexpOptional expr, Set<String> arg) {
			return ExpressionKind.OTHER;
		}

		@Override
		public ExpressionKind visit(RegexpOneOrMore expr, Set<String> arg) {
			return ExpressionKind.OTHER;
		}

		@Override
		public ExpressionKind visit(RegexpZeroOrMore expr, Set<String> arg) {
			return ExpressionKind.OTHER;
		}

		private ExpressionKind visit(Expression part, Set<String> arg) {
			return VisitHelper.visitExpression(this, part, arg);
		}

		private ExpressionKind visit(Regexp part, Set<String> arg) {
			return VisitHelper.visitRegexp(this, part, arg);
		}
	}

	ExpressionVisitor<ExpressionKind, Set<String>> _getKind = new GetKind();

	@Override
	public Expression visit(Alternative expr, Void arg) {
		Expression result = super.visit(expr, arg);

		int literals = 0;
		boolean isEnum = true;
		for (Expression choice : expr.getExpressions()) {
			switch (kind(choice)) {
			case LITERAL: {
				literals++;
				break;
			}
			case LITERALS: {
				break;
			}
			case OTHER: {
				isEnum = false;
				break;
			}
			}
		}
		
		if (isEnum) {
			if (literals < expr.getExpressions().size()) {
				// Inlining.
				for (ListIterator<Expression> it = expr.getExpressions().listIterator(); it
						.hasNext();) {
					Expression choice = it.next();
					if (kind(choice) == ExpressionKind.LITERALS) {
						it.remove();

						for (Expression replacement : expandLiterals(choice)) {
							it.add(replacement);
						}
					}
				}
			}
		} else if (literals > 1) {
			// TODO
		} else if (literals == 1) {
			// TODO
		}
		
		return result;
	}

	private List<Expression> expandLiterals(Expression choice) {
		ExpressionVisitor<Void, List<List<Expression>>> expander = new LiteralExpander();

		List<List<Expression>> literals = new ArrayList<>();
		literals.add(new ArrayList<Expression>());
		VisitHelper.visitExpression(expander, choice, literals);

		List<Expression> result = new ArrayList<>(literals.size());
		for (List<Expression> literal : literals) {
			result.add(TransformUtil.toExpression(literal));
		}
		return result;
	}

	private ExpressionKind kind(Expression choice) {
		return VisitHelper.visitExpression(_getKind, choice, new HashSet<String>());
	}
	
	
}
