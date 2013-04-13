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
import java.net.URL;
import java.util.Locale;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dom4j.io.OutputFormat;

/**
 * Static values used by all classes
 * 
 * @author "Frederic Bregier"
 * 
 */
public class StaticValues {
	
	public static final String PREFIX_TEMPFILE = "vitam_tmp_";
	
	public static final String CONFIG_VITAM = "vitam-config.xml";
	public static final String FORMAT_XFMT = "x-fmt/";
	public static final String UTF_8 = "UTF-8";
	public static final String ISO_8859_15 = "ISO-8859-15";
	public static final String windows_1252 = "windows-1252";
	public static final String CURRENT_OUTPUT_ENCODING = UTF_8;
	public static OutputFormat defaultOutputFormat;
	
	public static ConfigLoader config;
	public static PreferencesResourceBundle LABELS;
	// Parsers
	public static final String SAX_DRIVER = "org.xml.sax.driver";
	public static final String TRANSFORMER_FACTORY = "javax.xml.transform.TransformerFactory";
	public static final String SAXON_TRANSFORMER_NAME = "net.sf.saxon.TransformerFactoryImpl";
	public static final String XALAN_TRANSFORMER_NAME = "org.apache.xalan.processor.TransformerFactoryImpl";
	// Xsd
	public static final String DEFAULT_XSD = "/resources/seda/v10/seda_v1-0_archivetransfer.xsd";
	// Schematron
	public static final String DEFAULT_SCHEMATRON = "/resources/seda/seda_v10.sch";
	// Xsl
	public static final String DEFAULT_XSL = "/resources/seda/seda2ead-1.0.xsl";

	// Fixed element
	public static final String RESOURCES_LICENSE_TXT = "/resources/LICENSE.txt";
	public static final String RESOURCES_SCHEMATRON_SHOW_ERRORS_XSL = "/resources/schematron/show_errors.xsl";
	public static final String RESOURCES_SCHEMATRON_ISO_SVRL_FOR_XSLT1_XSL = "/resources/schematron/iso_svrl_for_xslt1.xsl";
	public static final String RESOURCES_SCHEMATRON_ISO_SVRL_FOR_XSLT2_XSL = "/resources/schematron/iso_svrl_for_xslt2.xsl";
	public static final String RESOURCES_SCHEMATRON_ISO_ABSTRACT_EXPAND_XSL = "/resources/schematron/iso_abstract_expand.xsl";
	public static final String RESOURCES_SCHEMATRON_ISO_DSDL_INCLUDE_XSL = "/resources/schematron/iso_dsdl_include.xsl";
	public static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";
	public static final String EXTERNAL_SCHEMALOCATION = "http://apache.org/xml/properties/schema/external-schemaLocation";
	public static final String HTTP_XML_ORG_SAX_FEATURES_VALIDATION = "http://xml.org/sax/features/validation";
	public static final String XSD2SCHEMA = "/resources/schematron/xsd2schema.xsl";
	public static final String DROID2XSD = "/resources/seda/DROID2SEDA_filetype.xsl";
	public static final String XML_SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";
	public static final String XML_SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";
	public static final String XML_SHA512 = "http://www.w3.org/2001/04/xmlenc#sha512";

	public static final String ABOUT = "Copyright (c) 2012 Ministere de la Culture et de la Communication\n"
			+ "Sous-Direction du Systeme d'Information\nProjet Vitam\n\nVersion: " + Version.ID
			+ "\n\nContributeurs: Frederic Bregier\n\n"
			+ "Site web: http://www.archivesnationales.culture.gouv.fr/\n\n"
			+ "Licence: GPLV3\n";
	public static String HELP_COMMAND;

	public static enum LBL {
		appName, option_langue, menu_file, menu_edit, menu_tools, menu_help,
		file_quit, 
		edit_copy, edit_clear,
		tools_list, tools_dir_format_test, tools_dir_format_output, tools_dir, tools_pdfa,
		tools_dir_digest, tools_dirfile, tools_file, tools_digest,
		help_about, help_config,
		label_about,
		action_nofile, action_wellformat, 
		action_attachment, action_digest, action_format,
		action_outformat, action_fin, action_pending, action_pdfa,
		error_error, error_warning, error_alerte,
		error_parser, error_walk, error_location, error_function,
		error_filenotfound, error_filenotfile, error_fileaccess,
		error_attachment, error_digest, error_format,
		error_droid, error_compare, error_unknownalgo, error_allowedalgo,
		error_rootxml, error_notequal, error_notenough,
		error_wrongformat, error_wrongoutput,
		error_initdroid, error_initfits, error_writer, error_analysis,
		error_fromextension, error_notfound, error_computedigest, error_pdfa,
		button_save, button_cancel, button_exit, button_update, config_attentionxsd,
		config_keywords, config_keywordsrk, config_keywordsnb, config_limitlow, config_limitkb;

