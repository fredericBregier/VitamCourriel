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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.dom4j.Element;

import fr.gouv.culture.vitam.eml.StringUtils.EMAIL_FIELDS;
import fr.gouv.culture.vitam.utils.ConfigLoader;
import fr.gouv.culture.vitam.utils.VitamArgument;
import fr.gouv.culture.vitam.utils.XmlDom;

/**
 * Inspired from https://github.com/nicbet/MailboxMiner
 * 
 * @author "Frederic Bregier"
 * 
 */
public class MailboxParser {
	static boolean debug = false;
	/**
	 * Minimum amount of consecutive header lines to encounter after a potential separator line
	 * before it is considered for splitting.
	 */
	private int HEADERTHRESHOLD = 2;

	/**
	 * Counts the number of collisions in the HashTable of seen messages. (I.e. counts duplicate
	 * e-mails)
	 */
	private int collisions;
	private int numberEmails = 0;

	/**
	 * Set whether to discard duplicate messages or not. Default behaviour is to silently get rid of
	 * duplicates.
	 */
	private boolean IGNOREDUPLICATES = true;
	
	public static enum UsableCharset{
		UTF_8, ISO_8859_1, ISO_8859_15, US_ASCII;
		
		public String name;
		private UsableCharset() {
			this.name = name().replace('_', '-');
		}
	}
	private String charset = UsableCharset.ISO_8859_15.name;

	public int getCollisions() {
		return collisions;
	}

	public void setCollisions(int collisions) {
		this.collisions = collisions;
	}

	public boolean isIGNOREDUPLICATES() {
		return IGNOREDUPLICATES;
	}

	public void setIGNOREDUPLICATES(boolean ignoreduplicates) {
		IGNOREDUPLICATES = ignoreduplicates;
	}

	public int getHEADERTHRESHOLD() {
		return HEADERTHRESHOLD;
	}

	public void setHEADERTHRESHOLD(int headerthreshold) {
		HEADERTHRESHOLD = headerthreshold;
	}

