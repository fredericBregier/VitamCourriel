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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import fr.gouv.culture.vitam.droid.DroidFileFormat;
import fr.gouv.culture.vitam.gui.VitamGui.RunnerLongTask;

/**
 * XML manipulation class using DOM (dom4j in particular)
 * 
 * @author "Frederic Bregier"
 * 
 */
public class XmlDom {
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

	public static DocumentFactory factory = DocumentFactory.getInstance();

	/**
	 * 
	 * @param current_file
	 * @param config
	 * @return the number of Document in the XML file
	 */
	static final public int countDocument(File current_file, ConfigLoader config) {
		SAXReader saxReader = new SAXReader();
		Document document;
		try {
			document = saxReader.read(current_file);
		} catch (DocumentException e) {
			System.err.println(StaticValues.LBL.error_error.get() + " " + e
					+ " - " +
					StaticValues.LBL.error_parser.get() + " ("
					+ SAXReader.class.getName() + ")");
			return 0;
		}
		removeAllNamespaces(document);
		@SuppressWarnings("unchecked")
		List<Node> nodes = document.selectNodes("//" + config.DOCUMENT_FIELD);
		if (nodes == null) {
			return 0;
		} else {
			return nodes.size();
		}
	}

	public static enum AllTestsItems {
		SystemError, GlobalError, GlobalWarning,
		FileError, FileWarning, FileSuccess,
		DigestError, DigestWarning, DigestSuccess,
		FormatError, FormatWarning, FormatSuccess,
		ShowError, ShowWarning, ShowSuccess
	}

