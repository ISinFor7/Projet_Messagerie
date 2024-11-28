package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Key;

import javax.crypto.Cipher;

public class Client {

	public static void main(String[] args) throws IOException {

		Socket echoSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		byte[] data;
		byte[] result;
		byte[] original;
		Key cle=null;
		Cipher chiffrem=null;
		try {
<<<<<<< HEAD:src/main/Client.java
			echoSocket=new Socket("localhost",4444); //à modifier
			out=new PrintWriter(echoSocket.getOutputStream(),true);
			in=new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
		}catch(UnknownHostException e) {
=======
			echoSocket = new Socket("Ordenateur", 4444); // à modifier
			out = new PrintWriter(echoSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
			System.out.println("connécté");
		} catch (UnknownHostException e) {
>>>>>>> c3eae76a05cdf171b252135b1cdbc55ebdb564d1:src/Client.java
			System.out.println("perdu");
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("io error");
			System.exit(-1);
		}
		BufferedReader mess = new BufferedReader(new InputStreamReader(System.in));
		String userinput;
		while ((userinput = mess.readLine()).equals("bye") == false) {
			out.println(userinput);
			System.out.println("echo:" + in.readLine());
			System.out.println("meta:" + userinput);

		}
		System.out.println("fin");
		out.close();
		in.close();
		mess.close();
		echoSocket.close();
	}
}