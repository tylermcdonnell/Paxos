package message;

import server.PValue;


/**
 * A p2a message in Paxos, sent from a commander to an acceptor.
 * @author Mike Feilbach
 *
 */
public class P2a extends Message
{
	private static final long serialVersionUID = 1L;

	// This commander's leader's ID.
	private int myLeaderId;
	
	private PValue pvalue;
	
	public P2a(int myLeaderId, PValue pvalue)
	{
		this.myLeaderId = myLeaderId;
		this.pvalue = pvalue;
	}
	
	public int getMyLeaderId()
	{
		return this.myLeaderId;
	}
	
	public PValue getMyPValue()
	{
		return this.pvalue;
	}

	@Override
	public String toString() {
		String retVal = "";
		retVal += "P2a: <myLeaderId: " + this.myLeaderId + ", " + this.pvalue + ">";
		return retVal;
	}
}
