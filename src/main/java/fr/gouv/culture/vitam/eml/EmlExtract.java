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
package fr.gouv.culture.vitam.eml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeMessage;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import fr.gouv.culture.vitam.digest.Base64;
import fr.gouv.culture.vitam.eml.StringUtils.EMAIL_FIELDS;
import fr.gouv.culture.vitam.extract.ExtractInfo;
import fr.gouv.culture.vitam.utils.Commands;
import fr.gouv.culture.vitam.utils.ConfigLoader;
import fr.gouv.culture.vitam.utils.StaticValues;
import fr.gouv.culture.vitam.utils.VitamArgument;
import fr.gouv.culture.vitam.utils.XmlDom;

/**
 * Class to try to handle EML files (email)
 * 
 * @author "Frederic Bregier"
 * 
 */
public class EmlExtract {
	
	public static HashMap<String, String> filEmls = new HashMap<String, String>();
	
	private static String addAddress(Element root, String entry, Address address, String except) {
		String value = address.toString();
		String ad = StringUtils.selectChevron(value);
		if (ad == null || (except != null && ad.equalsIgnoreCase(except))) {
			return null;
		}
		String nams = value.replace('<'+ad+'>', "");
		Element val = XmlDom.factory.createElement(entry);
		Element name = XmlDom.factory.createElement(EMAIL_FIELDS.emailName.name);
		Element addresse = XmlDom.factory.createElement(EMAIL_FIELDS.emailAddress.name);
		name.setText(StringUtils.unescapeHTML(nams, true, false));
		addresse.setText(StringUtils.unescapeHTML(ad, true, false));
		val.add(name);
		val.add(addresse);
		root.add(val);
		return value;
	}
	
	private static void addAddress(Element root, String entry, String []addresses, String except) {
		for (String address : addresses) {
			if (address.contains(",")) {
				// multiple emails
				String [] split = address.split(",");
				for (String sub : split) {
					String value = sub;
					String ad = StringUtils.selectChevron(value);
					if (ad == null || (except != null && ad.equalsIgnoreCase(except))) {
						continue;
					}
					String nams = value.replace('<'+ad+'>', "");
					Element val = XmlDom.factory.createElement(entry);
					Element name = XmlDom.factory.createElement(EMAIL_FIELDS.emailName.name);
					Element addresse = XmlDom.factory.createElement(EMAIL_FIELDS.emailAddress.name);
					name.setText(StringUtils.unescapeHTML(nams, true, false));
					addresse.setText(StringUtils.unescapeHTML(ad, true, false));
					val.add(name);
					val.add(addresse);
					root.add(val);
				}
			} else {
				String value = address;
				String ad = StringUtils.selectChevron(value);
				if (ad == null || (except != null && ad.equalsIgnoreCase(except))) {
					continue;
				}
				String nams = value.replace('<'+ad+'>', "");
				Element val = XmlDom.factory.createElement(entry);
				Element name = XmlDom.factory.createElement(EMAIL_FIELDS.emailName.name);
				Element addresse = XmlDom.factory.createElement(EMAIL_FIELDS.emailAddress.name);
				name.setText(StringUtils.unescapeHTML(nams, true, false));
				addresse.setText(StringUtils.unescapeHTML(ad, true, false));
				val.add(name);
				val.add(addresse);
				root.add(val);
			}
		}
	}
	
