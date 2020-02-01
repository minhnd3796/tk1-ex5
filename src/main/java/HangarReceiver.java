import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HangarReceiver extends Thread {
    private ServerSocket serverSocket;
    private Queue<Message> messageQueue;
    private ExecutorService pool;

    public HangarReceiver(ServerSocket serverSocket, Queue<Message> messageQueue) {
        this.serverSocket = serverSocket;
        this.messageQueue = messageQueue;
        this.pool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = this.serverSocket.accept();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        runTask(socket, this.messageQueue);
    }

    private void runTask(final Socket socket, Queue<Message> messageQueue) {
        // Here this thread constantly get the Message object out of the stream
        // and put it into the incoming message queue
        // to be retrieved later
        pool.execute(new Runnable() {
            @Override
			public void run() {
                SocketAddress localSocketAddress = socket.getLocalSocketAddress();
                
                // get the Message object out of the stream HERE
                try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());) {
                    Message input;
                    // check if the message queue implementation used is thread-safe
                    boolean lock = !(messageQueue.getClass().getPackage().getName().equals("java.util.concurrent"));
                    boolean result = false;
                    while (true) {
                        while ((input = (Message) in.readObject()) != null) {
                            // enqueuing Messages object into the queue HERE
                            if (lock) {
                                // message queue not thread-safe, lock it
                                synchronized (messageQueue) {
                                    result = messageQueue.offer(input);
                                }
                            } else {
                                result = messageQueue.offer(input);
                            }
                            if (!result) {
                                System.err.println("Receiver " + localSocketAddress + ": Could not add message to message queue!");
                            } else {
                                // System.out.println(input.getReceiver() + " got " + input.getNumSentAirplanes() + " airplanes from " + input.getSender() + " into its queue");
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
        });
    }
}