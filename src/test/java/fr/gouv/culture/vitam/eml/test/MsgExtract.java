/**
   This file is part of Waarp Project.

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All Waarp Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Waarp is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Waarp .  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.gouv.culture.vitam.eml.test;

/*
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.dom4j.Element;

import com.auxilii.msgparser.Message;
import com.auxilii.msgparser.MsgParser;
import com.auxilii.msgparser.RecipientEntry;
import com.auxilii.msgparser.attachment.Attachment;
import com.auxilii.msgparser.attachment.FileAttachment;
import com.auxilii.msgparser.attachment.MsgAttachment;

import fr.gouv.culture.vitam.eml.StringUtils.EMAIL_FIELDS;
import fr.gouv.culture.vitam.extract.ExtractInfo;
import fr.gouv.culture.vitam.utils.Commands;
import fr.gouv.culture.vitam.utils.ConfigLoader;
import fr.gouv.culture.vitam.utils.StaticValues;
import fr.gouv.culture.vitam.utils.VitamArgument;
import fr.gouv.culture.vitam.utils.XmlDom;
*/

/**
 * http://auxilii.com/msgparser/Page.php?id=100
 * 
 *  see http://www.rgagnon.com/javadetails/java-0613.html
 * @author "Frederic Bregier"
 *
 */
