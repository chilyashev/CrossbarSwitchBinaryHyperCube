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
    private static HashMap<String, InputChannel> channels;

    public static void push(int routerId, InputChannel channel) {
        channels.put(routerId + "_" + channel.getRouterId(), channel);
    }

    public static InputChannel get(int routerId, int id) {
        return channels.get(routerId + "_" + id);
    }
}
