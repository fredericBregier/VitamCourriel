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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.jdom.output.DOMOutputter;

import edu.harvard.hul.ois.fits.tools.ToolOutput;
import fr.gouv.culture.vitam.digest.DigestCompute;
import fr.gouv.culture.vitam.droid.DroidFileFormat;
import fr.gouv.culture.vitam.eml.EmlExtract;
import fr.gouv.culture.vitam.eml.MailboxParser;
import fr.gouv.culture.vitam.eml.MsgExtract2;
import fr.gouv.culture.vitam.eml.PstExtract;
import fr.gouv.culture.vitam.extract.ExtractInfo;

/**
 * Class to rassemble commands related to Droid / Fits computations
 * @author "Frederic Bregier"
 * 
 */
public class Commands {
	 /** 
	 * @param basename
	 * @param mimeCode
	 * @param format
	 * @param fic
	 * @param config
	 * @param argument
	 * @return the Element associated with the result
	 */
	public static Element showFormat(String basename, String mimeCode, String format,
			File fic, ConfigLoader config, VitamArgument argument) {
		try {
			Element root = XmlDom.factory.createElement("showformat");
			Element newElt = XmlDom.factory.createElement("file");
			newElt.addAttribute("filename", basename);
			if (mimeCode != null) {
				newElt.addAttribute("mime", mimeCode);
			}
			if (format != null) {
				newElt.addAttribute("puid", format);
			}
			root.add(newElt);
			newElt = XmlDom.factory.createElement("toolsversion");
			if (config.droidHandler != null) {
				newElt.addAttribute("pronom", config.droidHandler.getVersionSignature());
			}
			if (config.droidHandler != null) {
				newElt.addAttribute("droid", "6.1");
			}
			if (config.exif != null) {
				newElt.addAttribute("exif", config.exif.getToolInfo().version);
			}
			if (config.jhove != null) {
				newElt.addAttribute("jhove", config.jhove.getToolInfo().version);
			}
			root.add(newElt);
			addFormatIdentification(root, basename, fic, config, argument);
			return root;
		} catch (Exception e) {
			System.err.println("FITS_ERROR: " + e);
			e.printStackTrace();
			Element root = XmlDom.factory.createElement("showformat");
			Element newElt = XmlDom.factory.createElement("file");
			newElt.addAttribute("filename", basename);
			if (mimeCode != null) {
				newElt.addAttribute("mime", mimeCode);
			}
			if (format != null) {
				newElt.addAttribute("puid", format);
			}
			root.add(newElt);
			root.addAttribute("status", e.toString());
			return root;
		}
	}

