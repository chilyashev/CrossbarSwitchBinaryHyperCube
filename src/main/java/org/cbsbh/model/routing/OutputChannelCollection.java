package org.cbsbh.model.routing;

import java.util.HashMap;

/**
 * Description goes here
 * Date: 6/1/14 8:27 PM
 *
 * @author Mihail Chilyashev
 */
public class OutputChannelCollection {
    // ID => Channel
    private static HashMap<String, OutputChannel> channels;

    public static void push(int routerId, OutputChannel channel){
        channels.put(routerId + "_" + channel.getId(), channel);
    }
    public static OutputChannel get(int routerId, int id){
        return channels.get(routerId + "_" + id);
    }
}
