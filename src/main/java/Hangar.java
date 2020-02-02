import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Hangar extends Thread {
    // local state for each hangar
    private int currentNumAirplanesLocalState;

    // used to determine whether this hangar
    // is the snapshot initiator or not
    private boolean isSnapshotInitiator;

    // recorded incoming message channels
    private Queue<Message> recordedMessagesFirstIncomingChannel;
    private Queue<Message> recordedMessagesSecondIncomingChannel;

    // used to determine whether this hangar has received
    // the duplicate marker messages or not
    private boolean receivedMarkerFromFirstProcess;
    private boolean receivedMarkerFromSecondProcess;

    // used to determine whether this hangar
    // is recording its incoming message channels or not
    private boolean isRecordingFirstIncommingChannel;
    private boolean isRecordingSecondIncommingChannel;

    // name of the Hangar ("H1", "H2" or "H3")
    private String hangarName;

    // current number of planes in each hangar
    // should be atomic int, to avoid this var being edited by multiple
    // threads at the same time => planes are not preserved
    private AtomicInteger numAirplanes;

    // get random seed for each hangar
    private Random randomiser;

    // incoming message queue (FIFO structure)
    private Queue<Message> receivedMessageQueue;

    // the first and second destinations of this hangar
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

    public void setSnapshotInitiator() {
        this.isSnapshotInitiator = true;
    }

    public boolean getSnapshotInitiator() {
        return this.isSnapshotInitiator;
    }

    public boolean getReceivedMarkerFromFirstProcess() {
        return this.receivedMarkerFromFirstProcess;
    }

    public boolean getReceivedMarkerFromSecondProcess() {
        return this.receivedMarkerFromSecondProcess;
    }

    public void setReceivedMarkerFromFirstProcess() {
        this.receivedMarkerFromFirstProcess = true;
    }

    public void setReceivedMarkerFromSecondProcess() {
        this.receivedMarkerFromSecondProcess = true;
    }

    public void completeSnapshotSession() {
        this.isSnapshotInitiator = false;
        this.recordedMessagesFirstIncomingChannel = new LinkedList<Message>();
        this.recordedMessagesSecondIncomingChannel = new LinkedList<Message>();
        this.receivedMarkerFromFirstProcess = false;
        this.receivedMarkerFromSecondProcess = false;
        this.isRecordingFirstIncommingChannel = false;
        this.isRecordingSecondIncommingChannel = false;
    }

    public void recordLocalState() {
        this.currentNumAirplanesLocalState = this.getNumAirplanes();
    }

    public int getLocalState() {
        return this.currentNumAirplanesLocalState;
    }

    public Queue<Message> getFirstRecordedIncommingChannel() {
        return this.recordedMessagesFirstIncomingChannel;
    }

    public Queue<Message> getSecondRecordedIncommingChannel() {
        return this.recordedMessagesSecondIncomingChannel;
    }

    public Hangar(String hangarName, int incomingChannel1, int incomingChannel2) throws IOException {
        this.currentNumAirplanesLocalState = 0;
        this.isSnapshotInitiator = false;
        this.recordedMessagesFirstIncomingChannel = new LinkedList<Message>();
        this.recordedMessagesSecondIncomingChannel = new LinkedList<Message>();
        this.receivedMarkerFromFirstProcess = false;
        this.receivedMarkerFromSecondProcess = false;
        this.isRecordingFirstIncommingChannel = false;
        this.isRecordingSecondIncommingChannel = false;

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

    public void recordFirstIncomingChannel() {
        this.isRecordingFirstIncommingChannel = true;
    }

    public void recordSecondIncomingChannel() {
        this.isRecordingSecondIncommingChannel = true;
    }

    public void sendMarkerMessageToSecond() {
        Message markerMessage = new Message();
        markerMessage.setAsMarkerMessage();
        markerMessage.setSender(this.hangarName);
        String receivingHangarName = "";
        if (this.hangarName == "H1")
            receivingHangarName = "H2";
        else
            receivingHangarName = "H1";
        markerMessage.setReceiver(receivingHangarName);
        try {
            this.objectOutputStream1.writeObject(markerMessage);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void sendMarkerMessageToThird() {
        Message markerMessage = new Message();
        markerMessage.setAsMarkerMessage();
        markerMessage.setSender(this.hangarName);
        String receivingHangarName = "";
        if (this.hangarName == "H3")
            receivingHangarName = "H2";
        else
            receivingHangarName = "H3";
        markerMessage.setReceiver(receivingHangarName);
        try {
            this.objectOutputStream2.writeObject(markerMessage);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void sendAirplanes() {
        // number of transferred planes is randomly generated between 1 and 5
        // if the number of remaining planes in this hangar are less than the generated number
        // then the number of transferred planes equals to the remaining number
        int numSentAirplanes = 0;
        while (numSentAirplanes == 0) {
            numSentAirplanes = this.randomiser.nextInt(5) + 1;
            // the CURRENT number of remaing planes HAS TO BE SAVED into another var
            // because it can be modified concurrently by other threads
            // when it comes to the next this.numAirplanes.get() statement
            // so try to get that number right before the assignment statement
            int currentNumAirplanes = this.numAirplanes.get();
            if (numSentAirplanes > currentNumAirplanes)
                numSentAirplanes = currentNumAirplanes;
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
        Message receivedMessage = this.receivedMessageQueue.poll();
        String senderHangarName = receivedMessage.getSender();
        if (!receivedMessage.isMarkerMessage()) {
            // processing application messages (number of airplanes)
            if (((this.hangarName == "H1" && senderHangarName.equals("H2"))
                || ((this.hangarName == "H2" || this.hangarName == "H3") && senderHangarName.equals("H1")))
                && this.isRecordingFirstIncommingChannel) {
                    this.recordedMessagesFirstIncomingChannel.offer(receivedMessage);
                }
            if (((this.hangarName == "H3" && senderHangarName.equals("H2"))
                || ((this.hangarName == "H1" || this.hangarName == "H2") && senderHangarName.equals("H3")))
                && this.isRecordingSecondIncommingChannel) {
                    this.recordedMessagesSecondIncomingChannel.offer(receivedMessage);
                }
            int numReceivedAirplanes = receivedMessage.getNumSentAirplanes();
            this.numAirplanes.addAndGet(numReceivedAirplanes);
        } else {
            // Processing the marker message
            System.out.println(this.hangarName + " caught a marker message from " + senderHangarName);
            // if this hangar receive from the "first" other hangar
            if (((this.hangarName == "H1" && senderHangarName.equals("H2"))
                || ((this.hangarName == "H2" || this.hangarName == "H3") && senderHangarName.equals("H1")))) {
                    this.receivedMarkerFromFirstProcess = true;
                    if (this.isSnapshotInitiator) {
                        // if this hangar is the initiator
                        // simple stops recording on this incomming channel
                        this.isRecordingFirstIncommingChannel = false;
                    } else {
                        if (!this.receivedMarkerFromSecondProcess) {
                            // if this hangar first sees the marker message
                            // then record its local state
                            this.recordLocalState();
                            
                            // starts listening on the other incoming channel
                            this.recordSecondIncomingChannel();

                            // send marker messages to other hangar (processes)
                            this.sendMarkerMessageToSecond();
                            this.sendMarkerMessageToThird();
                        } else {
                            // if this hangar sees marker message twice (n - 1)
                            // then just terminate snapshot on this very hangar
                            this.isRecordingFirstIncommingChannel = false;
                        }
                    }
                }
            // if this hangar receive from the "second" other hangar
            if (((this.hangarName == "H3" && senderHangarName.equals("H2"))
                || ((this.hangarName == "H1" || this.hangarName == "H2") && senderHangarName.equals("H3")))) {
                    this.receivedMarkerFromSecondProcess = true;
                    if (this.isSnapshotInitiator) {
                        // if this hangar is the initiator
                        // simple stops recording on this incomming channel
                        this.isRecordingSecondIncommingChannel = false;
                    } else {
                        if (!this.receivedMarkerFromFirstProcess) {
                            // if this hangar is the initiator
                            // simple stops recording on this incomming channel
                            this.recordLocalState();

                            // starts listening on the other incoming channel
                            this.recordFirstIncomingChannel();
                            
                            // send marker messages to other hangar (processes)
                            this.sendMarkerMessageToSecond();
                            this.sendMarkerMessageToThird();
                        } else {
                            // if this hangar sees marker message twice (n - 1)
                            // then just terminate snapshot on this very hangar
                            this.isRecordingSecondIncommingChannel = false;
                        }
                    }
                }
        }
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
            sendAirplanes();
        }
    }
}