	/**
	 * Identify format of one file
	 * @param root where the information will be added
	 * @param basename
	 * @param fic
	 * @param config
	 * @param argument
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static final void addFormatIdentification(Element root, String basename, File fic, 
			ConfigLoader config, VitamArgument argument) throws Exception {
		try {
			Element identification, fileinfo, filestatus, metadata;
			
			identification = XmlDom.factory.createElement("identification");
			fileinfo = XmlDom.factory.createElement("fileinfo");
			filestatus = XmlDom.factory.createElement("filestatus");
			metadata = XmlDom.factory.createElement("metadata");
			root.add(identification);
			root.add(fileinfo);
			root.add(filestatus);
			root.add(metadata);
			
			String status = "ok";

			Element toAdd;
			// Droid
			boolean identityFound = false;
			Element idp = null;
			DroidFileFormat finalFormat = null;
			if (config.droidHandler != null) {
				List<DroidFileFormat> formats = config.droidHandler.checkFileFormat(fic, argument);
				Element newElt = XmlDom.factory.createElement("subidentities");
				if (formats != null && !formats.isEmpty()) {
					boolean multiple = argument.archive && formats.size() > 1;
					Iterator<DroidFileFormat> iterator = formats.iterator();
					DroidFileFormat droidFileFormat = iterator.next();
					finalFormat = droidFileFormat;
					if (droidFileFormat.getPUID().equals(DroidFileFormat.Unknown)) {
						idp = droidFileFormat.toElement(multiple);
						identification.add(idp);
					} else {
						if (! config.preventXfmt || !droidFileFormat.getPUID().startsWith(StaticValues.FORMAT_XFMT)) {
							identityFound = true;
						} else {
							status = "warning";
						}
						idp = droidFileFormat.toElement(multiple);
						identification.add(idp);
					}
					boolean other = false;
					while (iterator.hasNext()) {
						other = true;
						droidFileFormat = (DroidFileFormat) iterator.next();
						if (droidFileFormat.getPUID().equals(DroidFileFormat.Unknown)) {
							idp = droidFileFormat.toElement(multiple);
							newElt.add(idp);
						} else {
							//identityFound = true;
							idp = droidFileFormat.toElement(multiple);
							newElt.add(idp);
						}
					}
					if (other) {
						identification.add(newElt);
					}
				}
			}
			String mimeType = finalFormat.getMimeType();
			String pid = finalFormat.getPUID();
			if (identityFound && "message/rfc822".equals(mimeType)) {
				// email special task : EML
				// No rankid since done below
				Element special = EmlExtract.extractInfoEmail(fic, basename, argument, config);
				if (special == null) {
					status = "ko";
					root.addAttribute("status", status);
				} else {
					root.add(special);
					root.addAttribute("status", status);
				}
				return;
			} else if (identityFound && ("x-fmt/248".equals(pid) || "x-fmt/249".equals(pid))) {
				// email special task : PST
				config.addRankId(root);
				//String id = Long.toString(config.nbDoc.incrementAndGet());
				//root.addAttribute(EMAIL_FIELDS.rankId.name, id);
				idp.addAttribute("mime", "application/vnd.ms-outlook");
				Element special = PstExtract.extractInfoPst(fic, argument, config);
				if (special == null) {
					status = "ko";
					root.addAttribute("status", status);
				} else {
					root.add(special);
					root.addAttribute("status", status);
				}
				return;
			} else if (identityFound && "x-fmt/430".equals(pid)) {
				// email special task : MSG
				// no RankId since done below
				Element special = MsgExtract2.extractInfoEmail(fic, basename, argument, config);
				if (special == null) {
					status = "ko";
					root.addAttribute("status", status);
				} else {
					root.add(special);
					root.addAttribute("status", status);
				}
				return;
			} else if (!identityFound) {
				// email special task : MBOX RFC 4155 ?
				config.addRankId(root);
				//String id = Long.toString(config.nbDoc.incrementAndGet());
				//root.addAttribute(EMAIL_FIELDS.rankId.name, id);
				Element special = MailboxParser.extractInfoEmail(fic, argument, config);
				if (special != null) { 
					// should be a MBOX RFC 4155
					idp.addAttribute("mime", "application/mbox");
					root.add(special);
					root.addAttribute("status", status);
					return;
				}
			} else {
				// not email
				config.addRankId(root);
				//String id = Long.toString(config.nbDoc.incrementAndGet());
				//root.addAttribute(EMAIL_FIELDS.rankId.name, id);
			}
			ToolOutput toolOuput;
			List<Element> sublist;
			Document doc;

			DOMOutputter output = new DOMOutputter();
			DOMReader reader = new DOMReader();
			// Exiftool
			// identity fileinfo metadata
			if (config.exif != null) {
				toolOuput = config.exif.extractInfo(fic);
				org.jdom.Document docJdom = toolOuput.getFitsXml();
				org.w3c.dom.Document dom = output.output(docJdom);
				doc = reader.read(dom);
				XmlDom.removeAllNamespaces(doc);
				XmlDom.removeEmptyDocument(doc);
				// get identity fileinfo metadata
				boolean findSubTree = false;
				Element element = null;
				if (!identityFound) {
					element = (Element) doc.selectSingleNode("//identification");
					if (element != null) {
						sublist = element.elements();
						if (sublist != null && !sublist.isEmpty()) {
							toAdd = sublist.get(0);
							findSubTree = true;
							toAdd = (Element) toAdd.detach();
							Element newElt = XmlDom.factory.createElement("ExifTool");
							newElt.add(toAdd);
							toAdd = null;
							identification.add(newElt);
						}
						sublist = null;
					}
				} else {
					element = (Element) doc.selectSingleNode("//identification");
					if (element != null) {
						findSubTree = true;
					}
				}
				element = (Element) doc.selectSingleNode("//fileinfo");
				if (element != null) {
					toAdd = element;
					findSubTree = true;
					List<Element> list = toAdd.elements();
					for (Element element2 : list) {
						element2.detach();
						fileinfo.add(element2);
					}
				}
				element = (Element) doc.selectSingleNode("//metadata");
				if (element != null) {
					toAdd = element;
					findSubTree = true;
					List<Element> list = toAdd.elements();
					for (Element element2 : list) {
						element2.detach();
						metadata.add(element2);
					}
				}
				if (!findSubTree) {
					toAdd = (Element) doc.getRootElement().detach();
					Element newElt = XmlDom.factory.createElement("ExifTool");
					newElt.add(toAdd);
					root.add(newElt);
				}
			}

			// JHove
			// identity fileinfo filestatus metadata
			if (config.jhove != null) {
				toolOuput = config.jhove.extractInfo(fic);
				org.jdom.Document docJdom = toolOuput.getFitsXml();
				org.w3c.dom.Document dom = output.output(docJdom);
				doc = reader.read(dom);
				XmlDom.removeAllNamespaces(doc);
				XmlDom.removeEmptyDocument(doc);
				// get identity fileinfo metadata
				boolean findSubTree = false;
				Element element = null;
				if (!identityFound) {
					element = (Element) doc.selectSingleNode("//identification");
					if (element != null) {
						sublist = element.elements();
						if (sublist != null && !sublist.isEmpty()) {
							toAdd = sublist.get(0);
							findSubTree = true;
							toAdd = (Element) toAdd.detach();
							Element newElt = XmlDom.factory.createElement("JHove");
							newElt.add(toAdd);
							toAdd = null;
							identification.add(newElt);
						}
						sublist = null;
					}
				} else {
					element = (Element) doc.selectSingleNode("//identification");
					if (element != null) {
						findSubTree = true;
					}
				}
				element = (Element) doc.selectSingleNode("//fileinfo");
				if (element != null) {
					toAdd = element;
					findSubTree = true;
					List<Element> list = toAdd.elements();
					for (Element element2 : list) {
						XmlDom.checkPresence(element2, fileinfo);
					}
				}
				element = (Element) doc.selectSingleNode("//metadata");
				if (element != null) {
					toAdd = element;
					findSubTree = true;
					List<Element> list = toAdd.elements();
					for (Element element2 : list) {
						XmlDom.checkPresence(element2, metadata);
					}
				}
				element = (Element) doc.selectSingleNode("//filestatus");
				if (element != null) {
					toAdd = element;
					findSubTree = true;
					List<Element> list = toAdd.elements();
					for (Element element2 : list) {
						element2.detach();
						filestatus.add(element2);
					}
				}
				if (!findSubTree) {
					toAdd = (Element) doc.getRootElement().detach();
					Element newElt = XmlDom.factory.createElement("JHove");
					newElt.add(toAdd);
					root.add(newElt);
				}
			}
			// Keywords
			if (argument.extractKeyword && finalFormat != null) {
				Element keywords = ExtractInfo.exportMetadata(fic, basename, config, null, finalFormat);
				if (keywords != null) {
					XmlDom.removeEmptyElement(keywords);
					root.add(keywords);
				}
			}			
			// Global result
			root.addAttribute("status", status);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Check digest for one file
	 * 
	 * @param fic
	 * @param salgo
	 * @param sintegrity
	 * @return the Element with the result (.[@status='ok'] != null => OK)
	 */
	public final static Element checkDigest(File fic, String salgo, String sintegrity) {
		Element eltdigest = null;
		try {
			String compute = DigestCompute.getHashCode(fic, salgo);
			if (!compute.equals(sintegrity)) {
				eltdigest = XmlDom.factory.createElement("digest");
				eltdigest.addAttribute("status",
						StaticValues.LBL.error_digest.get());
				eltdigest.addAttribute("compute", compute);
				eltdigest.addAttribute("source", sintegrity);
				eltdigest.addAttribute("algo", salgo);
			} else {
				eltdigest = XmlDom.factory.createElement("digest");
				eltdigest.addAttribute("status",
						"ok");
				eltdigest.addAttribute("source", sintegrity);
				eltdigest.addAttribute("algo", salgo);
			}
		} catch (Exception e) {
			eltdigest = XmlDom.factory.createElement("digest");
			eltdigest.addAttribute("status",
					"Error: " +
							e.toString());
		}
		return eltdigest;
	}

