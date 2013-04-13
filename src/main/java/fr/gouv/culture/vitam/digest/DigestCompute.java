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
package fr.gouv.culture.vitam.digest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;

import fr.gouv.culture.vitam.droid.DroidFileFormat;
import fr.gouv.culture.vitam.droid.DroidHandler;
import fr.gouv.culture.vitam.utils.ConfigLoader;
import fr.gouv.culture.vitam.utils.StaticValues;
import fr.gouv.culture.vitam.utils.VitamArgument;
import fr.gouv.culture.vitam.utils.XmlDom;


/**
 * Digest compute
 * 
 * @author Frederic Bregier
 *
 */
public class DigestCompute {

	/**
	 * @param file
	 * @param algorithm
	 * @return the Hashcode according to the algorithm
	 * @throws Exception
	 */
	public final static String getHashCode(File file, String algorithm) throws Exception {
		FileInputStream fis = new FileInputStream(file);
		return DigestCompute.getHashCode(fis, algorithm);
	}

	/**
	 * @param base
	 * @param filename
	 * @param algorithm
	 * @return the Hashcode according to the algorithm
	 * @throws Exception
	 */
	public final static String getHashCode(File base, String filename, String algorithm)
			throws Exception {
		FileInputStream fis = new FileInputStream(new File(base, filename));
		return DigestCompute.getHashCode(fis, algorithm);
	}

	/**
	 * 
	 * @param fis
	 * @param algorithm
	 * @return the Hashcode according to the algorithm
	 * @throws Exception
	 */
	public final static String getHashCode(FileInputStream fis, String algorithm) throws Exception {
		MessageDigest md = MessageDigest.getInstance(algorithm);
		DigestInputStream dis = null;
		try {
			dis = new DigestInputStream(fis, md);
			byte[] buffer = new byte[8192];
			while (dis.read(buffer) != -1)
				;
		} finally {
			if (dis != null) {
				dis.close();
			}
		}
		byte[] bDigest = md.digest();
		return Base64.encode(bDigest, false);
	}

	/**
	 * Add the various Digest as specified in the argument to the droidFileFormat
	 * 
	 * @param droidFileFormat
	 * @param argument
	 */
	public final static void computeDroidFileFormatDigest(DroidFileFormat droidFileFormat,
			VitamArgument argument) {
		if (argument == null)
			return;
		if (argument.sha1 || argument.sha256 || argument.sha512) {
			String filename = droidFileFormat.getFilename();
			boolean ignoreNotFound = false;
			if (filename.startsWith("tar:") || filename.startsWith("bzip2:") ||
					filename.startsWith("gzip:") || filename.startsWith("zip:")) {
				ignoreNotFound = true;
			}
			FileInputStream in;
			try {
				in = new FileInputStream(filename);
			} catch (FileNotFoundException e1) {
				if (!ignoreNotFound) {
					System.err.println(StaticValues.LBL.error_filenotfile.get() + e1);
				}
				return;
			}
			String[] result = computeDigest(in, argument);
			if (argument.sha1)
				droidFileFormat.setSha1(result[0]);
			if (argument.sha256)
				droidFileFormat.setSha256(result[1]);
			if (argument.sha512)
				droidFileFormat.setSha512(result[2]);
		}
	}


