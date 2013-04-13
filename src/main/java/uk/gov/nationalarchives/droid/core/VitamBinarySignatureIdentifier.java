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
package uk.gov.nationalarchives.droid.core;

import uk.gov.nationalarchives.droid.core.signature.droid6.FFSignatureFile;

/**
 * Implementation of DroidCore which uses the droid binary signatures to identify files.
 * identifications.
 * 
 * @author rflitcroft
 * @author "Frederic Bregier"
 * 
 */
public class VitamBinarySignatureIdentifier extends
		uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier {

	/**
	 * Default constructor.
	 */
	public VitamBinarySignatureIdentifier() {
	}

	/**
	 * @return the sigFile
	 */
	public FFSignatureFile getSigFile() {
		return super.getSigFile();
	}

}
