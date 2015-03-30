package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;

/**
 * Description goes here
 * Date: 3/16/15 10:32 AM
 *
 * @author Mihail Chilyashev
 */
public class Arbiter implements Tickable{
    /*
    *
    * Всички изходни канали, достъпни от арбитъра
    */
    ArrayList<OutputStateStructure> allChannels;

    /**
     * Канали, които са отговорили с Grant
     */
    ArrayList<OutputStateStructure> grantOutputChannels;


    @Override
    public void tick() {

    }

    /**
     * Праща заявка до изходен канал
     */
    public void sendRequest(int id) {
        boolean found = false;
        // Обикалят се всички канали и се търси канал с точното id.
        // Като се намери, му се взема RRA-то и на него се праща заявка за пращане
        for (OutputStateStructure channel : allChannels) {
            if(channel.getNextNodeId() == id){
                found = true;
                channel.getRra().requestToSend(this);
                break;
            }
        }
        assert found : "Input channel with id " + id + " not found. A bitch, ain't it?";
    }

    /**
     * Праща заявки до списък от id-та на канали
     * @param ids списък от id-та на канали
     */
    public void sendRequest(int ids[]) {
        for (int id : ids) {
            sendRequest(id);
        }
    }

    /**
     * Връща Grant Acknowledge на първия канал, върнал Grant (първия канал в grantOutputChannels)
     * След като се върне ACK, grantOutputChannels се изпразва. Yeah.
     */
    public void sendGrantAck() {
        throw new NotImplementedException();
    }
}
