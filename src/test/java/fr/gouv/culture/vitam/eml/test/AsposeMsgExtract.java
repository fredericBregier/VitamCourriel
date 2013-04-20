package fr.gouv.culture.vitam.eml.test;

/*
import com.aspose.email.Attachment;
import com.aspose.email.HeaderCollection;
import com.aspose.email.MailAddressCollection;
import com.aspose.email.MailMessage;
import com.aspose.email.MessageFormat;
*/
public class AsposeMsgExtract {

	/**
	 * @param args
	 */
	/*
	public static void main(String[] args) {
        System.out.println("Loading messages....");
        if (args.length <= 0) {
        	System.err.println("Need a MSG as argument");
        	return;
        }
		// Set license
        setLicense();
        try
        {
            // load a message in EML, MSG or MHTML format
            //MailMessage message = MailMessage.load(strBaseFolder + "New.eml", MessageFormat.getEml());
            MailMessage message = MailMessage.load(args[0], MessageFormat.getMsg());
            //MailMessage message = MailMessage.load(strBaseFolder + "New.mht", MessageFormat.getMht());

            // Display subject
            System.out.println("Subject: " + message.getSubject());
            // Display sender's information
            System.out.println("From: " + message.getFrom());
            System.out.println("Sender: " + message.getSender());
            // Display recipient's information
            for (int i=0 ; i<message.getTo().size() ; i++)
            {
                System.out.println("To: " + message.getTo().get(i));
            }
            for (int i=0 ; i<message.getCC().size() ; i++)
            {
                System.out.println("Cc: " + message.getCC().get(i));
            }
            for (int i=0 ; i<message.getBcc().size() ; i++)
            {
                System.out.println("Bcc: " + message.getBcc().get(i));
            }
            System.out.println("Date: " + message.getDate());
            HeaderCollection coll = message.getHeaders();
            for (int i=0 ; i<coll.getCount() ; i++)
            {
                System.out.println("Headers: " + coll.get(i));
            }
            System.out.println("MessageId: " + message.getMessageId());
            System.out.println("Priority: " + message.getPriority());
            MailAddressCollection coll2 = message.getReplyToList();
            for (int i=0 ; i<coll2.size() ; i++)
            {
                System.out.println("ReplyTo: " + coll2.get(i));
            }
            System.out.println("ReversePath: " + message.getReversePath());
            
            // Display the attachment names
            for (int i=0 ; i<message.getAttachments().size() ; i++)
            {
                Attachment att = (Attachment)message.getAttachments().get(i);
                System.out.println("Attachment: " + att.getName());
            }

            // Display the text and HTML body of the message
            System.out.println("Text body: " + message.getTextBody());
            System.out.println("HTML body: " + message.getHtmlBody());
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
	}
	private static void setLicense()
    {
        try
        {
            // Set license. Provide full path and license file name
            com.aspose.email.License licEmail = new com.aspose.email.License();
            licEmail.hashCode();
            //licEmail.setLicense(strBaseFolder + "Aspose.Total.Product.Family.lic");
        }
        catch(Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }
    */
}
