import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class BufferedReaderEx extends BufferedReader
{
	
	/**
	 * Creates a buffering character-input stream that uses a default-sized
	 * input buffer.
	 *
	 * @param in A Reader
	 */
	public BufferedReaderEx(Reader in)
	{
		super(in);
	}
	
	/**
	 * Polls until stream is ready to be read, then mimics functionality of readLine().
	 * This allows for other threads to interrupt when blocking for an input.
	 *
	 * @return A String containing the contents of the line, not including any line-termination characters, or null if the end of the stream has been reached without reading any characters.
	 * @throws InterruptedException If any thread has interrupted the current thread. The interrupted status of the current thread is cleared when this exception is thrown.
	 * @throws IOException If an I/O error occurs.
	 */
	public String ReadLineWhenReady() throws InterruptedException, IOException
	{
		// Polls until stream is ready to be read
		while (!ready())
		{
			Thread.sleep(5);
		}
		
		return readLine();
	}
}
