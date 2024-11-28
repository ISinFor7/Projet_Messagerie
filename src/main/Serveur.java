package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.crypto.*;
import java.security.*;

public class Serveur {
	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		
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
		byte[] data;
		byte[] result;
		byte[] original;
		Key cle=null;
		Cipher chiffrem=null;
		try {
			clientSocket=serverSocket.accept();
			System.out.println("client connécté");
			out=new PrintWriter(clientSocket.getOutputStream(),true);
			in=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			KeyGenerator kg=KeyGenerator.getInstance("AES");
			cle=kg.generateKey();
			chiffrem=Cipher.getInstance("AES");
		}catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}catch(IOException e) {
			System.out.println("echec port 4444");
			System.exit(-1);
		}catch(Exception e) {
			e.printStackTrace();
		}
		BufferedReader messer=new BufferedReader(new InputStreamReader(System.in));
		String hostinput;
		while ((hostinput=messer.readLine()).equals("bye")==false) {
			chiffrem.init(Cipher.ENCRYPT_MODE, cle);
			data=hostinput.getBytes();
			result=chiffrem.doFinal(data);
			chiffrem.init(Cipher.ENCRYPT_MODE, cle);
			original=chiffrem.doFinal(result);
			out.println(result);
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