	/**
	 * Attached file accessibility test
	 * 
	 * @param current_file
	 * @param task
	 *            optional
	 * @param config
	 * @param argument
	 * @param checkDigest
	 * @param checkFormat
	 * @param showFormat
	 * @return VitamResult
	 */
	static final public VitamResult all_tests_in_one(File current_file, RunnerLongTask task,
			ConfigLoader config, VitamArgument argument, boolean checkDigest, boolean checkFormat,
			boolean showFormat) {
		SAXReader saxReader = new SAXReader();
		VitamResult finalResult = new VitamResult();
		Element root = initializeCheck(argument, finalResult, current_file);
		try {
			Document document = saxReader.read(current_file);
			removeAllNamespaces(document);
			Node nodesrc = document.selectSingleNode("/digests");
			String src = null;
			String localsrc = current_file.getParentFile().getAbsolutePath() + File.separator;
			if (nodesrc != null) {
				nodesrc = nodesrc.selectSingleNode("@source");
				if (nodesrc != null) {
					src = nodesrc.getText() + "/";
				}
			}
			if (src == null) {
				src = localsrc;
			}
			@SuppressWarnings("unchecked")
			List<Node> nodes = document.selectNodes("//" + config.DOCUMENT_FIELD);
			if (nodes == null) {
				Element result = fillInformation(argument, root, "result", "filefound", "0");
				addDate(argument, config, result);
			} else {
				String number = "" + nodes.size();
				Element result = fillInformation(argument, root, "result", "filefound", number);
				XMLWriter writer = null;
				writer = new XMLWriter(System.out, StaticValues.defaultOutputFormat);
				int currank = 0;
				for (Node node : nodes) {
					currank++;
					Node attachment = node.selectSingleNode(config.ATTACHMENT_FIELD);
					if (attachment == null) {
						continue;
					}
					Node file = attachment.selectSingleNode(config.FILENAME_ATTRIBUTE);
					if (file == null) {
						continue;
					}
					Node mime = null;
					Node format = null;
					if (checkFormat) {
						mime = attachment.selectSingleNode(config.MIMECODE_ATTRIBUTE);
						format = attachment.selectSingleNode(config.FORMAT_ATTRIBUTE);
					}
					String sfile = null;
					String smime = null;
					String sformat = null;
					String sintegrity = null;
					String salgo = null;
					if (file != null) {
						sfile = file.getText();
					}
					if (mime != null) {
						smime = mime.getText();
					}
					if (format != null) {
						sformat = format.getText();
					}
					// Now check
					// first existence check
					String ficname = src + sfile;
					Element check = fillInformation(argument, root, "check", "filename", sfile);
					File fic1 = new File(ficname);
					File fic2 = new File(localsrc + sfile);
					File fic = null;
					if (fic1.canRead()) {
						fic = fic1;
					} else if (fic2.canRead()) {
						fic = fic2;
					}
					if (fic == null) {
						fillInformation(argument, check, "find", "status",
								StaticValues.LBL.error_filenotfile.get());
						addDate(argument, config, result);
						finalResult.values[AllTestsItems.FileError.ordinal()]++;
						finalResult.values[AllTestsItems.GlobalError.ordinal()]++;
					} else {
						Element find = fillInformation(argument, check, "find", "status", "ok");
						addDate(argument, config, find);
						finalResult.values[AllTestsItems.FileSuccess.ordinal()]++;
						if (checkDigest) {
							@SuppressWarnings("unchecked")
							List<Node> integrities = node.selectNodes(config.INTEGRITY_FIELD);
							for (Node integrity : integrities) {
								Node algo = integrity.selectSingleNode(config.ALGORITHME_ATTRIBUTE);
								salgo = null;
								sintegrity = null;
								if (integrity != null) {
									sintegrity = integrity.getText();
									if (algo != null) {
										salgo = algo.getText();
									} else {
										salgo = config.DEFAULT_DIGEST;
									}
								}
								// Digest check
								if (salgo == null) {
									// nothing
								} else if (salgo.equals(StaticValues.XML_SHA1)) {
									salgo = "SHA-1";
								} else if (salgo.equals(StaticValues.XML_SHA256)) {
									salgo = "SHA-256";
								} else if (salgo.equals(StaticValues.XML_SHA512)) {
									salgo = "SHA-512";
								} else {
									salgo = null;
								}
	
								if(algo != null) {
									Element eltdigest = Commands.checkDigest(fic, salgo, sintegrity);
									if (eltdigest.selectSingleNode(".[@status='ok']") != null) {
										finalResult.values[AllTestsItems.DigestSuccess.ordinal()]++;
									} else {
										finalResult.values[AllTestsItems.DigestError.ordinal()]++;
										finalResult.values[AllTestsItems.GlobalError.ordinal()]++;
									}
									addDate(argument, config, eltdigest);
									addElement(writer, argument, check, eltdigest);
								}
							}
						}
						// Check format
						if (checkFormat && config.droidHandler != null) {
							try {
								// Droid
								List<DroidFileFormat> list =
										config.droidHandler.checkFileFormat(fic, argument);
								Element cformat = Commands.droidFormat(list, smime, sformat, sfile,
										fic);
								if (config.droidHandler != null) {
									cformat.addAttribute("pronom", config.droidHandler.getVersionSignature());
								}
								if (config.droidHandler != null) {
									cformat.addAttribute("droid", "6.1");
								}
								if (cformat.selectSingleNode(".[@status='ok']") != null) {
									finalResult.values[AllTestsItems.FormatSuccess.ordinal()]++;
								} else if (cformat.selectSingleNode(".[@status='warning']") != null) {
									finalResult.values[AllTestsItems.FormatWarning.ordinal()]++;
									finalResult.values[AllTestsItems.GlobalWarning.ordinal()]++;
								} else {
									finalResult.values[AllTestsItems.FormatError.ordinal()]++;
									finalResult.values[AllTestsItems.GlobalError.ordinal()]++;
								}
								addDate(argument, config, cformat);
								addElement(writer, argument, check, cformat);
							} catch (Exception e) {
								Element cformat = fillInformation(argument, check, "format",
										"status", "Error " + e.toString());
								addDate(argument, config, cformat);
								finalResult.values[AllTestsItems.SystemError.ordinal()]++;
							}
						}
						// Show format
						if (showFormat && config.droidHandler != null) {
							Element showformat = Commands.showFormat(sfile, smime, sformat,
									fic, config, argument);
							if (showformat.selectSingleNode(".[@status='ok']") != null) {
								finalResult.values[AllTestsItems.ShowSuccess.ordinal()]++;
							} else if (showformat.selectSingleNode(".[@status='warning']") != null) {
								finalResult.values[AllTestsItems.ShowWarning.ordinal()]++;
								finalResult.values[AllTestsItems.GlobalWarning.ordinal()]++;
							} else {
								finalResult.values[AllTestsItems.ShowError.ordinal()]++;
								finalResult.values[AllTestsItems.GlobalError.ordinal()]++;
							}
							addDate(argument, config, showformat);
							addElement(writer, argument, check, showformat);
						}
					}
					addDate(argument, config, result);
					root = finalizeOneCheck(argument, finalResult, current_file, number);
					if (root != null) {
						result = root.element("result");
					}
					if (task != null) {
						float value = ((float) currank) / (float) nodes.size();
						value *= 100;
						task.setProgressExternal((int) value);
					}
				}
			}
			document = null;
		} catch (DocumentException e1) {
			Element result = fillInformation(argument, root, "result", "parsererror",
					StaticValues.LBL.error_error.get() + " " + e1
							+ " - " +
							StaticValues.LBL.error_parser.get() + " ("
							+ SAXReader.class.getName() + ")");
			addDate(argument, config, result);
			finalResult.values[AllTestsItems.SystemError.ordinal()]++;
		} catch (UnsupportedEncodingException e1) {
			Element result = fillInformation(argument, root, "result", "parsererror",
					StaticValues.LBL.error_error.get() + " " + e1
							+ " - " +
							StaticValues.LBL.error_parser.get() + " ("
							+ XMLWriter.class.getName() + ")");
			addDate(argument, config, result);
			finalResult.values[AllTestsItems.SystemError.ordinal()]++;
		}
		finalizeAllCheck(argument, finalResult);
		return finalResult;
	}

