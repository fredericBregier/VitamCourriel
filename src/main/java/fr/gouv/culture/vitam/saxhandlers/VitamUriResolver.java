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

import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

/**
 * Utility to resolve a uri locally
 * 
 * @author "Frederic Bregier"
 * 
 */
public class VitamUriResolver implements URIResolver {
	private String[] base;

	public VitamUriResolver(String base) {
		this.base = new String[] { base };
	}

	public VitamUriResolver(String[] base) {
		this.base = base;
	}

	public Source resolve(String systemId, String base) {
		try {
			InputStream in = null;
			int i = 0;
			for (i = 0; i < this.base.length; i++) {
				in = getClass()
						.getResourceAsStream(this.base[i] + systemId);
				if (in != null) {
					break;
				}
			}
			if (in == null) {
				in = getClass()
						.getResourceAsStream(systemId);
				if (in == null) {
					return null;
				}
			}
			return new StreamSource(in);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}
}
