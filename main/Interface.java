package main;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Interface {
	private JFrame frame;
	private JTextField textField;
	private JTextArea messageArea;
	private JList<String> userList;
	private DefaultListModel<String> userListModel;
	private Client client;
	private String selectedUser = null;
	private String userName;
	private Key pricle;
	private Key pubcle;
	private Key cle=null;
	private Cipher chiffrem;

	// Enregistrement des conversations avec les utilisateurs
	private Map<String, StringBuilder> conversationHistory;

	public Interface(String serverAddress) {
		// Connection client - serveur
		try {
			client = new Client(serverAddress, 4442);
			// creation des clés
			KeyPairGenerator kp = KeyPairGenerator.getInstance("RSA");
			KeyPair cp = kp.genKeyPair();
			pricle = cp.getPrivate();
			pubcle = cp.getPublic();
			chiffrem = Cipher.getInstance("RSA");
		} catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			JOptionPane.showMessageDialog(null, "Impossible de se connecter au serveur");
			return;
		}

		conversationHistory = new HashMap<>();

		// Montre le menu de bienvenue et demande le nom
		showWelcomeMenu();

		// Mise en place de l'UI du chat
		frame = new JFrame("Chat App - " + userName);
		textField = new JTextField(40);
		messageArea = new JTextArea(20, 40);
		messageArea.setEditable(false);
		userListModel = new DefaultListModel<>();
		userList = new JList<>(userListModel);
		userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Configuration du layout
		JPanel userPanel = new JPanel(new BorderLayout());
		userPanel.add(new JLabel("Utilisateurs en ligne"), BorderLayout.NORTH);
		userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);

		frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
		frame.getContentPane().add(textField, BorderLayout.SOUTH);
		frame.getContentPane().add(userPanel, BorderLayout.EAST);

		// Envoi des messages
		textField.addActionListener(e -> {
			client.getOut().println(selectedUser + " needKey");
			while(cle==null) {
				System.out.println("tc");
			}
			String message = textField.getText();
			//System.out.println(cle.getAlgorithm());
			//System.out.println(cle.getEncoded());
			if (!message.isEmpty()) {
				if (selectedUser != null) {
					try {
						chiffrem.init(Cipher.ENCRYPT_MODE, cle);
					} catch (InvalidKeyException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					byte[] data = message.getBytes();
					byte[] result = null;
					try {
						result = chiffrem.doFinal(data);
					} catch (IllegalBlockSizeException | BadPaddingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					// conversion en String pour envoi
					String crypt = "";
					for (int i = 0; i < result.length; i++) {
						if (i == result.length - 1) {
							crypt = crypt + result[i];
						} else {
							crypt = crypt + result[i] + ",";
						}
					}
					try {
						chiffrem.init(Cipher.ENCRYPT_MODE, pubcle);
					} catch (InvalidKeyException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						result = chiffrem.doFinal(data);
					} catch (IllegalBlockSizeException | BadPaddingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					// conversion en String pour envoi
					System.out.println(crypt);
					crypt =crypt+"//";
					for (int i = 0; i < result.length; i++) {
						if (i == result.length - 1) {
							crypt = crypt + result[i];
						} else {
							crypt = crypt + result[i] + ",";
						}
					}
					//System.out.println(selectedUser + " " + crypt);
					client.getOut().println(selectedUser + " " + crypt);
					appendToConversation(selectedUser, "(À " + selectedUser + ") " + message);
					textField.setText("");
				} else {
					JOptionPane.showMessageDialog(frame, "Sélectionnez un utilisateur à qui envoyer le message.");
				}
			}
		});

		// Sélection de la cible du message
		userList.addListSelectionListener(e -> {
			selectedUser = userList.getSelectedValue();
			if (selectedUser != null) {
				displayConversation(selectedUser);
			}
		});

		// Réception du serveur
		new Thread(this::listenForMessages).start();

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent x) {
				// fermeture du socket
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("Fenêtre fermée");
				frame.dispose();
				System.exit(0);
			}
		});
		frame.pack();
		frame.setVisible(true);

		// Envoi du nom d'utilisateur au serveur
		client.getOut().println(userName);
	}

	private void showWelcomeMenu() {
		// Dialog box pour le nom de l'utilisateur
		userName = JOptionPane.showInputDialog(null, "Entrer votre nom:", "Bienvenue", JOptionPane.PLAIN_MESSAGE);
		if (userName == null || userName.trim().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Le nom ne peut pas être vide...");
			System.exit(0);
		}
	}

	private void listenForMessages() {
		try {
			String line;
			while ((line = client.getIn().readLine()) != null) {
				if (line.startsWith("/userlist")) {
					updateUserList(line.substring(9));
				} else {
					handleIncomingMessage(line);
				}
			}
		} catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			messageArea.append("Déconnecté du serveur.\n");
		}
	}

	private void handleIncomingMessage(String message)
			throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		// Réception des messages
		if (message.startsWith("(De ")) {
			int endIndex = message.indexOf(")");
			if (endIndex != -1) {
				String sender = message.substring(4, endIndex);
				chiffrem.init(Cipher.DECRYPT_MODE, pricle);
				byte[] code = new byte[256];
				String[] m=message.split(",");
				code[0]=Byte.decode(m[0].substring(endIndex+2));
				int j=1;
				while (j<256){
					code[j]=Byte.decode(m[j]);
					j++;
				}
				byte[] messa=chiffrem.doFinal(code);
				appendToConversation(sender,message.substring(0, endIndex+1)+new String(messa));
				if (selectedUser != null && selectedUser.equals(sender)) {
					displayConversation(sender);
				}
			}
		} else if (message.startsWith("(nk")) {
			int endIndex = message.indexOf(")");
			if (endIndex != -1) {
				String sender = message.substring(4, endIndex);
				// envoi de la clé au client
				byte[] ck = pubcle.getEncoded();
				//System.out.println(new String(ck));
				client.getOut().println(sender + " g" + Base64.getEncoder().encodeToString(ck));
			}
		}else if(message.startsWith("(gk")) {
			int endIndex = message.indexOf(")");
			if (endIndex != -1) {
				String testk = message.substring(endIndex+2);
				//System.out.println(testk);
				//System.out.println("test");
				byte[] kk = Base64.getDecoder().decode(testk);
				EncodedKeySpec pls = new X509EncodedKeySpec(kk);
				cle = KeyFactory.getInstance("RSA").generatePublic(pls);
				//System.out.println(new String(cle.getEncoded()));
			}
		} else if(message.startsWith("Bon")){
			messageArea.append(message + "\n");
		}else{
			byte[] messa=null;
			int endIndex = message.indexOf(")");
			if (endIndex != -1) {
				String sender = message.substring(4, endIndex);
				chiffrem.init(Cipher.DECRYPT_MODE, pricle);
				byte[] code = new byte[256];
				String[] m=message.split(",");
				code[0]=Byte.decode(m[0].substring(endIndex+2));
				int j=1;
				while (j<256){
					code[j]=Byte.decode(m[j]);
					j++;
				}
				messa=chiffrem.doFinal(code);
				appendToConversation(sender, new String(messa));
				if (selectedUser != null && selectedUser.equals(sender)) {
					displayConversation(sender);
				}
			}
			messageArea.append(new String(messa) + "\n");
		}
	}

	private void appendToConversation(String user, String message) {
		conversationHistory.putIfAbsent(user, new StringBuilder());
		conversationHistory.get(user).append(message).append("\n");
	}

	private void displayConversation(String user) {
		messageArea.setText(""); // Vide les messages affichés
		StringBuilder conversation = conversationHistory.getOrDefault(user, new StringBuilder());
		messageArea.append(conversation.toString());
	}

	private void updateUserList(String users) {
		SwingUtilities.invokeLater(() -> {
			userListModel.clear();
			String[] userArray = users.split(" ");
			for (String user : userArray) {
				if (!user.isEmpty() && !user.equals(userName)) {
					userListModel.addElement(user);
				}
			}
		});
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new Interface("localhost"));
	}
}
