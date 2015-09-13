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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Main {
	
	public static void main(String[] args) throws MalformedURLException, IOException, TransformerFactoryConfigurationError, TransformerException, ParserConfigurationException, SAXException {
		Transformer transformer;
		try(InputStream transformation = Main.class.getResourceAsStream("transform.xslt")) {
			Source xslt = new StreamSource(transformation);
			TransformerFactory factory = TransformerFactory.newInstance();
			factory.setURIResolver(new URIResolver() {
				@Override
				public Source resolve(String href, String base) throws TransformerException {
					return null;
				}
			});
			transformer = factory.newTransformer(xslt);
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		factory.setFeature("http://xml.org/sax/features/namespaces", true);
		factory.setFeature("http://xml.org/sax/features/validation", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		// open up the xml document
		DocumentBuilder parser = factory.newDocumentBuilder();
		
		URL srcUrl = new URL(args[0]);
		try(InputStream in = srcUrl.openStream()) {
			Document sourceDoc = parser.parse(in);
			Source source = new DOMSource(sourceDoc);
			
			try (OutputStream out = new FileOutputStream(new File(args[1]))) {
				Result result = new StreamResult(out);
				transformer.transform(source, result);
			}
		}
	}

}
