package org.cbsbh.model.routing;

import java.util.HashMap;

/**
 * Description goes here
 * Date: 6/1/14 8:23 PM
 *
 * @author Mihail Chilyashev
 */
public class InputChannelCollection {
    // ID => Channel
    private static HashMap<Long, InputChannel> channels;

    public static void push(InputChannel channel){
        channels.put(channel.getId(), channel);
    }
    public static InputChannel get(long id){
        return channels.get(id);
    }
}
