package org.cbsbh.model.routing;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
     * @return -1, ако никой не е върнал Grant
     */
    public int getNextNodeId() {
        if(grantOutputChannelIds.size() < 1){
            return -1;
        }
        //assert grantOutputChannelIds.size() > 0 : "";
        int id = grantOutputChannelIds.remove(0);
        grantOutputChannelIds.clear();
        sendGrantAck(id);
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
    private void sendGrantAck(int outputChannelId) {
        throw new NotImplementedException();
    }

    public void sendRequestByTR(long tr) {
        boolean requestSent = false;
        for (int i = 0; i < 12; i++) { // 12. Like the 12 bits in the TR. Duh...
            if ((tr & (1 << i)) == 1 << i) {
                int outputChannelId = nodeId ^ (1 << i);
                requestSent = requestSent || sendRequest(outputChannelId);
            }
        }
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
}
