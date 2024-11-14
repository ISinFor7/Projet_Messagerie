import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur extends Thread {
	ServerSocket serverSocket=null;//créé le socket afin de recevoir les connections sur le serveur
	private static ExecutorService executor = Executors.newFixedThreadPool(100);//met une limite pour la création de Threads

	Serveur() {
		try {
			serverSocket=new ServerSocket(4444);//mise en place du socket sur le port 4444
			this.start();//lancement du thread principal
		}catch (IOException e) {
			System.out.println("erreur port 4444");
			System.exit(-1);
		}
	}
	
	public void run() {
		while (true) {
			try {
				
				final Socket clientSocket=serverSocket.accept();
				executor.execute(new Runnable() {//lancement d'un thread pour un client
					public void run() { 
						try {
							//clientSocket=serverSocket.accept();
							out=new PrintWriter(clientSocket.getOutputStream(),true);
							in=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
							BufferedReader messer=new BufferedReader(new InputStreamReader(System.in));
							String hostinput;
							while ((hostinput=messer.readLine()).equals("bye")==false) {
								out.println(hostinput);
								System.out.println(hostinput);
								System.out.println("user:"+in.readLine());
								System.out.println("meta"+hostinput);
							}
						}catch(IOException e) {
							System.out.println("echec port 4444");
							System.exit(-1);
						}
					}
				})
			} catch (IOException ex) {
				Logger.getLogger(SimpleServer.class.getName()).log(Level.SEVERE, null, ex);
			} catch (Exception e) {
				System.out.println("Exceptiopn: "+e);
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		Serveur s = new Serveur()
		
		/* try {
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
	}*/

}
