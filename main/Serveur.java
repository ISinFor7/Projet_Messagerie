package main;

import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Serveur {
	private static final int PORT = 4442;
	private static Map<String, PrintWriter> clients = new ConcurrentHashMap<>();

	public static void main(String[] args) throws NoSuchAlgorithmException {
		System.out.println("Serveur lancé port " + PORT);
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			while (true) {
				new ClientHandler(serverSocket.accept()).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class ClientHandler extends Thread {
		private Socket socket;
		private PrintWriter out;
		private BufferedReader in;
		private String name;

		public ClientHandler(Socket socket) throws NoSuchAlgorithmException {
			this.socket = socket;
		}

		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);

				name = in.readLine();

				synchronized (clients) {
					if (name == null || name.isEmpty() || clients.containsKey(name)) {
						socket.close();
						return;
					}
				}
				out.println("Bonjour " + name);

				synchronized (clients) {
					clients.put(name, out);
					broadcastUserList();
				}

				System.out.println(name + " connecté.");

				String input;
				while ((input = in.readLine()) != null) {
					handlePrivateMessage(input);
				}
			} catch (IOException e) {
				System.out.println(name + " déconnecté car erreur.");
			} finally {
				if (name != null) {
					System.out.println(name + " déconnecté.");
					clients.remove(name);
					broadcastUserList();
				}
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void handlePrivateMessage(String input) {
			String[] parts = input.split(" ", 2);
			if (parts.length < 2) {
				out.println("Message invalide.");
				return;
			}
			//doublement des messages pour simplifier la crypto
			//manière de faire absolument pas optimisé mais fera là faire par manque de temp
			String messagedes = "";
			String messageenv = "";
			String targetUser = parts[0];
			String[] message = parts[1].split("//");
			if (message.length == 2) {
				messagedes = message[0];
				messageenv = message[1];
			}
			PrintWriter targetOut = clients.get(targetUser);
			if (targetOut != null) {
				//détecte une demande de clé
				if (parts[1].charAt(0) == 'n') {
					targetOut.println("(nk " + name + ") " + parts[1]);
				//détecte un envoi de clé
				} else if (parts[1].charAt(0) == 'g') {
					targetOut.println("(gk " + name + ") " + parts[1].substring(1));
				} else {
					targetOut.println("(De " + name + ") " + messagedes);
					// System.out.println("message de "+name+" à "+targetUser+" envoyé");
					out.println("(À " + targetUser + ") " + messageenv);
				}
			} else {
				out.println("User " + targetUser + " not found.");
			}
		}

		private void broadcastUserList() {
			StringBuilder userList = new StringBuilder("/userlist");
			for (String user : clients.keySet()) {
				userList.append(" ").append(user);
			}
			for (PrintWriter writer : clients.values()) {
				writer.println(userList.toString());
			}
		}
	}
}
