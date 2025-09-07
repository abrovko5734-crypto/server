import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static final Map<String, ClientHandler> clients = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    String username = in.readLine();
                    synchronized (clients) {
                        clients.put(username, new ClientHandler(username, socket, in, out));
                        broadcastOnlineUsers();
                    }

                    String line;
                    while ((line = in.readLine()) != null) {
                        String[] parts = line.split(":", 2);
                        if (parts.length == 2) {
                            String recipient = parts[0];
                            String message = parts[1];
                            if (clients.containsKey(recipient)) {
                                clients.get(recipient).sendMessage(username + ": " + message);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private static void broadcastOnlineUsers() {
        StringBuilder list = new StringBuilder("ONLINE:");
        for (String user : clients.keySet()) {
            list.append(user).append(",");
        }
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(list.toString());
        }
    }

    static class ClientHandler {
        String username;
        Socket socket;
        BufferedReader in;
        PrintWriter out;

        ClientHandler(String username, Socket socket, BufferedReader in, PrintWriter out) {
            this.username = username;
            this.socket = socket;
            this.in = in;
            this.out = out;
        }

        void sendMessage(String msg) {
            out.println(msg);
        }
    }
}
