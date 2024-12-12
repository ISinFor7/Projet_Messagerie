package cat;
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
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Base64.Encoder;

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
		Key cle2=null;
		KeyPair cp;
		Key clep=null;
		try {
			clientSocket=serverSocket.accept();
			System.out.println("client connécté");
			out=new PrintWriter(clientSocket.getOutputStream(),true);
			in=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			//KeyGenerator kg=KeyGenerator.getInstance("AES");
			//cle=kg.generateKey();
			KeyPairGenerator kp = KeyPairGenerator.getInstance("RSA");
			cp = kp.genKeyPair();
			cle=cp.getPrivate();
			cle2=cp.getPublic();
			byte[] ck=cle2.getEncoded();
			System.out.println(ck.length);
			//System.out.println("PK: " + Base64.getEncoder().encodeToString(ck));
			//System.out.println(Base64.getEncoder().encodeToString(ck).length());
			out.println(Base64.getEncoder().encodeToString(ck));;;
			KeyFactory kf=KeyFactory.getInstance("RSA");
			EncodedKeySpec publicKeySpec=new X509EncodedKeySpec(ck);
			clep=kf.generatePublic(publicKeySpec);
			//System.out.println(cle2.getEncoded());
			chiffrem=Cipher.getInstance("RSA");
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
			chiffrem.init(Cipher.DECRYPT_MODE, cle2);
			original=chiffrem.doFinal(result);
			chiffrem.init(Cipher.DECRYPT_MODE, clep);
			byte[] original2=chiffrem.doFinal(result);
			String enve="";
			for (int i=0;i<result.length;i++) {
				if (i==result.length-1) {					
					enve=enve+result[i];
				}else {
					enve=enve+result[i]+"\n";					
				}
			}
			//System.out.println(enve.length());
			out.println(enve);
			//System.out.println(new String(result).length());
			//System.out.println(result.length);
			//System.out.println(hostinput);
			System.out.println(new String(original));
			System.out.println("user:"+in.readLine());
			System.out.println("meta:"+hostinput);
		}
		
		System.out.println("fin");
		out.close();
		in.close();
		messer.close();
		clientSocket.close();
	}

}
