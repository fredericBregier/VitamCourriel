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
package fr.gouv.culture.vitam.utils;

import java.util.List;

import org.dom4j.Document;

/**
 * Class that handles Vitam Result from various modules
 * 
 * @author "Frederic Bregier"
 *
 */
public class VitamResult {
	/**
	 * array of String in that order<br>
	 * (Sys,<br>
	 * File,<br>
	 * Digest,<br>
	 * Format,<br>
	 * Show)
	 */
	public String[] labels;
	/**
	 * Output will be in STDOUT<br>
	 * <br>
	 * array of int in that order<br>
	 * (systemError,<br>
	 * file Error, Warning, Success,<br>
	 * digest Error, Warning, Success,<br>
	 * format Error, Warning, Success,<br>
	 * show Error, Warning, Success)
	 */
	public int[] values;
	/**
	 * Each document will be hold in one XML
	 */
	public List<Document> multiples;
	/**
	 * All documents will be hold in one unique XML
	 */
	public Document unique;

}
