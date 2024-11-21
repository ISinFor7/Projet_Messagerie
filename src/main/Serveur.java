package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {
	
	public static void main(String[] args) throws IOException {
		
		ServerSocket serverSocket=null;
		try {
			serverSocket=new ServerSocket(4444);
		}catch (IOException e) {
			System.out.println("erreur port 4444");
			System.exit(-1);
		}
		Socket clientSocket=null;
		PrintWriter out=null;
		BufferedReader in=null;
		try {
			clientSocket=serverSocket.accept();
			out=new PrintWriter(clientSocket.getOutputStream(),true);
			in=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		}catch(IOException e) {
			System.out.println("echec port 4444");
			System.exit(-1);
		}
		BufferedReader messer=new BufferedReader(new InputStreamReader(System.in));
		String hostinput;
		while ((hostinput=messer.readLine()).equals("bye")==false) {
			out.println(hostinput);
			System.out.println(hostinput);
			System.out.println("user:"+in.readLine());
			System.out.println("meta"+hostinput);
		}
		
		System.out.println("fin");
		out.close();
		in.close();
		messer.close();
		clientSocket.close();
	}

}