	/**
	 * Compute the various Digest as specified in the argument 
	 * 
	 * @param inputstream
	 * @param argument
	 * @return Array of String in order of SHA-1/SHA-256/SHA-512 as required by argument
	 */
	public final static String[] computeDigest(InputStream inputstream,
			VitamArgument argument) {
		String [] result = new String[3];
		result[0] = result[1] = result[2] = null;
		if (argument == null) {
			return result;
		}
		if (argument.sha1 || argument.sha256 || argument.sha512) {
			MessageDigest md1 = null, md256 = null, md512 = null;
			if (argument.sha1) {
				try {
					md1 = MessageDigest.getInstance("SHA-1");
				} catch (NoSuchAlgorithmException e) {
					System.err.println(StaticValues.LBL.error_computedigest.get() + e);
				}
			}
			if (argument.sha256) {
				try {
					md256 = MessageDigest.getInstance("SHA-256");
				} catch (NoSuchAlgorithmException e) {
					System.err.println(StaticValues.LBL.error_computedigest.get() + e);
				}
			}
			if (argument.sha512) {
				try {
					md512 = MessageDigest.getInstance("SHA-512");
				} catch (NoSuchAlgorithmException e) {
					System.err.println(StaticValues.LBL.error_computedigest.get() + e);
				}
			}
			int size = 0;
			byte[] buf = new byte[8192];
			try {
				while ((size = inputstream.read(buf)) >= 0) {
					if (md1 != null)
						md1.update(buf, 0, size);
					if (md256 != null)
						md256.update(buf, 0, size);
					if (md512 != null)
						md512.update(buf, 0, size);
				}
			} catch (IOException e) {
				System.err.println(StaticValues.LBL.error_computedigest.get() + e);
			} finally {
				try {
					inputstream.close();
				} catch (IOException e) {
				}
			}
			if (md1 != null)
				result[0] = Base64.encode(md1.digest(), false);
			if (md256 != null)
				result[1] = Base64.encode(md256.digest(), false);
			if (md512 != null)
				result[2] = Base64.encode(md512.digest(), false);
			return result;
		}
		return result;
	}

	/**
	 * 
	 * @param config
	 * @param src
	 * @param file
	 * @return Null in case of error, else return the Element config.DOCUMENT_FIELD
	 */
	public static Element checkDigest(ConfigLoader config, File src, File file) {
		String shortname = StaticValues.getSubPath(file, src);
		FileInputStream inputstream;
		try {
			inputstream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			System.err.println(StaticValues.LBL.error_computedigest.get() + ": " + shortname);
			return null;
		}
		String []shas = DigestCompute.computeDigest(inputstream, config.argument);
		//SEDA type since already configured
		Element result = XmlDom.factory.createElement(config.DOCUMENT_FIELD);
		Element attachment = XmlDom.factory.createElement(config.ATTACHMENT_FIELD);
		attachment.addAttribute(config.FILENAME_ATTRIBUTE.substring(1), shortname);
		result.add(attachment);
		if (shas[0] != null) {
			Element integrity = XmlDom.factory.createElement(config.INTEGRITY_FIELD);
			integrity.addAttribute(config.ALGORITHME_ATTRIBUTE.substring(1), StaticValues.XML_SHA1);
			integrity.setText(shas[0]);
			result.add(integrity);
		}
		if (shas[1] != null) {
			Element integrity = XmlDom.factory.createElement(config.INTEGRITY_FIELD);
			integrity.addAttribute(config.ALGORITHME_ATTRIBUTE.substring(1), StaticValues.XML_SHA256);
			integrity.setText(shas[1]);
			result.add(integrity);
		}
		if (shas[2] != null) {
			Element integrity = XmlDom.factory.createElement(config.INTEGRITY_FIELD);
			integrity.addAttribute(config.ALGORITHME_ATTRIBUTE.substring(1), StaticValues.XML_SHA512);
			integrity.setText(shas[2]);
			result.add(integrity);
		}
		String status = "ok";
		if ((shas[0] == null && config.argument.sha1) || 
				(shas[1] == null && config.argument.sha256) ||
				(shas[2] == null && config.argument.sha512)) {
			status = "error";
		}
		result.addAttribute("status", status);
		XmlDom.addDate(VitamArgument.ONEXML, config, result);
		return result;
	}

