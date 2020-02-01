import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Hangar extends Thread {
    private String hangarName;
    private AtomicInteger numAirplanes;
    private Random randomiser;
    private Queue<Message> receivedMessageQueue;
    private Socket destSocket1;
    private Socket destSocket2;
    private ObjectOutputStream objectOutputStream1;
    private ObjectOutputStream objectOutputStream2;

    public String getHangarName() {
        return this.hangarName;
    }

    public Random getRandomiser() {
        return this.randomiser;
    }

    public Queue<Message> getReceivedMessageQueue() {
        return this.receivedMessageQueue;
    }

    public Hangar(String hangarName, int incomingChannel1, int incomingChannel2) throws IOException {
        // initial number of airplanes in each Hangar
        this.hangarName = hangarName;
        this.numAirplanes = new AtomicInteger(10);
        this.destSocket1 = new Socket("localhost", incomingChannel1);
        this.destSocket2 = new Socket("localhost", incomingChannel2);
        this.objectOutputStream1 = new ObjectOutputStream(this.destSocket1.getOutputStream());
        this.objectOutputStream2 = new ObjectOutputStream(this.destSocket2.getOutputStream());
        this.receivedMessageQueue = new ConcurrentLinkedQueue<Message>();
        this.randomiser = new Random(Integer.parseInt(this.hangarName.substring(this.hangarName.length() - 1)));
        
    }

    public int getNumAirplanes() {
        return this.numAirplanes.get();
    }

    private void sendAirplanes() {
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
                if (this.hangarName == "H1")
                    receivingHangarName = "H2";
                else
                    receivingHangarName = "H1";
                applicationMessage.setReceiver(receivingHangarName);
                System.out.println("Transfer: " + this.hangarName + " -> " + receivingHangarName + " " + "(" + numSentAirplanes + ")");
                this.objectOutputStream1.writeObject(applicationMessage);
            } else {
                if (this.hangarName == "H3")
                    receivingHangarName = "H2";
                else
                    receivingHangarName = "H3";
                applicationMessage.setReceiver(receivingHangarName);
                System.out.println("Transfer: " + this.hangarName + " -> " + receivingHangarName + " " + "(" + numSentAirplanes + ")");
                this.objectOutputStream2.writeObject(applicationMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveAirplanes() {
        Message receivedAppMessage = this.receivedMessageQueue.poll();
        int numReceivedAirplanes = receivedAppMessage.getNumSentAirplanes();
        this.numAirplanes.addAndGet(numReceivedAirplanes);
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
            sendAirplanes();
            // System.out.println(this.hangarName + " has " + this.numAirplanes + " remaining!");
        }
    }
}