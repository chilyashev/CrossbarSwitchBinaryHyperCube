package org.cbsbh.ui;

import javafx.util.StringConverter;

/**
 * Created by Mihail Chilyashev on 4/14/16.
 * All rights reserved, unless otherwise noted.
 */
public class SliderStringConverter extends StringConverter<Number> {
    @Override
    public String toString(Number object) {
        return String.valueOf(object.intValue());
    }

    @Override
    public Number fromString(String string) {
        int ret = 0;
        try {
            ret = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            ret = 0;
        }
        return ret;
    }
}
