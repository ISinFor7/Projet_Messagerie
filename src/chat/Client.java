package chat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Client {

	public static void main(String[] args) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InterruptedException {

		Socket echoSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		byte[] data;
		byte[] result;
		byte[] original;
		//byte[] code;
		Key cle=null;
		Cipher chiffrem=null;
		try {
			echoSocket = new Socket("localhost", 4444); // à modifier
			out = new PrintWriter(echoSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
			System.out.println("connécté");
			String testk=in.readLine();
			byte[] kk=Base64.getDecoder().decode(testk);
			EncodedKeySpec pls=new X509EncodedKeySpec(kk);
			cle=KeyFactory.getInstance("RSA").generatePublic(pls);
			//System.out.println(testk);
			//System.out.println(testk.length());
			chiffrem=Cipher.getInstance("RSA");
		} catch (UnknownHostException e) {
			System.out.println("perdu");
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("io error");
			System.exit(-1);
		}
		BufferedReader mess = new BufferedReader(new InputStreamReader(System.in));
		String userinput;
		String serverinput;
		while ((userinput = mess.readLine()).equals("bye") == false && ((serverinput=in.readLine())!=null)) {
			chiffrem.init(Cipher.ENCRYPT_MODE, cle);
			byte[] m=userinput.getBytes();
			byte[] mes=chiffrem.doFinal(m);
			String mail="";
			for (int i=0;i<mes.length;i++) {
				if (i==mes.length-1) {					
					mail=mail+mes[i];
				}else {
					mail=mail+mes[i]+"\n";					
				}
			}
			out.println(mail);			
			chiffrem.init(Cipher.DECRYPT_MODE, cle);
			byte[] code = new byte[256];
			code[0]=Byte.decode(serverinput);
			int j=1;
			while (in.ready()){
				code[j]=Byte.decode(in.readLine());
				j++;
			}
			//System.out.println("crypt:"+new String(code)); affichage cprypté
			byte[] messa=chiffrem.doFinal(code);
			System.out.println("echo:" + new String(messa));
			System.out.println("meta:" + userinput);

		}
		System.out.println("fin");
		out.close();
		in.close();
		mess.close();
		echoSocket.close();
	}
}
