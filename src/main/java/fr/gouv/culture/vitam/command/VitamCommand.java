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
package fr.gouv.culture.vitam.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;

import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;

import fr.gouv.culture.vitam.digest.DigestCompute;
import fr.gouv.culture.vitam.droid.DroidFileFormat;
import fr.gouv.culture.vitam.droid.DroidHandler;
import fr.gouv.culture.vitam.eml.EmlExtract;
import fr.gouv.culture.vitam.pdfa.PdfaConverter;
import fr.gouv.culture.vitam.utils.Commands;
import fr.gouv.culture.vitam.utils.ConfigLoader;
import fr.gouv.culture.vitam.utils.StaticValues;
import fr.gouv.culture.vitam.utils.VitamArgument;
import fr.gouv.culture.vitam.utils.VitamArgument.VitamOutputModel;
import fr.gouv.culture.vitam.utils.VitamResult;
import fr.gouv.culture.vitam.utils.XmlDom;

/**
 * Command Line interface main class<br>
 * <br>
 * Need at least one of the following arguments:<br>
 * Mandatory one of (-p,--print filename | -3,--convertpdfa from to | -4,--checkdigest filename | -5,--createdigest ...)<br>
 * where -5,--createdigest source target (-notar | tarfile) (-noglobal | globaldir/prefix) (-noperfile | -perfile) [5 mandatory arguments]<br> 
 * [-r,--root root of xsd (default=CONFIG)]<br>
 * [-ff,--filefield field (default=CONFIG)]<br>
 * [-fa,--fileattrib attribut (default=CONFIG)]<br>
 * [-ma,--mimeattrib attribut (default=CONFIG)]<br>
 * [-ta,--formatattrib attribut (default=CONFIG)]<br>
 * [-df,--digestfield field (default=CONFIG)]<br>
 * [-aa,--algoattrib attribut (default=CONFIG)]<br>
 * [-n,--signature filename (default=CONFIG)]<br>
 * [-c,--container filename (default=CONFIG)]<br>
 * [-4,--checkdigest filename (default=false)]<br>
 * [-f,--checkformat (default=false)]<br>
 * [-a,--checkarchives (default=CONFIG) | -na,--notcheckarchives]<br>
 * [-v,--checkrecursive (default=CONFIG) | -nv,--notcheckrecursive]<br>
 * [-h,--computesha algo (where algo=SHA-1,SHA-256,SHA-512 or subset default=CONFIG) | -nh,--notcomputesha]<br>
 * [-e,--extensionrecur filter_in_comma_separated_list (default=no extension filter)]<br>
 * [-o,--showformat (default=false)]<br>
 * [-x,--formatoutput format (in TXT|XML|XMLS, default=CONFIG)]<br>
 * [-1,--outputfile filename (default=STDOUT)]<br>
 * [-z,--preventxfmt (default=CONFIG) | -nz,--notpreventxfmt for not preventing x-fmt]<br>
 * [-3,--convertpdfa from to (default=false)]<br>
 * [-m,--msperkb msPerKB (default=CONFIG)]<br>
 * [-t,--lowlimitms lowLimitInMs (default=CONFIG)]<br>
 * [-y,--extractkeyword (default=CONFIG) | -ny,--notextractkeyword no extraction]<br>
 * [-i,--ranklimit limitRankOccur (default=CONFIG)]<br>
 * [-w,--wordlimit limitWordOccur (default=CONFIG)]<br>
 * [--help] to print help<br>
 * [-0,--config configurationFile (default=vitam-config.xml)]<br>
 * 
 * @author Frederic Bregier
 * 
 */
public class VitamCommand {
	public static String FILEarg;
	public static String XSDroot;
	public static String ATTCHfield;
	public static String FILEattr;
	public static String FORMATattr;
	public static String MIMEattr;
	public static String IDENTfield;
	public static String ALGOattr;
	public static boolean checkFormat = false;
	public static String checkDigest = null;
	public static boolean showFormat = false;
	public static boolean useSchematron = true;
	public static boolean useXsl = true;
	public static boolean useXsd = true;
	public static String Sig;
	public static String Contain;
	public static String[] extensions;
	public static String outputformat = null;
	public static String outputfile = null;
	public static String xslarchive = null;
	public static String fromPdfA = null;
	public static String toPdfA = null;
	public static String digSource = null;
	public static String digTarget = null;
	public static String digGlobal = null;
	public static String digTar = null;
	public static boolean digPerFile = false;
	public static PrintStream outputStream = System.out;

