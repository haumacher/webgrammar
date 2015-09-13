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

import java.io.IOError;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class SourceWriter {

	public SourceWriter(OutputStreamWriter out) {
		_out = out;
	}

	private static final String INDENT = "  ";
	
	private final OutputStreamWriter _out;
	
	private boolean _startOfLine = true;

	private StringBuilder _indentation = new StringBuilder();

	public void line(String string) {
		write(string);
		nl();
	}

	public void br() {
		if (contentOnLine()) {
			nl();
		}
	}
	
	public boolean contentOnLine() {
		return !_startOfLine;
	}

	public void nl() {
		write("\n");
		_startOfLine = true;
	}

	public void flush() {
		try {
			_out.flush();
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	public void write(String string) {
		try {
			if (_startOfLine) {
				_out.append(_indentation);
				_startOfLine = false;
			}
			_out.append(string);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}
	
	public void writeSingleIndent() {
		write(INDENT);
	}
	
	public void indent() {
		_indentation.append(INDENT);
	}
	
	public void unindent() {
		_indentation.setLength(_indentation.length() - INDENT.length());
	}

}
