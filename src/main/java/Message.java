import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean markerMessage;
    private int numSentAirplanes;
    private String sender;
    private String receiver;

    public Message() {
        this.markerMessage = false;
        this.numSentAirplanes = 0;
        this.sender = "";
        this.receiver = "";
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return this.sender;
    }

    public String getReceiver() {
        return this.receiver;
    }

    public void setAsMarkerMessage() {
        this.markerMessage = true;
    }

    public void setNumSetAirplanes(int numSentAirplanes) {
        this.numSentAirplanes = numSentAirplanes;
    }

    public int getNumSentAirplanes() {
        return this.numSentAirplanes;
    }

    public boolean isMarkerMessage() {
        return this.markerMessage;
    }
}