	public final static void removeAllNamespaces(Document doc) {
		Element root = doc.getRootElement();
		Namespace namespace = root.getNamespace();
		if (namespace != Namespace.NO_NAMESPACE) {
			root.remove(namespace);
			removeNamespaces(root.content());
		}
	}

	public final static void unfixNamespaces(Document doc, Namespace original) {
		Element root = doc.getRootElement();
		if (original != null) {
			setNamespaces(root.content(), original);
		}
	}

	private final static void setNamespace(Element elem, Namespace ns) {
		elem.setQName(QName.get(elem.getName(), ns,
				elem.getQualifiedName()));
	}

	/**
	 * Recursively removes the namespace of the element and all its children: sets to
	 * Namespace.NO_NAMESPACE
	 */
	public final static void removeNamespaces(Element elem) {
		setNamespaces(elem, Namespace.NO_NAMESPACE);
	}

	/**
	 * Recursively removes the namespace of the list and all its children: sets to
	 * Namespace.NO_NAMESPACE
	 */
	private final static void removeNamespaces(List<?> l) {
		setNamespaces(l, Namespace.NO_NAMESPACE);
	}

	/**
	 * Recursively sets the namespace of the element and all its children.
	 */
	private final static void setNamespaces(Element elem, Namespace ns) {
		setNamespace(elem, ns);
		setNamespaces(elem.content(), ns);
	}

