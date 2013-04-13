/**
 * This file is part of Vitam Project.
 * 
 * Copyright 2010, Frederic Bregier, and individual contributors by the @author tags. See the
 * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
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
package fr.gouv.culture.vitam.eml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.dom4j.Element;
import org.dom4j.io.XMLWriter;

import com.pff.*;

import fr.gouv.culture.vitam.droid.DroidHandler;
import fr.gouv.culture.vitam.eml.StringUtils.EMAIL_FIELDS;
import fr.gouv.culture.vitam.extract.ExtractInfo;
import fr.gouv.culture.vitam.utils.Commands;
import fr.gouv.culture.vitam.utils.ConfigLoader;
import fr.gouv.culture.vitam.utils.StaticValues;
import fr.gouv.culture.vitam.utils.VitamArgument;
import fr.gouv.culture.vitam.utils.XmlDom;

/**
 * Based on https://github.com/rjohnsondev/java-libpst
 * 
 * @author "Frederic Bregier"
 * 
 */
public class PstExtract {
	String filename;
	int depth = -1;
	VitamArgument argument;
	ConfigLoader config;
	File curPath = null;
	public static boolean extractSeparateXmlFolder = true;
	
	/**
	 * @param filename
	 */
	private PstExtract(String filename, VitamArgument argument, ConfigLoader config) {
		this.filename = filename;
		this.argument = argument;
		this.config = config;
	}


	private void printDepth() {
		for (int x = 0; x < depth - 1; x++) {
			System.out.print(" | ");
		}
		System.out.print(" |- ");
	}

	/**
	 * Try to extract the following :
	 * 
	 * Taken from : http://www.significantproperties.org.uk/email-testingreport.html
	 * 
	 * message-id (Message-ID), References (References), In-Reply-To (In-Reply-To), Attachment
	 * subject (Subject), keywords sent-date (Date), Received-date (in Received last date),
	 * Trace-field (Received?)
	 * 
	 * 
	 * From (From), To (To), CC (Cc), BCC (Bcc), Content-Type, Content-Transfer-Encoding
	 * 
	 * ? DomainKey-Signature, Sender, X-Original-Sender, X-Forwarded-Message-Id,
	 * 
	 * 1) Core property set
	 * 
	 * The core property set indicates the minimum amount of information that is considered
	 * necessary to establish the authenticity and integrity of the email message
	 * 
	 * Local-part, Domain-part, Relationship, Subject, Trace-field , Message body with no mark-up,
	 * Attachments
	 * 
	 * 2) Message thread scenario
	 * 
	 * Email is frequently used as a communication method between two or more people. To understand
	 * the context in which a message was created it may be necessary to refer to earlier messages.
	 * To identify the thread of a discussion, the following fields should be provided, in addition
	 * to the core property set:
	 * 
	 * Local-part, Domain-part, Relationship, Subject, Trace-field, Message body with no mark-up,
	 * Attachments, Message-ID, References
	 * 
	 * 3) Recommended property set
	 * 
	 * The recommended property set indicates additional information that should be provided in an
	 * ideal scenario, if it is present within the email. The list
	 * 
	 * Local-part, Domain-part, Domain-literal (if present), Relationship, Subject, Trace-field,
	 * Attachments, Message-ID, References, Sent-date, Received date, Display name, In-reply-to,
	 * Keywords, Message body & associated mark-up (see table 6 for scenarios)
	 * 
	 * 
	 * 
	 * @param emlFile
	 * @param filename
	 * @param argument
	 * @param config
	 * @return
	 */
	public static Element extractInfoPst(File pstFile, VitamArgument argument, ConfigLoader config) {
		System.out.println("Start PST: "+ (new Date().toString()));
		PstExtract extract = new PstExtract(pstFile.getAbsolutePath(), argument, config);
		PSTFolder folder = null;
		try {
			PSTFile pstFile2 = new PSTFile(extract.filename);
			System.out.println(pstFile2.getMessageStore().getDisplayName());
			extract.currentRoot = extract.pstRoot;
			extract.currentRoot.addAttribute(EMAIL_FIELDS.filename.name, pstFile.getPath());
			// XXX FIXME multiple output
			if (argument.extractFile) {
				extract.curPath = new File(argument.outputDir, "PST_"+pstFile.getName());
				extract.curPath.mkdirs();
			} else if (extractSeparateXmlFolder) {
				extract.curPath = new File(pstFile.getParentFile(), "PST_"+pstFile.getName());
				extract.curPath.mkdirs();
			}
			folder = pstFile2.getRootFolder();
		} catch (Exception err) {
			err.printStackTrace();
			return null;
		}
		if (extractSeparateXmlFolder) {
			try {
				extract.writer = new XMLWriter(System.out, StaticValues.defaultOutputFormat);
			} catch (UnsupportedEncodingException e1) {
				System.err.println(StaticValues.LBL.error_writer.get() + e1.toString());
			}
		}
		extract.extractInfoFolder(folder);
		DroidHandler.cleanTempFiles();
		extract.currentRoot.addAttribute("nbMsg", config.nbDoc.toString());
		System.out.println("Stop PST: "+ (new Date().toString()));
		return extract.pstRoot;
	}

	Element pstRoot = XmlDom.factory.createElement(EMAIL_FIELDS.formatPST.name);
	Element currentRoot;
	protected XMLWriter writer;

