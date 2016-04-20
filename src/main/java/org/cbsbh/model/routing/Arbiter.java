package org.cbsbh.model.routing;

import java.util.ArrayList;

/**
 * Description goes here
 * Date: 3/16/15 10:32 AM
 *
 * @author Mihail Chilyashev
 */
public class Arbiter {

    int id;

    int channelId;
    /*
    *
    * Всички изходни канали, достъпни от арбитъра
    */
    ArrayList<OutputChannel> allChannels;

    /**
     * Канали, които са отговорили с Grant
     */
    ArrayList<Integer> grantOutputChannelIds;


    private int nodeId;
    private ArrayList<Integer> requestsSent = new ArrayList<>();


    public Arbiter(int nodeId, ArrayList<OutputChannel> allChannels, int id, int channelId) {
        this.id = id;
        this.nodeId = nodeId;
        this.allChannels = allChannels;
        this.channelId = channelId;
        grantOutputChannelIds = new ArrayList<>();
    }

    /**
     * Праща заявка до изходен канал
     */
    public boolean sendRequest(int id) {
        boolean found = false;
        // Обикалят се всички канали и се търси канал с точното id.
        // Като се намери, му се взема RRA-то и на него се праща заявка за пращане
        for (OutputChannel channel : allChannels) {
            if (channel.getNextNodeId() == id) {
                found = true;
                channel.getRra().requestToSend(this);
                break;
            }
        }
        assert found : "Input channel with id " + id + " not found. A bitch, ain't it?";
        return found;
    }

    /**
     * Вижда кой е върнал Grant.
     * Избира първия output канал, който е върнал Grant.
     * Изчиства списъка
     * Изпраща ACK на избрания output канал
     * Връща id-то на избрания канал.
     *
     * @return -1, ако никой не е върнал Grant
     */
    public int getNextNodeId(FIFOQueue fifoQueue) {
        if (grantOutputChannelIds.isEmpty()) {
            return -1;
        }

        assert grantOutputChannelIds.size() > 0 : "Никой не е върнал Grant";
        int id = grantOutputChannelIds.remove(0);

        long oldTR = fifoQueue.getFifo().peek().getTR();
        long dna = fifoQueue.getFifo().peek().getDNA();
        while(Long.bitCount(oldTR) <= Long.bitCount(dna ^ id)) {
            if (grantOutputChannelIds.isEmpty()) {
                return -1;
            }
            id = grantOutputChannelIds.remove(0);
            //assert false : "GO GUCK YOURSELF";
        }

        grantOutputChannelIds.clear();
        requestsSent.clear();
        sendGrantAck(fifoQueue, id);
        return id;
    }

    /**
     * Праща заявки до списък от id-та на канали
     *
     * @param ids списък от id-та на канали
     */
    public void sendRequest(int ids[]) {
        for (int id : ids) {
            sendRequest(id);
        }
    }

    /**
     * Връща Grant Acknowledge на първия канал, върнал Grant (първия канал в grantOutputChannelIds)
     * След като се върне ACK, grantOutputChannelIds се изпразва. Yeah.
     */
    private void sendGrantAck(FIFOQueue fifoQueue, int outputChannelId) {
        MPPNetwork.get(nodeId).getOutputChannel(outputChannelId).setAccepted(fifoQueue);
    }

    /**
     * Праща заявка за маршрутизиране
     *
     * @param tr Transport Mask.
     */
    public boolean sendRequestByTR(long tr) {
        //We first remove any residual shite:
        //grantOutputChannelIds.clear();

        boolean requestSent = false;
        for (int i = 0; i < 12; i++) { // 12. Like the 12 bits in the TR. Duh...
            if ((tr & (1 << i)) == 1 << i) {
                int outputChannelId = nodeId ^ (1 << i);
                boolean newRequestSent = sendRequest(outputChannelId);
                requestSent = requestSent || newRequestSent;
                requestsSent.add(outputChannelId);
            }
        }
        return requestSent;
    }

    public int getNodeId() {
        return nodeId;
    }


    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public String grantOutputChannelIdsToString() {
        StringBuffer ret = new StringBuffer();
        for (Integer grantOutputChannelId : grantOutputChannelIds) {
            ret.append(grantOutputChannelId);
        }

        return String.join(", ", ret);
    }

    public void takeGrant(Integer outputChannelId) {
        //При получаване на грант трябва да се обиколят всички изходни канали, към които е бил пратен рекуест и да се оттегли пратения рекуест
//        for (int oldReqOCID : requestsSent) {
//            if(oldReqOCID != outputChannelId && oldReqOCID != -1) {
//                allChannels.get(allChannels.indexOf(oldReqOCID)).getRra().removeRequest(this);
//            }
//        }
        grantOutputChannelIds.add(outputChannelId);
    }

    public boolean hasGrant() {
        return !grantOutputChannelIds.isEmpty();
    }

    public ArrayList<Integer> getRequestsSent() {
        return requestsSent;
    }
}