public class MsgExtract {
	/*
	static File curPath = null;
	
	private static void addAddress(Element root, String entry, String address, String namead) {
		Element val = XmlDom.factory.createElement(entry);
		Element name = XmlDom.factory.createElement(EMAIL_FIELDS.emailName.name);
		Element addresse = XmlDom.factory.createElement(EMAIL_FIELDS.emailAddress.name);
		if (namead != null) {
			name.setText(StringUtils.unescapeHTML(namead, true, false));
			val.add(name);
		}
		if (address != null) {
			addresse.setText(StringUtils.unescapeHTML(address, true, false));
			val.add(addresse);
		}
		root.add(val);
	}
	*/
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
	 * @param msgFile
	 * @param filename
	 * @param argument
	 * @param config
	 * @return
	 */
	/*
	public static Element extractInfoEmail(File msgFile, String filename, VitamArgument argument,
			ConfigLoader config) {
		Element root = XmlDom.factory.createElement(EMAIL_FIELDS.formatMSG.name);
		MsgParser msgp = new MsgParser();
		Message msg = null;
		try {
			msg = msgp.parseMsg(msgFile);
			extractInfoSubEmail(msg, msgFile.getParentFile(), root, argument, config);
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
		return root;
	}
	
	private static String extractInfoSubEmail(Message msg, File curDir, Element root, VitamArgument argument,
			ConfigLoader config) {
		Element keywords = XmlDom.factory.createElement(EMAIL_FIELDS.keywords.name);
		Element metadata = XmlDom.factory.createElement(EMAIL_FIELDS.metadata.name);
		String result = "";
		try {
			String id = config.addRankId(root);
			curPath = new File(curDir, "MSG_"+id);
			//root.addAttribute(EMAIL_FIELDS.rankId.name, id);
			
			System.out.println("getMessageClass: "+msg.getMessageClass());
			// => IPM.Note = msg
			
			System.out.println("getPropertyListing: "+msg.getPropertyListing()); // DEBUG
			
			String fromEmail = msg.getFromEmail();
			String fromName = msg.getFromName();
			if (fromEmail != null || fromName != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.from.name);
				addAddress(sub, EMAIL_FIELDS.fromUnit.name, fromEmail, fromName);
				metadata.add(sub);
			}
			
			List<RecipientEntry> list = msg.getRecipients();
			if (list != null && ! list.isEmpty()) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.toRecipients.name);
				for (RecipientEntry recipientEntry : list) {
					String toEmail = recipientEntry.getToEmail();
					String toName = recipientEntry.getToName();
					addAddress(sub, EMAIL_FIELDS.toUnit.name, toEmail, toName);
				}
				metadata.add(sub);
			}
			try {
				list = msg.getCcRecipients();
				if (list != null && ! list.isEmpty()) {
					Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.ccRecipients.name);
					for (RecipientEntry recipientEntry : list) {
						String toEmail = recipientEntry.getToEmail();
						String toName = recipientEntry.getToName();
						addAddress(sub, EMAIL_FIELDS.ccUnit.name, toEmail, toName);
					}
					metadata.add(sub);
				}
			} catch (Exception e) {
			}
			try {
				list = msg.getBccRecipients();
				if (list != null && ! list.isEmpty()) {
					Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.bccRecipients.name);
					for (RecipientEntry recipientEntry : list) {
						String toEmail = recipientEntry.getToEmail();
						String toName = recipientEntry.getToName();
						addAddress(sub, EMAIL_FIELDS.bccUnit.name, toEmail, toName);
					}
					metadata.add(sub);
				}
			} catch (Exception e) {
			}
			System.out.println("getToEmail: "+msg.getToEmail());
			System.out.println("getDisplayCc: "+msg.getDisplayCc());
			System.out.println("getDisplayBcc: "+msg.getDisplayBcc());
			*/
			/*
			msg.getToRecipient();
			msg.getDisplayTo();
			msg.getToEmail();
			msg.getToName();
			msg.getCcRecipients();
			msg.getDisplayCc();
			msg.getBccRecipients();
			msg.getDisplayBcc();
			*/
			/*
			String subject = msg.getSubject();
			if (subject != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.subject.name);
				sub.setText(StringUtils.unescapeHTML(subject, true, false));
				metadata.add(sub);
			}
			
			Date sentDate = msg.getClientSubmitTime();
			Date receivedDate = msg.getCreationDate();
			System.out.println("Date ? " +sentDate+ receivedDate +":" + msg.getDate() + ":" +msg.getLastModificationDate());
			if (sentDate == null) {
				sentDate = msg.getDate();
			}
			if (sentDate != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.sentDate.name);
				sub.setText(sentDate.toString());
				metadata.add(sub);
			}
			if (receivedDate != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.receivedDate.name);
				sub.setText(receivedDate.toString());
				metadata.add(sub);
			}
			
			String headers_in_1 = msg.getHeaders();
			System.out.println("headers: "+headers_in_1);
			Set<Integer> props = msg.getPropertyCodes();
			for (Integer code : props) {
				Object obj = msg.getPropertyValue(code);
				System.out.println("Prop: "+code+" = "+obj.getClass().getName()+":"+obj);
				if (obj.getClass().getName().equals("[B")) {
					System.out.println("\t=> "+new String((byte []) obj));
				}
			}
			//msg.getPropertyValue(code);
			
			/*
			String [] headers = message.getHeader("Received");
			if (headers != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.receptionTrace.name);
				MailDateFormat mailDateFormat = null;
				long maxTime = 0;
				if (receivedDate == null) {
					mailDateFormat = new MailDateFormat();
				}
				for (String string : headers) {
					Element sub2 = XmlDom.factory.createElement(EMAIL_FIELDS.trace.name);
					sub2.setText(StringUtils.unescapeHTML(string, true, false));
					sub.add(sub2);
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
				metadata.add(sub);
			}
			int internalSize = message.getSize();
			if (internalSize > 0) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.emailSize.name);
				sub.setText(Integer.toString(internalSize));
				metadata.add(sub);
			}
			String description = message.getDescription();
			if (description != null) {
				Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.description.name);
				sub.setText(StringUtils.unescapeHTML(description, true, false));
				metadata.add(sub);
			}
			*/
			/*
			String messageId = msg.getMessageId();
			if (messageId != null) {
				messageId = StringUtils.removeChevron(StringUtils.unescapeHTML(
						messageId, true, false)).trim();
				if (messageId.length() > 1) {
					Element sub = XmlDom.factory.createElement(EMAIL_FIELDS.messageId.name);
					sub.setText(messageId);
					metadata.add(sub);
				}
			}
			/*
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
			*/
			
