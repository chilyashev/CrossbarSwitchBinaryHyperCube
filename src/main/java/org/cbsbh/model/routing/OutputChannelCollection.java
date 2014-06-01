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
    private static HashMap<Long, OutputChannel> channels;

    public static void push(OutputChannel channel){
        channels.put(channel.getId(), channel);
    }
    public static OutputChannel get(long id){
        return channels.get(id);
    }
}
