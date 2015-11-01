package message;

import java.io.Serializable;

/**
 * A generic message class which all messages will inherit from.
 * @author Mike Feilbach
 *
 */
public abstract class Message implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	// Force all messages to implement this method.
	public abstract String toString();
}
