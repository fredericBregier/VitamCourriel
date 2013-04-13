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

import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;

import fr.gouv.culture.vitam.digest.DigestCompute;
import fr.gouv.culture.vitam.utils.VitamArgument;
import fr.gouv.culture.vitam.utils.XmlDom;

import uk.gov.nationalarchives.droid.core.signature.FileFormat;

/**
 * Meta FileFormat of Droid FileFormat
 * 
 * @author "Frederic Bregier"
 * 
 */
public class DroidFileFormat {
	public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
	private String filename;
	private String sha1, sha256, sha512;
	private VitamArgument argument;
	private FileFormat fileFormat;
	private HashMap<String, String> allFormats;
	public static final String Unknown = "Unknown";

	public static final FileFormat UnknownFormat = new FileFormat();
	{
		UnknownFormat.setMimeType(Unknown);
		UnknownFormat.setAttributeValue("PUID", Unknown);
		UnknownFormat.setAttributeValue("Name", Unknown);
	}

	/**
	 * @param filename
	 * @param fileFormat
	 */
	public DroidFileFormat(String filename, FileFormat fileFormat) {
		this.filename = filename;
		this.fileFormat = fileFormat;
		this.argument = VitamArgument.NOFEATURE;
	}

	/**
	 * 
	 * @param filename
	 * @param fileFormat
	 * @param argument
	 */
	public DroidFileFormat(String filename, FileFormat fileFormat,
			VitamArgument argument) {
		this.filename = filename;
		this.fileFormat = fileFormat;
		this.argument = argument == null ? VitamArgument.NOFEATURE : argument;
		if (this.argument.sha1 || this.argument.sha256 || this.argument.sha512) {
			DigestCompute.computeDroidFileFormatDigest(this, this.argument);
		}
	}

	/**
	 * @return the filename
	 */
	public final String getFilename() {
		return filename;
	}

	/**
	 * @return the fileFormat
	 */
	public final FileFormat getFileFormat() {
		return fileFormat;
	}

	/**
	 * 
	 * @return the number of file extensions.
	 */
	public final int getNumExtensions() {
		return fileFormat.getNumExtensions();
	}

	/**
	 * 
	 * @return The mime type of the file format.
	 */
	public final String getMimeType() {
		String mime = fileFormat.getMimeType();
		if (mime == null || mime.length() < 3) {
			return APPLICATION_OCTET_STREAM;
		}
		return mime;
	}

	/**
	 * 
	 * @param theIndex
	 *            The index of the file extension
	 * @return the file extension.
	 */
	public final String getExtension(final int theIndex) {
		return fileFormat.getExtension(theIndex);
	}

	/**
	 * 
	 * @return A list of extensions defined against this file format.
	 */
	public final List<String> getExtensions() {
		return fileFormat.getExtensions();
	}

	/**
	 * 
	 * @return the name of this file format.
	 */
	public final String getFormat() {
		return fileFormat.getName();
	}

	/**
	 * 
	 * @return The version of this file format.
	 */
	public final String getVersion() {
		return fileFormat.getVersion();
	}

	/**
	 * 
	 * @return the puid of this file format.
	 */
	public final String getPUID() {
		return fileFormat.getPUID();
	}

	/**
	 * 
	 * @return TXT Header (from toString)
	 */
	public final static String toStringCsvHeader() {
		return "Filename,Puid,MimeType,Format,Version,[ (extensions )*],SHA-1=xxx,SHA-256=xxx,SHA-512=xxx";
	}

	/**
	 * 
	 * @return TXT format
	 */
	public final String toStringCsv() {
		StringBuilder builder = new StringBuilder(filename);
		builder.append(',');
		builder.append(getPUID());
		builder.append(',');
		builder.append(getMimeType());
		builder.append(',');
		builder.append(getFormat());
		builder.append(',');
		builder.append(getVersion());
		builder.append(',');
		if (getNumExtensions() > 0) {
			builder.append("[ ");
			for (String extension : getExtensions()) {
				builder.append(extension);
				builder.append(' ');
			}
			builder.append(']');
		}
		builder.append(',');
		if (sha1 != null) {
			builder.append("SHA-1=");
			builder.append(sha1);
		}
		builder.append(',');
		if (sha256 != null) {
			builder.append("SHA-256=");
			builder.append(sha256);
		}
		builder.append(',');
		if (sha512 != null) {
			builder.append("SHA-512=");
			builder.append(sha512);
		}
		return builder.toString();
	}

