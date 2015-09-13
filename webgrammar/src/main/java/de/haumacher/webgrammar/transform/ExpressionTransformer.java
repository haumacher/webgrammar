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
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import de.haumacher.values.Property;
import de.haumacher.values.Value;
import de.haumacher.values.ValueDescriptor;
import de.haumacher.webgrammar.model.Alternative;
import de.haumacher.webgrammar.model.Concat;
import de.haumacher.webgrammar.model.Constant;
import de.haumacher.webgrammar.model.ContextFreeRule;
import de.haumacher.webgrammar.model.Epsilon;
import de.haumacher.webgrammar.model.Expression;
import de.haumacher.webgrammar.model.F;
import de.haumacher.webgrammar.model.Grammar;
import de.haumacher.webgrammar.model.Iteration;
import de.haumacher.webgrammar.model.NaryExpression;
import de.haumacher.webgrammar.model.NonTerminal;
import de.haumacher.webgrammar.model.Optional;
import de.haumacher.webgrammar.model.Rule;
import de.haumacher.webgrammar.model.UnaryExpression;
import de.haumacher.webgrammar.model.visit.ExpressionVisitor;
import de.haumacher.webgrammar.model.visit.VisitHelper;

public abstract class ExpressionTransformer<A> implements ExpressionVisitor<Expression, A> {

	private Grammar _grammar;
	
	private Map<String, Rule> _rules;

	public void visit(Grammar grammar) {
		_grammar = grammar;
		visit(grammar, null);
		_grammar = null;
	}
	
	public void visit(Grammar grammar, A arg) {
		for (Rule rule : grammar.getRules().toArray(new Rule[grammar.getRules().size()])) {
			if (rule instanceof ContextFreeRule) {
				visit((ContextFreeRule) rule, arg);
			}
		}
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
	
	public void visit(ContextFreeRule rule, A arg) {
		descendUnary(rule, arg);
	}

	protected <E extends UnaryExpression> E descendUnary(E expr, A arg) {
		Expression inner = expr.getExpression();
		Expression innerResult = transform(inner, arg);
		if (innerResult == null) {
			return null;
		} else {
			expr.setExpression(innerResult);
			return expr;
		}
	}
	
	protected void descendNary(NaryExpression expr, A arg) {
		ListIterator<Expression> it = expr.getExpressions().listIterator();
		while (it.hasNext()) {
			Expression sub = it.next();
			Expression result = transform(sub, arg);
			if (result == null) {
				it.remove();
			} else {
				it.set(result);
			}
		}
	}

	private Expression transform(Expression sub, A arg) {
		if (sub == null) {
			return null;
		}
		return VisitHelper.visitExpression(this, sub, arg);
	}

	@Override
	public Expression visit(Concat expr, A arg) {
		descendNary(expr, arg);
		return expr;
	}

	@Override
	public Expression visit(Alternative expr, A arg) {
		descendNary(expr, arg);
		return expr;
	}

	@Override
	public Expression visit(Optional expr, A arg) {
		return descendUnary(expr, arg);
	}

	@Override
	public Expression visit(Iteration expr, A arg) {
		expr.setSeparator(transform(F.getSeparator(expr), arg));
		
		return descendUnary(expr, arg);
	}

	@Override
	public Expression visit(Constant expr, A arg) {
		return expr;
	}

	@Override
	public Expression visit(Epsilon expr, A arg) {
		return expr;
	}

	@Override
	public Expression visit(NonTerminal expr, A arg) {
		return expr;
	}
	
	public static Expression copy(Expression expression) {
		return clone(expression);
	}

	public static List<Expression> copy(List<Expression> expressions) {
		return cloneList(expressions);
	}
	
	private static <T> T clone(T obj) {
		if (obj == null) {
			return null;
		}
		Value item = (Value) obj;
		ValueDescriptor<?> descriptor = item.descriptor();
		Value result = (Value) descriptor.newInstance();
		for (Property property : descriptor.getProperties().values()) {
			Object value = item.value(property);
			switch (property.getKind()) {
			case VALUE: {
				value = clone((Value) value);
				break;
			}
			case LIST: {
				value = cloneList((List<?>) value);
				break;
			}
			case INDEX: {
				value = cloneMap((Map<?, ?>) value, property.getIndexProperty());
				break;
			}
			}
			result.putValue(property, value);
		}
		return (T) result;
	}
	
	private static <T> List<T> cloneList(List<T> list) {
		List<T> result = new ArrayList<>();
		for (Object element : list) {
			Value clone = clone((Value) element);
			result.add((T) clone);
		}
		return result;
	}

	private static Map<?, ?> cloneMap(Map<?, ?> map, Property indexProperty) {
		Map<Object, Object> result = new HashMap<>();
		for (Object element : map.values()) {
			Value clone = clone((Value) element);
			result.put(clone.value(indexProperty), clone);
		}
		return result;
	}
	
}
