import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Hangar extends Thread {
    private int numAirplanes;
    private ServerSocket serverSocket;
    private Socket socket1;
    private Socket socket2;
    private Queue<Message> receivedMessageQueue;
    private Random randomiser;
    private ObjectOutputStream objectOutputStream1;
    private ObjectOutputStream objectOutputStream2;
    private ExecutorService pool;
    private int listeningPortNumber;
    private String sendingHangarName;

    public Hangar(ServerSocket serverSocket, int listeningPortNumber, int hangarPort1, int hangarPort2)
            throws IOException {
        // initial number of airplanes in each Hangar
        this.numAirplanes = 10;
        this.serverSocket = serverSocket;
        this.listeningPortNumber = listeningPortNumber;
        this.socket1 = new Socket("localhost", hangarPort1);
        this.socket2 = new Socket("localhost", hangarPort2);
        this.objectOutputStream1 = new ObjectOutputStream(this.socket1.getOutputStream());
        this.objectOutputStream2 = new ObjectOutputStream(this.socket2.getOutputStream());
        this.receivedMessageQueue = new ConcurrentLinkedQueue<Message>();
        pool = Executors.newCachedThreadPool();
        if (this.listeningPortNumber == 37961)
            this.sendingHangarName = "Hangar 1";
        else if (this.listeningPortNumber == 37962)
            this.sendingHangarName = "Hangar 2";
        else
            this.sendingHangarName = "Hangar 3";
        this.randomiser = new Random(this.listeningPortNumber);
    }

    public int getNumAirPlanes() {
        return this.numAirplanes;
    }

    private void sendAirPlanes() {
        int numSentAirplanes = this.randomiser.nextInt(5) + 1;
        if (numSentAirplanes > this.numAirplanes)
            numSentAirplanes = 0;
        this.numAirplanes -= numSentAirplanes;
        Message applicationMessage = new Message();
        applicationMessage.setNumSetAirplanes(numSentAirplanes);
        int sentToHangar = this.randomiser.nextInt(2);
        String receivingHangarName = "";
        try {
            if (sentToHangar == 0) {
                if (this.listeningPortNumber == 37961)
                    receivingHangarName = "Hangar 2";
                else
                    receivingHangarName = "Hangar 1";
                System.out.println(
                        this.sendingHangarName + " is sending " + numSentAirplanes + " airplanes to " + receivingHangarName);
                this.objectOutputStream1.writeObject(applicationMessage);
            } else {
                if (this.listeningPortNumber == 37963)
                    receivingHangarName = "Hangar 2";
                else
                    receivingHangarName = "Hangar 3";
                System.out.println(
                        this.sendingHangarName + " is sending " + numSentAirplanes + " airplanes to " + receivingHangarName);
                this.objectOutputStream2.writeObject(applicationMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveAirPlanes() {
        Message receivedAppMessage = this.receivedMessageQueue.poll();
        int numReceivedAirplanes = receivedAppMessage.getNumSentAirplaines();
        this.numAirplanes += numReceivedAirplanes;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep((this.randomiser.nextInt(4) + 1) * 1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            sendAirPlanes();
            System.out.println(this.sendingHangarName + " has " + this.numAirplanes + " remaining!");
        }
    }
}