	/**
	 * @param withFilename
	 *            True if filename attribute will be added
	 * @return the associate Element in Dom4j
	 */
	public final Element toElement(boolean withFilename) {
		Element identity = XmlDom.factory.createElement("identity");
		identity.addAttribute("format", getFormat());
		String mimetype = getMimeType();
		if (mimetype == null || mimetype.length() == 0) {
			mimetype = APPLICATION_OCTET_STREAM;
		}
		identity.addAttribute("mime", mimetype);
		String version = getVersion();
		if (version != null) {
			identity.addAttribute("version", version);
		}
		identity.addAttribute("puid", getPUID());
		int len = getNumExtensions();
		if (len > 0) {
			String extension = null;
			for (int i = 0; i < len; i++) {
				if (extension == null) {
					extension = getExtension(i);
				} else {
					extension += "," + getExtension(i);
				}
			}
			identity.addAttribute("extensions", extension);
		}
		if (sha1 != null) {
			identity.addAttribute("sha-1", sha1);
		}
		if (sha256 != null) {
			identity.addAttribute("sha-256", sha256);
		}
		if (sha512 != null) {
			identity.addAttribute("sha-512", sha512);
		}
		if (withFilename) {
			identity.addAttribute("filename", filename);
		}
		return identity;
	}

	/**
	 * 
	 * @return the associate Element in JDOM1
	 */
	public final org.jdom.Element toElementJdom1() {
		org.jdom.Element identity = new org.jdom.Element("identification");
		identity.setAttribute("format", getFormat());
		String mimetype = getMimeType();
		if (mimetype == null || mimetype.length() == 0) {
			mimetype = APPLICATION_OCTET_STREAM;
		}
		identity.setAttribute("mimetype", mimetype);
		String version = getVersion();
		if (version != null) {
			identity.setAttribute("version", version);
		}
		identity.setAttribute("puid", getPUID());
		int len = getNumExtensions();
		if (len > 0) {
			String extension = null;
			for (int i = 0; i < len; i++) {
				if (extension == null) {
					extension = getExtension(i);
				} else {
					extension += "," + getExtension(i);
				}
			}
			identity.setAttribute("extensions", extension);
		}
		if (sha1 != null) {
			identity.setAttribute("sha-1", sha1);
		}
		if (sha256 != null) {
			identity.setAttribute("sha-256", sha256);
		}
		if (sha512 != null) {
			identity.setAttribute("sha-512", sha512);
		}
		identity.setText(filename);
		return identity;
	}

	/**
	 * @return the sha1
	 */
	public final String getSha1() {
		return sha1;
	}

	/**
	 * @param sha1
	 *            the sha1 to set
	 */
	public final void setSha1(String sha1) {
		this.sha1 = sha1;
	}

	/**
	 * @return the sha256
	 */
	public final String getSha256() {
		return sha256;
	}

	/**
	 * @param sha256
	 *            the sha256 to set
	 */
	public final void setSha256(String sha256) {
		this.sha256 = sha256;
	}

	/**
	 * @return the sha512
	 */
	public final String getSha512() {
		return sha512;
	}

	/**
	 * @param sha512
	 *            the sha512 to set
	 */
	public final void setSha512(String sha512) {
		this.sha512 = sha512;
	}

	/**
	 * @return the argument
	 */
	public VitamArgument getArgument() {
		return argument;
	}

	/**
	 * @return the allFormats
	 */
	public HashMap<String, String> getAllFormats() {
		return allFormats;
	}

	/**
	 * @param allFormats
	 *            the allFormats to set
	 */
	public void setAllFormats(HashMap<String, String> allFormats) {
		this.allFormats = allFormats;
	}

}
