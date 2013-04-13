/**
 * This file is part of Vitam Project.
 * 
 * Copyright 2010, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of individual contributors.
 * 
 * All Vitam Project is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Vitam is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Vitam. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package fr.gouv.culture.vitam.saxhandlers;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import fr.gouv.culture.vitam.utils.StaticValues;

/**
 * Sax error handler
 * 
 * @author "Frederic Bregier"
 * 
 */
public class SaxErrorHandler extends DefaultHandler {
	private boolean hadError;

	public SaxErrorHandler() {
		super();
		hadError = false;
	}

	public boolean hadError() {
		return hadError;
	}

	public void warning(SAXParseException ex) throws SAXException {
		printError(StaticValues.LBL.error_warning.get(), ex);
	}

	public void error(SAXParseException ex) throws SAXException {
		hadError = true;
		printError(StaticValues.LBL.error_error.get(), ex);
	}

	public void fatalError(SAXParseException ex) throws SAXException {
		hadError = true;
		printError(StaticValues.LBL.error_alerte.get(), ex);
	}

	private void printError(String type, SAXParseException ex) {
		StringBuilder builder = new StringBuilder("[");
		builder.append(type);
		builder.append("] ");
		if (ex == null) {
			builder.append("!!!");
		} else {
			String systemId = ex.getSystemId();
			if (systemId != null) {
				int index = systemId.lastIndexOf('/');
				if (index != -1) {
					systemId = systemId.substring(index + 1);
				}
				builder.append(systemId);
			}
			builder.append(':');
			builder.append(ex.getLineNumber());
			builder.append(':');
			builder.append(ex.getColumnNumber());
			builder.append(": ");
			builder.append(ex.getMessage());
		}
		System.err.println(builder.toString());
	}
}
