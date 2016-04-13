package org.cbsbh.model.routing;

import org.cbsbh.model.structures.SignalArray;

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
        boolean found = false,
                requestSent = false; // Това е нужно, защото каналът винаги трябва да е намерен, но има случаи,
                                    // в които изходният канал е в състояние 6 и ще си зачисти картата със заявки без значение дали ги е видял
        // Обикалят се всички канали и се търси канал с точното id.
        // Като се намери, му се взема RRA-то и на него се праща заявка за пращане
        for (OutputChannel channel : allChannels) {
            if (channel.getNextNodeId() == id) {
                found = true;
                // Заявка за изпращане се праща само на канали, които могат да я поемат, т.е. са в правилното състояние
                if (channel.hasSignal(SignalArray.STRB_SIG)) {
                    requestSent = true;
                    channel.getRra().requestToSend(this);
                }
                break;
            }
        }
        assert found : "Input channel with id " + id + " not found. A bitch, ain't it?";
        return requestSent;
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
        if (grantOutputChannelIds.size() < 1) {
            return -1;
        }

        assert grantOutputChannelIds.size() > 0 : "Никой не е върнал Grant";
        int id = grantOutputChannelIds.remove(0);
        grantOutputChannelIds.clear();
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
        boolean requestSent = false;
        for (int i = 0; i < 12; i++) { // 12. Like the 12 bits in the TR. Duh...
            if ((tr & (1 << i)) == 1 << i) {
                int outputChannelId = nodeId ^ (1 << i);
                boolean newRequestSent = sendRequest(outputChannelId);
                requestSent = requestSent || newRequestSent;
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
        grantOutputChannelIds.add(outputChannelId);
    }

    public boolean hasGrant() {
        return !grantOutputChannelIds.isEmpty();
    }
}