			/*
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
			*/
			/*
			// Plain text e-mail body
			String body = "";
			if (argument.extractKeyword || config.extractFile) {
				body = msg.getBodyText();
				boolean isTxt = true;
				boolean isHttp = false;
				if (body == null || body.isEmpty()) {
					isTxt = false;
					body = msg.getBodyHTML();
					isHttp = true;
					if (body == null || body.isEmpty()) {
						isHttp = false;
						body = msg.getBodyRTF();
					}
				}
				if (body != null && ! body.isEmpty()) {
					if (config.extractFile) {
						// XXX FIXME could saved email from HTML Body (clearer) if possible
						// use curRank in name, and attachment will be under directory named
						// add currank in field
						File newDir = curPath;
						newDir.mkdir();
						String filenamebody = messageId;
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
							html = msg.getBodyHTML();
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
							rtf = msg.getBodyRTF();
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
			
			Element identification = null;
			List<Attachment> atts = msg.getAttachments();
			
			if (atts != null && ! atts.isEmpty()) {
				File oldPath = curPath;
				if (config.extractFile) {
					File newDir = curPath;
					newDir.mkdir();
					curPath = newDir;
				}
				identification = XmlDom.factory.createElement(EMAIL_FIELDS.attachments.name);
				// get the number of attachments for this message
				int NumberOfAttachments = atts.size();
				identification.addAttribute(EMAIL_FIELDS.attNumber.name, Integer.toString(NumberOfAttachments));
				// get a specific attachment from this email.
				for (Attachment att : atts) {
					// do something with attachment
					if (att instanceof FileAttachment) {
						// a file
						FileAttachment fatt = (FileAttachment) att;
						if (argument.extractKeyword) {
							result += " " + extractInfoAttachment(fatt, identification, argument, config);
						} else {
							extractInfoAttachment(fatt, identification, argument, config);
						}
					} else if (att instanceof MsgAttachment) {
						// another Message
						Element newElt = XmlDom.factory.createElement(EMAIL_FIELDS.subidentity.name);
						Element identifications = XmlDom.factory.createElement("identification");
						Element identity = XmlDom.factory.createElement("identity");
						identity.addAttribute("format", "Microsoft Outlook Email Message");
						identity.addAttribute("mime", "application/vnd.ms-outlook");
						identity.addAttribute("puid", "x-fmt/430");
						identity.addAttribute("extensions", "msg");
						identifications.add(identity);
						newElt.add(identifications);
						if (argument.extractKeyword) {
							result += extractInfoSubEmail(((MsgAttachment) att).getMessage(), curDir, newElt, argument, config);
						} else {
							extractInfoSubEmail(((MsgAttachment) att).getMessage(), curDir, newElt, argument, config);
						}
					}
				}
				curPath = oldPath;
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
		} catch (UnsupportedOperationException e) {
			System.err.println(StaticValues.LBL.error_error.get() + e.toString());
			e.printStackTrace();
			String status = "Error during identification";
			root.addAttribute(EMAIL_FIELDS.status.name, status);
		}
		return result;
	}
	
	private static String extractInfoAttachment(FileAttachment fatt, Element identification, VitamArgument argument, ConfigLoader config) {
		Element newElt = XmlDom.factory.createElement(EMAIL_FIELDS.subidentity.name);
		String filename = null;
		String result = "";

		long size = fatt.getSize();

		// Check file
		filename = fatt.getLongFilename();
		if (filename.isEmpty()) {
			filename = fatt.getFilename();
		}
		filename = StringUtils.toFileName(filename);
		FileOutputStream out = null;
		File filetemp = null;
		try {
			String tempfilename = filename.isEmpty() ? (config.nbDoc.get()+1)+"_unknownAttachment.eml" : filename;
			// Force out as eml
			if (config.extractFile) {
				filetemp = new File(curPath, tempfilename);
			} else {
				filetemp = File.createTempFile(StaticValues.PREFIX_TEMPFILE, tempfilename);
			}
			out = new FileOutputStream(filetemp);
			byte [] data = fatt.getData();
			out.write(data);
			out.close();
			data = null;
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
			if (! config.extractFile) {
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
		} finally {
			if (filetemp != null && ! config.extractFile) {
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
		String mimetag = fatt.getMimeTag();
		if (mimetag != null) {
			Element elt = XmlDom.factory.createElement("attchmentMimeType");
			elt.setText(mimetag);
			newElt.add(elt);
		}
		identification.add(newElt);
		return result;
	}
	*/

}
