package org.cbsbh.model.routing.packet;

import java.util.Random;

/**
 * Description goes here
 * Date: 6/5/14 5:43 PM
 *
 * @author Mihail Chilyashev
 */
public class PacketGenerator {
    private final static Random r = new Random();


    public static Packet generate(int numChannels, int tr){ // TODO: tr
        int address = r.nextInt(2<<numChannels) & 0xfff;
        return new Packet(address, 0, r.nextLong(), r.nextLong(), r.nextLong());
    }
}
