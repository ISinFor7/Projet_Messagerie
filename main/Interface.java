package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
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

    //Enregistrement des conversations avec les utilisateurs
    private Map<String, StringBuilder> conversationHistory;

    public Interface(String serverAddress) {
        // Connection client - serveur
        try {
            client = new Client(serverAddress, 4444);
        } catch (IOException e) {
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
            String message = textField.getText();
            if (!message.isEmpty()) {
                if (selectedUser != null) {
                    client.getOut().println(selectedUser + " " + message);
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
            	//fermeture du socket
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
        } catch (IOException e) {
            messageArea.append("Déconnecté du serveur.\n");
        }
    }

    private void handleIncomingMessage(String message) {
        // Réception des messages
        if (message.startsWith("(De ")) {
            int endIndex = message.indexOf(")");
            if (endIndex != -1) {
                String sender = message.substring(4, endIndex);
                appendToConversation(sender, message);
                if (selectedUser != null && selectedUser.equals(sender)) {
                    displayConversation(sender);
                }
            }
        } else {
            messageArea.append(message + "\n");
        }
    }

    private void appendToConversation(String user, String message) {
        conversationHistory.putIfAbsent(user, new StringBuilder());
        conversationHistory.get(user).append(message).append("\n");
    }

    private void displayConversation(String user) {
        messageArea.setText("");  // Vide les messages affichés
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