		public String label;

		private LBL() {
			label = this.name().replaceFirst("_", ".");
		}

		public String get() {
			return LABELS.get(this.label);
		}
	}

	public final static void initialize() {
		StaticValues.LABELS = new PreferencesResourceBundle(Locale.getDefault());
		config = new ConfigLoader(CONFIG_VITAM);
		defaultOutputFormat = OutputFormat.createPrettyPrint();
		defaultOutputFormat.setEncoding(CURRENT_OUTPUT_ENCODING);
		if (LBL.option_langue.get().equalsIgnoreCase("fr")) {
			HELP_COMMAND = "Necessite au moins \"--print fichier\" ou \"--convertpdfa source destination\" ou \"--checkdigest fichier\" ou \"--createdigest 5-args\" comme argument\n"
					+
					"\tObligatoirement un parmis (-p,--print fichier | -3,--convertpdfa source destination | -4,--checkdigest fichier | -5,--createdigest 5-args)\n" +
					"\tou -5,--createdigest source cible (-notar | fichiertar) (-noglobal | globaldir/prefix) (-noperfile | -perfile) [5 arguments obligatoires]\n" +
					"\t[-r,--root racine du xsd (defaut="
					+ config.CURRENT_XSD_ROOT
					+ ")]\n"
					+
					"\t[-ff,--filefield champ (defaut="
					+ config.ATTACHMENT_FIELD
					+ ")]\n"
					+
					"\t[-fa,--fileattrib attribut (defaut="
					+ config.FILENAME_ATTRIBUTE
					+ ")]\n"
					+
					"\t[-ma,--mimeattrib attribut (defaut="
					+ config.MIMECODE_ATTRIBUTE
					+ ")]\n"
					+
					"\t[-ta,--formatattrib attribut (defaut="
					+ config.FORMAT_ATTRIBUTE
					+ ")]\n"
					+
					"\t[-df,--digestfield champ (defaut="
					+ config.INTEGRITY_FIELD
					+ ")]\n"
					+
					"\t[-aa,--algoattrib attribut (defaut="
					+ config.ALGORITHME_ATTRIBUTE
					+ ")]\n"
					+
					"\t[-n,--signature fichier (defaut="
					+ config.SIGNATURE_FILE
					+ ")]\n"
					+
					"\t[-c,--container fichier (defaut="
					+ config.CONTAINER_SIGNATURE_FILE
					+ ")]\n"
					+
					"\t[-4,--checkdigest fichier (defaut=faux)]\n"
					+
					"\t[-f,--checkformat (defaut=faux)]\n"
					+
					"\t[-a,--checkarchives (defaut=" + config.argument.archive + ") | -na,--notcheckarchives]\n"
					+
					"\t[-v,--checkrecursive (defaut=" + config.argument.recursive + ") | -nv,--notcheckrecusrive]\n"
					+
					"\t[-h,--computesha algo (où algo=SHA-1,SHA-256,SHA-512 ou un sous-ensemble, defaut=" + config.argument.sha1 + ":" + config.argument.sha256 + ":" + config.argument.sha512 + ") | -nh,--notcomputesha]\n"
					+
					"\t[-e,--extensionrecur filtre_liste_a_virgule (defaut=pas de filtre sur l'extension)]\n"
					+
					"\t[-o,--showformat (defaut=faux)]\n" +
					"\t[-x,--formatoutput format (in TXT|XML|XMLS, defaut=XML)]\n" +
					"\t[-1,--outputfile fichier (defaut=STDOUT)]\n" +
					"\t[-z,--preventxfmt (defaut=" + config.preventXfmt + ") | -nz,--notpreventxfmt]\n" +
					"\t[-3,--convertpdfa source destination (defaut=faux)]\n" +
					"\t[-k,--msperkb msParKo (defaut=" + config.msPerKB + ")]\n" +
					"\t[-t,--lowlimitms limiteBasseEnMs (default=" + config.lowLimitMs + ")]\n" +
					"\t[-y,--extractkeyword (defaut=" + config.argument.extractKeyword + ") | -ny,--notextractkeyword]\n" +
					"\t[-i,--ranklimit limitRankOccur (default=" + config.rankLimit + ")]\n" +
					"\t[-w,--wordlimit limitWordOccur (default=" + config.wordLimit + ")]\n" +
					"\t[--help] pour imprimer cette aide\n" +
					"\t[-0,--config configurationFile (defaut=vitam-config.xml)]";
		} else {
			HELP_COMMAND = "Need at least \"--print filename\" or \"--convertpdfa from to\" or \"--checkdigest filename\" or \"--createdigest 5-args\" as argument\n" +
					"\tMandatory one of (-p,--print filename | -3,--convertpdfa from to | -4,--checkdigest filename | -5,--createdigest 5-args)\n" +
					"\twhere -5,--createdigest source target (-notar | tarfile) (-noglobal | globaldir/prefix) (-noperfile | -perfile) [5 mandatory arguments]\n"+
					"\t[-r,--root root of xsd (default="
					+ config.CURRENT_XSD_ROOT
					+ ")]\n"
					+
					"\t[-ff,--filefield field (default="
					+ config.ATTACHMENT_FIELD
					+ ")]\n"
					+
					"\t[-fa,--fileattrib attribute (default="
					+ config.FILENAME_ATTRIBUTE
					+ ")]\n"
					+
					"\t[-ma,--mimeattrib attribute (default="
					+ config.MIMECODE_ATTRIBUTE
					+ ")]\n"
					+
					"\t[-ta,--formatattrib attribute (default="
					+ config.FORMAT_ATTRIBUTE
					+ ")]\n"
					+
					"\t[-df,--digestfield field (default="
					+ config.INTEGRITY_FIELD
					+ ")]\n"
					+
					"\t[-aa,--algoattrib attribute (default="
					+ config.ALGORITHME_ATTRIBUTE
					+ ")]\n"
					+
					"\t[-n,--signature filename (default="
					+ config.SIGNATURE_FILE
					+ ")]\n"
					+
					"\t[-c,--container filename (default="
					+ config.CONTAINER_SIGNATURE_FILE
					+ ")]\n"
					+
					"\t[-4,--checkdigest filename (default=false)]\n"
					+
					"\t[-f,--checkformat (default=false)]\n"
					+
					"\t[-a,--checkarchives (default=" + config.argument.archive + ") | -na,--notcheckarchives]\n"
					+
					"\t[-v,--checkrecursive (default=" + config.argument.recursive + ") | -nv,--notcheckrecusrive]\n"
					+
					"\t[-h,--computesha algo (where algo=SHA-1,SHA-256,SHA-512 or subset default=" + config.argument.sha1 + ":" + config.argument.sha256 + ":" + config.argument.sha512 + ") | -nh,--notcomputesha]\n"
					+
					"\t[-e,--extensionrecur filter_in_comma_separated_list (default=no extension filter)]\n"
					+
					"\t[-o,--showformat (default=false)]\n" +
					"\t[-x,--formatoutput format (in TXT|XML|XMLS, default=XML)]\n" +
					"\t[-1,--outputfile filename (default=STDOUT)]\n" +
					"\t[-z,--preventxfmt (default=" + config.preventXfmt + ") | -nz,--notpreventxfmt]\n" +
					"\t[-3,--convertpdfa from to (default=false)]\n" +
					"\t[-k,--msperkb msPerKB (default=" + config.msPerKB + ")]\n" +
					"\t[-t,--lowlimitms lowLimitInMs (default=" + config.lowLimitMs + ")]\n" +
					"\t[-y,--extractkeyword (default=" + config.argument.extractKeyword + ") | -ny,--notextractkeyword]\n" +
					"\t[-i,--ranklimit limitRankOccur (default=" + config.rankLimit + ")]\n" +
					"\t[-w,--wordlimit limitWordOccur (default=" + config.wordLimit + ")]\n" +
					"\t[--help] to print help\n" +
					"\t[-0,--config configurationFile (default=vitam-config.xml)]";
		}
		Logger.getRootLogger().setLevel(Level.OFF);
	}