	/**
	 * Recursively sets the namespace of the List and all children if the current namespace is match
	 */
	private final static void setNamespaces(List<?> l, Namespace ns) {
		Node n = null;
		for (int i = 0; i < l.size(); i++) {
			n = (Node) l.get(i);

			if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
				Namespace namespace = ((Attribute) n).getNamespace();
				if (!namespace.equals(ns)) {
					((Attribute) n).setNamespace(ns);
				}
			}
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Namespace namespace = ((Element) n).getNamespace();
				if (!namespace.equals(ns)) {
					if (ns.equals(Namespace.NO_NAMESPACE)) {
						((Element) n).remove(namespace);
					}
					setNamespaces((Element) n, ns);
				}
			}
		}
	}

	public final static Element fillInformation(VitamArgument argument,
			Element parent, String name, String attribut, String info) {
		Element element = null;
		switch (argument.outputModel) {
			case TXT:
				System.out.println("\t" + name.toUpperCase() + ": " + attribut + " = " + info);
				break;
			case MultipleXML:
			case OneXML:
				element = factory.createElement(name);
				element.addAttribute(attribut, info);
				parent.add(element);
				break;
		}
		return element;
	}

	public final static void addElement(XMLWriter writer,
			VitamArgument argument, Element parent, Element sub) {
		switch (argument.outputModel) {
			case TXT:
				try {
					if (writer != null) {
						writer.write(sub);
					}
				} catch (IOException e) {
				}
				break;
			case MultipleXML:
			case OneXML:
				if (sub != null) {
					parent.add(sub);
				}
		}

	}

	public final static void addAttribute(VitamArgument argument,
			Element element, String attribut, String info) {
		switch (argument.outputModel) {
			case TXT:
				System.out.println("\t\t" + attribut + " = " + info);
				break;
			case MultipleXML:
			case OneXML:
				element.addAttribute(attribut, info);
				break;
		}
	}

	public final static Element initializeCheck(VitamArgument argument, VitamResult result,
			File current_file) {
		Element root = null;
		AllTestsItems[] allTestsItems = AllTestsItems.values();
		result.labels = new String[allTestsItems.length];
		result.values = new int[allTestsItems.length];
		for (int i = 0; i < allTestsItems.length; i++) {
			result.labels[i] = allTestsItems[i].name();
		}
		switch (argument.outputModel) {
			case TXT:
				System.out.println("XMLFILE: file = " + current_file.getAbsolutePath() + "\n");
				break;
			case MultipleXML:
				result.multiples = new ArrayList<Document>();
			case OneXML:
				root = factory.createElement("vitam");
				result.unique = factory.createDocument(root);
				Element element = factory.createElement("xmlfile");
				element.addAttribute("file", current_file.getAbsolutePath());
				root.add(element);
				break;
		}
		return root;
	}

	public final static Element finalizeOneCheck(VitamArgument argument, VitamResult result,
			File current_file, String number) {
		Element root = null;
		switch (argument.outputModel) {
			case TXT:
				System.out
						.println("\n\n======================================================================\n");
				break;
			case MultipleXML:
				result.multiples.add(result.unique);
				root = factory.createElement("vitam");
				result.unique = factory.createDocument(root);
				fillInformation(argument, root, "xmlfile", "file",
						current_file.getAbsolutePath());
				fillInformation(argument, root, "result", "filefound", number);
				break;
			case OneXML:
				root = result.unique.getRootElement();
				break;
		}
		return root;
	}

	public final static void finalizeAllCheck(VitamArgument argument, VitamResult result) {
		switch (argument.outputModel) {
			case TXT:
				break;
			case MultipleXML:
				result.unique = null;
				break;
			case OneXML:
				break;
		}
	}

	public final static void addDate(VitamArgument argument, ConfigLoader config, Element root) {
		addAttribute(argument, root, "date", config.dateFormat.format(new Date()));
	}

	public final static void removeEmptyDocument(Document doc) {
		@SuppressWarnings("unchecked")
		Iterator<Node> nodes = doc.nodeIterator();
		while (nodes.hasNext()) {
			Node node = (Node) nodes.next();
			if (node instanceof Element) {
				removeEmptyElement((Element) node);
			} else if (node instanceof Attribute) {
				removeEmptyAttribute((Attribute) node);
			}
		}
	}

	public final static void removeEmptyAttribute(Attribute attrib) {
		if (attrib.getValue().length() == 0) {
			attrib.detach();
		}
	}

	public final static void removeEmptyElement(Element root) {
		// look first at attribute
		if (root.attributeCount() > 0) {
			@SuppressWarnings("unchecked")
			Iterator<Attribute> attribs = root.attributeIterator();
			while (attribs.hasNext()) {
				Attribute attribute = (Attribute) attribs.next();
				removeEmptyAttribute(attribute);
			}
		}
		@SuppressWarnings("unchecked")
		Iterator<Element> elements = root.elementIterator();
		while (elements.hasNext()) {
			Element elt = (Element) elements.next();
			// look at its descendant
			removeEmptyElement(elt);
			if (elt.attributeCount() > 0) {
				continue;
			}
			if (elt.hasContent()) {
				continue;
			}
			elt.detach();
		}
	}
	/**
	 * 
	 * @param source
	 *            what we are going to add
	 * @param target
	 *            where we are going to add
	 */
	public static void checkPresence(Element source, Element target) {
		if (target.selectSingleNode("./" + source.getName()) != null) {
			// node already there so checking sub node if any
			@SuppressWarnings("unchecked")
			List<Element> listElements = source.elements();
			@SuppressWarnings("unchecked")
			List<Attribute> listAttributes = source.attributes();
			Element newSource = (Element) target.selectSingleNode("./" + source.getName());
			if (newSource != null) {
				for (Attribute attribute : listAttributes) {
					checkPresence(attribute, newSource);
				}
				for (Element element : listElements) {
					checkPresence(element, newSource);
				}
			}
		} else {
			source.detach();
			target.add(source);
		}
	}

	/**
	 * 
	 * @param source
	 *            what we are going to add
	 * @param target
	 *            where we are going to add
	 */
	public static void checkPresence(Attribute source, Element target) {
		if (target.selectSingleNode("./@" + source.getName()) != null) {
			// not ignoring this attribute
			source.detach();
			target.add(source);
		}
	}
}
