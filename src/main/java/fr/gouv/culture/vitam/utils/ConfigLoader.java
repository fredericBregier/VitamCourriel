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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import edu.harvard.hul.ois.fits.Fits;
import edu.harvard.hul.ois.fits.exceptions.FitsConfigurationException;
import edu.harvard.hul.ois.fits.exceptions.FitsException;
import edu.harvard.hul.ois.fits.tools.exiftool.Exiftool;
import edu.harvard.hul.ois.fits.tools.jhove.Jhove;
import fr.gouv.culture.vitam.droid.DroidHandler;
import fr.gouv.culture.vitam.eml.StringUtils.EMAIL_FIELDS;
import fr.gouv.culture.vitam.utils.VitamArgument.VitamOutputModel;

/**
 * Configuration Loader from file/environment
 * 
 * @author "Frederic Bregier"
 * 
 */
public class ConfigLoader {
	public String xmlFile = null;
	
	public boolean useSaxonToGetRootFromXSD = true;
	public boolean useXslt1ForSchematron = true;
	public int nbDocument = 0;
	public DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public String BASE_RESOURCES = null;
	// Parsers
	public String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
	public String DEFAULT_TRANSFORMER_NAME = StaticValues.SAXON_TRANSFORMER_NAME;
	// Xsd
	public String CURRENT_XSD_ROOT = "ArchiveTransfer";
	public String DEFAULT_LOCATION = "fr:gouv:culture:archivesdefrance:seda:v1.0";
	// Field
	public String DOCUMENT_FIELD = "Document";
	public String ATTACHMENT_FIELD = "Attachment";
	public String FILENAME_ATTRIBUTE = "@filename";
	public String INTEGRITY_FIELD = "Integrity";
	public String ALGORITHME_ATTRIBUTE = "@algorithme";
	public String MIMECODE_ATTRIBUTE = "@mimeCode";
	public String FORMAT_ATTRIBUTE = "@format";
	
	public AtomicLong nbDoc = new AtomicLong(0);

	// XPATH //DOCUMENT_FIELD example: //Document
	// XPATH //DOCUMENT_FIELD/ATTACHMENT_FIELD example: //Document/Attachment
	// XPATH //DOCUMENT_FIELD/ATTACHMENT_FIELD/FILENAME_ATTRIBUTE example:
	// //Document/Attachment/@filename
	// XPATH //DOCUMENT_FIELD/ATTACHMENT_FIELD/MIMECODE_ATTRIBUTE example:
	// //Document/Attachment/@mimeCode
	// XPATH //DOCUMENT_FIELD/ATTACHMENT_FIELD/FORMAT_ATTRIBUTE example:
	// //Document/Attachment/@format
	// XPATH //DOCUMENT_FIELD/INTEGRITY_FIELD example: //Document/Integrity
	// XPATH //DOCUMENT_FIELD/INTEGRITY_FIELD/ALGORITHME_ATTRIBUTE example:
	// //Document/Integrity/@algorithme

	// Digest
	public String DEFAULT_DIGEST = StaticValues.XML_SHA1;

	// Droid V6.1
	public DroidHandler droidHandler = null;
	public String SIGNATURE_FILE = "droid-6.1/DROID_SignatureFile_V67.xml";
	public String CONTAINER_SIGNATURE_FILE = "droid-6.1/container-signature-20120828.xml";
	public VitamArgument argument = new VitamArgument();

	// FITS 0.6.1
	public String FITS_HOME = "fits";
	public Fits fits = null;
	public Exiftool exif = null;
	public Jhove jhove = null;
	
	// LibreOffice/OpenOffice
	public String LIBREOFFICE_HOME = "C:\\Program Files (x86)\\LibreOffice 3.5";
	public String UNOCONV = "unoconv/unoconv.py";
	public long msPerKB = 300;
	public long lowLimitMs = 60000;
	
