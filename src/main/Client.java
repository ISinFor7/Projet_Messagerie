package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	public static void main(String[] args) throws IOException {
		
		Socket echoSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		try {
			echoSocket=new Socket("Ordenateur",4444); //à modifier
			out=new PrintWriter(echoSocket.getOutputStream(),true);
			in=new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
		}catch(UnknownHostException e) {
			System.out.println("perdu");
			System.exit(-1);
		}catch(IOException e) {
			System.out.println("io error");
			System.exit(-1);
		}
		BufferedReader mess=new BufferedReader(new InputStreamReader(System.in));
		String userinput;
		while((userinput=mess.readLine()).equals("bye")==false) {
			System.out.println(userinput!="bye");
			out.println(userinput);
			System.out.println("echo:"+in.readLine());
			System.out.println("meta:"+userinput);
			
		}
		System.out.println("fin");
		out.close();
		in.close();
		mess.close();
		echoSocket.close();
	}
}