package org.cbsbh.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Created by Mihail Chilyashev on 2/21/16.
 * All rights reserved, unless otherwise noted.
 */
public class TableHolderWindow extends JFrame {
    private JTable table;
    private JScrollPane scrollPane;
    DefaultTableModel model;

    public TableHolderWindow() throws HeadlessException {
        // UI Design crap
        setSize(1000, 800);
        setLayout(new BorderLayout());

        // Initializing UI crap
        model = new DefaultTableModel(new String[]{
                "NodeID","IC ID","FIFOQueue №","OC ID","State","Buffer element count","isHeader"
        }, 0);
        table = new JTable(model);
        scrollPane = new JScrollPane(table);
        //scrollPane.add("CENTER", table);
        // Adding UI
        add(scrollPane);

        // Misc
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setVisible(true);
    }

    public void addRow(String row) {
        model.addRow(new String[]{row});
    }
}
