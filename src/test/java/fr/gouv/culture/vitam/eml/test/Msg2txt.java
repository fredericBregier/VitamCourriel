package fr.gouv.culture.vitam.eml.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Calendar;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;

public class Msg2txt {
	/**
	 * The stem used to create file names for the text file and the directory
	 * that contains the attachments.
	 */
	private String fileNameStem;
	
	/**
	 * The Outlook MSG file being processed.
	 */
	private MAPIMessage msg;
	
	public Msg2txt(String fileName) throws IOException {
		fileNameStem = fileName;
		if(fileNameStem.endsWith(".msg") || fileNameStem.endsWith(".MSG")) {
			fileNameStem = fileNameStem.substring(0, fileNameStem.length() - 4);
		}
		msg = new MAPIMessage(fileName);
	}
	
	/**
	 * Processes the message.
	 * 
	 * @throws IOException if an exception occurs while writing the message out
	 */
	public void processMessage() throws IOException {
		String txtFileName = fileNameStem + ".txt";
		String attDirName = fileNameStem + "-att";
		PrintWriter txtOut = null;
		try {
			txtOut = new PrintWriter(txtFileName);
			try {
				String displayFrom = msg.getDisplayFrom();
				txtOut.println("From: "+displayFrom);
			} catch (ChunkNotFoundException e) {
				// ignore
			}
			try {
				String displayTo = msg.getDisplayTo();
				txtOut.println("To: "+displayTo);
			} catch (ChunkNotFoundException e) {
				// ignore
			}
			try {
				String displayCC = msg.getDisplayCC();
				txtOut.println("CC: "+displayCC);
			} catch (ChunkNotFoundException e) {
				// ignore
			}
			try {
				String displayBCC = msg.getDisplayBCC();
				txtOut.println("BCC: "+displayBCC);
			} catch (ChunkNotFoundException e) {
				// ignore
			}
			try {
				String subject = msg.getSubject();
				txtOut.println("Subject: "+subject);
			} catch (ChunkNotFoundException e) {
				// ignore
			}
			try {
				String body = msg.getTextBody();
				txtOut.println("BODY:\n"+body);
			} catch (ChunkNotFoundException e) {
				System.err.println("No message body");
			}
			
			try {
				String test = msg.getConversationTopic();
				txtOut.println("getConversationTopic: "+test);
			} catch (ChunkNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DocumentSummaryInformation info = msg.getDocumentSummaryInformation();
			if (info != null) {
				txtOut.println("DocumentSummaryInformation: "+info.toString());
				txtOut.println("\t getByteCount: "+info.getByteCount());
				txtOut.println("\t getCategory: "+info.getCategory());
				txtOut.println("\t getCompany: "+info.getCompany());
				txtOut.println("\t getLineCount: "+info.getLineCount());
				txtOut.println("\t getManager: "+info.getManager());
				txtOut.println("\t getPresentationFormat: "+info.getPresentationFormat());
			}
			try {
				String [] test = msg.getHeaders();
				for (String string : test) {
					txtOut.println("getHeaders: "+string);
				}
			} catch (ChunkNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Calendar cal = msg.getMessageDate();
				txtOut.println("getMessageDate: "+cal.toString());
			} catch (ChunkNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				String [] test = msg.getRecipientEmailAddressList();
				for (String string : test) {
					txtOut.println("getRecipientEmailAddressList: "+string);
				}
			} catch (ChunkNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				String [] test = msg.getRecipientNamesList();
				for (String string : test) {
					txtOut.println("getRecipientNamesList: "+string);
				}
			} catch (ChunkNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SummaryInformation info2 = msg.getSummaryInformation();
			if (info2 != null) {
				txtOut.println("getSummaryInformation: "+info2.toString());
				txtOut.println("\t getApplicationName: "+info2.getApplicationName());
				txtOut.println("\t getAuthor: "+info2.getAuthor());
				txtOut.println("\t getComments: "+info2.getComments());
				txtOut.println("\t getKeywords: "+info2.getKeywords());
				txtOut.println("\t getLastAuthor: "+info2.getLastAuthor());
				txtOut.println("\t getRevNumber: "+info2.getRevNumber());
				txtOut.println("\t getSubject: "+info2.getSubject());
				txtOut.println("\t getTemplate: "+info2.getTemplate());
				txtOut.println("\t getTitle: "+info2.getTitle());
				txtOut.println("\t getCreateDateTime: "+info2.getCreateDateTime());
				txtOut.println("\t getLastSaveDateTime: "+info2.getLastSaveDateTime());
			}
			
			AttachmentChunks[] attachments = msg.getAttachmentFiles();
			if(attachments.length > 0) {
				File d = new File(attDirName);
				if(d.mkdir()) {
					for(AttachmentChunks attachment : attachments) {
						processAttachment(attachment, d);
					}
				} else {
					System.err.println("Can't create directory "+attDirName);
				}
			}
		} finally {
			if(txtOut != null) {
				txtOut.close();
			}
		}
	}
	
	/**
	 * Processes a single attachment: reads it from the Outlook MSG file and
	 * writes it to disk as an individual file.
	 *
	 * @param attachment the chunk group describing the attachment
	 * @param dir the directory in which to write the attachment file
	 * @throws IOException when any of the file operations fails
	 */
	public void processAttachment(AttachmentChunks attachment, 
	      File dir) throws IOException {
	   String fileName = attachment.attachFileName.toString();
	   if(attachment.attachLongFileName != null) {
	      fileName = attachment.attachLongFileName.toString();
	   }
	   
		File f = new File(dir, fileName);
		OutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream(f);
			fileOut.write(attachment.attachData.getValue());
		} finally {
			if(fileOut != null) {
				fileOut.close();
			}
		}
	}
	
	/**
	 * Processes the list of arguments as a list of names of Outlook MSG files.
	 * 
	 * @param args the list of MSG files to process
	 */
	public static void main(String[] args) {
		if(args.length <= 0) {
			System.err.println("No files names provided");
		} else {
			for(int i = 0; i < args.length; i++) {
				try {
					Msg2txt processor = new Msg2txt(args[i]);
					processor.processMessage();
				} catch (IOException e) {
					System.err.println("Could not process "+args[i]+": "+e);
				}
			}
		}
	}
}
