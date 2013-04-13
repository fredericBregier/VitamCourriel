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

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import fr.gouv.culture.vitam.utils.ConfigLoader;
import fr.gouv.culture.vitam.utils.StaticValues;

/**
 * Xml validation from a XSD
 * 
 * @author "Frederic Bregier"
 * 
 */
public class XsdValidContentHandler extends DefaultHandler {
	private boolean isDocumentElement = false;
	private boolean hadError = false;
	private ConfigLoader config;

	/**
	 * @param config
	 */
	public XsdValidContentHandler(ConfigLoader config) {
		this.config = config;
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (!isDocumentElement) {
			isDocumentElement = true;
			if (!localName.equals(config.CURRENT_XSD_ROOT)
					|| !uri.equals(config.DEFAULT_LOCATION)) {
				hadError = true;
				System.err
						.println(StaticValues.LBL.error_rootxml.get() + " {"
								+ uri
								+ "}"
								+ localName
								+ StaticValues.LBL.error_notequal.get()
								+ " {" + config.DEFAULT_LOCATION
								+ "}:" + config.CURRENT_XSD_ROOT);
			}
		}
	}

	public boolean hadError() {
		StaticValues.freeMemory();
		return hadError;
	}
}
