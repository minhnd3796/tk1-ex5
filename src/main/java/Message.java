import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean markerMessage;
    private int numSentAirplanes;

    public Message() {
        this.markerMessage = false;
        this.numSentAirplanes = 0;
    }

    public void setAsMarkerMessage() {
        this.markerMessage = true;
    }

    public void setNumSetAirplanes(int numSentAirplanes) {
        this.numSentAirplanes = numSentAirplanes;
    }

    public int getNumSentAirplaines() {
        return this.numSentAirplanes;
    }

    public boolean isMarkerMessage() {
        return this.markerMessage;
    }
}