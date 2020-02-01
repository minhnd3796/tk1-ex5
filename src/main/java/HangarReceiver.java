import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketAddress;

public class HangarReceiver extends Thread {
    private Hangar hangar;

    public HangarReceiver(Hangar hangar) {
        this.hangar = hangar;
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = this.hangar.getServerSocket().accept();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        SocketAddress localSocketAddress = socket.getLocalSocketAddress();
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());) {
            Message input;
            // check if the message queue implementation used is thread-safe
            boolean lock = !(this.hangar.getReceivedMessageQueue().getClass().getPackage().getName().equals("java.util.concurrent"));
            boolean result = false;
            while (true) {
                while ((input = (Message) in.readObject()) != null) {
                    if (lock) {
                        // message queue not thread-safe, lock it
                        synchronized (this.hangar.getReceivedMessageQueue()) {
                            result = this.hangar.getReceivedMessageQueue().offer(input);
                        }
                    } else {
                        result = this.hangar.getReceivedMessageQueue().offer(input);
                    }
                    if (!result) {
                        System.err.println("Receiver " + localSocketAddress + ": Could not add message to message queue!");
                    } else {
                        System.out.println(input.getReceiver() + " got " + input.getNumSentAirplanes() + " airplanes from " + input.getSender() + " into its queue");
                    }
                }
            }
        } catch (EOFException e) {
            System.out.println("Receiver " + localSocketAddress + ": Got a EOFException. This indicates that a client closed the connection");
        } catch (IOException e) {
            System.err.println("Receiver " + localSocketAddress + ": Got the following error:");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Receiver " + localSocketAddress + ": Got the following error:");
            e.printStackTrace();
        }
    }
}