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
package de.haumacher.webgrammar.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RegexParser {

	enum Mode {
		NORMAL, ESCAPED;
	}
	
	enum Modifier {
		OPTIONAL, ONE_OR_MORE, ZERO_OR_MORE;
	}
	
	abstract static class Expr {
		
		@Override
		public abstract String toString();

		public Expr append(Expr expr) {
			return new Concat(this, expr);
		}

		public abstract Expr modify(Modifier modifier);

		public abstract Expr addChoice();

	}
	
	abstract static class PrimitiveExpr extends Expr {
		
		@Override
		public Expr modify(Modifier modifier) {
			switch (modifier) {
			case OPTIONAL:
				return new Optional(this);
			case ZERO_OR_MORE:
				return new Repeat(this, true);
			case ONE_OR_MORE:
				return new Repeat(this, false);
			}
			throw new UnsupportedOperationException("Unknown modifier: " + modifier);
		}
		
		@Override
		public Expr addChoice() {
			return new Choice(this);
		}
		
	}
	
	static class Any extends PrimitiveExpr {

		@Override
		public String toString() {
			return "~[\"\\n\", \"\\r\"]";
		}
		
	}
	
	static class Repeat extends PrimitiveExpr {

		private final Expr _expr;
		private final boolean _mayBeEmpty;

		public Repeat(Expr expr, boolean mayBeEmpty) {
			_expr = expr;
			_mayBeEmpty = mayBeEmpty;
		}

		@Override
		public String toString() {
			return "(" + _expr + ")" + (_mayBeEmpty ? "*" : "+");
		}
		
	}
	
	static class Alphabet extends PrimitiveExpr {
		
		abstract static class Part {
			@Override
			public abstract String toString();
		}
		
		static class Single extends Part {

			private final char _ch;

			public Single(char ch) {
				this._ch = ch;
			}

			@Override
			public String toString() {
				return '\"' + encode(_ch) + '\"';
			}
			
		}
		
		static class Range extends Part {

			private final char _first;
			private final char _last;

			public Range(char first, char last) {
				_first = first;
				_last = last;
			}
			
			@Override
			public String toString() {
				return '\"' + encode(_first) + "\" - \"" + encode(_last) + '\"';
			}
		}
		
		private final boolean _negative;
		private final List<Part> _parts = new ArrayList<>();

		public Alphabet(boolean negative) {
			this._negative = negative;
		}

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			if (_negative) {
				result.append("~");
			}
			result.append("[");
			boolean first = true;
			for (Part part : _parts) {
				if (first) {
					first = false;
				} else {
					result.append(", ");
				}
				result.append(part);
			}
			result.append("]");
			return result.toString();
		}

		public void addRange(char first, char last) {
			_parts.add(new Range(first, last));
		}

		public void addChar(char ch) {
			_parts.add(new Single(ch));
		}

	}
	
	static class Char extends PrimitiveExpr {
		
		private char _ch;
		
		public Char(char ch) {
			_ch = ch;
		}

		@Override
		public String toString() {
			return '\"' + encode(_ch) + '\"';
		}
		
	}
	
	static class Choice extends Expr {
		
		private final List<Expr> _exprs = new ArrayList<>();
		
		private boolean _open = true;
		
		public Choice(Expr first) {
			_exprs.add(first);
		}
		
		@Override
		public Expr append(Expr expr) {
			if (_open) {
				_exprs.add(expr);
				_open = false;
			} else {
				int last = _exprs.size() - 1;
				_exprs.set(last, _exprs.get(last).append(expr));
			}
			return this;
		}
		
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			boolean first = true;
			result.append('(');
			for (Expr expr : _exprs) {
				if (first) {
					first = false;
				} else {
					result.append(" | ");
				}
				
				result.append('(');
				result.append(expr);
				result.append(')');
			}
			result.append(')');
			return result.toString();
		}
		
		@Override
		public Expr modify(Modifier modifier) {
			int last = _exprs.size() - 1;
			_exprs.set(last, _exprs.get(last).modify(modifier));
			return this;
		}
		
		@Override
		public Expr addChoice() {
			_open = true;
			return this;
		}
	}
	
	static class Concat extends Expr {
		
		private final List<Expr> _exprs = new ArrayList<>();

		public Concat() {
			super();
		}
		
		public Concat(Expr expr1, Expr expr2) {
			this();
			_exprs.add(expr1);
			_exprs.add(expr2);
		}

		@Override
		public Expr append(Expr expr) {
			if (_exprs.isEmpty()) {
				_exprs.add(expr);
			} else {
				Expr last = _exprs.remove(_exprs.size() - 1);
				Expr result = last.append(expr);
				if (result instanceof Concat) {
					_exprs.addAll(((Concat) result)._exprs);
				} else {
					_exprs.add(result);
				}
			}
			return this;
		}
		
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			boolean first = true;
			for (Expr expr : _exprs) {
				if (first) {
					first = false;
				} else {
					result.append(' ');
				}
				result.append(expr);
			}
			return result.toString();
		}
		
		@Override
		public Expr modify(Modifier modifier) {
			int last = _exprs.size() - 1;
			_exprs.set(last, _exprs.get(last).modify(modifier));
			return this;
		}
		
		@Override
		public Expr addChoice() {
			return new Choice(this);
		}
		
	}
	
	static class Optional extends PrimitiveExpr {
		private final Expr _expr;

		public Optional(Expr expr) {
			_expr = expr;
		}
		
		@Override
		public String toString() {
			return "(" + _expr + ")?";
		}
		
	}
	
	class Atom extends PrimitiveExpr {
		private final Expr _expr;
		
		public Atom(Expr expr) {
			_expr = expr;
		}
		
		@Override
		public String toString() {
			return "(" + _expr + ")";
		}
		
	}

	static String encode(char ch) {
		switch (ch) {
		case '\t': return "\\t";
		case '\n': return "\\n";
		case '\r': return "\\r";
		case '\f': return "\\f";
		case '\\': return "\\\\";
		case '\"': return "\\\"";
		default: return Character.toString(ch);
		}
	}

	private Mode _mode = Mode.NORMAL;
	
	private Stack<Expr> _stack = new Stack<>();
	
	static class Chars {

		private final String _src;
		
		private int _pos = 0;

		public Chars(String src) {
			_src = src;
		}
		
		public char peek() {
			return _src.charAt(_pos);
		}
		
		public char pop() {
			return _src.charAt(_pos++);
		}
		
		public void consume() {
			_pos++;
		}
		
		public boolean avail() {
			return _pos < _src.length();
		}
		
	}
	
	public Expr parse(String src) {
		_stack.push(new Concat());
		process(new Chars(src));
		return _stack.pop();
	}

	private void process(Chars src) {
		while (src.avail()) {
			char ch = src.pop();
			
			switch (_mode) {
			case NORMAL: {
				switch (ch) {
				case '\\': {
					_mode = Mode.ESCAPED;
					break;
				}
				
				case '.': {
					append(new Any());
					break;
				}
				
				case '?': {
					current().modify(Modifier.OPTIONAL);
					break;
				}
				
				case '*': {
					current().modify(Modifier.ZERO_OR_MORE);
					break;
				}
				
				case '+': {
					current().modify(Modifier.ONE_OR_MORE);
					break;
				}
				
				case '|': {
					_stack.push(_stack.pop().addChoice());
					break;
				}
				
				case '(': {
					_stack.push(new Concat());
					break;
				}
				
				case ')': {
					Expr contents = _stack.pop();
					append(new Atom(contents));
					break;
				}
				
				case '[': {
					boolean negative = src.peek() == '^';
					if (negative) {
						src.consume();
					}
					
					Alphabet alphabet = new Alphabet(negative);
					
					while (true) {
						char first = src.pop();
						if (first == ']') {
							break;
						}
						if (first == '\\') {
							first = decode(src.pop());
						}
						
						if (src.peek() == '-') {
							src.consume();
							
							char last = src.pop();
							if (last == '\\') {
								last = decode(src.pop());
							}
							alphabet.addRange(first, last);
						} else {
							alphabet.addChar(first);
						}
					}
					append(alphabet);
					
					break;
				}
				
				default: {
					appendChar(ch);
					break;
				}
				
				}
				break;
			}
			
			case ESCAPED: {
				appendChar(decode(ch));
				_mode = Mode.NORMAL;
				break;
			}
			
			}
		}
	}

	private char decode(char ch) {
		char decoded;
		switch (ch) {
		case 't': {
			decoded = '\t';
			break;
		}
		case 'n': {
			decoded = '\n';
			break;
		}
		case 'r': {
			decoded = '\r';
			break;
		}
		case 'f': {
			decoded = '\f';
			break;
		}
		default: {
			decoded = ch;
			break;
		}
		}
		return decoded;
	}

	private void appendChar(char ch) {
		append(new Char(ch));
	}

	private void append(Expr expr) {
		_stack.push(_stack.pop().append(expr));
	}

	private Expr current() {
		return _stack.peek();
	}

}