	/**
	 * Try to validate the format from the XML and the Droid output
	 * 
	 * @param list
	 * @param mimeCode
	 * @param format
	 * @param ficname
	 *            (name as in XML)
	 * @param fic
	 *            real File
	 * 
	 * @return Element with the result (.[@status='ok'] != null => OK)
	 */
	public final static Element droidFormat(List<DroidFileFormat> list, String mimeCode,
			String format,
			String ficname, File fic) {
		boolean localError = false;
		boolean localWarning = false;
		Element cformat = XmlDom.factory.createElement("format");
		cformat.addAttribute("srcpuid", format);
		cformat.addAttribute("srcmime", mimeCode);
		Hashtable<String, HashSet<String>> table = new Hashtable<String, HashSet<String>>();
		Hashtable<String, HashSet<String>> table2 = new Hashtable<String, HashSet<String>>();
		getMimesFormats(table, table2, list, fic.getAbsolutePath(), cformat);
		HashSet<String> formats2 = table2.get("_ALL_");
		if ((mimeCode != null) && (format != null)) {
			if (table.containsKey(mimeCode)) {
				HashSet<String> formats = table.get(mimeCode);
				if (!formats.contains(format)) {
					formats = table2.get(mimeCode);
					if (!formats.contains(format)) {
						localError = true;
						fillElementResult(cformat, table, table2,
								StaticValues.LBL.error_format.get(), "correctpuid", null);
					} else {
						localWarning = true;
						fillElementResult(cformat, table, table2,
								"warning", "correctpuid", "compatiblepuid");
					}
				}
			} else {
				if (table2.containsKey(mimeCode)) {
					HashSet<String> formats = table2.get(mimeCode);
					if (!formats.contains(format)) {
						localError = true;
						fillElementResult(cformat, table, table2,
								StaticValues.LBL.error_format.get(), "correctmime", null);
					} else {
						localWarning = true;
						fillElementResult(cformat, table, table2,
								"warning", "correctmime", "compatiblemime");
					}
				} else {
					localError = true;
					fillElementResult(cformat, table, table2,
							StaticValues.LBL.error_format.get(), "correctmime", null);
				}
			}
		} else {
			if (mimeCode != null) {
				if (!table.containsKey(mimeCode)) {
					if (table2.containsKey(mimeCode)) {
						localWarning = true;
						fillElementResult(cformat, table, table2,
								"warning", "correctmime", "compatiblemime");
					} else {
						localError = true;
						fillElementResult(cformat, table, table2,
								StaticValues.LBL.error_format.get(), "correctmime", null);
					}
				}
			}
			if (format != null) {
				HashSet<String> formats = table.get("_ALL_");
				if (!formats.contains(format)) {
					formats = table2.get("_ALL_");
					if (!formats.contains(format)) {
						localWarning = true;
						fillElementResult(cformat, table, table2,
								"warning", "correctpuid", "compatiblepuid");
					} else {
						localError = true;
						fillElementResult(cformat, table, table2,
								StaticValues.LBL.error_format.get(), "correctpuid", null);
					}
				}
			}
		}
		if (!localError && !localWarning) {
			fillElementResult(cformat, table, table2,
					"ok", "found", null);
		} else if (localWarning) {
			// fill subformat
			Element newElt = XmlDom.factory.createElement("subidentities");
			for (String puid : formats2) {
				DroidFileFormat droidFileFormat = StaticValues.config.droidHandler
						.getDroidFileFormatFromPuid(puid, ficname);
				if (droidFileFormat == null) {
					System.err.println(StaticValues.LBL.error_notfound.get() + puid);
					continue;
				}
				Element subformat = droidFileFormat.toElement(false);
				newElt.add(subformat);
			}
			cformat.add(newElt);
		}
		table.clear();
		table2.clear();
		return cformat;
	}