	/**
	 * Parse a given .mbox file and return a list of {@link Message} objects.
	 * 
	 * @param filename
	 *            a {@link String} containing the path to an .mbox file.
	 * @return a {@link List} of {@link Message} objects extracted from the file.
	 */
	private List<Message> parseMessages(String filename) {
		collisions = 0;
		List<Message> messages = new ArrayList<Message>();
		HashSet<Integer> seenMessages = new HashSet<Integer>();

		// Open the file for reading
		BufferedReader reader = null;
		try {
			StringBuilder inputBuilder = new StringBuilder();
			String line = "";
			reader = new BufferedReader(new FileReader(filename));
			// Read the mbox file line by line
			while ((line = reader.readLine()) != null) {
				inputBuilder.append(line);
				inputBuilder.append(System.getProperty("line.separator"));
			}

			String text = inputBuilder.toString();
			inputBuilder = null;

			String[] rawlines = null;//text.split("(\n\r)|(\n)|(\r)");
			rawlines = text.split("\r?\n|\r");
			if (debug)
				System.err.println("Split file into " + rawlines.length + " lines");

			Pattern seperatorPattern = Pattern.compile("^From (.*?) (.*?):(.*?):(.*?)$"); // From
																							// Apache
																							// JAMES
																							// server
			Pattern headerPattern = Pattern.compile("^[\\x21-\\x39\\x3B-\\x7E]+:(.*)$"); // From RFC
																							// 5322
																							// - Oct
																							// 2008
			String ssep = System.getProperty("line.separator");

			// Here comes the big ugly loop ...
			int lastFoundSepLine = -1;
			Map<Integer, Integer> separatorsMap = new HashMap<Integer, Integer>();

			for (int line_num = 0; line_num < rawlines.length; line_num++) {

				String currentLine = rawlines[line_num];

				// If we found a header name line
				if (headerPattern.matcher(currentLine).matches()) {
					/*if (debug)
						System.err.println("HEADER MATCH! " + line_num);*/
					if (lastFoundSepLine != -1) {
						if (separatorsMap.containsKey(lastFoundSepLine)) {
							int numHeaders = separatorsMap.get(lastFoundSepLine);
							numHeaders++;
							separatorsMap.put(lastFoundSepLine, numHeaders);
						}
					}
				}

				// If we found a separator line
				if (seperatorPattern.matcher(currentLine).matches()) {
					/*if (debug)
						System.err.println("SEP MATCH! " + line_num);*/
					lastFoundSepLine = line_num;
					separatorsMap.put(lastFoundSepLine, 0);
				}
			}
			// Treat the end of the file as potential separator ;-)
			separatorsMap.put(rawlines.length, HEADERTHRESHOLD);

			// Compose the messages
			// If we read at least HEADERTHRESHOLD many headers after the separator
			List<Integer> separators = new ArrayList<Integer>();
			for (Integer x : separatorsMap.keySet()) {
				if (separatorsMap.get(x) >= HEADERTHRESHOLD) {
					separators.add(x);
				} else {
					// Line x is a bogus header line and should be escaped!!
					rawlines[x.intValue()] = ">" + rawlines[x.intValue()];
				}
			}

			Collections.sort(separators);

			for (int i = 0; i < separators.size() - 1; i++) {
				int startLine = separators.get(i);
				int endLine = separators.get(i + 1);
				if (debug)
					System.err.println("Message from lines " + startLine + " - " + endLine + " (" + (endLine-startLine+1) + ")");
				// compose a raw message
				StringBuilder rawMsgBuilder = new StringBuilder();
				for (int l = startLine + 1; l < endLine; l++) {
					rawMsgBuilder.append(rawlines[l] + ssep);
				}
				String rawMessageText = rawMsgBuilder.toString().trim();
				int hashKey = rawMessageText.hashCode();
				if (!seenMessages.contains(hashKey)) {
					// XXX FIXME should take care one by one and not storing it
					/*System.err.println("-----------------------------");
					System.err.print(rawMessageText);
					System.err.println();*/
					convertTextToMimeMessage(rawMessageText);
					numberEmails++;
					//messages.add(convertTextToMimeMessage(rawMessageText));
					if (IGNOREDUPLICATES) {
						seenMessages.add(hashKey);
					}
				} else {
					if (debug)
						System.err.println("Duplicated message found");
					collisions++;
				}
			}
			// end compose the last message if that one was valid

			if (debug)
				System.err.println("Split into " + numberEmails + " messages!");

		} catch (IOException e) {
			if (debug)
				System.err.println("Error while trying to read file: " + filename);
			if (debug)
				System.err.println(e.getMessage());
			if (debug)
				System.err.println("-------- Stacktrace ----------");
			if (debug)
				e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		return messages;
	}

	VitamArgument argument;
	ConfigLoader config;
	File curPath;
	
	public static Element extractInfoEmail(File mboxFile, VitamArgument argument, ConfigLoader config) {
		Element root = XmlDom.factory.createElement(EMAIL_FIELDS.formatMBOX.name);
		root.addAttribute(EMAIL_FIELDS.filename.name, mboxFile.getPath());
		MailboxParser parser = new MailboxParser();
		parser.argument = argument;
		parser.config = config;
		File oldDir = argument.currentOutputDir;
		if (argument.currentOutputDir == null) {
			if (config.outputDir != null) {
				argument.currentOutputDir = new File(config.outputDir);
			} else {
				argument.currentOutputDir = new File(mboxFile.getParentFile().getAbsolutePath());
			}
		}
		if (config.extractFile) {
			parser.curPath = new File(argument.currentOutputDir, "MBOX_"+mboxFile.getName());
			parser.curPath.mkdirs();
			argument.currentOutputDir = parser.curPath;
		}
		Element res = parser.extractInfoMbox(mboxFile, root);
		argument.currentOutputDir = oldDir;
		return res;
	}
	
	private Element extractInfoMbox(File mboxFile, Element root) {
		collisions = 0;
		HashSet<Integer> seenMessages = new HashSet<Integer>();

		// Open the file for reading
		BufferedReader reader = null;
		try {
			StringBuilder inputBuilder = new StringBuilder();
			String line = "";
			FileInputStream inputStream = new FileInputStream(mboxFile);
			reader = new BufferedReader(new InputStreamReader(inputStream, charset));
			// Read the mbox file line by line
			while ((line = reader.readLine()) != null) {
				inputBuilder.append(line);
				inputBuilder.append(System.getProperty("line.separator"));
			}

			String text = inputBuilder.toString();
			inputBuilder = null;

			String[] rawlines = null;//text.split("(\n\r)|(\n)|(\r)");
			rawlines = text.split("\r?\n|\r");
			if (debug)
				System.err.println("Split file into " + rawlines.length + " lines");

			Pattern seperatorPattern = Pattern.compile("^From (.*?) (.*?):(.*?):(.*?)$"); // From
																							// Apache
																							// JAMES
																							// server
			Pattern headerPattern = Pattern.compile("^[\\x21-\\x39\\x3B-\\x7E]+:(.*)$"); // From RFC
																							// 5322
																							// - Oct
																							// 2008
			String ssep = System.getProperty("line.separator");

			// Here comes the big ugly loop ...
			int lastFoundSepLine = -1;
			Map<Integer, Integer> separatorsMap = new HashMap<Integer, Integer>();

			for (int line_num = 0; line_num < rawlines.length; line_num++) {

				String currentLine = rawlines[line_num];

				// If we found a header name line
				if (headerPattern.matcher(currentLine).matches()) {
					/*if (debug)
						System.err.println("HEADER MATCH! " + line_num);*/
					if (lastFoundSepLine != -1) {
						if (separatorsMap.containsKey(lastFoundSepLine)) {
							int numHeaders = separatorsMap.get(lastFoundSepLine);
							numHeaders++;
							separatorsMap.put(lastFoundSepLine, numHeaders);
						}
					}
				}

				// If we found a separator line
				if (seperatorPattern.matcher(currentLine).matches()) {
					/*if (debug)
						System.err.println("SEP MATCH! " + line_num);*/
					lastFoundSepLine = line_num;
					separatorsMap.put(lastFoundSepLine, 0);
				}
			}
			// Treat the end of the file as potential separator ;-)
			separatorsMap.put(rawlines.length, HEADERTHRESHOLD);

			// Compose the messages
			// If we read at least HEADERTHRESHOLD many headers after the separator
			List<Integer> separators = new ArrayList<Integer>();
			for (Integer x : separatorsMap.keySet()) {
				if (separatorsMap.get(x) >= HEADERTHRESHOLD) {
					separators.add(x);
				} else {
					// Line x is a bogus header line and should be escaped!!
					rawlines[x.intValue()] = ">" + rawlines[x.intValue()];
				}
			}

			Collections.sort(separators);

			for (int i = 0; i < separators.size() - 1; i++) {
				int startLine = separators.get(i);
				int endLine = separators.get(i + 1);
				if (debug)
					System.err.println("Message from lines " + startLine + " - " + endLine + " (" + (endLine-startLine+1) + ")");
				else System.out.print('.');
				// compose a raw message
				StringBuilder rawMsgBuilder = new StringBuilder();
				for (int l = startLine + 1; l < endLine; l++) {
					rawMsgBuilder.append(rawlines[l] + ssep);
				}
				String rawMessageText = rawMsgBuilder.toString().trim();
				int hashKey = rawMessageText.hashCode();
				if (!seenMessages.contains(hashKey)) {
					MimeMessage message = convertTextToMimeMessage(rawMessageText);
					if (message == null) {
						Element newElt = XmlDom.factory.createElement(EMAIL_FIELDS.formatEML.name);
						String status = "Error during identification";
						newElt.addAttribute(EMAIL_FIELDS.status.name, status);
						root.add(newElt);
					} else {
						numberEmails++;
						if (IGNOREDUPLICATES) {
							seenMessages.add(hashKey);
						}
						Element emlroot = XmlDom.factory.createElement(EMAIL_FIELDS.formatEML.name);
						// <identity format="Internet Message Format" mime="message/rfc822" puid="fmt/278" extensions="eml"/>
						Element identification = XmlDom.factory.createElement("identification");
						Element identity = XmlDom.factory.createElement("identity");
						identity.addAttribute("format", "Internet Message Format");
						identity.addAttribute("mime", "message/rfc822");
						identity.addAttribute("puid", "fmt/278");
						identity.addAttribute("extensions", "eml");
						identification.add(identity);
						emlroot.add(identification);
						EmlExtract.extractInfoMessage(message, emlroot, argument, config);
						root.add(emlroot);
						/*
						if (config.extractFile) {
							File old = argument.currentOutputDir;
							String id = emlroot.attributeValue(EMAIL_FIELDS.rankId.name);
							if (config.extractFile) {
								File newOutDir = new File(argument.currentOutputDir, id);
								newOutDir.mkdirs();
								argument.currentOutputDir = newOutDir;
							}
							// XXX FIXME should write rawMessageText to eml file using id+"_"+message.getSubject()+".eml"
							System.out.println("should write rawMessageText to eml file using "+id+" and subdir .eml");
							argument.currentOutputDir = old;
						}
						*/
					}
				} else {
					if (debug)
						System.err.println("Duplicated message found");
					collisions++;
				}
			}
			// end compose the last message if that one was valid
			if (numberEmails == 0) {
				// not a MBOX
				root = null;
			} else {
				System.err.println("Split into " + numberEmails + " messages!");
			}

		} catch (IOException e) {
			System.err.println("Error while trying to read file: " + mboxFile.getAbsolutePath()+" "+e.getMessage());
			if (debug)
				System.err.println("-------- Stacktrace ----------");
			if (debug)
				e.printStackTrace();
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		if (root != null) {
			root.addAttribute("nbEml", config.nbDoc.toString());
		}

		return root;
	}
	/**
	 * Parse a text block as an email and convert it into a mime message
	 * 
	 * @param emailBody
	 *            The headers and body of an email. This will be parsed into a mime message and
	 *            stored
	 */
	private static MimeMessage convertTextToMimeMessage(String emailBody) {
		// this.emailBody = emailBody;
		MimeMessage mimeMessage = null;
		// Parse the mime message as we have the full message now (in string format)
		ByteArrayInputStream mb = new ByteArrayInputStream(emailBody.getBytes());
		Properties props = System.getProperties();
		Session session = Session.getDefaultInstance(props);
		try {
			mimeMessage = new MimeMessage(session, mb);

		} catch (MessagingException e) {
			System.err.println("Error converting raw message to MimeMessage");
			if (debug)
				e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Error converting raw message to MimeMessage");
			if (debug)
				e.printStackTrace();
		}

		return mimeMessage;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Please supply a path to an mbox file to parse");
			return;
		}
		MailboxParser mailboxParser = new MailboxParser();
		mailboxParser.parseMessages(args[0]);
		System.out.println("MailBox collisions: "+mailboxParser.getCollisions());
	}

}