	public static int createDigest(File src, File dst, File ftar, File fglobal, 
			boolean oneDigestPerFile, String []extensions, String mask, boolean prefix) {
		if (mask == null) {
			return createDigest(src, dst, ftar, fglobal, oneDigestPerFile, extensions);
		}
		try {
			if (prefix) {
				// fix mask
				List<File> filesToScan = new ArrayList<File>();
				File dirToSearch = src;
				if (!dirToSearch.isDirectory()) {
					if (dirToSearch.canRead() && dirToSearch.getName().startsWith(mask)) {
						filesToScan.add(dirToSearch);
					} else {
						throw new CommandExecutionException("Resources not found");
					}
				} else {
					Collection<File> temp = FileUtils.listFiles(dirToSearch, extensions, 
							StaticValues.config.argument.recursive);
					for (File file : temp) {
						if (file.getName().startsWith(mask)) {
							filesToScan.add(file);
						}
					}
					temp.clear();
				}
				return createDigest(src, dst, ftar, fglobal, oneDigestPerFile, filesToScan);
			} else {
				// RegEx mask
				File dirToSearch = src;
				if (!dirToSearch.isDirectory()) {
					List<File> filesToScan = new ArrayList<File>();
					if (dirToSearch.canRead() && dirToSearch.getName().matches(mask+".*")) {
						filesToScan.add(dirToSearch);
						String global = dirToSearch.getName().replaceAll("[^"+mask+"].*", "") 
								+ "_all_digests.xml";
						fglobal = new File(fglobal, global);
					} else {
						throw new CommandExecutionException("Resources not found");
					}
					return createDigest(src, dst, ftar, fglobal, oneDigestPerFile, filesToScan);
				} else {
					HashMap<String, List<File>> hashmap = new HashMap<String, List<File>>();
					Collection<File> temp = FileUtils.listFiles(dirToSearch, extensions, 
							StaticValues.config.argument.recursive);
					List<File> filesToScan = null;
					Pattern pattern = Pattern.compile(mask+".*");
					Pattern pattern2 = Pattern.compile(mask);
					for (File file : temp) {
						String filename = file.getName();
						Matcher matcher = pattern.matcher(filename);
						if (matcher.matches()) {
							String end = pattern2.matcher(filename).replaceFirst("");
							int pos = filename.indexOf(end);
							if (pos <= 0) {
								System.err.println("Cannot find : "+end + " in " + filename);
								continue;
							}
							String global = filename.substring(0, pos);
							if (global.charAt(global.length()-1) != '_') {
								global += "_";
							}
							global += "all_digests.xml";
							filesToScan = hashmap.get(global);
							if (filesToScan == null) {
								filesToScan = new ArrayList<File>();
								hashmap.put(global, filesToScan);
							}
							filesToScan.add(file);
						}
					}
					temp.clear();
					int res = 0;
					for (String global : hashmap.keySet()) {
						File fglobalnew = new File(fglobal, global);
						res += createDigest(src, dst, ftar, fglobalnew, oneDigestPerFile, hashmap.get(global));
					}
					hashmap.clear();
					return res;
				}
			}
		} catch (CommandExecutionException e1) {
			System.err.println(StaticValues.LBL.error_error.get() + " " + e1.toString());
			return -1;
		}
	}
	