	// Last directory scan
	public File lastScannedDirectory = null;
	public boolean guiProposeFileSaving = false;
	public boolean preventXfmt = false;

	// Metadata extraction
	public int rankLimit = 1;
	public int wordLimit = 100;
	
	public String addRankId(Element root) {
		String id = Long.toString(nbDoc.incrementAndGet());
		root.addAttribute(EMAIL_FIELDS.rankId.name, id);
		return id;
	}
	private static String getProperty(Properties properties, String key, String defaultValue) {
		if (properties.containsKey(key)) {
			return properties.getProperty(key);
		}
		properties.setProperty(key, defaultValue);
		return defaultValue;
	}
	private static int getProperty(Properties properties, String key, int defaultValue) {
		if (properties.containsKey(key)) {
			try {
				int value = Integer.parseInt(properties.getProperty(key));
				return value;
			} catch (NumberFormatException e) {
			}
		}
		properties.setProperty(key, Integer.toString(defaultValue));
		return defaultValue;
	}
	private static long getProperty(Properties properties, String key, long defaultValue) {
		if (properties.containsKey(key)) {
			try {
				long value = Long.parseLong(properties.getProperty(key));
				return value;
			} catch (NumberFormatException e) {
			}
		}
		properties.setProperty(key, Long.toString(defaultValue));
		return defaultValue;
	}

	public void updateProperties(Properties properties) {
		useSaxonToGetRootFromXSD = getProperty(properties, "vitam.usesaxonxsdroot", 1) == 1;
		DEFAULT_PARSER_NAME = getProperty(properties, StaticValues.SAX_DRIVER,
				DEFAULT_PARSER_NAME);
		DEFAULT_TRANSFORMER_NAME = getProperty(properties, 
				StaticValues.TRANSFORMER_FACTORY,
				DEFAULT_TRANSFORMER_NAME);
		CURRENT_XSD_ROOT = getProperty(properties, "vitam.root", CURRENT_XSD_ROOT);
		DEFAULT_LOCATION = getProperty(properties, "vitam.location", DEFAULT_LOCATION);
		DOCUMENT_FIELD = getProperty(properties, "vitam.docfield", DOCUMENT_FIELD);
		ATTACHMENT_FIELD = getProperty(properties, "vitam.filefield", ATTACHMENT_FIELD);
		FILENAME_ATTRIBUTE = getProperty(properties, "vitam.fileattrib",
				FILENAME_ATTRIBUTE);
		INTEGRITY_FIELD = getProperty(properties, "vitam.digestfield", INTEGRITY_FIELD);
		ALGORITHME_ATTRIBUTE = getProperty(properties, "vitam.algoattrib",
				ALGORITHME_ATTRIBUTE);
		MIMECODE_ATTRIBUTE = getProperty(properties, "vitam.mimeattrib",
				MIMECODE_ATTRIBUTE);
		FORMAT_ATTRIBUTE = getProperty(properties, "vitam.formatattrib", FORMAT_ATTRIBUTE);
		SIGNATURE_FILE = getProperty(properties, "vitam.signature", SIGNATURE_FILE);
		CONTAINER_SIGNATURE_FILE = getProperty(properties, "vitam.container",
				CONTAINER_SIGNATURE_FILE);
		argument.archive = getProperty(properties, "vitam.checkarchives", 0) == 1;
		argument.recursive = getProperty(properties, "vitam.checkrecursive", 0) == 1;
		argument.sha1 = getProperty(properties, "vitam.sha1", 1) == 1;
		argument.sha256 = getProperty(properties, "vitam.sha256", 0) == 1;
		argument.sha512 = getProperty(properties, "vitam.sha512", 0) == 1;
		argument.extractKeyword = getProperty(properties, "vitam.extractkeyword", 1) == 1;
		int ivalue = getProperty(properties, "vitam.output",
				VitamOutputModel.OneXML.ordinal());
		if (ivalue < VitamOutputModel.values().length) {
			argument.outputModel = VitamOutputModel.values()[ivalue];
		} else {
			argument.outputModel = VitamOutputModel.OneXML;
		}
		guiProposeFileSaving = getProperty(properties, "vitam.guisave", 0) == 1;
		preventXfmt = getProperty(properties, "vitam.preventxfmt", 1) == 1;
		FITS_HOME = getProperty(properties, "vitam.fits", FITS_HOME);
		LIBREOFFICE_HOME = getProperty(properties, "vitam.libreoffice", LIBREOFFICE_HOME);
		UNOCONV = getProperty(properties, "vitam.unoconv", UNOCONV);
		msPerKB = getProperty(properties, "vitam.msperkb", 300);
		lowLimitMs = getProperty(properties, "vitam.lowlimitms", 60000);
		wordLimit = getProperty(properties, "vitam.wordlimit", 100);
		rankLimit = getProperty(properties, "vitam.ranklimit", 1);
	}

