/**
 * This file is part of Waarp Project.
 * 
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the
 * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
 * 
 * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Waarp . If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.gouv.culture.vitam.eml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.dom4j.Element;

import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;

import fr.gouv.culture.vitam.eml.StringUtils.EMAIL_FIELDS;
import fr.gouv.culture.vitam.extract.ExtractInfo;
import fr.gouv.culture.vitam.utils.Commands;
import fr.gouv.culture.vitam.utils.ConfigLoader;
import fr.gouv.culture.vitam.utils.StaticValues;
import fr.gouv.culture.vitam.utils.VitamArgument;
import fr.gouv.culture.vitam.utils.XmlDom;

/**
 * http://auxilii.com/msgparser/Page.php?id=100
 * 
 * see http://www.rgagnon.com/javadetails/java-0613.html
 * 
 * @author "Frederic Bregier"
 * 
 */
public class MsgExtract2 {

	private static void addAddress(Element root, String entry, String address) {
		Element val = XmlDom.factory.createElement(entry);
		String ad = StringUtils.selectChevron(address);
		if (ad == null) {
			ad = "";
		}
		String nams = address.replace('<' + ad + '>', "");
		if (nams.length() > 0) {
			Element name = XmlDom.factory.createElement(EMAIL_FIELDS.emailName.name);
			name.setText(StringUtils.unescapeHTML(nams, true, false));
			val.add(name);
		}
		if (ad != null && ad.length() > 0) {
			Element addresse = XmlDom.factory.createElement(EMAIL_FIELDS.emailAddress.name);
			addresse.setText(StringUtils.unescapeHTML(ad, true, false));
			val.add(addresse);
		}
		if (val.hasContent()) {
			root.add(val);
		}
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
	 * @param msgFile
	 * @param filename
	 * @param argument
	 * @param config
	 * @return
	 */
	public static Element extractInfoEmail(File msgFile, String filename, VitamArgument argument,
			ConfigLoader config) {
		File oldDir = argument.currentOutputDir;
		if (argument.currentOutputDir == null) {
			if (config.outputDir != null) {
				argument.currentOutputDir = new File(config.outputDir);
			} else {
				argument.currentOutputDir = new File(msgFile.getParentFile().getAbsolutePath());
			}
		}
		Element root = XmlDom.factory.createElement(EMAIL_FIELDS.formatMSG.name);
		try {
			//System.out.println("msg: "+msgFile.getAbsolutePath());
			MAPIMessage msg = new MAPIMessage(msgFile.getAbsolutePath());
			extractInfoSubEmail(msg, argument.currentOutputDir, root, argument, config);
		} catch (UnsupportedOperationException e) {
			System.err.println(StaticValues.LBL.error_error.get() + e.toString());
			e.printStackTrace();
			String status = "Error during identification";
			root.addAttribute(EMAIL_FIELDS.status.name, status);
		} catch (IOException e) {
			System.err.println(StaticValues.LBL.error_error.get() + e.toString());
			e.printStackTrace();
			String status = "Error during identification";
			root.addAttribute(EMAIL_FIELDS.status.name, status);
		}
		argument.currentOutputDir = oldDir;
		return root;
	}

	public static enum Keywords {
		Date("Date: "), XOriginalArrivalTime("X-OriginalArrivalTime: "), MessageId("Message-ID: "), InReplyTo(
				"In-reply-to: "),
		Received("Received: "), NextOne("	"),
		From("From: "), To("To: "), Cc("Cc: "), Bcc("Bcc: "), ReturnPath("Return-Path: "),
		Importance("Importance: "), Priority("X-Priority: "),
		XFolder("X-Folder: "), XSDOC("X-SDOC: "), Sensitivity("Sensitivity: ");

		public String name;

		private Keywords(String name) {
			this.name = name;
		}
	}

	private static String extractInfoSubEmail(MAPIMessage msg, File curDir, Element root,
			VitamArgument argument,
			ConfigLoader config) {
		File curPath = null;
		Element keywords = XmlDom.factory.createElement(EMAIL_FIELDS.keywords.name);
		Element metadata = XmlDom.factory.createElement(EMAIL_FIELDS.metadata.name);

		String id = config.addRankId(root);
		curPath = new File(curDir, "MSG_" + id);
		//System.out.println("start of "+id);
		String[] values = new String[Keywords.values().length];
		for (int i = 0; i < Keywords.values().length; i++) {
			values[i] = null;
		}
		String[] test = null;
		try {
			test = msg.getHeaders();
		} catch (ChunkNotFoundException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		}
		int lastRank = -1;
		for (String string : test) {
			if (string.startsWith(Keywords.NextOne.name) && lastRank >= 0) {
				String recv = string.substring(Keywords.NextOne.name.length());
				if (values[lastRank] == null) {
					values[lastRank] = recv;
				} else {
					values[lastRank] += (Keywords.Received.ordinal() == lastRank ? "\n" : " ")
							+ recv;
				}
			} else {
				if (string.startsWith(Keywords.Date.name)) {
					values[Keywords.Date.ordinal()] = string.substring(Keywords.Date.name
							.length());
					lastRank = -1;
				} else if (string.startsWith(Keywords.XOriginalArrivalTime.name)) {
					values[Keywords.XOriginalArrivalTime.ordinal()] = string
							.substring(Keywords.XOriginalArrivalTime.name.length());
					int pos = values[Keywords.XOriginalArrivalTime.ordinal()]
							.indexOf(" FILETIME=");
					if (pos > 0) {
						values[Keywords.XOriginalArrivalTime.ordinal()] = values[Keywords.XOriginalArrivalTime
								.ordinal()].substring(0, pos);
					}
					lastRank = -1;
				} else if (string.startsWith(Keywords.MessageId.name)) {
					values[Keywords.MessageId.ordinal()] = string
							.substring(Keywords.MessageId.name.length());
					values[Keywords.MessageId.ordinal()] = StringUtils.removeChevron(
							StringUtils.unescapeHTML(values[Keywords.MessageId.ordinal()],
									true, false)).trim();
					lastRank = -1;
				} else if (string.startsWith(Keywords.InReplyTo.name)) {
					String reply = StringUtils.removeChevron(StringUtils.unescapeHTML(
							string.substring(Keywords.InReplyTo.name.length()), true, false));
					if (values[Keywords.InReplyTo.ordinal()] == null) {
						values[Keywords.InReplyTo.ordinal()] = reply;
					} else {
						values[Keywords.InReplyTo.ordinal()] += " " + reply;
					}
					lastRank = Keywords.InReplyTo.ordinal();
				} else if (string.startsWith(Keywords.Received.name)) {
					String recv = string.substring(Keywords.Received.name.length());
					if (values[Keywords.Received.ordinal()] == null) {
						values[Keywords.Received.ordinal()] = recv;
					} else {
						values[Keywords.Received.ordinal()] += "\n" + recv;
					}
					lastRank = Keywords.Received.ordinal();
				} else if (string.startsWith(Keywords.From.name)) {
					values[Keywords.From.ordinal()] = string.substring(Keywords.From.name
							.length());
					lastRank = -1;
				} else if (string.startsWith(Keywords.To.name)) {
					if (values[Keywords.To.ordinal()] == null) {
						values[Keywords.To.ordinal()] = string.substring(Keywords.To.name
								.length());
					} else {
						values[Keywords.To.ordinal()] += " "
								+ string.substring(Keywords.To.name.length());
					}
					lastRank = Keywords.To.ordinal();
				} else if (string.startsWith(Keywords.Cc.name)) {
					if (values[Keywords.Cc.ordinal()] == null) {
						values[Keywords.Cc.ordinal()] = string.substring(Keywords.Cc.name
								.length());
					} else {
						values[Keywords.Cc.ordinal()] += " "
								+ string.substring(Keywords.Cc.name.length());
					}
					lastRank = Keywords.Cc.ordinal();
				} else if (string.startsWith(Keywords.Bcc.name)) {
					if (values[Keywords.Bcc.ordinal()] == null) {
						values[Keywords.Bcc.ordinal()] = string.substring(Keywords.Bcc.name
								.length());
					} else {
						values[Keywords.Bcc.ordinal()] += " "
								+ string.substring(Keywords.Bcc.name.length());
					}
					lastRank = Keywords.Bcc.ordinal();
				} else if (string.startsWith(Keywords.ReturnPath.name)) {
					if (values[Keywords.ReturnPath.ordinal()] == null) {
						values[Keywords.ReturnPath.ordinal()] = string
								.substring(Keywords.ReturnPath.name.length());
					} else {
						values[Keywords.ReturnPath.ordinal()] += " "
								+ string.substring(Keywords.ReturnPath.name.length());
					}
					lastRank = Keywords.ReturnPath.ordinal();
				} else if (string.startsWith(Keywords.Importance.name)) {
					values[Keywords.Importance.ordinal()] = string
							.substring(Keywords.Importance.name.length());
					lastRank = -1;
				} else if (string.startsWith(Keywords.Priority.name)) {
					values[Keywords.Priority.ordinal()] = string
							.substring(Keywords.Priority.name.length());
					lastRank = -1;
				} else if (string.startsWith(Keywords.XFolder.name)) {
					values[Keywords.XFolder.ordinal()] = string.substring(Keywords.XFolder.name
							.length());
					lastRank = -1;
				} else if (string.startsWith(Keywords.XSDOC.name)) {
					values[Keywords.XSDOC.ordinal()] = string.substring(Keywords.XSDOC.name
							.length());
					lastRank = -1;
				} else if (string.startsWith(Keywords.Sensitivity.name)) {
					values[Keywords.Sensitivity.ordinal()] = string
							.substring(Keywords.Sensitivity.name.length());
					lastRank = -1;
				} else {
					lastRank = -1;
				}
			}
		}
		/*for (int i = 0; i < Keywords.values().length; i++) {
			System.out.println(Keywords.values()[i].name()+": "+values[i]);
		}*/

		if (values[Keywords.XFolder.ordinal()] != null) {
			Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.folder.name);
			sub.addAttribute(EMAIL_FIELDS.folderName.name, values[Keywords.XFolder.ordinal()]);
			metadata.add(sub);
		}
		String fromEmail = values[Keywords.From.ordinal()];
		if (fromEmail != null) {
			Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.from.name);
			addAddress(sub, EMAIL_FIELDS.fromUnit.name, fromEmail);
			String fromEmail2 = values[Keywords.ReturnPath.ordinal()];
			if (fromEmail2 != null && !fromEmail.contains(fromEmail2)) {
				addAddress(sub, EMAIL_FIELDS.fromUnit.name, fromEmail2);
			}
			metadata.add(sub);
		} else {
			String fromEmail2 = values[Keywords.ReturnPath.ordinal()];
			if (fromEmail2 != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.from.name);
				addAddress(sub, EMAIL_FIELDS.fromUnit.name, fromEmail2);
				metadata.add(sub);
			}
		}
		fromEmail = values[Keywords.To.ordinal()];
		if (fromEmail != null) {
			Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.toRecipients.name);
			String[] to = fromEmail.split(",");
			for (String string2 : to) {
				addAddress(sub, EMAIL_FIELDS.toUnit.name, string2);
			}
			metadata.add(sub);
		}
		fromEmail = values[Keywords.Cc.ordinal()];
		if (fromEmail != null) {
			Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.ccRecipients.name);
			String[] to = fromEmail.split(",");
			for (String string2 : to) {
				addAddress(sub, EMAIL_FIELDS.ccUnit.name, string2);
			}
			metadata.add(sub);
		}
		fromEmail = values[Keywords.Bcc.ordinal()];
		if (fromEmail != null) {
			Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.bccRecipients.name);
			String[] to = fromEmail.split(",");
			for (String string2 : to) {
				addAddress(sub, EMAIL_FIELDS.bccUnit.name, string2);
			}
			metadata.add(sub);
		}

		String subject = null;
		try {
			subject = msg.getSubject();
		} catch (ChunkNotFoundException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		if (subject != null) {
			Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.subject.name);
			sub.setText(StringUtils.unescapeHTML(subject, true, false));
			metadata.add(sub);
		}
		subject = null;
		try {
			subject = msg.getConversationTopic();
		} catch (ChunkNotFoundException e3) {
			//System.err.println(e3.getMessage());
		}
		if (subject != null) {
			Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.conversationTopic.name);
			sub.setText(StringUtils.unescapeHTML(subject, true, false));
			metadata.add(sub);
		}
		if (values[Keywords.Date.ordinal()] != null) {
			Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.sentDate.name);
			sub.setText(values[Keywords.Date.ordinal()]);
			metadata.add(sub);
		}
		if (values[Keywords.XOriginalArrivalTime.ordinal()] != null) {
			Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.receivedDate.name);
			sub.setText(values[Keywords.XOriginalArrivalTime.ordinal()]);
			metadata.add(sub);
		}
		if (values[Keywords.Received.ordinal()] != null) {
			Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.receptionTrace.name);
			String[] traces = values[Keywords.Received.ordinal()].split("\n");
			for (String string : traces) {
				Element sub3 = XmlDom.factory.createElement(EMAIL_FIELDS.trace.name);
				sub3.setText(string);
				sub.add(sub3);
			}
			metadata.add(sub);
		}
		if (values[Keywords.XSDOC.ordinal()] != null) {
			Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.emailSize.name);
			sub.setText(values[Keywords.XSDOC.ordinal()]);
			metadata.add(sub);
		}
		String messageId = values[Keywords.MessageId.ordinal()];
		if (messageId != null) {
			messageId = StringUtils.removeChevron(StringUtils.unescapeHTML(
					messageId, true, false)).trim();
			if (messageId.length() > 1) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.messageId.name);
				sub.setText(messageId);
				metadata.add(sub);
			}
		}
		String InReplyToId = values[Keywords.InReplyTo.ordinal()];
		if (InReplyToId != null) {
			InReplyToId = StringUtils.removeChevron(StringUtils.unescapeHTML(InReplyToId, true,
					false)).trim();
			if (InReplyToId.length() > 1) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.inReplyTo.name);
				sub.setText(InReplyToId);
				if (messageId != null && messageId.length() > 1) {
					String old = EmlExtract.filEmls.get(InReplyToId);
					if (old == null) {
						old = messageId;
					} else {
						old += "," + messageId;
					}
					EmlExtract.filEmls.put(InReplyToId, old);
				}
				metadata.add(sub);
			}
			InReplyToId = null;
		}
		Element prop = XmlDom.factory.createElement(EMAIL_FIELDS.properties.name);
		String imp = values[Keywords.Importance.ordinal()];
		if (imp != null && imp.length() > 0) {
			try {
				int Priority = Integer.parseInt(imp);
				switch (Priority) {
					case 5:
						imp = "LOWEST";
						break;
					case 4:
						imp = "LOW";
						break;
					case 3:
						imp = "NORMAL";
						break;
					case 2:
						imp = "HIGH";
						break;
					case 1:
						imp = "HIGHEST";
						break;
					default:
						imp = "LEV" + Priority;
				}
			} catch (NumberFormatException e) {
				// ignore since imp will be used as returned
			}
			prop.addAttribute(EMAIL_FIELDS.importance.name, imp);
		}
		imp = values[Keywords.Priority.ordinal()];
		if (imp != null && imp.length() > 0) {
			try {
				int Priority = Integer.parseInt(imp);
				switch (Priority) {
					case 5:
						imp = "LOWEST";
						break;
					case 4:
						imp = "LOW";
						break;
					case 3:
						imp = "NORMAL";
						break;
					case 2:
						imp = "HIGH";
						break;
					case 1:
						imp = "HIGHEST";
						break;
					default:
						imp = "LEV" + Priority;
				}
			} catch (NumberFormatException e) {
				// ignore since imp will be used as returned
			}
			prop.addAttribute(EMAIL_FIELDS.priority.name, imp);
		}
		if (values[Keywords.Sensitivity.ordinal()] != null) {
			prop.addAttribute(EMAIL_FIELDS.sensitivity.name, values[Keywords.Sensitivity.ordinal()]);
		}
		AttachmentChunks[] files = msg.getAttachmentFiles();
		boolean Attachments = (files != null && files.length > 0);
		prop.addAttribute(EMAIL_FIELDS.hasAttachment.name, Boolean.toString(Attachments));
		metadata.add(prop);

		String result = "";
		Element identification = null;
		if (Attachments) {
			File oldPath = curPath;
			if (config.extractFile) {
				File newDir = new File(curPath, id);
				newDir.mkdir();
				curPath = newDir;
			}
			identification = XmlDom.factory.createElement(EMAIL_FIELDS.attachments.name);
			// get the number of attachments for this message
			int NumberOfAttachments = files.length;
			identification.addAttribute(EMAIL_FIELDS.attNumber.name,
					Integer.toString(NumberOfAttachments));
			// get a specific attachment from this email.
			for (int attachmentNumber = 0; attachmentNumber < NumberOfAttachments; attachmentNumber++) {
				AttachmentChunks attachment = files[attachmentNumber];
				if (argument.extractKeyword) {
					result += " "
							+ extractInfoAttachment(attachment, identification, argument, config,
									curPath);
				} else {
					extractInfoAttachment(attachment, identification, argument, config, curPath);
				}
			}
			curPath = oldPath;
		}
		// Plain text e-mail body
		String body = "";
		if (argument.extractKeyword || config.extractFile) {
			try {
				body = msg.getTextBody();
			} catch (ChunkNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			boolean isTxt = true;
			boolean isHttp = false;
			if (body == null || body.isEmpty()) {
				isTxt = false;
				try {
					body = msg.getHtmlBody();
				} catch (ChunkNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				isHttp = true;
				if (body == null || body.isEmpty()) {
					isHttp = false;
					try {
						body = msg.getRtfBody();
					} catch (ChunkNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if (body != null && !body.isEmpty()) {
				if (config.extractFile) {
					// XXX FIXME could saved email from HTML Body (clearer) if possible
					// use curRank in name, and attachment will be under directory named
					// add currank in field
					File newDir = new File(curPath, id);
					newDir.mkdir();
					String filenamebody = messageId;
					if (filenamebody == null || !filenamebody.isEmpty()) {
						filenamebody = id;
					}
					String html = null;
					if (isHttp) {
						html = body;
					}
					String rtf = null;
					if (!isTxt && !isHttp) {
						rtf = body;
					}
					if (isTxt) {
						FileOutputStream output = null;
						try {
							output = new FileOutputStream(new File(newDir, filenamebody + ".txt"));
							byte[] bb = body.getBytes();
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
						try {
							html = msg.getHtmlBody();
						} catch (ChunkNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (html != null && !html.isEmpty()) {
						FileOutputStream output = null;
						try {
							output = new FileOutputStream(new File(newDir, filenamebody + ".html"));
							byte[] bb = html.getBytes();
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
							rtf = msg.getRtfBody();
						} catch (ChunkNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (rtf != null && !rtf.isEmpty()) {
						FileOutputStream output = null;
						try {
							output = new FileOutputStream(new File(newDir, filenamebody + ".rtf"));
							byte[] bb = rtf.getBytes();
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
		//System.out.println("end of "+id);
		return result;
	}

	private static String extractInfoAttachment(AttachmentChunks fatt, Element identification,
			VitamArgument argument, ConfigLoader config, File curPath) {
		Element newElt = XmlDom.factory.createElement(EMAIL_FIELDS.subidentity.name);
		String filename = null;
		String result = "";
		byte[] bytes = fatt.attachData.getValue();
		long size = bytes.length;

		// Check file
		filename = fatt.attachLongFileName.toString();
		if (filename.isEmpty()) {
			filename = fatt.attachFileName.toString();
		}
		filename = StringUtils.toFileName(filename);
		FileOutputStream out = null;
		File filetemp = null;
		try {
			String tempfilename = filename.isEmpty() ? (config.nbDoc.get() + 1)
					+ "_unknownAttachment.msg" : filename;
			// Force out as eml
			if (config.extractFile) {
				filetemp = new File(curPath, tempfilename);
			} else {
				filetemp = File.createTempFile(StaticValues.PREFIX_TEMPFILE, tempfilename);
			}
			out = new FileOutputStream(filetemp);
			out.write(bytes);
			out.close();
			bytes = null;
			// Now check file against Droid or more
			try {
				Commands.addFormatIdentification(newElt, filename, filetemp, config, argument);
				if (argument.extractKeyword) {
					// get back keyword in the main list
					Element keyw = (Element) newElt.selectSingleNode(EMAIL_FIELDS.keywords.name);
					if (keyw != null) {
						StringBuilder builder = new StringBuilder();
						@SuppressWarnings("unchecked")
						List<Element> elts = (List<Element>) keyw
								.selectNodes(EMAIL_FIELDS.keywordRank.name);
						for (Element elt : elts) {
							String value = elt.attributeValue(EMAIL_FIELDS.keywordOccur.name);
							int occur = Integer.parseInt(value) / 2 + 1;
							@SuppressWarnings("unchecked")
							List<Element> words = (List<Element>) elt
									.selectNodes(EMAIL_FIELDS.keywordWord.name);
							for (Element eword : words) {
								String word = eword.attributeValue(EMAIL_FIELDS.keywordValue.name)
										+ " ";
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
				// String id = Long.toString(config.nbDoc.incrementAndGet());
				// newElt.addAttribute(EMAIL_FIELDS.rankId.name, id);
				String status = "Error during identification";
				e.printStackTrace();
				newElt.addAttribute(EMAIL_FIELDS.status.name, status);
				return "";
			}
			// then clear
			if (!config.extractFile) {
				filetemp.delete();
			}
		} catch (IOException e) {
			config.addRankId(newElt);
			// String id = Long.toString(config.nbDoc.incrementAndGet());
			// newElt.addAttribute(EMAIL_FIELDS.rankId.name, id);
			e.printStackTrace();
			String status = "Error during access to attachment";
			newElt.addAttribute(EMAIL_FIELDS.status.name, status);
			identification.add(newElt);
			return "";
		} finally {
			if (filetemp != null && !config.extractFile) {
				filetemp.delete();
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e2) {
				}
			}
		}
		if (filename != null) {
			Element elt = XmlDom.factory.createElement(EMAIL_FIELDS.filename.name);
			elt.setText(filename);
			newElt.add(elt);
		}
		if (size > 0) {
			Element elt = XmlDom.factory.createElement(EMAIL_FIELDS.attSize.name);
			elt.setText(Long.toString(size));
			newElt.add(elt);
		}
		String mimetag = fatt.attachMimeTag.toString();
		if (mimetag != null) {
			Element elt = XmlDom.factory.createElement("attchmentMimeType");
			elt.setText(mimetag);
			newElt.add(elt);
		}
		identification.add(newElt);
		return result;
	}


}
