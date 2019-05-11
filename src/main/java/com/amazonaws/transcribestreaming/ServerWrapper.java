package com.amazonaws.transcribestreaming;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerWrapper implements Closeable
{
    public ServerWrapper(Integer port) 
    {
        _port = port;
    }

    private final Integer _port;
    private PrintWriter _out;
    private ServerSocket _serverSocket;
    private Socket _clientSocket;

	public final PrintWriter getOut()
	{
		return _out;
	}
	private void setOut(PrintWriter value)
	{
		_out = value;
	}

    public final void open() throws IOException
    {
        _serverSocket = new ServerSocket(_port);
        _clientSocket = _serverSocket.accept();
        _out = new PrintWriter(_clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
            new InputStreamReader(_clientSocket.getInputStream()));
    
        String inputLine, outputLine;
        
        //while ((inputLine = in.readLine()) != null) {
        //}
    }

    @Override
    public void close() throws IOException {
        _out.close();
        _serverSocket.close();
        _clientSocket.close();
    }
}
