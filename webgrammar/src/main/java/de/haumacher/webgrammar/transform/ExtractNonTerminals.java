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

import static de.haumacher.webgrammar.transform.TransformUtil.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import de.haumacher.webgrammar.model.Alternative;
import de.haumacher.webgrammar.model.Concat;
import de.haumacher.webgrammar.model.ContextFreeRule;
import de.haumacher.webgrammar.model.Expression;
import de.haumacher.webgrammar.model.F;
import de.haumacher.webgrammar.model.Grammar;
import de.haumacher.webgrammar.model.NonTerminal;
import de.haumacher.webgrammar.model.Rule;
import de.haumacher.webgrammar.model.UnaryExpression;

/**
 * A -> B C D E
 * X -> C D
 * =>
 * A -> B X E
 * X -> C D
 * 
 * A -> B C D
 * X -> C 
 *      where C is a single non-terminal.
 * =/=>
 * A -> B X D
 * X -> C
 * 
 * @author <a href="http://haumacher.de">Bernhard Haumacher</a>
 */
public class ExtractNonTerminals extends ExpressionTransformer<ContextFreeRule> {
	
	static class Expansions {
		private final int _size;
		private final Map<List<Expression>, ContextFreeRule> _ruleByExpansion = new HashMap<>();
		
		public Expansions(int size) {
			_size = size;
		}

		public int getSize() {
			return _size;
		}
		
		public void put(List<Expression> expansion, ContextFreeRule rule) {
			_ruleByExpansion.put(expansion, rule);
		}

		public ContextFreeRule getRule(List<Expression> expansion) {
			return _ruleByExpansion.get(expansion);
		}
	}

	/**
	 * {@link Expansions} sorted by descending size.
	 */
	private ArrayList<Expansions> _expansions;

	@Override
	public void visit(Grammar grammar, ContextFreeRule arg) {
		Map<Integer, Expansions> expansionBySize = new HashMap<>();
		for (Rule rule : grammar.getRules()) {
			if (rule instanceof ContextFreeRule) {
				ContextFreeRule cfRule = (ContextFreeRule) rule;
				List<Expression> expansion = TransformUtil.asList(cfRule.getExpression());
				int size = expansion.size();
				if (size == 1) {
					Expression single = expansion.get(0);
					if (single instanceof NonTerminal) {
						if (!(getRule(((NonTerminal) single).getName()) instanceof ContextFreeRule)) {
							// Do not replace single tokens by the rule expanding to that token.
							continue;
						}
					}
				}
				Expansions expansions = expansionBySize.get(size);
				if (expansions == null) {
					expansions = new Expansions(size);
					expansionBySize.put(size, expansions);
				}
				expansions.put(expansion, cfRule);
			}
		}
		
		ArrayList<Expansions> expansions = new ArrayList<>(expansionBySize.values());
		Collections.sort(expansions, new Comparator<Expansions>() {
			@Override
			public int compare(Expansions o1, Expansions o2) {
				if (o1.getSize() < o2.getSize()) {
					return 1;
				}
				if (o1.getSize() > o2.getSize()) {
					return -1;
				}
				return 0;
			}
		});
		_expansions = expansions;
		
		super.visit(grammar, arg);
	}
	
	@Override
	public void visit(ContextFreeRule rule, ContextFreeRule arg) {
		super.visit(rule, rule);
	}
	
	@Override
	protected <E extends UnaryExpression> E descendUnary(E expr, ContextFreeRule arg) {
		Expression inner = expr.getExpression();
		Expression innerResult = transform(inner, arg);
		expr.setExpression(innerResult);
		
		return super.descendUnary(expr, arg);
	}

	private Expression transform(Expression expr, ContextFreeRule arg) {
		List<Expression> expressions = asModifiableList(expr);
		transformListInline(expressions, arg);
		return toExpression(expressions);
	}

	@Override
	public Expression visit(Concat expr, ContextFreeRule arg) {
		transformListInline(expr.getExpressions(), arg);
		
		return super.visit(expr, arg);
	}
	
	@Override
	public Expression visit(Alternative expr, ContextFreeRule arg) {
		for (ListIterator<Expression> it = expr.getExpressions().listIterator(); it.hasNext(); ) {
			it.set(transform(it.next(), arg));
		}
		
		return super.visit(expr, arg);
	}
	
	private void transformListInline(List<Expression> expressions, ContextFreeRule arg) {
		for (Expansions expansions : _expansions) {
			if (expansions.getSize() > expressions.size()) {
				continue;
			}
			
			for (int offset = 0, max = expressions.size() - expansions.getSize(); offset <= max; offset++) {
				ContextFreeRule rule = expansions.getRule(expressions.subList(offset, offset + expansions.getSize()));
				if (rule != null && rule != arg) {
					remove(expressions, offset, expansions.getSize());
					expressions.add(offset, nt(rule));
					
					max -= expansions.getSize() - 1;
				}
			}
		}
	}

	private NonTerminal nt(ContextFreeRule rule) {
		NonTerminal nt = F.nonTerminal();
		nt.setName(rule.getName());
		return nt;
	}
	
}