	private void extractInfoFolder(PSTFolder folder) {
		depth++;
		Element curdepth = currentRoot;
		// the root folder doesn't have a display name
		if (depth > 0) {
			printDepth();
			System.out.println(folder.getDisplayName());
		}

		// go through the folders...
		if (folder.hasSubfolders()) {
			Vector<PSTFolder> childFolders;
			try {
				childFolders = folder.getSubFolders();
				for (PSTFolder childFolder : childFolders) {
					Element nextdepth = XmlDom.factory.createElement(EMAIL_FIELDS.folder.name);
					nextdepth.addAttribute(EMAIL_FIELDS.folderName.name, childFolder.getDisplayName());
					File pastDir = curPath;
					if (argument.extractFile || (extractSeparateXmlFolder && writer != null)) {
						// XXX FIXME multiple output
						curPath = new File(curPath, childFolder.getDisplayName());
						curPath.mkdir();
						nextdepth.addAttribute(EMAIL_FIELDS.folderFile.name, curPath.getPath());
					}
					currentRoot = nextdepth;
					long before = config.nbDoc.get();
					extractInfoFolder(childFolder);
					long after = config.nbDoc.get();
					currentRoot.addAttribute("nbSubMsg", Long.toString(after-before));
					currentRoot.addAttribute(EMAIL_FIELDS.status.name, "ok");
					if (extractSeparateXmlFolder && writer != null) {
						File separate = new File(curPath, "info_"+childFolder.getDisplayName()+".xml");
						FileOutputStream out2 = null;
						try {
							out2 = new FileOutputStream(separate);
							writer.setOutputStream(out2);
							writer.write(nextdepth);
							writer.flush();
							nextdepth = XmlDom.factory.createElement(EMAIL_FIELDS.folder.name);
							nextdepth.addAttribute(EMAIL_FIELDS.folderName.name, childFolder.getDisplayName());
							nextdepth.addAttribute(EMAIL_FIELDS.filename.name, separate.getPath());
							nextdepth.addAttribute("nbSubMsg", Long.toString(after-before));
							nextdepth.addAttribute(EMAIL_FIELDS.status.name, "ok");
						} catch (UnsupportedEncodingException e) {
						} catch (IOException e) {
						} finally {
							try {
								if (out2 != null) {
									out2.close();
								}
							} catch (IOException e) {
							}
						}
					}
					curdepth.add(nextdepth);

					// XXX FIXME if multiple files as output => curPath + metadata.xml = 
					// currentRoot to duplicate (single node) and detach to save
					curPath = pastDir;
					currentRoot = curdepth;
				}
			} catch (PSTException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// and now the emails for this folder
		if (folder.getContentCount() > 0) {
			depth++;
			currentRoot = curdepth;
			PSTMessage email;
			try {
				email = (PSTMessage) folder.getNextChild();
				while (email != null) {
					System.out.print('.');
					extractInfoMessage(email);
					email = (PSTMessage) folder.getNextChild();
				}
			} catch (PSTException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			depth--;
			System.out.println();
		}
		depth--;
	}

	private String extractInfoAttachment(PSTAttachment attachment, Element identification) {
		Element newElt = XmlDom.factory.createElement(EMAIL_FIELDS.subidentity.name);
		String filename = null;
		String result = "";
		Date creationTime = attachment.getCreationTime();
		if (creationTime != null) {
			Element elt = XmlDom.factory.createElement(EMAIL_FIELDS.creationTime.name);
			elt.setText(creationTime.toString());
			newElt.add(elt);
		}
		boolean isMsg = false;
		try {
			PSTMessage msg = attachment.getEmbeddedPSTMessage();
			if (msg != null) {
				Element cur = currentRoot;
				currentRoot = newElt;
				if (argument.extractKeyword) {
					result = extractInfoMessage(msg);
				} else {
					extractInfoMessage(msg);
				}
				currentRoot = cur;
				isMsg = true;
			}
		} catch (IOException e) {
		} catch (PSTException e) {
		}
		if (!isMsg) {
			// Check file
			filename = attachment.getLongFilename();
			if (filename.isEmpty()) {
				filename = attachment.getFilename();
			}
			filename = StringUtils.toFileName(filename);
			long size = 0;
			if (true) {
				InputStream attachmentStream = null;
				FileOutputStream out = null;
				File filetemp = null;
				try {
					attachmentStream = attachment.getFileInputStream();
					String tempfilename = filename.isEmpty() ? (config.nbDoc.get()+1)+"_unknownAttachment.eml" : filename;
					// Force out as eml
					if (argument.extractFile) {
						filetemp = new File(curPath, tempfilename);
					} else {
						filetemp = File.createTempFile(StaticValues.PREFIX_TEMPFILE, tempfilename);
					}
					out = new FileOutputStream(filetemp);
					// 8176 is the block size used internally and should give the best performance
					int bufferSize = 8176;
					byte[] buffer = new byte[bufferSize];
					int count;
					do {
						count = attachmentStream.read(buffer);
						if (count >= 0) {
							out.write(buffer, 0, count);
							size += count;
						}
					} while (count == bufferSize);
					out.close();
					attachmentStream.close();
					// Now check file against Droid or more
					try {
						Commands.addFormatIdentification(newElt, filename, filetemp, config, argument);
						if (argument.extractKeyword) {
							// get back keyword in the main list
							Element keyw = (Element) newElt.selectSingleNode(EMAIL_FIELDS.keywords.name);
							if (keyw != null) {
								StringBuilder builder = new StringBuilder();
								@SuppressWarnings("unchecked")
								List<Element> elts = (List<Element>) keyw.selectNodes(EMAIL_FIELDS.keywordRank.name);
								for (Element elt : elts) {
									String value = elt.attributeValue(EMAIL_FIELDS.keywordOccur.name);
									int occur = Integer.parseInt(value) / 2 + 1;
									@SuppressWarnings("unchecked")
									List<Element> words = (List<Element>) elt.selectNodes(EMAIL_FIELDS.keywordWord.name);
									for (Element eword : words) {
										String word = eword.attributeValue(EMAIL_FIELDS.keywordValue.name) + " ";
										for (int i = 0; i < occur; i++) {
											builder.append(word);
										}
									}
								}
								result = builder.toString().trim();
							}
						}
	
					} catch (Exception e) {
						config.addRankId(newElt);
						//String id = Long.toString(config.nbDoc.incrementAndGet());
						//newElt.addAttribute(EMAIL_FIELDS.rankId.name, id);
						String status = "Error during identification";
						e.printStackTrace();
						newElt.addAttribute(EMAIL_FIELDS.status.name, status);
						return "";
					}
					// then clear
					if (! argument.extractFile) {
						filetemp.delete();
					}
				} catch (IOException e) {
					config.addRankId(newElt);
					//String id = Long.toString(config.nbDoc.incrementAndGet());
					//newElt.addAttribute(EMAIL_FIELDS.rankId.name, id);
					e.printStackTrace();
					String status = "Error during access to attachment";
					newElt.addAttribute(EMAIL_FIELDS.status.name, status);
					identification.add(newElt);
					return "";
				} catch (PSTException e) {
					config.addRankId(newElt);
					//String id = Long.toString(config.nbDoc.incrementAndGet());
					//newElt.addAttribute(EMAIL_FIELDS.rankId.name, id);
					e.printStackTrace();
					String status = "Error during access to attachment";
					newElt.addAttribute(EMAIL_FIELDS.status.name, status);
					identification.add(newElt);
					return "";
				} finally {
					if (filetemp != null && ! argument.extractFile) {
						filetemp.delete();
					}
					if (out != null) {
						try {
							out.close();
						} catch (IOException e2) {
						}
					}
					if (attachmentStream != null) {
						try {
							attachmentStream.close();
						} catch (IOException e2) {
						}
					}
				}
			}
			if (filename != null) {
				Element elt = XmlDom.factory.createElement(EMAIL_FIELDS.filename.name);
				elt.setText(filename);
				newElt.add(elt);
			}
			if (size == 0) {
				size = attachment.getAttachSize();
			}
			if (size > 0) {
				Element elt = XmlDom.factory.createElement(EMAIL_FIELDS.attSize.name);
				elt.setText(Long.toString(size));
				newElt.add(elt);
			}
		}
		identification.add(newElt);
		return result;
	}

	private static void addAddress(Element root, String entry, String name, String address) {
		Element val = XmlDom.factory.createElement(entry);
		if (name != null && name.length() > 0) {
			Element nm = XmlDom.factory.createElement(EMAIL_FIELDS.emailName.name);
			nm.setText(StringUtils.unescapeHTML(name, true, false));
			val.add(nm);
		}
		if (address != null && address.length() > 0) {
			Element nm = XmlDom.factory.createElement(EMAIL_FIELDS.emailAddress.name);
			nm.setText(StringUtils.unescapeHTML(address, true, false));
			val.add(nm);
		}
		root.add(val);
	}
	
	private Element extractInfoContact(PSTContact contact) {
		Element root = XmlDom.factory.createElement("contact");
		String value = null;
		
		value = contact.getAccount();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("AccountName").addText(value));
		}
		value = contact.getGivenName();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("GivenName").addText(value));
		}
		value = contact.getSurname();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("SurName").addText(value));
		}
		value = contact.getSMTPAddress();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("SMTPAddress").addText(value));
		}
		value = contact.getEmail1AddressType();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("Email1AddressType").addText(value));
		}
		value = contact.getEmail1EmailAddress();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("Email1Address").addText(value));
		}
		value = contact.getCallbackTelephoneNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("CallbackTelephoneNumber").addText(value));
		}
		value = contact.getGeneration();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("Generation").addText(value));
		}
		value = contact.getGovernmentIdNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("GovernmentIDNumber").addText(value));
		}
		value = contact.getBusinessTelephoneNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("BusinessTelephoneNumber").addText(value));
		}
		value = contact.getHomeTelephoneNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("HomeTelephoneNumber").addText(value));
		}
		value = contact.getInitials();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("Initials").addText(value));
		}
		value = contact.getKeyword();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("Keyword").addText(value));
		}
		value = contact.getLanguage();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("Language").addText(value));
		}
		value = contact.getLocation();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("Location").addText(value));
		}
		value = contact.getMhsCommonName();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("MHSCommonName").addText(value));
		}
		value = contact.getOrganizationalIdNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("OrganizationalIdnumber").addText(value));
		}
		value = contact.getOriginalDisplayName();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("OriginalDisplayName").addText(value));
		}
		value = contact.getPostalAddress();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("PostalAddress").addText(value));
		}
		value = contact.getCompanyName();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("CompanyName").addText(value));
		}
		value = contact.getTitle();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("Title").addText(value));
		}
		value = contact.getDepartmentName();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("DepartmentName").addText(value));
		}
		value = contact.getOfficeLocation();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("OfficeLocation").addText(value));
		}
		value = contact.getPrimaryTelephoneNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("PrimaryTelephone").addText(value));
		}
		value = contact.getBusiness2TelephoneNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("SecondaryBusinessTelephoneNumber").addText(value));
		}
		value = contact.getMobileTelephoneNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("MobileTelephoneNumber").addText(value));
		}
		value = contact.getRadioTelephoneNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("RadioTelephoneNumber").addText(value));
		}
		value = contact.getCarTelephoneNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("CarTelephoneNumber").addText(value));
		}
		value = contact.getOtherTelephoneNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("OtherTelephoneNumber").addText(value));
		}
		value = contact.getTransmittableDisplayName();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("TransmittableDisplayName").addText(value));
		}
		value = contact.getPagerTelephoneNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("PagerTelephoneNumber").addText(value));
		}
		value = contact.getPrimaryFaxNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("PrimaryFaxNumber").addText(value));
		}
		value = contact.getBusinessFaxNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("BusinessFaxNumber").addText(value));
		}
		value = contact.getHomeFaxNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("HomeFaxNumber").addText(value));
		}
		value = contact.getBusinessAddressCountry();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("BusinessAddressCountry").addText(value));
		}
		value = contact.getBusinessAddressCity();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("BusinessAddressCity").addText(value));
		}
		value = contact.getBusinessAddressStateOrProvince();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("BusinessAddressState").addText(value));
		}
		value = contact.getBusinessAddressStreet();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("BusinessAddressStreet").addText(value));
		}
		value = contact.getBusinessPostalCode();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("BusinessPostalCode").addText(value));
		}
		value = contact.getBusinessPoBox();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("BusinessPOBox").addText(value));
		}
		value = contact.getTelexNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("TelexNumber").addText(value));
		}
		value = contact.getIsdnNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("ISDNNumber").addText(value));
		}
		value = contact.getAssistantTelephoneNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("AssistantTelephoneNumber").addText(value));
		}
		value = contact.getHome2TelephoneNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("HomeTelephone2").addText(value));
		}
		value = contact.getAssistant();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("Assistant").addText(value));
		}
		value = contact.getHobbies();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("Hobbies").addText(value));
		}
		value = contact.getMiddleName();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("MiddleName").addText(value));
		}
		value = contact.getDisplayNamePrefix();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("DisplayNamePrefix").addText(value));
		}
		value = contact.getProfession();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("Profession").addText(value));
		}
		value = contact.getPreferredByName();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("PreferredByName").addText(value));
		}
		value = contact.getSpouseName();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("SpouseName").addText(value));
		}
		value = contact.getComputerNetworkName();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("ComputerNetworkName").addText(value));
		}
		value = contact.getCustomerId();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("CustomerID").addText(value));
		}
		value = contact.getTtytddPhoneNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("TTY_TDDPhone").addText(value));
		}
		value = contact.getFtpSite();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("FtpSite").addText(value));
		}
		value = contact.getManagerName();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("ManagerName").addText(value));
		}
		value = contact.getNickname();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("Nickname").addText(value));
		}
		value = contact.getPersonalHomePage();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("PersonalHomePage").addText(value));
		}
		value = contact.getBusinessHomePage();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("BusinessHomePage").addText(value));
		}
		value = contact.getCompanyMainPhoneNumber();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("CompanyMainPhone").addText(value));
		}
		value = contact.getChildrensNames();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("ChildrensNames").addText(value));
		}
		value = contact.getHomeAddressCity();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("HomeAddressCity").addText(value));
		}
		value = contact.getHomeAddressCountry();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("HomeAddressCountry").addText(value));
		}
		value = contact.getHomeAddressPostalCode();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("HomeAddressPostalCode").addText(value));
		}
		value = contact.getHomeAddressStateOrProvince();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("HomeAddressState").addText(value));
		}
		value = contact.getHomeAddressStreet();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("HomeAddressStreet").addText(value));
		}
		value = contact.getHomeAddressPostOfficeBox();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("HomeAddressPostOfficeBox").addText(value));
		}
		value = contact.getOtherAddressCity();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("OtherAddressCity").addText(value));
		}
		value = contact.getOtherAddressCountry();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("OtherAddressCountry").addText(value));
		}
		value = contact.getOtherAddressPostalCode();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("OtherAddressPostalCode").addText(value));
		}
		value = contact.getOtherAddressStateOrProvince();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("OtherAddressState").addText(value));
		}
		value = contact.getOtherAddressStreet();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("OtherAddressStreet").addText(value));
		}
		value = contact.getOtherAddressPostOfficeBox();
		if (value != null && ! value.isEmpty()) {
		root.add(XmlDom.factory.createElement("OtherAddressPostOfficeBox").addText(value));
		}
		value = contact.getBody();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("contactBodyref").addText(value));
		}
		return root;
	}
	private Element extractInfoTask(PSTTask task) {
		Element root = XmlDom.factory.createElement("task");

		String value = null;
		
		Integer ival = task.getTaskStatus();
		root.add(XmlDom.factory.createElement("TaskStatus").addText(ival.toString()));
		Double dval = task.getPercentComplete();
		root.add(XmlDom.factory.createElement("PercentComplete").addText(dval.toString()));
		Boolean bval = task.isTeamTask();
		root.add(XmlDom.factory.createElement("isTeamTask").addText(bval.toString()));
		Date date = task.getTaskStartDate();
		if (date != null) {
			root.add(XmlDom.factory.createElement("TaskStartDate").addText(date.toString()));
		}
		date = task.getTaskDueDate();
		if (date != null) {
			root.add(XmlDom.factory.createElement("TaskDueDate").addText(date.toString()));
		}
		date = task.getTaskDateCompleted();
		if (date != null) {
			root.add(XmlDom.factory.createElement("TaskDateCompleted").addText(date.toString()));
		}
		ival = task.getTaskActualEffort();
		root.add(XmlDom.factory.createElement("TaskActualEffort").addText(ival.toString()));
		ival = task.getTaskEstimatedEffort();
		root.add(XmlDom.factory.createElement("TaskEstimatedEffort").addText(ival.toString()));
		ival = task.getTaskVersion();
		root.add(XmlDom.factory.createElement("TaskVersion").addText(ival.toString()));
		bval = task.isTaskComplete();
		root.add(XmlDom.factory.createElement("isTaskComplete").addText(bval.toString()));
		value = task.getTaskOwner();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("TaskOwner").addText(value));
		}
		value = task.getTaskAssigner();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("TaskAssigner").addText(value));
		}
		value = task.getTaskLastUser();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("TaskLastUser").addText(value));
		}
		ival = task.getTaskOrdinal();
		root.add(XmlDom.factory.createElement("TaskOrdinal").addText(ival.toString()));
		bval = task.isTaskFRecurring();
		root.add(XmlDom.factory.createElement("isTaskFRecurring").addText(bval.toString()));
		value = task.getTaskRole();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("TaskRole").addText(value));
		}
		ival = task.getTaskOwnership();
		root.add(XmlDom.factory.createElement("TaskOwnership").addText(ival.toString()));
		ival = task.getAcceptanceState();
		root.add(XmlDom.factory.createElement("AcceptanceState").addText(ival.toString()));
		return root;
	}
	private Element extractInfoActivity(PSTActivity activity) {
		Element root = XmlDom.factory.createElement("activity");

		String value = null;
		
		value = activity.getLogType();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("LogType").addText(value));
		}
		Date date = activity.getLogStart();
		if (date != null) {
			root.add(XmlDom.factory.createElement("LogStart").addText(date.toString()));
		}
		Integer ival = activity.getLogDuration();
		root.add(XmlDom.factory.createElement("LogDuration").addText(ival.toString()));
		date = activity.getLogEnd();
		if (date != null) {
			root.add(XmlDom.factory.createElement("LogEnd").addText(date.toString()));
		}
		ival = activity.getLogFlags();
		root.add(XmlDom.factory.createElement("LogFlags").addText(ival.toString()));
		Boolean bval = activity.isDocumentPrinted();
		root.add(XmlDom.factory.createElement("isDocumentPrinted").addText(bval.toString()));
		bval = activity.isDocumentSaved();
		root.add(XmlDom.factory.createElement("isDocumentSaved").addText(bval.toString()));
		bval = activity.isDocumentRouted();
		root.add(XmlDom.factory.createElement("isDocumentRouted").addText(bval.toString()));
		bval = activity.isDocumentPosted();
		root.add(XmlDom.factory.createElement("isDocumentPosted").addText(bval.toString()));
		value = activity.getLogTypeDesc();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("LogTypeDesc").addText(value));
		}
		return root;
	}
	private Element extractInfoRss(PSTRss rss) {
		Element root = XmlDom.factory.createElement("rss");
		
		String value = null;
		
		value = rss.getPostRssChannelLink();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("PostRssChannelLink").addText(value));
		}
		value = rss.getPostRssItemLink();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("PostRssItemLink").addText(value));
		}
		Integer ival = rss.getPostRssItemHash();
		root.add(XmlDom.factory.createElement("PostRssItemHash").addText(ival.toString()));
		value = rss.getPostRssItemGuid();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("PostRssItemGuid").addText(value));
		}
		value = rss.getPostRssChannel();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("PostRssChannel").addText(value));
		}
		value = rss.getPostRssItemXml();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("PostRssItemXml").addText(value));
		}
		value = rss.getPostRssSubscription();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("PostRssSubscription").addText(value));
		}
		return root;
	}
	private static final String SysTime(PSTTimeZone.SYSTEMTIME st) {
		return "("+st.wDayOfWeek+":"+st.wDay+":"+st.wMonth+":"+st.wYear+"-"+st.wHour+":"+st.wMinute+":"+st.wSecond+":"+st.wMilliseconds+")";
	}
	private static final String TZString(PSTTimeZone zone) {
		return "{Name: "+zone.getName()+ ", Start: "+SysTime(zone.getStart())+", Bias: "+zone.getBias() +
				", DaylightBias: "+zone.getDaylightBias()+ ", DaylightStart: "+SysTime(zone.getDaylightStart()) + 
				", SimpleTimeZone: "+zone.getSimpleTimeZone().toString() + 
				", StandardBias: "+zone.getStandardBias() + ", StandardStart: "+SysTime(zone.getStandardStart())+"}";
	}
	private Element extractInfoAppointment(PSTAppointment appointment) {
		Element root = XmlDom.factory.createElement("appointment");

		String value = null;
		
		value = appointment.getLocation();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("Location").addText(value));
		}
		Date date = appointment.getStartTime();
		if (date != null) {
			root.add(XmlDom.factory.createElement("StartTime").addText(date.toString()));
		}
		PSTTimeZone zone = appointment.getStartTimeZone();
		if (zone != null) {
			root.add(XmlDom.factory.createElement("StartTimeZone").addText(TZString(zone)));
		}
		date = appointment.getEndTime();
		if (date != null) {
			root.add(XmlDom.factory.createElement("EndTime").addText(date.toString()));
		}
		zone = appointment.getEndTimeZone();
		if (zone != null) {
			root.add(XmlDom.factory.createElement("EndTimeZone").addText(TZString(zone)));
		}
		zone = appointment.getRecurrenceTimeZone();
		if (zone != null) {
			root.add(XmlDom.factory.createElement("RecurrenceTimeZone").addText(TZString(zone)));
		}
		Integer ival = appointment.getDuration();
		root.add(XmlDom.factory.createElement("Duration").addText(ival.toString()));
		ival = appointment.getMeetingStatus();
		root.add(XmlDom.factory.createElement("MeetingStatus").addText(ival.toString()));
		ival = appointment.getResponseStatus();
		root.add(XmlDom.factory.createElement("ResponseStatus").addText(ival.toString()));
		Boolean bval = appointment.isRecurring();
		root.add(XmlDom.factory.createElement("isRecurring").addText(bval.toString()));
		date = appointment.getRecurrenceBase();
		if (date != null) {
			root.add(XmlDom.factory.createElement("RecurrenceBase").addText(date.toString()));
		}
		ival = appointment.getRecurrenceType();
		root.add(XmlDom.factory.createElement("RecurrenceType").addText(ival.toString()));
		value = appointment.getRecurrencePattern();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("RecurrencePattern").addText(value));
		}
		value = appointment.getAllAttendees();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("AllAttendees").addText(value));
		}
		value = appointment.getToAttendees();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("ToAttendees").addText(value));
		}
		value = appointment.getCCAttendees();
		if (value != null && ! value.isEmpty()) {
			root.add(XmlDom.factory.createElement("CCAttendees").addText(value));
		}
		ival = appointment.getAppointmentSequence();
		root.add(XmlDom.factory.createElement("AppointmentSequence").addText(ival.toString()));

		// online meeting properties
		bval = appointment.isOnlineMeeting();
		if (bval) {
			root.add(XmlDom.factory.createElement("isOnlineMeeting").addText(bval.toString()));
			ival = appointment.getNetMeetingType();
			root.add(XmlDom.factory.createElement("NetMeetingType").addText(ival.toString()));
			value = appointment.getNetMeetingServer();
			if (value != null && ! value.isEmpty()) {
				root.add(XmlDom.factory.createElement("NetMeetingServer").addText(value));
			}
			value = appointment.getNetMeetingOrganizerAlias();
			if (value != null && ! value.isEmpty()) {
				root.add(XmlDom.factory.createElement("NetMeetingOrganizerAlias").addText(value));
			}
			bval = appointment.getNetMeetingAutostart();
			root.add(XmlDom.factory.createElement("NetMeetingAutostart").addText(bval.toString()));
			bval = appointment.getConferenceServerAllowExternal();
			root.add(XmlDom.factory.createElement("ConferenceServerAllowExternal").addText(bval.toString()));
			value = appointment.getNetMeetingDocumentPathName();
			if (value != null && ! value.isEmpty()) {
				root.add(XmlDom.factory.createElement("NetMeetingDocumentPathName").addText(value));
			}
			value = appointment.getNetShowURL();
			if (value != null && ! value.isEmpty()) {
				root.add(XmlDom.factory.createElement("NetShowURL").addText(value));
			}
			date = appointment.getAttendeeCriticalChange();
			if (date != null) {
				root.add(XmlDom.factory.createElement("AttendeeCriticalChange").addText(date.toString()));
			}
			date = appointment.getOwnerCriticalChange();
			if (date != null) {
				root.add(XmlDom.factory.createElement("OwnerCriticalChange").addText(date.toString()));
			}
			value = appointment.getConferenceServerPassword();
			if (value != null && ! value.isEmpty()) {
				root.add(XmlDom.factory.createElement("ConferenceServerPassword").addText(value));
			}
			bval = appointment.getAppointmentCounterProposal();
			root.add(XmlDom.factory.createElement("AppointmentCounterProposal").addText(bval.toString()));
			bval = appointment.isSilent();
			root.add(XmlDom.factory.createElement("isSilent").addText(bval.toString()));
			value = appointment.getRequiredAttendees();
			if (value != null && ! value.isEmpty()) {
				root.add(XmlDom.factory.createElement("RequiredAttendees").addText(value));
			}
		}
		return root;
	}

	private String extractInfoMessage(PSTMessage email) {
		if (email instanceof PSTContact) {
			Element node = extractInfoContact((PSTContact) email);
			config.addRankId(node);
			//node.addAttribute(EMAIL_FIELDS.rankId.name, id);
			Element identifications = XmlDom.factory.createElement("identification");
			Element identity = XmlDom.factory.createElement("identity");
			identity.addAttribute("format", "Microsoft Outlook Address Book");
			identity.addAttribute("mime", "application/vnd.ms-outlook");
			identifications.add(identity);
			node.add(identifications);
			node.addAttribute(EMAIL_FIELDS.status.name, "ok");
			currentRoot.add(node);
			return "";
		} else if (email instanceof PSTTask) {
			Element node = extractInfoTask((PSTTask) email);
			config.addRankId(node);
			//node.addAttribute(EMAIL_FIELDS.rankId.name, id);
			Element identifications = XmlDom.factory.createElement("identification");
			Element identity = XmlDom.factory.createElement("identity");
			identity.addAttribute("format", "Microsoft Outlook Task");
			identity.addAttribute("mime", "application/vnd.ms-outlook");
			identifications.add(identity);
			node.add(identifications);
			node.addAttribute(EMAIL_FIELDS.status.name, "ok");
			currentRoot.add(node);
			return "";
		} else if (email instanceof PSTActivity) {
			Element node = extractInfoActivity((PSTActivity) email);
			config.addRankId(node);
			//node.addAttribute(EMAIL_FIELDS.rankId.name, id);
			Element identifications = XmlDom.factory.createElement("identification");
			Element identity = XmlDom.factory.createElement("identity");
			identity.addAttribute("format", "Microsoft Outlook Activity");
			identity.addAttribute("mime", "application/vnd.ms-outlook");
			identifications.add(identity);
			node.add(identifications);
			node.addAttribute(EMAIL_FIELDS.status.name, "ok");
			currentRoot.add(node);
			return "";
		} else if (email instanceof PSTRss) {
			Element node = extractInfoRss((PSTRss) email);
			config.addRankId(node);
			//node.addAttribute(EMAIL_FIELDS.rankId.name, id);
			Element identifications = XmlDom.factory.createElement("identification");
			Element identity = XmlDom.factory.createElement("identity");
			identity.addAttribute("format", "Microsoft Outlook Rss");
			identity.addAttribute("mime", "application/vnd.ms-outlook");
			identifications.add(identity);
			node.add(identifications);
			node.addAttribute(EMAIL_FIELDS.status.name, "ok");
			currentRoot.add(node);
			return "";
		} else if (email instanceof PSTAppointment) {
			Element node = extractInfoAppointment((PSTAppointment) email);
			config.addRankId(node);
			//node.addAttribute(EMAIL_FIELDS.rankId.name, id);
			Element identifications = XmlDom.factory.createElement("identification");
			Element identity = XmlDom.factory.createElement("identity");
			identity.addAttribute("format", "Microsoft Outlook Appointment");
			identity.addAttribute("mime", "application/vnd.ms-outlook");
			identifications.add(identity);
			node.add(identifications);
			node.addAttribute(EMAIL_FIELDS.status.name, "ok");
			currentRoot.add(node);
			return "";
		}
		Element root = XmlDom.factory.createElement(EMAIL_FIELDS.formatMSG.name);
		Element keywords = XmlDom.factory.createElement(EMAIL_FIELDS.keywords.name);
		Element metadata = XmlDom.factory.createElement(EMAIL_FIELDS.metadata.name);

		String id = config.addRankId(root);
		//root.addAttribute(EMAIL_FIELDS.rankId.name, id);
		Element identifications = XmlDom.factory.createElement("identification");
		Element identity = XmlDom.factory.createElement("identity");
		identity.addAttribute("format", "Microsoft Outlook Email Message");
		identity.addAttribute("mime", "application/vnd.ms-outlook");
		identity.addAttribute("puid", "x-fmt/430");
		identity.addAttribute("extensions", "msg");
		identifications.add(identity);
		root.add(identifications);

		Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.from.name);
		addAddress(sub, EMAIL_FIELDS.fromUnit.name, email.getSenderName(), email.getSenderEmailAddress());
		metadata.add(sub);
		int NumberOfRecipients = 0;
		Element toRecipients = XmlDom.factory.createElement(EMAIL_FIELDS.toRecipients.name);
		Element ccRecipients = XmlDom.factory.createElement(EMAIL_FIELDS.ccRecipients.name);
		Element bccRecipients = XmlDom.factory.createElement(EMAIL_FIELDS.bccRecipients.name);
		try {
			NumberOfRecipients = email.getNumberOfRecipients();
		} catch (PSTException e1) {
		} catch (IOException e1) {
		}
		for (int i = 0; i < NumberOfRecipients; i++) {
			try {
				PSTRecipient recipient = email.getRecipient(i);
				// MAPI_TO = 1; MAPI_CC = 2; MAPI_BCC = 3;
				Element choose = null;
				String type = "??";
				switch (recipient.getRecipientType()) {
					case PSTRecipient.MAPI_TO:
						type = EMAIL_FIELDS.toUnit.name;
						choose = toRecipients;
						break;
					case PSTRecipient.MAPI_CC:
						type = EMAIL_FIELDS.ccUnit.name;
						choose = ccRecipients;
						break;
					case PSTRecipient.MAPI_BCC:
						type = EMAIL_FIELDS.bccUnit.name;
						choose = bccRecipients;
						break;
				}
				if (choose != null) {
					addAddress(choose, type, recipient.getDisplayName(),
							recipient.getEmailAddress());
				}
			} catch (PSTException e) {
			} catch (IOException e) {
			}
		}
		if (toRecipients.hasContent()) {
			metadata.add(toRecipients);
		}
		if (ccRecipients.hasContent()) {
			metadata.add(ccRecipients);
		}
		if (bccRecipients.hasContent()) {
			metadata.add(bccRecipients);
		}
		// get the subject
		String Subject = email.getSubject();
		if (Subject != null) {
			sub = XmlDom.factory.createElement(EMAIL_FIELDS.subject.name);
			sub.setText(StringUtils.unescapeHTML(Subject, true, false));
			metadata.add(sub);
		}
		// Conversation topic This is basically the subject from which Fwd:, Re, etc.
		Subject = email.getConversationTopic();
		if (Subject != null) {
			sub = XmlDom.factory.createElement(EMAIL_FIELDS.conversationTopic.name);
			sub.setText(StringUtils.unescapeHTML(Subject, true, false));
			metadata.add(sub);
		}
		// get the client submit time (sent ?)
		Date ClientSubmitTime = email.getClientSubmitTime();
		if (ClientSubmitTime != null) {
			sub = XmlDom.factory.createElement(EMAIL_FIELDS.sentDate.name);
			sub.setText(ClientSubmitTime.toString());
			metadata.add(sub);
		}
		// Message delivery time
		Date MessageDeliveryTime = email.getMessageDeliveryTime();
		if (MessageDeliveryTime != null) {
			sub = XmlDom.factory.createElement(EMAIL_FIELDS.receivedDate.name);
			sub.setText(MessageDeliveryTime.toString());
			metadata.add(sub);
		}
		// Transport message headers ASCII or Unicode string These contain the SMTP e-mail headers.
		String TransportMessageHeaders = email.getTransportMessageHeaders();
		if (TransportMessageHeaders != null) {
			sub = XmlDom.factory.createElement(EMAIL_FIELDS.receptionTrace.name);
			sub.add(XmlDom.factory.createElement(EMAIL_FIELDS.trace.name)
					.addText(StringUtils.unescapeHTML(TransportMessageHeaders, true, false)));
			metadata.add(sub);
			TransportMessageHeaders = null;
		}
		long internalSize = email.getMessageSize();
		if (internalSize > 0) {
			sub = XmlDom.factory.createElement(EMAIL_FIELDS.emailSize.name);
			sub.setText(Long.toString(internalSize));
			metadata.add(sub);
		}
		// Message ID for this email as allocated per rfc2822
		String InternetMessageId = email.getInternetMessageId();
		if (InternetMessageId != null) {
			InternetMessageId = StringUtils.removeChevron(StringUtils.unescapeHTML(
					InternetMessageId, true, false)).trim();
			if (InternetMessageId.length() > 1) {
				sub = XmlDom.factory.createElement(EMAIL_FIELDS.messageId.name);
				sub.setText(InternetMessageId);
				metadata.add(sub);
			}
		}
		// In-Reply-To
		String InReplyToId = email.getInReplyToId();
		if (InReplyToId != null) {
			InReplyToId = StringUtils.removeChevron(StringUtils.unescapeHTML(InReplyToId, true,
					false)).trim();
			if (InReplyToId.length() > 1) {
				sub = XmlDom.factory.createElement(EMAIL_FIELDS.inReplyTo.name);
				sub.setText(InReplyToId);
				if (InternetMessageId != null && InternetMessageId.length() > 1) {
					String old = EmlExtract.filEmls.get(InReplyToId);
					if (old == null) {
						old = InternetMessageId;
					} else {
						old += "," + InternetMessageId;
					}
					EmlExtract.filEmls.put(InReplyToId, old);
				}
				metadata.add(sub);
			}
			InReplyToId = null;
			InternetMessageId = null;
		}
		sub = XmlDom.factory.createElement(EMAIL_FIELDS.properties.name);
		// is the action flag for this item "forward"?
		boolean Forwarded = email.hasForwarded();
		sub.addAttribute(EMAIL_FIELDS.propForwarded.name, Boolean.toString(Forwarded));
		// is the action flag for this item "replied"?
		boolean Replied = email.hasReplied();
		sub.addAttribute(EMAIL_FIELDS.propReplied.name, Boolean.toString(Replied));
		//
		boolean Read = email.isRead();
		sub.addAttribute(EMAIL_FIELDS.propRead.name, Boolean.toString(Read));
		//
		boolean Unsent = email.isUnsent();
		sub.addAttribute(EMAIL_FIELDS.propUnsent.name, Boolean.toString(Unsent));
		// Recipient Reassignment Prohibited Boolean 0 = false 0 != true
		boolean RecipientReassignmentProhibited = email.getRecipientReassignmentProhibited();
		sub.addAttribute(EMAIL_FIELDS.propRecipientReassignmentProhibited.name,
				Boolean.toString(RecipientReassignmentProhibited));
		// get the importance of the email
		// PSTMessage.IMPORTANCE_LOW + PSTMessage.IMPORTANCE_NORMAL + PSTMessage.IMPORTANCE_HIGH
		int Importance = email.getImportance();
		String imp = "??";
		switch (Importance) {
			case PSTMessage.IMPORTANCE_LOW:
				imp = "LOW";
				break;
			case PSTMessage.IMPORTANCE_NORMAL:
				imp = "NORMAL";
				break;
			case PSTMessage.IMPORTANCE_HIGH:
				imp = "HIGH";
				break;
		}
		sub.addAttribute(EMAIL_FIELDS.importance.name, imp);
		// Priority Integer 32-bit signed -1 = NonUrgent 0 = Normal 1 = Urgent
		int Priority = email.getPriority();
		switch (Priority) {
			case -1:
				imp = "LOW";
				break;
			case 0:
				imp = "NORMAL";
				break;
			case 1:
				imp = "HIGH";
				break;
			default:
				imp = "LEV" + Priority;
		}
		sub.addAttribute(EMAIL_FIELDS.priority.name, imp);
		// Sensitivity Integer 32-bit signed sender's opinion of the sensitivity of an email 0 =
		// None 1 = Personal 2 = Private 3 = Company Confidential
		int Sensitivity = email.getSensitivity();
		String sens = "??";
		switch (Sensitivity) {
			case 0:
				sens = "None";
				break;
			case 1:
				sens = "Personal";
				break;
			case 2:
				sens = "Private";
				break;
			case 3:
				sens = "Confidential";
				break;
		}
		sub.addAttribute(EMAIL_FIELDS.sensitivity.name, sens);

		//
		boolean Attachments = email.hasAttachments();
		sub.addAttribute(EMAIL_FIELDS.hasAttachment.name, Boolean.toString(Attachments));
		metadata.add(sub);
		
		String result = "";
		Element identification = null;
		if (Attachments) {
			File oldPath = curPath;
			if (argument.extractFile) {
				File newDir = new File(curPath, id);
				newDir.mkdir();
				curPath = newDir;
			}
			identification = XmlDom.factory.createElement(EMAIL_FIELDS.attachments.name);
			// get the number of attachments for this message
			int NumberOfAttachments = email.getNumberOfAttachments();
			identification.addAttribute(EMAIL_FIELDS.attNumber.name, Integer.toString(NumberOfAttachments));
			// get a specific attachment from this email.
			for (int attachmentNumber = 0; attachmentNumber < NumberOfAttachments; attachmentNumber++) {
				try {
					PSTAttachment attachment = email.getAttachment(attachmentNumber);
					if (argument.extractKeyword) {
						result += " " + extractInfoAttachment(attachment, identification);
					} else {
						extractInfoAttachment(attachment, identification);
					}
				} catch (PSTException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			curPath = oldPath;
		}
		// Plain text e-mail body
		String body = "";
		if (argument.extractKeyword || argument.extractFile) {
			body = email.getBody();
			boolean isTxt = true;
			boolean isHttp = false;
			if (body == null || body.isEmpty()) {
				isTxt = false;
				body = email.getBodyHTML();
				isHttp = true;
				if (body == null || body.isEmpty()) {
					isHttp = false;
					try {
						body = email.getRTFBody();
					} catch (PSTException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (body != null && ! body.isEmpty()) {
				if (argument.extractFile) {
					// XXX FIXME could saved email from HTML Body (clearer) if possible
					// use curRank in name, and attachment will be under directory named
					// add currank in field
					File newDir = new File(curPath, id);
					newDir.mkdir();
					String filenamebody = InternetMessageId;
					if (filenamebody == null || ! filenamebody.isEmpty()) {
						filenamebody = id;
					}
					String html = null;
					if (isHttp) {
						html = body;
					}
					String rtf = null;
					if (!isTxt && ! isHttp) {
						rtf = body;
					}
					if (isTxt) {
						FileOutputStream output = null;
						try {
							output = new FileOutputStream(new File(newDir, filenamebody+".txt"));
							byte []bb = body.getBytes(StaticValues.CURRENT_OUTPUT_ENCODING);
							output.write(bb, 0, bb.length);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							if (output != null) {
								try {
									output.close();
								} catch (IOException e) {
								}
							}
						}
						html = email.getBodyHTML();
					}
					if (html != null && ! html.isEmpty()) {
						FileOutputStream output = null;
						try {
							output = new FileOutputStream(new File(newDir, filenamebody+".html"));
							byte []bb = html.getBytes(StaticValues.CURRENT_OUTPUT_ENCODING);
							output.write(bb, 0, bb.length);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							if (output != null) {
								try {
									output.close();
								} catch (IOException e) {
								}
							}
						}
						html = null;
					}
					if (isTxt || isHttp) {
						try {
							rtf = email.getRTFBody();
						} catch (PSTException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (rtf != null && ! rtf.isEmpty()) {
						FileOutputStream output = null;
						try {
							output = new FileOutputStream(new File(newDir, filenamebody+".rtf"));
							byte []bb = rtf.getBytes(StaticValues.CURRENT_OUTPUT_ENCODING);
							output.write(bb, 0, bb.length);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							if (output != null) {
								try {
									output.close();
								} catch (IOException e) {
								}
							}
						}
						rtf = null;
					}
				}
			}
		}
		if (metadata.hasContent()) {
			root.add(metadata);
		}
		if (identification != null && identification.hasContent()) {
			root.add(identification);
		}
		if (argument.extractKeyword) {
			result = body + " " + result;
			body = null;
			ExtractInfo.exportMetadata(keywords, result, "", config, null);
			if (keywords.hasContent()) {
				root.add(keywords);
			}
		}
		root.addAttribute(EMAIL_FIELDS.status.name, "ok");
		currentRoot.add(root);
		return result;
	}

}
