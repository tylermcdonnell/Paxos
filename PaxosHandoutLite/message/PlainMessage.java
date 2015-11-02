package message;

/**
 * A bare-bone message with a content field.  General purpose.
 * @author Mike Feilbach
 *
 */
public class PlainMessage extends Message
{
	private static final long serialVersionUID = 1L;
	
	private String content;
	
	public PlainMessage(String messageContent)
	{
		this.content = messageContent;
	}

	@Override
	public String toString()
	{
		String retVal = "";
		retVal += "PlainMessage: <Content: " + this.content + ">";
		return retVal;
	}

}
