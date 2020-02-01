import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Hangar extends Thread {
    private AtomicInteger numAirplanes;
    private ServerSocket serverSocket;
    private Socket socket1;
    private Socket socket2;
    private Queue<Message> receivedMessageQueue;
    private Random randomiser;
    private ObjectOutputStream objectOutputStream1;
    private ObjectOutputStream objectOutputStream2;
    private int listeningPortNumber;
    private String hangarName;

    public String getHangarName() {
        return this.hangarName;
    }

    public Random getRandomiser() {
        return this.randomiser;
    }

    public Queue<Message> getReceivedMessageQueue() {
        return this.receivedMessageQueue;
    }

    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }

    public Hangar(ServerSocket serverSocket, int listeningPortNumber, int hangarPort1, int hangarPort2)
            throws IOException {
        // initial number of airplanes in each Hangar
        this.numAirplanes = new AtomicInteger(10);
        this.serverSocket = serverSocket;
        this.listeningPortNumber = listeningPortNumber;
        this.socket1 = new Socket("localhost", hangarPort1);
        this.socket2 = new Socket("localhost", hangarPort2);
        this.objectOutputStream1 = new ObjectOutputStream(this.socket1.getOutputStream());
        this.objectOutputStream2 = new ObjectOutputStream(this.socket2.getOutputStream());
        this.receivedMessageQueue = new ConcurrentLinkedQueue<Message>();
        if (this.listeningPortNumber == 37961)
            this.hangarName = "Hangar 1";
        else if (this.listeningPortNumber == 37962)
            this.hangarName = "Hangar 2";
        else
            this.hangarName = "Hangar 3";
        this.randomiser = new Random(this.listeningPortNumber);
        
    }

    public int getNumAirPlanes() {
        return this.numAirplanes.get();
    }

    private void sendAirPlanes() {
        int numSentAirplanes = 0;
        while (numSentAirplanes == 0) {
            numSentAirplanes = this.randomiser.nextInt(5) + 1;
            if (numSentAirplanes > this.numAirplanes.get())
                numSentAirplanes = this.numAirplanes.get();
        }
        this.numAirplanes.addAndGet(-numSentAirplanes);
        Message applicationMessage = new Message();
        applicationMessage.setNumSetAirplanes(numSentAirplanes);
        applicationMessage.setSender(this.hangarName);
        int sentToHangar = this.randomiser.nextInt(2);
        try {
            String receivingHangarName = "";
            if (sentToHangar == 0) {
                if (this.listeningPortNumber == 37961)
                    receivingHangarName = "Hangar 2";
                else
                    receivingHangarName = "Hangar 1";
                applicationMessage.setReceiver(receivingHangarName);
                System.out.println(this.hangarName + " is sending " + numSentAirplanes + " airplanes to " + receivingHangarName);
                this.objectOutputStream1.writeObject(applicationMessage);
            } else {
                if (this.listeningPortNumber == 37963)
                    receivingHangarName = "Hangar 2";
                else
                    receivingHangarName = "Hangar 3";
                applicationMessage.setReceiver(receivingHangarName);
                System.out.println(this.hangarName + " is sending " + numSentAirplanes + " airplanes to " + receivingHangarName);
                this.objectOutputStream2.writeObject(applicationMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveAirPlanes() {
        Message receivedAppMessage = this.receivedMessageQueue.poll();
        int numReceivedAirplanes = receivedAppMessage.getNumSentAirplanes();
        this.numAirplanes.addAndGet(numReceivedAirplanes);
        System.out.println(this.hangarName + " successfully dispatched " + numReceivedAirplanes + " from " + receivedAppMessage.getSender() + ". Now it has " + this.getNumAirPlanes());
    }

    @Override
    public void run() {
        
        while (true) {
            // try {
            //     Thread.sleep((this.randomiser.nextInt(4) + 1) * 1000);
            // } catch (InterruptedException e) {
            //     // TODO Auto-generated catch block
            //     e.printStackTrace();
            // }
            sendAirPlanes();
            System.out.println(this.hangarName + " has " + this.numAirplanes + " remaining!");
        }
    }
}