import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;
import java.util.TimerTask;

public class RMIServer extends UnicastRemoteObject implements RMIInterface {
    static final long serialVersionUID = 1L;
    int clientNo = 1; // Id for RMIServer to indentify RMIClients
    RMIInterface ci;
    boolean isBackup; // Is backup server?
    MulticastSocket socket = null;
    final String MULTICAST_ADDRESS = "224.0.224.0";
    final int PORT = 4369;

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        new RMIServer();
    }

    RMIServer() throws RemoteException, NotBoundException, MalformedURLException {
        super();
        connectToRMIServer();
    }

    public void connectToRMIServer() throws AccessException, RemoteException, MalformedURLException, NotBoundException {
        try {
            LocateRegistry.createRegistry(1099).rebind("RMIConnection", this);
            this.isBackup = false;
            System.out.println("Primary RMIServer ready...");
            System.out.println("Print model: \"[Message responsible] Message\"");
        } catch (Exception e) {
            if (e instanceof java.rmi.server.ExportException) {
                // Ja ha conexao. Quer dizer que temos de tomar posicao de backup server
                try {
                    ci = (RMIInterface) Naming.lookup("RMIConnection");
                    String msg = ci.sayHello("server");
                    System.out.println(msg);
                    this.isBackup = true;
                    checkPrimaryServerStatus();
                } catch (Exception er) {
                    System.out.println(
                            "\nERROR: Something went wrong. Couldn't be either primary or secondary server. Aborting program...");
                    System.out.println("Exception: " + er);
                    System.exit(-1);
                }
            } else {
                System.out.println("\nERROR: Something went wrong. Aborting program...");
                System.out.println("Exception: " + e);
                System.exit(-1);
            }
        }
    }

    // OK! Dá
    // For the secondary server to check if primary failed
    public void checkPrimaryServerStatus() {

        Timer timer = new Timer();
        TimerTask myTask = new TimerTask() {
            @Override
            public void run() {
                // whatever you need to do every 2 seconds
                try {
                    String res = ci.testPrimary();
                    System.out.println("[Primary server] " + res);
                } catch (RemoteException e) {
                    System.out.println("Primary server not responding. Assuming primary functions...");
                    timer.cancel();
                }
            }
        };

        timer.schedule(myTask, 2000, 2000);
    }

    public String connectToMulticast(int clientNo, String msg) {
        try {
            // Send
            socket = new MulticastSocket(PORT); // create socket and bind it
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);

            byte[] bufferSend = msg.getBytes();
            DatagramPacket packetSend = new DatagramPacket(bufferSend, bufferSend.length, group, PORT);
            socket.send(packetSend);

            // Receive
            String type;
            int receivedClientNo;
            String msgReceive;
            do {
                byte[] bufferReceive = new byte[64 * 1024];
                DatagramPacket packetReceive = new DatagramPacket(bufferReceive, bufferReceive.length);
                socket.receive(packetReceive);
                msgReceive = new String(packetReceive.getData(), 0, packetReceive.getLength());

                // System.out.println("Mensagem recebida: " + msgReceive);

                String[] parameters = msgReceive.split(";");
                type = parameters[0].split("\\|")[1];
                receivedClientNo = Integer.parseInt(parameters[1].split("\\|")[1]);

                System.out.println("Type = " + type);

            } while ((!type.contains("Result")) || (receivedClientNo != clientNo));

            // System.out.println("Mensagem final: " + msgReceive);
            socket.close();
            return msgReceive;
            // dar return de msgReceive

        } catch (Exception e) {
            socket.close();
            System.out.println("ERROR: Something went from. Did you forget the flag? Aborting program...");
            System.exit(-1);
        }
        // Necessary but unimportant
        return null;
    }

    public String sayHello(String type) throws RemoteException {
        if (type.compareTo("client") == 0) {
            System.out.println("[Client no " + clientNo + "] " + "Has just connected.");
            clientNo++;
            return "Connected to RMI Primary Server successfully!\nServer gave me the id no " + (clientNo - 1);
        } else {
            System.out.println("[Backup server] Has just connected.");
            return "Connected to RMI Primary Server successfully!";
        }
    }

    public String testPrimary() throws RemoteException {
        // Called by backup to test
        // Message from primary to backup to confirm that all is ok
        return "All good";
    }

    public String register(int clientNo, String username, String password) throws RemoteException {
        String msg = "type|register;clientNo|" + clientNo + ";username|" + username + ";password|" + password;
        System.out.println("Mensgem a ser enviada: " + msg);
        String msgReceive = connectToMulticast(clientNo, msg);
        System.out.println("Mensagem recebida: " + msgReceive);
        return msgReceive;
    }

    public String login(int clientNo, String username, String password) throws RemoteException {
        String msg = "type|login;clientNo|" + clientNo + ";username|" + username + ";password|" + password;
        System.out.println("Mensgem a ser enviada: " + msg);
        String msgReceive = connectToMulticast(clientNo, msg);
        System.out.println("Mensagem recebida: " + msgReceive);
        return msgReceive;
    }

}