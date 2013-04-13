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
package fr.gouv.culture.vitam.droid;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.gouv.culture.vitam.utils.VitamArgument;

import uk.gov.nationalarchives.droid.core.signature.FileFormat;
import uk.gov.nationalarchives.droid.core.signature.droid6.FFSignatureFile;

/**
 * Used to get output of Tools like Droid from System.out
 * 
 * 
 * @author "Frederic Bregier"
 * 
 */
public class DroidFileFormatOutputStream extends ByteArrayOutputStream {
	List<DroidFileFormat> formats;
	FFSignatureFile signatureFile = null;
	VitamArgument argument;

	public DroidFileFormatOutputStream(FFSignatureFile signatureFile, VitamArgument argument) {
		formats = new ArrayList<DroidFileFormat>(1);
		this.signatureFile = signatureFile;
		this.argument = argument == null ? VitamArgument.NOFEATURE : argument;
	}

	/**
	 * Override this method to intercept the output text. Each line of text output will actually
	 * involve invoking this method twice:
	 * 
	 * a) for the actual text message b) for the newLine string
	 */
	public void flush() {
		String result = new String(this.toByteArray(), 0, this.size());
		reset();
		if (result == null || result.isEmpty() || !result.contains(",")) {
			return;
		}
		String[] args = result.split(",");
		// check if filename contains ","
		int rank = 0;
		for (String string : args) {
			if (rank == 0) {
				rank++;
				continue;
			}
			if (string.startsWith("fmt") || string.startsWith("x-fmt")) {
				break;
			}
			rank++;
		}
		String[] finalArgs = null;
		if (rank == args.length) {
			// there should be Unknown at last
			rank = args.length-1;
		}
		finalArgs = new String[args.length-rank+1];
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < rank; i++) {
			if (i != 0) {
				builder.append(',');
			}
			builder.append(args[i]);
		}
		finalArgs[0] = builder.toString();
		for (int i = rank; i < args.length; i++) {
			finalArgs[i-rank+1] = args[i];
		}
		args = finalArgs;
		if (args[1].equalsIgnoreCase(DroidFileFormat.Unknown)) {
			formats.add(outputFormatExtension(args[0]));
		} else {
			formats.add(outputFormat(args));
		}
	}

	/**
	 * 
	 * @return Get the result as List of DroidFileFormat
	 */
	public List<DroidFileFormat> getResult() {
		return formats;
	}

	private final void verifyMimeType(FileFormat format) {
		String mimetype = format.getMimeType();
		if (mimetype == null || mimetype.length() == 0) {
			mimetype = DroidFileFormat.APPLICATION_OCTET_STREAM;
			format.setMimeType(mimetype);
		}
	}

	private final DroidFileFormat outputFormat(String[] args) {
		FileFormat format = signatureFile.getFileFormat(args[1]);
		if (format != null) {
			verifyMimeType(format);
			DroidFileFormat newone = new DroidFileFormat(args[0], format, argument);
			HashMap<String, String> hash = new HashMap<String, String>();
			for (int i = 1; i < args.length; i++) {
				FileFormat fileFormat = signatureFile.getFileFormat(args[i]);
				verifyMimeType(fileFormat);
				hash.put(args[i], fileFormat.getMimeType());
			}
			newone.setAllFormats(hash);
			return newone;
		} else {
			return outputFormatExtension(args[0]);
		}
	}

	private final DroidFileFormat outputFormatExtension(String fileName) {
		//System.err.println(StaticValues.LBL.error_fromextension.get() + fileName);
		String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
		List<FileFormat> formats = signatureFile.getTentativeFormatsForExtension(extension);
		if (formats == null) {
			formats = signatureFile.getFileFormatsForExtension(extension);
		}
		if (formats != null && !formats.isEmpty()) {
			FileFormat format = formats.get(0);
			verifyMimeType(format);
			HashMap<String, String> hash = new HashMap<String, String>();
			for (FileFormat fileFormat : formats) {
				verifyMimeType(fileFormat);
				hash.put(fileFormat.getPUID(), fileFormat.getMimeType());
			}
			DroidFileFormat newone = new DroidFileFormat(fileName, format, argument);
			newone.setAllFormats(hash);
			return newone;
		} else {
			return new DroidFileFormat(fileName, DroidFileFormat.UnknownFormat, argument);
		}
	}

}