	public final static String resourceToFile(String arg) {
		File test = new File(arg);
		if (test.exists()) {
			return test.getAbsolutePath();
		}
		URL url = arg.getClass().getResource(arg);
		if (url == null) {
			return null;
		}
		String value = url.toString();
		value = value.substring(6).replaceAll("%20", "\\ ");
		return new File(value).getAbsolutePath();
	}

	public final static String resourceToParent(String arg) {
		return new File(arg.getClass().getResource(arg).toString()).getParentFile().toURI()
				.getPath();
	}

	public final static String resourceToURL(String arg) {
		return arg.getClass().getResource(arg).getFile();
	}


	/**
	 * Utility to free memory
	 */
	public final static void freeMemory() {
		long total = Runtime.getRuntime().totalMemory();
		for (int i = 0; i < 10; i++) {
			System.gc();
		}
		while (true) {
			System.gc();
			long newtotal = Runtime.getRuntime().totalMemory();
			if ((((double) (total - newtotal)) / (double) total) < 0.1) {
				break;
			} else {
				total = newtotal;
			}
		}
		System.gc();
	}

	/**
	 * 
	 * @param fullPath
	 * @param fromPath
	 * @return the sub path starting from fromPath (inclusive last dir) of fullPath
	 */
	public static final String getSubPath(File fullPath, File fromPath) {
		String spath = fullPath.getAbsolutePath();
		String sparent = fromPath.getAbsolutePath() + File.separator;
		return spath.replace(sparent, "");
	}
}