	public void setProperties(Properties properties) {
		useSaxonToGetRootFromXSD = getProperty(properties, "vitam.usesaxonxsdroot", useSaxonToGetRootFromXSD ? 1 : 0) == 1;
		DEFAULT_PARSER_NAME = getProperty(properties, StaticValues.SAX_DRIVER,
				DEFAULT_PARSER_NAME);
		DEFAULT_TRANSFORMER_NAME = getProperty(properties, 
				StaticValues.TRANSFORMER_FACTORY,
				DEFAULT_TRANSFORMER_NAME);
		CURRENT_XSD_ROOT = getProperty(properties, "vitam.root", CURRENT_XSD_ROOT);
		DEFAULT_LOCATION = getProperty(properties, "vitam.location", DEFAULT_LOCATION);
		DOCUMENT_FIELD = getProperty(properties, "vitam.docfield", DOCUMENT_FIELD);
		ATTACHMENT_FIELD = getProperty(properties, "vitam.filefield", ATTACHMENT_FIELD);
		FILENAME_ATTRIBUTE = getProperty(properties, "vitam.fileattrib",
				FILENAME_ATTRIBUTE);
		INTEGRITY_FIELD = getProperty(properties, "vitam.digestfield", INTEGRITY_FIELD);
		ALGORITHME_ATTRIBUTE = getProperty(properties, "vitam.algoattrib",
				ALGORITHME_ATTRIBUTE);
		MIMECODE_ATTRIBUTE = getProperty(properties, "vitam.mimeattrib",
				MIMECODE_ATTRIBUTE);
		FORMAT_ATTRIBUTE = getProperty(properties, "vitam.formatattrib", FORMAT_ATTRIBUTE);
		SIGNATURE_FILE = getProperty(properties, "vitam.signature", SIGNATURE_FILE);
		CONTAINER_SIGNATURE_FILE = getProperty(properties, "vitam.container",
				CONTAINER_SIGNATURE_FILE);
		argument.archive = getProperty(properties, "vitam.checkarchives", argument.archive ? 1 : 0) == 1;
		argument.recursive = getProperty(properties, "vitam.checkrecursive", argument.recursive ? 1 : 0) == 1;
		argument.sha1 = getProperty(properties, "vitam.sha1", argument.sha1 ? 1 : 0) == 1;
		argument.sha256 = getProperty(properties, "vitam.sha256", argument.sha256 ? 1 : 0) == 1;
		argument.sha512 = getProperty(properties, "vitam.sha512", argument.sha512 ? 1 : 0) == 1;
		argument.extractKeyword = getProperty(properties, "vitam.extractkeyword", argument.extractKeyword ? 1 : 0) == 1;
		int ivalue = getProperty(properties, "vitam.output", argument.outputModel.ordinal());
		if (ivalue < VitamOutputModel.values().length) {
			argument.outputModel = VitamOutputModel.values()[ivalue];
		} else {
			argument.outputModel = VitamOutputModel.OneXML;
		}
		guiProposeFileSaving = getProperty(properties, "vitam.guisave", guiProposeFileSaving ? 1 : 0) == 1;
		preventXfmt = getProperty(properties, "vitam.preventxfmt", preventXfmt ? 1 : 0) == 1;
		FITS_HOME = getProperty(properties, "vitam.fits", FITS_HOME);
		LIBREOFFICE_HOME = getProperty(properties, "vitam.libreoffice", LIBREOFFICE_HOME);
		UNOCONV = getProperty(properties, "vitam.unoconv", UNOCONV);
		msPerKB = getProperty(properties, "vitam.msperkb", msPerKB);
		lowLimitMs = getProperty(properties, "vitam.lowlimitms", lowLimitMs);
		wordLimit = getProperty(properties, "vitam.wordlimit", wordLimit);
		rankLimit = getProperty(properties, "vitam.ranklimit", rankLimit);
	}