	public static int createDigest(File src, File dst, File ftar, File fglobal, 
				boolean oneDigestPerFile, String []extensions) {
		try {
			List<File> filesToScan = DroidHandler.matchedFiled(
					new File[] { src },
					extensions,
					StaticValues.config.argument.recursive);
			if (src.isFile()) {
				src = src.getParentFile();
			}
			return createDigest(src, dst, ftar, fglobal, oneDigestPerFile, filesToScan);
		} catch (CommandExecutionException e1) {
			System.err.println(StaticValues.LBL.error_error.get() + " " + e1.toString());
			return -1;
		}
	}
	public static int createDigest(File src, File dst, File ftar, File fglobal, 
		boolean oneDigestPerFile, List<File> filesToScan) {
		try {
			Element global = null;
			Document globalDoc = null;
			if (fglobal != null) {
				global = XmlDom.factory.createElement("digests");
				global.addAttribute("source", src.getAbsolutePath());
				globalDoc = XmlDom.factory.createDocument(global);
			}
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding(StaticValues.CURRENT_OUTPUT_ENCODING);
			XMLWriter writer = new XMLWriter(format);
			int error = 0;
			int currank = 0;
			for (File file : filesToScan) {
				currank ++;
				Element result = DigestCompute.checkDigest(StaticValues.config, src, file);
				if (result.selectSingleNode(".[@status='ok']") == null) {
					System.err.println(StaticValues.LBL.error_error.get() +
							StaticValues.LBL.error_computedigest.get()
							+ StaticValues.getSubPath(file, src));
					error++;
				} else if (oneDigestPerFile) {
					Element rootElement = XmlDom.factory.createElement("digest");
					Document unique = XmlDom.factory.createDocument(rootElement);
					rootElement.add(result);
					FileOutputStream out = null;
					String shortname = StaticValues.getSubPath(file, src);
					String shortnameWithoutFilename = shortname.substring(0, shortname.lastIndexOf(file.getName()));
					File fdirout = new File(dst, shortnameWithoutFilename);
					fdirout.mkdirs();
					File fout = new File(fdirout, file.getName() + "_digest.xml");
					try {
						out = new FileOutputStream(fout);
						writer.setOutputStream(out);
						writer.write(unique);
						writer.close();
					} catch (FileNotFoundException e) {
						System.err.println(StaticValues.LBL.error_error.get()
								+ fout.getAbsolutePath()
								+ " " + e.toString());
						error++;
					} catch (IOException e) {
						System.err.println(StaticValues.LBL.error_error.get()
								+ fout.getAbsolutePath()
								+ " " + e.toString());
						if (out != null) {
							try {
								out.close();
							} catch (IOException e1) {
							}
						}
						error++;
					}
					result.detach();
				}
				if (fglobal != null) {
					global.add(result);
				}
			}
			if (ftar != null) {
				currank ++;
				Element result = DigestCompute.checkDigest(StaticValues.config, src, ftar);
				if (result.selectSingleNode(".[@status='ok']") == null) {
					System.err
							.println(StaticValues.LBL.error_error.get() +
									StaticValues.LBL.error_computedigest.get()
									+ ftar.getAbsolutePath());
					error++;
				} else if (oneDigestPerFile) {
					Element rootElement = XmlDom.factory.createElement("digest");
					Document unique = XmlDom.factory.createDocument(rootElement);
					rootElement.add(result);
					FileOutputStream out = null;
					File fout = new File(dst, ftar.getName() + "_tar_digest.xml");
					try {
						out = new FileOutputStream(fout);
						writer.setOutputStream(out);
						writer.write(unique);
						writer.close();
					} catch (FileNotFoundException e) {
						System.err.println(StaticValues.LBL.error_error.get()
								+ fout.getAbsolutePath()
								+ " " + e.toString());
						error++;
					} catch (IOException e) {
						System.err.println(StaticValues.LBL.error_error.get()
								+ fout.getAbsolutePath()
								+ " " + e.toString());
						if (out != null) {
							try {
								out.close();
							} catch (IOException e1) {
							}
						}
						error++;
					}
					result.detach();
				}
				if (fglobal != null) {
					global.add(result);
				}
			}
			if (fglobal != null) {
				if (error > 0) {
					global.addAttribute("status", "error");
				} else {
					global.addAttribute("status", "ok");
				}
				XmlDom.addDate(VitamArgument.ONEXML, StaticValues.config, global);
				FileOutputStream out;
				try {
					out = new FileOutputStream(fglobal);
					writer.setOutputStream(out);
					writer.write(globalDoc);
					writer.close();
				} catch (FileNotFoundException e) {
					System.err.println(StaticValues.LBL.error_error.get()
							+ fglobal.getAbsolutePath()
							+ " " + e.toString());
					error++;
				} catch (IOException e) {
					System.err.println(StaticValues.LBL.error_error.get()
							+ fglobal.getAbsolutePath()
							+ " " + e.toString());
					error++;
				}
			}
			if (error > 0) {
				System.err.println(StaticValues.LBL.error_error.get() + " Digest" +
						" [ " + currank + (error > 0 ? " (" + StaticValues.LBL.error_error.get() + error + " ) " : "" ) + " ]");
				return -error;
			}
			return currank;
		} catch (UnsupportedEncodingException e) {
			System.err.println(StaticValues.LBL.error_error.get() + " " + e.toString());
			return -1;
		}

	}
}
