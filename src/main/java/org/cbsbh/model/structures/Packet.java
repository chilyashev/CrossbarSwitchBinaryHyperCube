package org.cbsbh.model.structures;

import org.cbsbh.Debug;

import java.util.ArrayList;

/**
 * A packet is an array of flits.
 * Date: 14/4/16 11:51 PM
 *
 * @author Georgi Georgiev
 */
public class Packet extends ArrayList<Flit> {

    public Packet () {super();}

    public Packet (int sourceId, int targetId, int packetSize) {
        ///Generate head flit:
        Flit flit = new Flit(sourceId, targetId, Flit.FLIT_TYPE_HEADER);
        flit.setDNA(targetId);
        flit.setTR(sourceId ^ flit.getDNA());
        Debug.printf("> [Just the tip] Generating a message. From %d to %d", sourceId, flit.getDNA());
        this.add(flit);

        //Generate body flits
        while (packetSize-- > 0) {
            this.add(new Flit(sourceId, targetId, Flit.FLIT_TYPE_BODY));
        }

        //Generate tail flit:
        this.add(new Flit(sourceId, targetId, Flit.FLIT_TYPE_TAIL));
    }
}