	/**
	 * Try to extract the following :
	 * 
	 * Taken from : http://www.significantproperties.org.uk/email-testingreport.html
	 * 
	 * message-id (Message-ID), References (References), In-Reply-To (In-Reply-To), Attachment
	 * subject (Subject), keywords 
	 * sent-date (Date), Received-date (in Received last date), Trace-field (Received?)
	 * 
	 * 
	 * From (From), To (To), CC (Cc), BCC (Bcc), Content-Type, Content-Transfer-Encoding
	 * 
	 * ? DomainKey-Signature, Sender, X-Original-Sender, X-Forwarded-Message-Id, 
	 * 
	 * 1) Core property set
	 * 
	 * The core property set indicates the minimum amount of information that is considered necessary to establish the authenticity and integrity of the email message
	 * 
	 * Local-part, Domain-part, Relationship, Subject, Trace-field , Message body with no mark-up, Attachments
	 * 
	 * 2) Message thread scenario
	 * 
	 * Email is frequently used as a communication method between two or more people. To understand the context in which a message was created it may be necessary to refer to earlier messages. To identify the thread of a discussion, the following fields should be provided, in addition to the core property set:
	 * 
	 * Local-part, Domain-part, Relationship, Subject, Trace-field, Message body with no mark-up, Attachments, Message-ID, References
	 * 
	 * 3) Recommended property set
	 * 
	 * The recommended property set indicates additional information that should be provided in an ideal scenario, if it is present within the email. The list
	 * 
	 * Local-part, Domain-part, Domain-literal (if present), Relationship, Subject, Trace-field, Attachments, Message-ID, References, Sent-date, Received date,
	 * Display name, In-reply-to, Keywords, Message body & associated mark-up (see table 6 for scenarios)
	 * 
	 * 
	 * 
	 * @param emlFile
	 * @param filename
	 * @param argument
	 * @param config
	 * @return
	 */
	public static Element extractInfoEmail(File emlFile, String filename, VitamArgument argument,
			ConfigLoader config) {
		File oldDir = argument.currentOutputDir;
		if (argument.currentOutputDir == null) {
			if (config.outputDir != null) {
				argument.currentOutputDir = new File(config.outputDir);
			} else {
				argument.currentOutputDir = new File(emlFile.getParentFile().getAbsolutePath());
			}
		}

		MimeMessage message = null;
		try {
			message = createOneMessageFromFile(emlFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Element newElt = XmlDom.factory.createElement(EMAIL_FIELDS.formatEML.name);
			String status = "Error during identification";
			newElt.addAttribute(EMAIL_FIELDS.status.name, status);
			return newElt;
		} catch (MessagingException e) {
			e.printStackTrace();
			Element newElt = XmlDom.factory.createElement(EMAIL_FIELDS.formatEML.name);
			String status = "Error during identification";
			newElt.addAttribute(EMAIL_FIELDS.status.name, status);
			return newElt;
		}
		Element root = XmlDom.factory.createElement(EMAIL_FIELDS.formatEML.name);
		extractInfoMessage(message, root, argument, config);
		argument.currentOutputDir = oldDir;
		return root;
	}
	public static String extractInfoMessage(MimeMessage message, Element root, VitamArgument argument,
			ConfigLoader config) {
		File oldDir = argument.currentOutputDir;
		if (argument.currentOutputDir == null) {
			if (config.outputDir != null) {
				argument.currentOutputDir = new File(config.outputDir);
			}
		}
		Element keywords = XmlDom.factory.createElement(EMAIL_FIELDS.keywords.name);
		Element metadata = XmlDom.factory.createElement(EMAIL_FIELDS.metadata.name);
		String skey = "";
		String id = config.addRankId(root);
		Address[] from = null;
		Element sub2 = null;
		try {
			from = message.getFrom();
		} catch (MessagingException e1) {
			String[] partialResult;
			try {
				partialResult = message.getHeader("From");
				if (partialResult != null && partialResult.length > 0) {
					sub2 = XmlDom.factory.createElement(EMAIL_FIELDS.from.name);
					Element add = XmlDom.factory.createElement(EMAIL_FIELDS.fromUnit.name);
					add.setText(partialResult[0]);
					sub2.add(add);
				}
			} catch (MessagingException e) {
			}
		}
		Address sender = null;
		try {
			sender = message.getSender();
		} catch (MessagingException e1) {
			String[] partialResult;
			try {
				partialResult = message.getHeader("Sender");
				if (partialResult != null && partialResult.length > 0) {
					if (sub2 == null) {
						sub2 = XmlDom.factory.createElement(EMAIL_FIELDS.from.name);
						Element add = XmlDom.factory.createElement(EMAIL_FIELDS.fromUnit.name);
						add.setText(partialResult[0]);
						sub2.add(add);
					}
				}
			} catch (MessagingException e) {
			}
		}
		if (from != null && from.length > 0) {
			String value0 = null;
			Element sub = (sub2 != null ? sub2 : XmlDom.factory.createElement(EMAIL_FIELDS.from.name));
			if (sender != null) {
				value0 = addAddress(sub, EMAIL_FIELDS.fromUnit.name, sender, null);
			}
			for (Address address : from) {
				addAddress(sub, EMAIL_FIELDS.fromUnit.name, address, value0);
			}
			metadata.add(sub);
		} else if (sender != null) {
			Element sub = (sub2 != null ? sub2 : XmlDom.factory.createElement(EMAIL_FIELDS.from.name));
			addAddress(sub, EMAIL_FIELDS.fromUnit.name, sender, null);
			metadata.add(sub);
		} else {
			if (sub2 != null) {
				metadata.add(sub2);
			}
		}
		Address[] replyTo = null;
		try {
			replyTo = message.getReplyTo();
			if (replyTo != null && replyTo.length > 0) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.replyTo.name);
				for (Address address : replyTo) {
					addAddress(sub, EMAIL_FIELDS.fromUnit.name, address, null);
				}
				metadata.add(sub);
			}
		} catch (MessagingException e1) {
			String[] partialResult;
			try {
				partialResult = message.getHeader("ReplyTo");
				if (partialResult != null && partialResult.length > 0) {
					sub2 = XmlDom.factory.createElement(EMAIL_FIELDS.replyTo.name);
					addAddress(sub2, EMAIL_FIELDS.fromUnit.name, partialResult, null);
					/*Element add = XmlDom.factory.createElement(EMAIL_FIELDS.fromUnit.name);
					add.setText(partialResult[0]);
					sub2.add(add);*/
					metadata.add(sub2);
				}
			} catch (MessagingException e) {
			}
		}
		Address[] toRecipients = null;
		try {
			toRecipients = message.getRecipients(Message.RecipientType.TO);
			if (toRecipients != null && toRecipients.length > 0) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.toRecipients.name);
				for (Address address : toRecipients) {
					addAddress(sub, EMAIL_FIELDS.toUnit.name, address, null);
				}
				metadata.add(sub);
			}
		} catch (MessagingException e1) {
			String[] partialResult;
			try {
				partialResult = message.getHeader("To");
				if (partialResult != null && partialResult.length > 0) {
					sub2 = XmlDom.factory.createElement(EMAIL_FIELDS.toRecipients.name);
					addAddress(sub2, EMAIL_FIELDS.toUnit.name, partialResult, null);
					/*for (String string : partialResult) {
						Element add = XmlDom.factory.createElement(EMAIL_FIELDS.toUnit.name);
						add.setText(string);
						sub2.add(add);
					}*/
					metadata.add(sub2);
				}
			} catch (MessagingException e) {
			}
		}
		Address[] ccRecipients;
		try {
			ccRecipients = message.getRecipients(Message.RecipientType.CC);
			if (ccRecipients != null && ccRecipients.length > 0) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.ccRecipients.name);
				for (Address address : ccRecipients) {
					addAddress(sub, EMAIL_FIELDS.ccUnit.name, address, null);
				}
				metadata.add(sub);
			}
		} catch (MessagingException e1) {
			String[] partialResult;
			try {
				partialResult = message.getHeader("Cc");
				if (partialResult != null && partialResult.length > 0) {
					sub2 = XmlDom.factory.createElement(EMAIL_FIELDS.ccRecipients.name);
					addAddress(sub2, EMAIL_FIELDS.ccUnit.name, partialResult, null);
					/*for (String string : partialResult) {
						Element add = XmlDom.factory.createElement(EMAIL_FIELDS.ccUnit.name);
						add.setText(string);
						sub2.add(add);
					}*/
					metadata.add(sub2);
				}
			} catch (MessagingException e) {
			}
		}
		Address[] bccRecipients;
		try {
			bccRecipients = message.getRecipients(Message.RecipientType.BCC);
			if (bccRecipients != null && bccRecipients.length > 0) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.bccRecipients.name);
				for (Address address : bccRecipients) {
					addAddress(sub, EMAIL_FIELDS.bccUnit.name, address, null);
				}
				metadata.add(sub);
			}
		} catch (MessagingException e1) {
			String[] partialResult;
			try {
				partialResult = message.getHeader("Cc");
				if (partialResult != null && partialResult.length > 0) {
					sub2 = XmlDom.factory.createElement(EMAIL_FIELDS.bccRecipients.name);
					addAddress(sub2, EMAIL_FIELDS.bccUnit.name, partialResult, null);
					/*for (String string : partialResult) {
						Element add = XmlDom.factory.createElement(EMAIL_FIELDS.bccUnit.name);
						add.setText(string);
						sub2.add(add);
					}*/
					metadata.add(sub2);
				}
			} catch (MessagingException e) {
			}
		}
		try {
			String subject = message.getSubject();
			if (subject != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.subject.name);
				sub.setText(StringUtils.unescapeHTML(subject, true, false));
				metadata.add(sub);
			}
			Date sentDate = message.getSentDate();
			if (sentDate != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.sentDate.name);
				sub.setText(sentDate.toString());
				metadata.add(sub);
			}
			Date receivedDate = message.getReceivedDate();
			if (receivedDate != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.receivedDate.name);
				sub.setText(receivedDate.toString());
				metadata.add(sub);
			}
			String [] headers = message.getHeader("Received");
			if (headers != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.receptionTrace.name);
				MailDateFormat mailDateFormat = null;
				long maxTime = 0;
				if (receivedDate == null) {
					mailDateFormat = new MailDateFormat();
				}
				for (String string : headers) {
					Element sub3 = XmlDom.factory.createElement(EMAIL_FIELDS.trace.name);
					sub3.setText(StringUtils.unescapeHTML(string, true, false));
					sub.add(sub3);
					if (receivedDate == null) {
						int pos = string.lastIndexOf(';');
						if (pos > 0) {
							String recvdate = string.substring(pos+2).replaceAll("\t\n\r\f", "").trim();
							try {
								Date date = mailDateFormat.parse(recvdate);
								if (date.getTime() > maxTime) {
									maxTime = date.getTime();
								}
							} catch (ParseException e) {
							}
						}
					}
				}
				if (receivedDate == null) {
					Element subdate = XmlDom.factory.createElement(EMAIL_FIELDS.receivedDate.name);
					Date date = new Date(maxTime);
					subdate.setText(date.toString());
					metadata.add(subdate);
				}
				metadata.add(sub);
			}
			int internalSize = message.getSize();
			if (internalSize > 0) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.emailSize.name);
				sub.setText(Integer.toString(internalSize));
				metadata.add(sub);
			}
			String encoding = message.getEncoding();
			if (encoding != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.encoding.name);
				sub.setText(StringUtils.unescapeHTML(encoding, true, false));
				metadata.add(sub);
			}
			String description = message.getDescription();
			if (description != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.description.name);
				sub.setText(StringUtils.unescapeHTML(description, true, false));
				metadata.add(sub);
			}
			String contentType = message.getContentType();
			if (contentType != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.contentType.name);
				sub.setText(StringUtils.unescapeHTML(contentType, true, false));
				metadata.add(sub);
			}
			headers = message.getHeader("Content-Transfer-Encoding");
			if (headers != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.contentTransferEncoding.name);
				StringBuilder builder = new StringBuilder();
				for (String string : headers) {
					builder.append(StringUtils.unescapeHTML(string, true, false));
					builder.append(' ');
				}
				sub.setText(builder.toString());
				metadata.add(sub);
			}
			String []contentLanguage = message.getContentLanguage();
			if (contentLanguage != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.contentLanguage.name);
				StringBuilder builder = new StringBuilder();
				for (String string : contentLanguage) {
					builder.append(StringUtils.unescapeHTML(string, true, false));
					builder.append(' ');
				}
				sub.setText(builder.toString());
				metadata.add(sub);
			}
			String contentId = message.getContentID();
			if (contentId != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.contentId.name);
				sub.setText(StringUtils.removeChevron(StringUtils.unescapeHTML(contentId, true, false)));
				metadata.add(sub);
			}
			String disposition = message.getDisposition();
			if (disposition != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.disposition.name);
				sub.setText(StringUtils.removeChevron(StringUtils.unescapeHTML(disposition, true, false)));
				metadata.add(sub);
			}
			headers = message.getHeader("Keywords");
			if (headers != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.msgKeywords.name);
				StringBuilder builder = new StringBuilder();
				for (String string : headers) {
					builder.append(StringUtils.unescapeHTML(string, true, false));
					builder.append(' ');
				}
				sub.setText(builder.toString());
				metadata.add(sub);
			}
			String messageId = message.getMessageID();
			if (messageId != null) {
				messageId = StringUtils.removeChevron(StringUtils.unescapeHTML(messageId, true, false)).trim();
				if (messageId.length() > 1) {
					Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.messageId.name); 
					sub.setText(messageId);
					metadata.add(sub);
				}
			}
			headers = message.getHeader("In-Reply-To");
			String inreplyto = null;
			if (headers != null) {
				StringBuilder builder = new StringBuilder();
				for (String string : headers) {
					builder.append(StringUtils.removeChevron(StringUtils.unescapeHTML(string, true, false)));
					builder.append(' ');
				}
				inreplyto = builder.toString().trim();
				if (inreplyto.length() > 0) {
					Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.inReplyTo.name);
					sub.setText(inreplyto);
					if (messageId != null && messageId.length() > 1) {
						String old = filEmls.get(inreplyto);
						if (old == null) {
							old = messageId;
						} else {
							old += ","+messageId;
						}
						filEmls.put(inreplyto, old);
					}
					metadata.add(sub);
				}
			}
			headers = message.getHeader("References");
			if (headers != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.references.name);
				StringBuilder builder = new StringBuilder();
				for (String string : headers) {
					builder.append(StringUtils.removeChevron(StringUtils.unescapeHTML(string, true, false)));
					builder.append(' ');
				}
				String []refs = builder.toString().trim().split(" ");
				for (String string : refs) {
					if (string.length() > 0) {
						Element ref = XmlDom.factory.createElement(EMAIL_FIELDS.reference.name);
						ref.setText(string);
						sub.add(ref);
					}
				}
				metadata.add(sub);
			}
			Element prop = XmlDom.factory.createElement(EMAIL_FIELDS.properties.name);
			headers = message.getHeader("X-Priority");
			if (headers == null) {
				headers = message.getHeader("Priority");
				if (headers != null && headers.length > 0) {
					prop.addAttribute(EMAIL_FIELDS.priority.name, headers[0]);
				}
			} else if (headers != null && headers.length > 0) {
				String imp = headers[0];
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
			headers = message.getHeader("Sensitivity");
			if (headers != null && headers.length > 0) {
				prop.addAttribute(EMAIL_FIELDS.sensitivity.name, headers[0]);
			}
			headers = message.getHeader("X-RDF");
			if (headers != null && headers.length > 0) {
				System.err.println("Found X-RDF");
				StringBuilder builder = new StringBuilder();
				for (String string : headers) {
					builder.append(string);
					builder.append("\n");
				}
				try {
					byte [] decoded  = org.apache.commons.codec.binary.Base64.decodeBase64(builder.toString());
					String rdf = new String(decoded);
					Document tempDocument = DocumentHelper.parseText(rdf);
					Element xrdf = prop.addElement("x-rdf");
					xrdf.add(tempDocument.getRootElement());
				} catch (Exception e) {
					System.err.println("Cannot decode X-RDF: "+e.getMessage());
				}
			}
			try {
				File old = argument.currentOutputDir;
				if (config.extractFile) {
					File newOutDir = new File(argument.currentOutputDir, id);
					newOutDir.mkdirs();
					argument.currentOutputDir = newOutDir;
				}
				if (argument.extractKeyword) {
					skey = handleMessage(message, metadata, prop, id, argument, config);
					// should have hasAttachment
					if (prop.hasContent()) {
						metadata.add(prop);
					}
					if (metadata.hasContent()) {
						root.add(metadata);
					}
					ExtractInfo.exportMetadata(keywords, skey, "", config, null);
					if (keywords.hasContent()) {
						root.add(keywords);
					}
				} else {
					handleMessage(message, metadata, prop, id, argument, config);
					// should have hasAttachment
					if (prop.hasContent()) {
						metadata.add(prop);
					}
					if (metadata.hasContent()) {
						root.add(metadata);
					}
				}
				argument.currentOutputDir = old;
			} catch (IOException e) {
				System.err.println(StaticValues.LBL.error_error.get() + e.toString());
			}
			try {
				message.getInputStream().close();
			} catch (IOException e) {
				System.err.println(StaticValues.LBL.error_error.get() + e.toString());
			}
			root.addAttribute(EMAIL_FIELDS.status.name, "ok");
		} catch (MessagingException e) {
			System.err.println(StaticValues.LBL.error_error.get() + e.toString());
			e.printStackTrace();
			String status = "Error during identification";
			root.addAttribute(EMAIL_FIELDS.status.name, status);
		} catch (Exception e) {
			System.err.println(StaticValues.LBL.error_error.get() + e.toString());
			e.printStackTrace();
			String status = "Error during identification";
			root.addAttribute(EMAIL_FIELDS.status.name, status);
		}
		argument.currentOutputDir = oldDir;
		return skey;
	}

	private static final MimeMessage createOneMessageFromFile(File emlFile) throws FileNotFoundException, MessagingException {
		Properties props = System.getProperties();
		Session session = Session.getDefaultInstance(props);
        /*props.put("mail.host", "smtp.vitamdomain.com");
        props.put("mail.transport.protocol", "smtp");
        Session session = Session.getDefaultInstance(props, null);*/
		InputStream source = new FileInputStream(emlFile);
        return new MimeMessage(session, source);
	}
	
	private static final String[] extractContentType(String contentType, String contentTypeEncoding) {
		String charset = null;
		int pos = contentType.indexOf(';');
		if (pos > 0) {
			charset = contentType.substring(pos+1).trim();
			contentType = contentType.substring(0, pos).trim();
			pos = charset.indexOf("charset=");
			if (pos >= 0) {
				charset = charset.substring(pos);
				charset = charset.replace("charset=", "").trim();
				pos = charset.indexOf(';');
				if (pos > 0) {
					charset = charset.substring(0, pos).trim();
				}
				if (charset.startsWith("\"")) {
					pos = charset.indexOf('\"', 2);
					if (pos > 0) {
						charset = charset.substring(1, pos).trim();
					}
				}
			} else {
				charset = null;
			}
		}
		String [] result = new String[4];
		result[0] = contentType;
		result[1] = charset;
		result[2] = contentTypeEncoding;
		if ("text/plain".equals(contentType)) {
			result[3] = ".txt";
		} else if ("text/html".equals(contentType)) {
			result[3] = ".html";
		} else {
			result[3] = ".unknown";
		}
		//System.out.println(contentType+":"+charset+":"+contentTypeEncoding+":"+result[3]);
		return result;
	}
	
	private static final String saveBody(InputStream stream, String []aresult, String id, VitamArgument argument, ConfigLoader config) throws MessagingException, IOException {
		String tosave = null;
		if (config.extractFile) {
			FileOutputStream outputStream = new FileOutputStream(new File(argument.currentOutputDir, id+"_body"+aresult[3]));
			if (aresult[2] != null && aresult[2].equals("quoted-printable")) {
				tosave = StringUtils.unescapeQuotedPrintable(stream, aresult[1]);
			} else {
				/*if (aresult[1] != null) {
					tosave = new String(((String) content).getBytes(), aresult[1]);
				} else {
					tosave = ((String) content);
				}*/
				tosave = StringUtils.undecodeString(stream, aresult[1]);
				//tosave = content;
			}
			outputStream.write(tosave.getBytes(StaticValues.CURRENT_OUTPUT_ENCODING));
			//outputStream.write(tosave.getBytes());
			outputStream.flush();
			outputStream.close();
		} else if (argument.extractKeyword) {
			if (aresult[2].equals("quoted-printable")) {
				tosave = StringUtils.unescapeQuotedPrintable(stream, aresult[1]);
			} else {
				/*if (aresult[1] != null) {
					tosave = new String(((String) content).getBytes(), aresult[1]);
				} else {
					tosave = ((String) content);
				}*/
				tosave = StringUtils.undecodeString(stream, aresult[1]);
			}
		}
		return tosave;
	}
	
	private static final String handleMessage(Message message, Element metadata, Element prop, String id,
			VitamArgument argument, ConfigLoader config) throws IOException, MessagingException {
		Object content = message.getContent();
		String [] cte = message.getHeader("Content-Transfer-Encoding");
		String [] aresult = null;
		if (cte != null && cte.length > 0) {
			aresult = extractContentType(message.getContentType(), cte[0]);
		} else {
			aresult = extractContentType(message.getContentType(), null);
		}
		String result = "";
		if (content instanceof String) {
			Element body = XmlDom.factory.createElement("body");
			body.addAttribute("mime", aresult[0]);
			if (aresult[1] != null) {
				body.addAttribute("charset", aresult[1]);
			}
			metadata.add(body);
			//result = saveBody((String) content.toString(), aresult, id, argument, config);
			result = saveBody(message.getInputStream(), aresult, id, argument, config);
		} else if (content instanceof Multipart) {
			// handle multi part
			prop.addAttribute(EMAIL_FIELDS.hasAttachment.name, "true");
			Multipart mp = (Multipart) content;
			Element identification = XmlDom.factory.createElement(EMAIL_FIELDS.attachments.name);
			String value = handleMultipart(mp, identification, id, argument, config);
			if (identification.hasContent()) {
				metadata.add(identification);
			}
			if (argument.extractKeyword) {
				result = value;
			}
		}
		return result;
	}

	private static final String handleMultipart(Multipart mp, Element identification, String id,
			VitamArgument argument, ConfigLoader config) throws MessagingException, IOException {
		int count = mp.getCount();
		String result = "";
		identification.addAttribute(EMAIL_FIELDS.attNumber.name, Integer.toString(count-1));
		for (int i = 0; i < count; i++) {
			BodyPart bp = mp.getBodyPart(i);
			
			Object content = bp.getContent();
			if (content instanceof String) {
				String [] cte = bp.getHeader("Content-Transfer-Encoding");
				String [] aresult = null;
				if (cte != null && cte.length > 0) {
					aresult = extractContentType(bp.getContentType(), cte[0]);
				} else {
					aresult = extractContentType(bp.getContentType(), null);
				}
				Element emlroot = XmlDom.factory.createElement("body");
				// <identity format="Internet Message Format" mime="message/rfc822" puid="fmt/278" extensions="eml"/>
				Element subidenti = XmlDom.factory.createElement("identification");
				Element identity = XmlDom.factory.createElement("identity");
				identity.addAttribute("format", "Internet Message Body Format");
				identity.addAttribute("mime", aresult[0] != null ? aresult[0] : "unknown");
				identity.addAttribute("extensions", aresult[3] != null ? aresult[3].substring(1) : "unknown");
				if (aresult[1] != null) {
					identity.addAttribute("charset", aresult[1]);
				}
				identification.add(identity);
				emlroot.add(subidenti);
				identification.add(emlroot);
				//result += " " + saveBody((String) content.toString(), aresult, id, argument, config);
				result += " " + saveBody(bp.getInputStream(), aresult, id, argument, config);
			} else if (content instanceof InputStream) {
				// handle input stream
				if (argument.extractKeyword) {
					result += " " + addSubIdentities(identification, bp, (InputStream) content, argument, config);
				} else {
					addSubIdentities(identification, bp, (InputStream) content, argument, config);
				}
				((InputStream) content).close();
			} else if (content instanceof Message) {
				Message message = (Message) content;
				// XXX perhaps using Commands.addFormatIdentification
				Element emlroot = XmlDom.factory.createElement(EMAIL_FIELDS.formatEML.name);
				// <identity format="Internet Message Format" mime="message/rfc822" puid="fmt/278" extensions="eml"/>
				Element subidenti = XmlDom.factory.createElement("identification");
				Element identity = XmlDom.factory.createElement("identity");
				identity.addAttribute("format", "Internet Message Format");
				identity.addAttribute("mime", "message/rfc822");
				identity.addAttribute("puid", "fmt/278");
				identity.addAttribute("extensions", "eml");
				identification.add(identity);
				emlroot.add(subidenti);
				identification.add(emlroot);
				if (argument.extractKeyword) {
					result += " " + extractInfoMessage((MimeMessage) message, emlroot, argument, config);
				} else {
					extractInfoMessage((MimeMessage) message, emlroot, argument, config);
				}
			} else if (content instanceof Multipart) {
				Multipart mp2 = (Multipart) content;
				if (argument.extractKeyword) {
					result += " " + handleMultipartRecur(mp2, identification, id+"_"+i, argument, config);
				} else {
					handleMultipartRecur(mp2, identification, id+"_"+i, argument, config);
				}
			}
		}
		return result;
	}

	private static final String addSubIdentities(Element identification, BodyPart bp, InputStream inputStream, VitamArgument argument, ConfigLoader config) {
		Element newElt = XmlDom.factory.createElement(EMAIL_FIELDS.subidentity.name);
		String filename = null;
		String result = "";
		try {
			filename = bp.getFileName();
			filename = StringUtils.toFileName(filename);
			if (filename != null) {
				Element elt = XmlDom.factory.createElement(EMAIL_FIELDS.filename.name);
				elt.setText(filename);
				newElt.add(elt);
			} else {
				filename = "eml.eml";
			}
		} catch (MessagingException e) {
		}
		try {
			int size = bp.getSize();
			if (size > 0) {
				Element elt = XmlDom.factory.createElement(EMAIL_FIELDS.attSize.name);
				elt.setText(Integer.toString(size));
				newElt.add(elt);
			}
		} catch (MessagingException e) {
		}
		try {
			String description = bp.getDescription();
			if (description != null) {
				Element elt = XmlDom.factory.createElement(EMAIL_FIELDS.description.name);
				elt.setText(description);
				newElt.add(elt);
			}
		} catch (MessagingException e) {
		}
		try {
			String disposition = bp.getDisposition();
			if (disposition != null) {
				Element elt = XmlDom.factory.createElement(EMAIL_FIELDS.disposition.name);
				elt.setText(disposition);
				newElt.add(elt);
			}
		} catch (MessagingException e) {
		}
		File filetemp = null;
		FileOutputStream outputStream = null;
		try {
			// Force out to analysis
			if (config.extractFile) {
				filetemp = new File(argument.currentOutputDir, filename);
			} else {
				filetemp = File.createTempFile(StaticValues.PREFIX_TEMPFILE, filename);
			}
			byte [] buffer = new byte[8192];
			int read = 0;
			outputStream = new FileOutputStream(filetemp);
			while ((read = inputStream.read(buffer)) >= 0) {
				outputStream.write(buffer, 0, read);
			}
			outputStream.close();
			outputStream = null;
		} catch (IOException e1) {
			if (filetemp != null && ! config.extractFile) {
				filetemp.delete();
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
				}
			}
			String status = "Error during access to attachment";
			newElt.addAttribute(EMAIL_FIELDS.status.name, status);
			identification.add(newElt);
			return "";
		}
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
						int occur = Integer.parseInt(value) / 2 +1;
						@SuppressWarnings("unchecked")
						List<Element> words = (List<Element>) elt.selectNodes(EMAIL_FIELDS.keywordWord.name);
						for (Element eword : words) {
							String word = eword.attributeValue(EMAIL_FIELDS.keywordValue.name)+ " ";
							for (int i = 0; i < occur; i++) {
								builder.append(word);
							}
						}
					}
					result = builder.toString().trim();
				}
			}
			
		} catch (Exception e) {
			String status = "Error during identification";
			e.printStackTrace();
			config.addRankId(newElt);
			newElt.addAttribute(EMAIL_FIELDS.status.name, status);
		}
		if (filetemp != null && ! config.extractFile) {
			filetemp.delete();
		}
		identification.add(newElt);
		return result;
	}
	
	private static final String handleMessageRecur(Message message, Element identification, String id, VitamArgument argument, ConfigLoader config) throws IOException, MessagingException {
		Object content = message.getContent();
		String result = "";
		if (content instanceof String) {
			String [] cte = message.getHeader("Content-Transfer-Encoding");
			String [] aresult = null;
			if (cte != null && cte.length > 0) {
				aresult = extractContentType(message.getContentType(), cte[0]);
			} else {
				aresult = extractContentType(message.getContentType(), null);
			}
			Element emlroot = XmlDom.factory.createElement("body");
			// <identity format="Internet Message Format" mime="message/rfc822" puid="fmt/278" extensions="eml"/>
			Element subidenti = XmlDom.factory.createElement("identification");
			Element identity = XmlDom.factory.createElement("identity");
			identity.addAttribute("format", "Internet Message Body Format");
			identity.addAttribute("mime", aresult[0] != null ? aresult[0] : "unknown");
			identity.addAttribute("extensions", aresult[3] != null ? aresult[3].substring(1) : "unknown");
			if (aresult[1] != null) {
				identity.addAttribute("charset", aresult[1]);
			}
			identification.add(identity);
			emlroot.add(subidenti);
			identification.add(emlroot);
			//result += " " + saveBody((String) content.toString(), aresult, id, argument, config);
			result += " " + saveBody(message.getInputStream(), aresult, id, argument, config);
			// ignore string
		} else if (content instanceof Multipart) {
			Multipart mp = (Multipart) content;
			if (argument.extractKeyword) {
				result = handleMultipartRecur(mp, identification, id, argument, config);
			} else {
				handleMultipartRecur(mp, identification, id, argument, config);
			}
			// handle multi part
		}
		return result;
	}

	private static final String handleMultipartRecur(Multipart mp, Element identification, String id, VitamArgument argument, ConfigLoader config) throws MessagingException, IOException {
		int count = mp.getCount();
		String result = "";
		for (int i = 0; i < count; i++) {
			BodyPart bp = mp.getBodyPart(i);
			Object content = bp.getContent();
			if (content instanceof String) {
				String [] cte = bp.getHeader("Content-Transfer-Encoding");
				String [] aresult = null;
				if (cte != null && cte.length > 0) {
					aresult = extractContentType(bp.getContentType(), cte[0]);
				} else {
					aresult = extractContentType(bp.getContentType(), null);
				}
				Element emlroot = XmlDom.factory.createElement("body");
				// <identity format="Internet Message Format" mime="message/rfc822" puid="fmt/278" extensions="eml"/>
				Element subidenti = XmlDom.factory.createElement("identification");
				Element identity = XmlDom.factory.createElement("identity");
				identity.addAttribute("format", "Internet Message Body Format");
				identity.addAttribute("mime", aresult[0] != null ? aresult[0] : "unknown");
				identity.addAttribute("extensions", aresult[3] != null ? aresult[3].substring(1) : "unknown");
				if (aresult[1] != null) {
					identity.addAttribute("charset", aresult[1]);
				}
				identification.add(identity);
				emlroot.add(subidenti);
				identification.add(emlroot);
				//result += " " + saveBody((String) content.toString(), aresult, id, argument, config);
				result += " " + saveBody(bp.getInputStream(), aresult, id, argument, config);
				// ignore string
			} else if (content instanceof InputStream) {
				// handle input stream
				if (argument.extractKeyword) {
					result += " "+ addSubIdentities(identification, bp, (InputStream) content, argument, config);
				} else {
					addSubIdentities(identification, bp, (InputStream) content, argument, config);
				}
			} else if (content instanceof Message) {
				Message message = (Message) content;
				if (argument.extractKeyword) {
					result += " "+ handleMessageRecur(message, identification, id+"_"+i, argument, config);
				} else {
					handleMessageRecur(message, identification, id+"_"+i, argument, config);
				}
			} else if (content instanceof Multipart) {
				Multipart mp2 = (Multipart) content;
				if (argument.extractKeyword) {
					result += " "+ handleMultipartRecur(mp2, identification, id+"_"+i, argument, config);
				} else {
					handleMultipartRecur(mp2, identification, id+"_"+i, argument, config);
				}
			}
		}
		return result;
	}
}
