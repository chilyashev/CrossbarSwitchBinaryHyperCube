/**
 * Created by Mihail Chilyashev on 6/12/16.
 * All rights reserved, unless otherwise noted.
 */

package org.cbsbh.ui.screens;

import org.cbsbh.ui.AbstractScreen;

/**
 * Used to test blank screens with no functionality.
 */
public class BlankController extends AbstractScreen{
    @Override
    public void init() {
        System.out.printf("%s loaded.\n", BlankController.class.getSimpleName());
    }
}
