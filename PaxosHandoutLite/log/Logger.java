package log;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Logger implements Runnable {

	private static Logger instance = null;
	
	private ConcurrentLinkedQueue<String> q;
	
	private Logger()
	{
		q = new ConcurrentLinkedQueue<String>();
	}
	
	public static Logger getInstance()
	{
		if (instance == null)
		{
			instance = new Logger();
		}
		return instance;
	}
	
	public void print(String s)
	{
		q.add(s);
	}
	
	public void println(String s)
	{
		q.add(s + "\n");
	}
	
	public void run()
	{
		while (true)
		{
			while (q.peek() != null)
			{
				System.out.println(q.remove());
			}
		}
	}
}