	/**
	 * Enum only to check no double options setup
	 * @author "Frederic Bregier"
	 *
	 */
	static enum VerifOptions {
		p,print,
		createdigest,
		r,root,
		ff,filefield,
		fa,fileattrib,
		ma,mimeattrib,
		ta,formatattrib,
		df,digestfield,
		aa,algoattrib,
		n,signature,
		c,container,
		checkdigest,
		f,checkformat,
		a,checkarchives,na,notcheckarchives,
		v,checkrecursive,nv,notcheckrecursive,
		h,computesha,nh,notcomputesha,
		e,extensionrecur,
		o,showformat,
		x,formatoutput,
		outputfile,
		z,preventxfmt,nz,notpreventxmft,
		convertpdfa,
		k,msperkb,
		t,lowlimitms,
		y,extractkeyword,ny,notextractkeyword,
		i,ranklimit,
		w,wordlimit,
		help,
		config
	}
	
	public static void printHelp(ConfigLoader config) {
		System.err.println(StaticValues.HELP_COMMAND);
		System.err.println("\n" + StaticValues.ABOUT);
	}

	/**
	 * Check args and construct the options
	 * 
	 * @param args
	 * @param config
	 * @return True if correctly initiated
	 */
	public static boolean checkArgs(String[] args, ConfigLoader config) {
		if (args.length == 0) {
			return false;
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-0") || args[i].equalsIgnoreCase("--config")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				// load configuration again
				File configFile = new File(args[i]);
				if (! configFile.canRead()) {
					System.err.println(StaticValues.LBL.error_filenotfile.get() + args[i] + "/" + args[i-1]);
					return false;
				}
				StaticValues.config = new ConfigLoader(configFile.getAbsolutePath());
			} else if (args[i].equalsIgnoreCase("--help")) {
				return false;
			}
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-r") || args[i].equalsIgnoreCase("--root")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				XSDroot = args[i];
			} else if (args[i].equalsIgnoreCase("-ff") || args[i].equalsIgnoreCase("--filefield")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				ATTCHfield = args[i];
			} else if (args[i].equalsIgnoreCase("-fa") || args[i].equalsIgnoreCase("--fileattrib")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				FILEattr = args[i];
			} else if (args[i].equalsIgnoreCase("-ma") || args[i].equalsIgnoreCase("--mimeattrib")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				MIMEattr = args[i];
			} else if (args[i].equalsIgnoreCase("-ta")
					|| args[i].equalsIgnoreCase("--formatattrib")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				FORMATattr = args[i];
			} else if (args[i].equalsIgnoreCase("-df") || args[i].equalsIgnoreCase("--digestfield")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				IDENTfield = args[i];
			} else if (args[i].equalsIgnoreCase("-aa") || args[i].equalsIgnoreCase("--algoattrib")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				ALGOattr = args[i];
			} else if (args[i].equalsIgnoreCase("-p") || args[i].equalsIgnoreCase("--print")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				FILEarg = args[i].replace('/', File.separatorChar)
						.replace('\\', File.separatorChar);
			} else if (args[i].equalsIgnoreCase("-n") || args[i].equalsIgnoreCase("--signature")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				config.SIGNATURE_FILE = args[i];
			} else if (args[i].equalsIgnoreCase("-c") || args[i].equalsIgnoreCase("--container")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				config.CONTAINER_SIGNATURE_FILE = args[i];
			} else if (args[i].equalsIgnoreCase("-f") || args[i].equalsIgnoreCase("--checkformat")) {
				checkFormat = true;
			} else if (args[i].equalsIgnoreCase("-o") || args[i].equalsIgnoreCase("--showformat")) {
				showFormat = true;
			} else if (args[i].equalsIgnoreCase("-a")
					|| args[i].equalsIgnoreCase("--checkarchives")) {
				config.argument.archive = true;
			} else if (args[i].equalsIgnoreCase("-v")
					|| args[i].equalsIgnoreCase("--checkrecursive")) {
				config.argument.recursive = true;
			} else if (args[i].equalsIgnoreCase("-z")
					|| args[i].equalsIgnoreCase("--preventxfmt")) {
				config.preventXfmt = true;
			} else if (args[i].equalsIgnoreCase("-y")
					|| args[i].equalsIgnoreCase("--extractkeyword")) {
				config.argument.extractKeyword = true;
			} else if (args[i].equalsIgnoreCase("-e")
					|| args[i].equalsIgnoreCase("--extensionrecur")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				extensions = args[i].split(",");
			} else if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("--computesha")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				String[] temp = args[i].split(",");
				for (String string : temp) {
					if (string.equalsIgnoreCase("sha-1"))
						config.argument.sha1 = true;
					if (string.equalsIgnoreCase("sha-256"))
						config.argument.sha256 = true;
					if (string.equalsIgnoreCase("sha-512"))
						config.argument.sha512 = true;
				}
			} else if (args[i].equalsIgnoreCase("-x") || args[i].equalsIgnoreCase("--formatoutput")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				outputformat = args[i];
			} else if (args[i].equalsIgnoreCase("-1") || args[i].equalsIgnoreCase("--outputfile")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				outputfile = args[i];
			} else if (args[i].equalsIgnoreCase("-3") || args[i].equalsIgnoreCase("--convertpdfa")) {
				i++;
				if (i+1 >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				fromPdfA = args[i];
				i++;
				toPdfA = args[i];
			} else if (args[i].equalsIgnoreCase("-4") || args[i].equalsIgnoreCase("--checkdigest")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				checkDigest = args[i];
			} else if (args[i].equalsIgnoreCase("-k")
					|| args[i].equalsIgnoreCase("--msperkb")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				try {
					long value = Long.parseLong(args[i]);
					config.msPerKB = value;
				} catch (NumberFormatException e) {
					System.err.println(StaticValues.LBL.error_error.get() + args[i - 1] +
							" : " + e.toString());
					return false;
				}
			} else if (args[i].equalsIgnoreCase("-i") || args[i].equalsIgnoreCase("--ranklimit")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				try {
					config.rankLimit = Integer.parseInt(args[i]);
				} catch (NumberFormatException e) {
					System.err.println(StaticValues.LBL.error_error.get() + args[i - 1] +
							" : " + e.toString());
					return false;
				}
			} else if (args[i].equalsIgnoreCase("-w") || args[i].equalsIgnoreCase("--wordlimit")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				try {
					config.wordLimit = Integer.parseInt(args[i]);
				} catch (NumberFormatException e) {
					System.err.println(StaticValues.LBL.error_error.get() + args[i - 1] +
							" : " + e.toString());
					return false;
				}
			} else if (args[i].equalsIgnoreCase("-t") || args[i].equalsIgnoreCase("--lowlimitms")) {
				i++;
				if (i >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				try {
					config.lowLimitMs = Integer.parseInt(args[i]);
				} catch (NumberFormatException e) {
					System.err.println(StaticValues.LBL.error_error.get() + args[i - 1] +
							" : " + e.toString());
					return false;
				}
			} else if (args[i].equalsIgnoreCase("-5") || args[i].equalsIgnoreCase("--createdigest")) {
				i++;
				if (i+4 >= args.length) {
					System.err.println(StaticValues.LBL.error_notenough.get() + args[i - 1]);
					return false;
				}
				digSource = args[i];
				i++;
				digTarget = args[i];
				i++;
				digTar = args[i];
				if (digTar.equalsIgnoreCase("--notar")) {
					digTar = null;
				}
				i++;
				digGlobal = args[i];
				if (digGlobal.equalsIgnoreCase("--noglobal")) {
					digGlobal = null;
				}
				i++;
				digPerFile = args[i].equalsIgnoreCase("--perfile");
				// From here negation of options
			} else if (args[i].equalsIgnoreCase("-ny") || args[i].equalsIgnoreCase("--notextractkeyword")) {
				config.argument.extractKeyword = false;
			} else if (args[i].equalsIgnoreCase("-na") || args[i].equalsIgnoreCase("--notcheckarchives")) {
				config.argument.archive = false;
			} else if (args[i].equalsIgnoreCase("-nv") || args[i].equalsIgnoreCase("--notcheckrecursive")) {
				config.argument.recursive = false;
			} else if (args[i].equalsIgnoreCase("-nz") || args[i].equalsIgnoreCase("--notpreventxfmt")) {
				config.preventXfmt = false;
			} else if (args[i].equalsIgnoreCase("-nh") || args[i].equalsIgnoreCase("--notcomputesha")) {
				config.argument.sha1 = false;
				config.argument.sha256 = false;
				config.argument.sha512 = false;
			}
		}
		if (FILEarg == null && fromPdfA == null && checkDigest == null && digSource == null) {
			return false;
		}
		if (XSDroot != null) {
			config.CURRENT_XSD_ROOT = XSDroot;
		}
		if (ATTCHfield != null) {
			config.ATTACHMENT_FIELD = ATTCHfield;
		}
		if (FILEattr != null) {
			config.FILENAME_ATTRIBUTE = FILEattr;
		}
		if (MIMEattr != null) {
			config.MIMECODE_ATTRIBUTE = MIMEattr;
		}
		if (FORMATattr != null) {
			config.FORMAT_ATTRIBUTE = FORMATattr;
		}
		if (IDENTfield != null) {
			config.INTEGRITY_FIELD = IDENTfield;
		}
		if (ALGOattr != null) {
			config.ALGORITHME_ATTRIBUTE = ALGOattr;
		}
		if (outputfile != null) {
			File file = new File(outputfile);
			try {
				outputStream = new PrintStream(file);
			} catch (FileNotFoundException e) {
				System.err.println(StaticValues.LBL.error_wrongoutput.get());
				outputStream = System.out;
			}
		}
		if (outputformat != null) {
			if (outputformat.equalsIgnoreCase("txt")) {
				config.argument.outputModel = VitamOutputModel.TXT;
			} else if (outputformat.equalsIgnoreCase("xml")) {
				config.argument.outputModel = VitamOutputModel.OneXML;
			} else if (outputformat.equalsIgnoreCase("xmls")) {
				config.argument.outputModel = VitamOutputModel.MultipleXML;
			} else {
				System.err.println(StaticValues.LBL.error_wrongformat.get());
			}
		}
		return true;
	}

	/**
	 * Main command line method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		StaticValues.initialize();
		if (!checkArgs(args, StaticValues.config)) {
			printHelp(StaticValues.config);
			System.exit(1);
		}
		try {
			StaticValues.config.initDroid();
		} catch (CommandExecutionException e) {
			System.err.println(StaticValues.LBL.error_initdroid.get() + e);
		}

		try {
			StaticValues.config.initFits();
		} catch (CommandExecutionException e) {
			System.err.println(StaticValues.LBL.error_initfits.get() + e);
		}

		if (FILEarg != null) {
			checkFilesType();
		}
		if (fromPdfA != null) {
			convertPdfa();
		}
		if (checkDigest != null) {
			computeDigest();
		}
		if (outputfile != null) {
			outputStream.flush();
			outputStream.close();
		}
		if (digSource != null) {
			createDigest();
		}
		System.out.println("\n" + StaticValues.LBL.action_fin.get());
	}
	
	public static void createDigest() {
		if (digSource == null || digTarget == null) {
			System.err.println(
					"Source & Destination invalides");
			return;
		}
		File src = new File(digSource);
		File dst = new File(digTarget);
		if (!src.exists() || !dst.exists()) {
			System.err.println(
					"Source & Destination invalides");
			return;
		}
		boolean oneDigestPerFile = digPerFile;
		File fglobal = null;
		if (digGlobal != null) {
			fglobal = new File(digGlobal).getParentFile();
			if (!fglobal.exists()) {
				System.err.println(
						"Global invalide");
				fglobal = null;
			} else {
				File fout = new File(digGlobal + "_all_digests.xml");
				fglobal = fout;
			}
		}
		File ftar = null;
		if (digTar != null) {
			ftar = new File(digTar);
			if (!ftar.exists()) {
				System.err.println(
						"TAR/ZIP invalide");
				ftar = null;
			}
		}
		int currank = DigestCompute.createDigest(src, dst, ftar, fglobal, oneDigestPerFile, extensions);
		if (currank > 0) {
			System.out
					.println(StaticValues.LBL.action_digest.get() +
							" [ " + currank  + " ]");
		}
	}
	
	public static void computeDigest() {
		XMLWriter writer = null;
		try {
			writer = new XMLWriter(outputStream, StaticValues.defaultOutputFormat);
		} catch (UnsupportedEncodingException e1) {
			System.err.println(StaticValues.LBL.error_writer.get() + ": " + e1.toString());
			return;
		}
		File basedir = new File(checkDigest);
		List<File> files;
		try {
			files = DroidHandler.matchedFiled(new File[] { basedir },
					extensions,
					StaticValues.config.argument.recursive);
		} catch (CommandExecutionException e1) {
			System.err.println(StaticValues.LBL.error_error.get() + e1.toString());
			return;
		}
		System.out.println("Digest...");
		Element root = null;
		VitamResult vitamResult = new VitamResult();
		if (basedir.isFile()) {
			basedir = basedir.getParentFile();
		}
		if (StaticValues.config.argument.outputModel == VitamOutputModel.OneXML) {
			root = XmlDom.factory.createElement("digests");
			root.addAttribute("source", basedir.getAbsolutePath());
			vitamResult.unique = XmlDom.factory.createDocument(root);
		}
		int currank = 0;
		int error = 0;
		for (File file : files) {
			currank++;
			String shortname;
			shortname = StaticValues.getSubPath(file, basedir);
			FileInputStream inputstream;
			try {
				inputstream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				System.err.println(StaticValues.LBL.error_computedigest.get() + ": " + shortname);
				continue;
			}
			String []shas = DigestCompute.computeDigest(inputstream, StaticValues.config.argument);
			//SEDA type since already configured
			Element result = XmlDom.factory.createElement(StaticValues.config.DOCUMENT_FIELD);
			Element attachment = XmlDom.factory.createElement(StaticValues.config.ATTACHMENT_FIELD);
			attachment.addAttribute(StaticValues.config.FILENAME_ATTRIBUTE.substring(1), shortname);
			result.add(attachment);
			if (shas[0] != null) {
				Element integrity = XmlDom.factory.createElement(StaticValues.config.INTEGRITY_FIELD);
				integrity.addAttribute(StaticValues.config.ALGORITHME_ATTRIBUTE.substring(1), StaticValues.XML_SHA1);
				integrity.setText(shas[0]);
				result.add(integrity);
			}
			if (shas[1] != null) {
				Element integrity = XmlDom.factory.createElement(StaticValues.config.INTEGRITY_FIELD);
				integrity.addAttribute(StaticValues.config.ALGORITHME_ATTRIBUTE.substring(1), StaticValues.XML_SHA256);
				integrity.setText(shas[1]);
				result.add(integrity);
			}
			if (shas[2] != null) {
				Element integrity = XmlDom.factory.createElement(StaticValues.config.INTEGRITY_FIELD);
				integrity.addAttribute(StaticValues.config.ALGORITHME_ATTRIBUTE.substring(1), StaticValues.XML_SHA512);
				integrity.setText(shas[2]);
				result.add(integrity);
			}
			if ((shas[0] == null && StaticValues.config.argument.sha1) || 
					(shas[1] == null && StaticValues.config.argument.sha256) ||
					(shas[2] == null && StaticValues.config.argument.sha512)) {
				result.addAttribute("status", "error");
				error ++;
			} else {
				result.addAttribute("status", "ok");
			}
			XmlDom.addDate(StaticValues.config.argument, StaticValues.config, result);
			if (root != null) {
				root.add(result);
			} else {
				// multiple
				root = XmlDom.factory.createElement("digests");
				root.addAttribute("source", basedir.getAbsolutePath());
				root.add(result);
				try {
					writer.write(root);
				} catch (IOException e) {
					System.err.println(StaticValues.LBL.error_error.get() + e.toString());
				}
				root = null;
			}
		}
		if (root != null) {
			if (error == 0) {
				root.addAttribute("status", "ok");
			} else {
				root.addAttribute("status", "error on " + error + " / " + currank + " file checks");
			}
			XmlDom.addDate(StaticValues.config.argument, StaticValues.config, root);
			try {
				writer.write(vitamResult.unique);
			} catch (IOException e) {
				System.err.println(StaticValues.LBL.error_analysis.get() + e);
			}
		}
		System.out
				.println(StaticValues.LBL.action_digest.get() +
						" [ " + currank + (error > 0 ? " (" + StaticValues.LBL.error_error.get() + error + " ) " : "" ) + " ]");
	}

	private static Element addPdfaElement(Element root, Element pdfa, File basedir, File baseoutdir,
			boolean error, String serror, String puid, VitamResult vitamResult) {
		XmlDom.addDate(StaticValues.config.argument, StaticValues.config, pdfa);
		if (puid != null) {
			pdfa.addAttribute("puid", puid);
		}
		if (root != null) {
			root.add(pdfa);
		} else {
			// multiple
			root = XmlDom.factory.createElement("transform");
			root.addAttribute("source", basedir.getAbsolutePath());
			root.addAttribute("target", baseoutdir.getAbsolutePath());
			Document document = XmlDom.factory.createDocument(root);
			root.add(pdfa);
			if (error) {
				root.addAttribute("status", serror);
			} else {
				root.addAttribute("status", "ok");
			}
			vitamResult.multiples.add(document);
		}
		return root;
	}
	
	public static void convertPdfa() {
		XMLWriter writer = null;
		try {
			writer = new XMLWriter(outputStream, StaticValues.defaultOutputFormat);
		} catch (UnsupportedEncodingException e1) {
			System.err.println(StaticValues.LBL.error_writer.get() + ": " + e1.toString());
			return;
		}
		File basedir = new File(fromPdfA);
		List<File> files;
		try {
			files = DroidHandler.matchedFiled(new File[] { basedir },
					extensions,
					StaticValues.config.argument.recursive);
		} catch (CommandExecutionException e1) {
			System.err.println(StaticValues.LBL.error_error.get() + e1.toString());
			return;
		}
		if (basedir.isFile()) {
			basedir = basedir.getParentFile();
		}
		File baseoutdir = new File(toPdfA);
		if (! baseoutdir.exists()) {
			baseoutdir.mkdirs();
		}
		if (baseoutdir.isFile()) {
			baseoutdir = baseoutdir.getParentFile();
		}
		int errorcpt = 0;
		boolean checkDroid = false;
		try {
			StaticValues.config.initDroid();
			checkDroid = true;
		} catch (CommandExecutionException e) {
			System.err.println(StaticValues.LBL.error_initdroid.get() + e.toString());
		}
		System.out.println("\nTransform PDF/A-1B\n");
		Element root = null;
		Element temp = null;
		VitamResult vitamResult = new VitamResult();
		if (StaticValues.config.argument.outputModel == VitamOutputModel.OneXML) {
			root = XmlDom.factory.createElement("transform");
			root.addAttribute("source", basedir.getAbsolutePath());
			root.addAttribute("target", baseoutdir.getAbsolutePath());
			vitamResult.unique = XmlDom.factory.createDocument(root);
		} else {
			// force multiple
			vitamResult.multiples = new ArrayList<Document>();
		}
		for (File file : files) {
			String basename = file.getName();
			File rootdir;
			String subpath = null;
			if (file.getParentFile().equals(basedir)) {
				rootdir = basedir;
				subpath = File.separator;
			} else {
				rootdir = file.getParentFile();
				subpath = rootdir.getAbsolutePath().replace(basedir.getAbsolutePath(), "") +
						File.separator;
			}
			String fullname = subpath + basename;
			String puid = null;
			if (checkDroid) {
				try {
					List<DroidFileFormat> list = 
							StaticValues.config.droidHandler.checkFileFormat(file, 
									StaticValues.config.argument);
					if (list == null || list.isEmpty()) {
						System.err.println("Ignore: " + fullname);
						Element pdfa = XmlDom.factory.createElement("convert");
						Element newElt = XmlDom.factory.createElement("file");
						newElt.addAttribute("filename", fullname);
						pdfa.add(newElt);
						addPdfaElement(root, pdfa, basedir, baseoutdir, 
								true, "Error: filetype not found", null, vitamResult);
						errorcpt ++;
						continue;
					}
					DroidFileFormat type = list.get(0);
					puid = type.getPUID();
					if (puid.startsWith(StaticValues.FORMAT_XFMT) ||
							puid.equals("fmt/411")) { // x-fmt or RAR
						System.err.println("Ignore: " + fullname + " " + puid);
						Element pdfa = XmlDom.factory.createElement("convert");
						Element newElt = XmlDom.factory.createElement("file");
						newElt.addAttribute("filename", fullname);
						pdfa.add(newElt);
						addPdfaElement(root, pdfa, basedir, baseoutdir, 
								true, "Error: filetype not allowed", puid, vitamResult);
						errorcpt ++;
						continue;
					}
				} catch (CommandExecutionException e) {
					// ignore
				}
			}
			System.out.println("PDF/A-1B convertion... " + fullname);
			long start = System.currentTimeMillis();
			Element pdfa = PdfaConverter.convertPdfA(subpath, basename, basedir, baseoutdir, 
					StaticValues.config);
			long end = System.currentTimeMillis();
			boolean error = false;
			if (pdfa.selectSingleNode(".[@status='ok']") == null) {
				error = true;
				errorcpt ++;
			}
			if (error) {
				System.err.println(StaticValues.LBL.error_pdfa.get() +
						" PDF/A-1B KO: " + fullname + 
						" " + ((end-start) * 1024 / file.length()) + " ms/KB " +
						(end-start) + " ms " + "\n");
			} else {
				System.out.println("PDF/A-1B OK: " + fullname + 
						" " + ((end-start) * 1024 / file.length()) + " ms/KB " +
						(end-start) + " ms " +
						"\n");
			}
			temp = addPdfaElement(root, pdfa, basedir, baseoutdir, error, "error", puid, vitamResult);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
		if (root != null) {
			XmlDom.addDate(StaticValues.config.argument, StaticValues.config, root);
			if (errorcpt > 0) {
				root.addAttribute("status", "error found");
			} else {
				root.addAttribute("status", "ok");
			}
			try {
				writer.write(vitamResult.unique);
			} catch (IOException e) {
				System.err.println(StaticValues.LBL.error_analysis.get() + e);
			}
		} else {
			XmlDom.addDate(StaticValues.config.argument, StaticValues.config, temp);
			try {
				writer.write(temp);
			} catch (IOException e) {
			}
		}
		if (errorcpt < files.size()) {
			System.out.println(StaticValues.LBL.action_pdfa.get() +
					" [ " + files.size() +
					(errorcpt > 0 ? " (" + StaticValues.LBL.error_error.get() + errorcpt + " )" : "" ) + " ]");
		} else {
			System.err.println(StaticValues.LBL.error_pdfa.get() +
					" [ " + StaticValues.LBL.error_error.get() + errorcpt + " ]");
		}
	}
	
	public static void checkFilesType() {
		File fic = new File(FILEarg);
		if (!fic.exists()) {
			System.err.println(StaticValues.LBL.error_filenotfile.get() + ": " + FILEarg);
			return;
		} else {
			System.out.println("\n" + StaticValues.LBL.tools_dir_format_output.get() + "\n");
			Document global = null;
			Element root = null;
			XMLWriter writer = null;
			try {
				writer = new XMLWriter(outputStream, StaticValues.defaultOutputFormat);
			} catch (UnsupportedEncodingException e1) {
				System.err.println(StaticValues.LBL.error_writer.get() + ": " + e1.toString());
				return;
			}
			if (StaticValues.config.argument.outputModel == VitamOutputModel.OneXML) {
				root = XmlDom.factory.createElement("checkfiles");
				root.addAttribute("source", FILEarg);
				global = XmlDom.factory.createDocument(root);
				EmlExtract.filEmls.clear();
			}
			if (showFormat) {
				if (StaticValues.config.droidHandler == null &&
						StaticValues.config.exif == null &&
						StaticValues.config.jhove == null) {
					System.err.println(StaticValues.LBL.error_initfits.get());
					return;
				}
				try {
					List<File> files =
							DroidHandler.matchedFiled(new File[] { fic },
									extensions,
									StaticValues.config.argument.recursive);
					for (File file : files) {
						String shortname;
						if (fic.isDirectory()) {
							shortname = StaticValues.getSubPath(file, fic);
						} else {
							shortname = FILEarg;
						}
						Element result = Commands.showFormat(shortname,
								null, null,
								file, StaticValues.config, StaticValues.config.argument);
						XmlDom.addDate(StaticValues.config.argument, StaticValues.config, result);
						if (root != null) {
							root.add(result);
						} else {
							writer.write(result);
							System.out
									.println("\n========================================================");
						}
					}
				} catch (CommandExecutionException e) {
					System.err.println(StaticValues.LBL.error_analysis.get() + e);
					e.printStackTrace();
				} catch (IOException e) {
					System.err.println(StaticValues.LBL.error_analysis.get() + e);
				}
			} else {
				if (StaticValues.config.droidHandler == null) {
					System.err.println(StaticValues.LBL.error_initdroid.get());
					return;
				}
				if (root != null) {
					Element newElt = XmlDom.factory.createElement("toolsversion");
					if (StaticValues.config.droidHandler != null) {
						newElt.addAttribute("pronom", StaticValues.config.droidHandler.getVersionSignature());
					}
					if (StaticValues.config.droidHandler != null) {
						newElt.addAttribute("droid", "6.1");
					}
					root.add(newElt);
				}
				List<DroidFileFormat> list;
				try {
					VitamArgument argument =
							new VitamArgument(StaticValues.config.argument.archive,
									StaticValues.config.argument.recursive, true, true, true,
									StaticValues.config.argument.outputModel,
									StaticValues.config.argument.checkSubFormat,
									StaticValues.config.argument.extractKeyword);
					List<File> files =
							DroidHandler.matchedFiled(new File[] { fic },
									extensions,
									argument.recursive);
					list = StaticValues.config.droidHandler.checkFilesFormat(files,
							argument, null);
					String pathBeforeArg = fic.getCanonicalPath();
					pathBeforeArg = pathBeforeArg.substring(0, pathBeforeArg.indexOf(FILEarg));
					for (DroidFileFormat droidFileFormat : list) {
						Element fileformat = droidFileFormat.toElement(true);
						Attribute filename = fileformat.attribute("filename");
						if (filename != null) {
							String value = filename.getText();
							filename.setText(value.replace(pathBeforeArg, ""));
						}
						XmlDom.addDate(StaticValues.config.argument, StaticValues.config,
								fileformat);
						if (root != null) {
							root.add(fileformat);
						} else {
							writer.write(fileformat);
							System.out
									.println("\n========================================================");
						}
					}
				} catch (CommandExecutionException e) {
					System.err.println(StaticValues.LBL.error_analysis.get() + e);
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					System.err.println(StaticValues.LBL.error_analysis.get() + e);
				} catch (IOException e) {
					System.err.println(StaticValues.LBL.error_analysis.get() + e);
				}
			}
			if (global != null) {
				XmlDom.addDate(StaticValues.config.argument, StaticValues.config, root);
				if (! EmlExtract.filEmls.isEmpty()) {
					Element sortEml = XmlDom.factory.createElement("emlsort");
					for (String parent : EmlExtract.filEmls.keySet()) {
						Element eparent = XmlDom.factory.createElement("parent");
						String fil = EmlExtract.filEmls.get(parent);
						eparent.addAttribute("messageId", parent);
						String []fils = fil.split(",");
						for (String mesg : fils) {
							if (mesg != null && mesg.length() > 1) {
								Element elt = XmlDom.factory.createElement("descendant");
								elt.addAttribute("messageId", mesg);
								eparent.add(elt);
							}
						}
						sortEml.add(eparent);
					}
					root.add(sortEml);
				}
				try {
					writer.write(global);
				} catch (IOException e) {
					System.err.println(StaticValues.LBL.error_analysis.get() + e);
				}
			}
		}
	}

}