	private static void fillElementResult(Element cformat,
			Hashtable<String, HashSet<String>> table,
			Hashtable<String, HashSet<String>> table2, String status, String correct,
			String compatible) {
		table.remove("_ALL_");
		cformat.addAttribute("status", status);
		cformat.addAttribute(correct, table.toString());
		if (compatible != null) {
			table2.remove("_ALL_");
			cformat.addAttribute(compatible, table2.toString());
		}
	}

	private final static void getMimesFormats(
			Hashtable<String, HashSet<String>> table,
			Hashtable<String, HashSet<String>> table2,
			List<DroidFileFormat> list, String fullPath, Element element) {
		HashSet<String> allFormats = new HashSet<String>();
		HashSet<String> allFormats2 = new HashSet<String>();
		boolean withFilename = (list.size() > 1);
		for (DroidFileFormat identity : list) {
			// ensure that if Archive traversal is enabled, we don't use it
			if (identity.getFilename().equals(fullPath)) {
				// check correct signature
				Element subformat = identity.toElement(withFilename);
				String mime = identity.getMimeType();
				HashSet<String> formats = new HashSet<String>();
				String puid = identity.getPUID();
				element.add(subformat);
				formats.add(puid);
				allFormats.addAll(formats);
				if (table.containsKey(mime)) {
					HashSet<String> f = table.get(mime);
					f.addAll(formats);
					table.put(mime, f);
				} else {
					table.put(mime, formats);
				}
				// check additional valid signatures
				HashMap<String, String> map = identity.getAllFormats();
				for (Entry<String, String> entry : map.entrySet()) {
					puid = entry.getKey();
					mime = entry.getValue();
					formats = new HashSet<String>();
					formats.add(puid);
					allFormats2.addAll(formats);
					if (table2.containsKey(mime)) {
						HashSet<String> f = table2.get(mime);
						f.addAll(formats);
						table2.put(mime, f);
					} else {
						table2.put(mime, formats);
					}
				}
			}
		}
		table.put("_ALL_", allFormats);
		table2.put("_ALL_", allFormats2);
	}
}