	public boolean saveConfig() {
		if (xmlFile != null) {
			// based on XML config file
			File config = new File(xmlFile);
			Properties properties = new Properties();
			try {
				setProperties(properties);
				FileOutputStream out = new FileOutputStream(config);
				properties.storeToXML(out, "Vitam Tools configuration", StaticValues.CURRENT_OUTPUT_ENCODING);
				return true;
			} catch (FileNotFoundException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
		}
		return false;
	}
	public void initialize(String xmlFile) {
		boolean configured = false;
		if (xmlFile != null) {
			this.xmlFile = xmlFile;
			// based on XML config file
			File config = new File(xmlFile);
			if (config.canRead()) {
				Properties properties = new Properties();
				FileInputStream in;
				try {
					in = new FileInputStream(config);
					properties.loadFromXML(in);
					in.close();
					
					updateProperties(properties);
					
					FileOutputStream out = new FileOutputStream(config);
					properties.storeToXML(out, "Vitam Tools configuration", StaticValues.CURRENT_OUTPUT_ENCODING);
					configured = true;
				} catch (FileNotFoundException e) {
				} catch (IOException e) {
				}
			}
		}
		if (!configured) {
			// based on environment setup
			useSaxonToGetRootFromXSD = SystemPropertyUtil.getAndSetInt("vitam.usesaxonxsdroot", 1) == 1;
			DEFAULT_PARSER_NAME = SystemPropertyUtil.getAndSet(StaticValues.SAX_DRIVER,
					DEFAULT_PARSER_NAME);
			DEFAULT_TRANSFORMER_NAME = SystemPropertyUtil.getAndSet(
					StaticValues.TRANSFORMER_FACTORY,
					DEFAULT_TRANSFORMER_NAME);
			CURRENT_XSD_ROOT = SystemPropertyUtil.getAndSet("vitam.root", CURRENT_XSD_ROOT);
			DEFAULT_LOCATION = SystemPropertyUtil.getAndSet("vitam.location", DEFAULT_LOCATION);
			DOCUMENT_FIELD = SystemPropertyUtil.getAndSet("vitam.docfield", DOCUMENT_FIELD);
			ATTACHMENT_FIELD = SystemPropertyUtil.getAndSet("vitam.filefield", ATTACHMENT_FIELD);
			FILENAME_ATTRIBUTE = SystemPropertyUtil.getAndSet("vitam.fileattrib",
					FILENAME_ATTRIBUTE);
			INTEGRITY_FIELD = SystemPropertyUtil.getAndSet("vitam.digestfield", INTEGRITY_FIELD);
			ALGORITHME_ATTRIBUTE = SystemPropertyUtil.getAndSet("vitam.algoattrib",
					ALGORITHME_ATTRIBUTE);
			MIMECODE_ATTRIBUTE = SystemPropertyUtil.getAndSet("vitam.mimeattrib",
					MIMECODE_ATTRIBUTE);
			FORMAT_ATTRIBUTE = SystemPropertyUtil.getAndSet("vitam.formatattrib", FORMAT_ATTRIBUTE);
			SIGNATURE_FILE = SystemPropertyUtil.getAndSet("vitam.signature", SIGNATURE_FILE);
			CONTAINER_SIGNATURE_FILE = SystemPropertyUtil.getAndSet("vitam.container",
					CONTAINER_SIGNATURE_FILE);
			argument.archive = SystemPropertyUtil.getAndSetInt("vitam.checkarchives", 0) == 1;
			argument.recursive = SystemPropertyUtil.getAndSetInt("vitam.checkrecursive", 0) == 1;
			argument.sha1 = SystemPropertyUtil.getAndSetInt("vitam.sha1", 1) == 1;
			argument.sha256 = SystemPropertyUtil.getAndSetInt("vitam.sha256", 0) == 1;
			argument.sha512 = SystemPropertyUtil.getAndSetInt("vitam.sha512", 0) == 1;
			argument.extractKeyword = SystemPropertyUtil.getAndSetInt("vitam.extractkeyword", 1) == 1;
			int value = SystemPropertyUtil.getAndSetInt("vitam.output",
					VitamOutputModel.OneXML.ordinal());
			if (value < VitamOutputModel.values().length) {
				argument.outputModel = VitamOutputModel.values()[value];
			} else {
				argument.outputModel = VitamOutputModel.OneXML;
			}
			guiProposeFileSaving = SystemPropertyUtil.getAndSetInt("vitam.guisave", 0) == 1;
			preventXfmt = SystemPropertyUtil.getAndSetInt("vitam.preventxfmt", 1) == 1;
			FITS_HOME = SystemPropertyUtil.getAndSet("vitam.fits", FITS_HOME);
			LIBREOFFICE_HOME = SystemPropertyUtil.getAndSet("vitam.libreoffice", LIBREOFFICE_HOME);
			UNOCONV = SystemPropertyUtil.getAndSet("vitam.unoconv", UNOCONV);
			msPerKB = SystemPropertyUtil.getAndSetInt("vitam.msperkb", 300);
			lowLimitMs = SystemPropertyUtil.getAndSetInt("vitam.lowlimitms", 60000);
			wordLimit = SystemPropertyUtil.getAndSetInt("vitam.wordlimit", 100);
			rankLimit = SystemPropertyUtil.getAndSetInt("vitam.ranklimit", 1);

			saveConfig();
		}
		String val = StaticValues.resourceToFile(StaticValues.DEFAULT_SCHEMATRON);
		File file = new File(val);
		BASE_RESOURCES = file.getParentFile().getParentFile().getParent();
		Logger.getRootLogger().setLevel(Level.OFF);
	}

	/**
	 * Droid initialization (if necessary)
	 * 
	 * @throws CommandExecutionException
	 */
	public void initDroid() throws CommandExecutionException {
		if (droidHandler == null) {
			String signatureFile = StaticValues.resourceToFile(SIGNATURE_FILE);
			String containerSignatureFile = StaticValues.resourceToFile(CONTAINER_SIGNATURE_FILE);

			// Init Signature
			DroidHandler.initialize(signatureFile, containerSignatureFile, -1);

			// Prepare command
			droidHandler = new DroidHandler();
		}
	}

	public void initFits() throws CommandExecutionException {
		if (fits == null) {
			try {
				fits = new Fits(FITS_HOME);
				exif = new Exiftool();
				jhove = new Jhove();
			} catch (FitsConfigurationException e) {
				throw new CommandExecutionException("Cannot initialize Fits", e);
			} catch (FitsException e) {
				throw new CommandExecutionException("Cannot initialize sub component of Fits", e);
			}
		}
	}

	/**
	 * 
	 */
	public ConfigLoader(String configfile) {
		initialize(configfile);
	}

}
