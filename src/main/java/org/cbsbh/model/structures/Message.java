package org.cbsbh.model.structures;

import org.cbsbh.model.routing.packet.Packet;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Description goes here
 * Date: 7/8/14 8:24 PM
 *
 * @author Mihail Chilyashev
 */
public class Message {
    int target;
    int source;

    private LinkedList<Long> data;

    public Message() {
    }

    public Message(int target, int source, LinkedList<Long> data) {
        this.target = target;
        this.source = source;
        this.data = data;
    }

    public Packet getPacket() {
        if (data.size() < 1) {
            // throw an exception
            return null;
        }

        long data_l = data.pop();

        if (data.size() < 1) {
            return new Packet(target, target ^ source, 0x00000000, data_l, 0x00000000);
        }


        return new Packet(target, target ^ source, 0x00000000, data_l, data.pop());
    }

    public ArrayList<Packet> getAsPackets() {
        ArrayList<Packet> ret = new ArrayList<>();
        /*if (data.size() < 1) {
            // throw an exception
            return null;
        }*/
        while (!data.isEmpty()) {

            long data_l = data.pop();
            if (data.size() >= 1) {
                ret.add(new Packet(target, target ^ source, 0xffffffff, data_l, data.pop()));
            } else {
                ret.add(new Packet(target, target ^ source, 0xffffffff, data_l, 0x00000007));
            }

        }
        return ret;
    }


    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public LinkedList<Long> getData() {
        return data;
    }

    public void setData(LinkedList<Long> data) {
        this.data = data;
    }
}
