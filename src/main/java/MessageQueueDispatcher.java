public class MessageQueueDispatcher extends Thread {
    private Hangar hangar;

    public MessageQueueDispatcher(Hangar hangar) {
        this.hangar = hangar;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep((this.hangar.getRandomiser().nextInt(3) + 1) * 1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (!this.hangar.getReceivedMessageQueue().isEmpty()) {
                this.hangar.receiveAirplanes();
            }
        }
        
    }
}