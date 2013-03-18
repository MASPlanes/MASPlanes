/*
 * Software License Agreement (BSD License)
 *
 * Copyright (c) 2012, IIIA-CSIC, Artificial Intelligence Research Institute
 * All rights reserved.
 *
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 *
 *   Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 *
 *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute
 *   nor the names of its contributors may be used to
 *   endorse or promote products derived from this
 *   software without specific prior written permission of
 *   IIIA-CSIC, Artificial Intelligence Research Institute
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package es.csic.iiia.planes.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class Display extends JFrame {

    private static int DEFAULT_WIDTH = 500;
    private static int DEFAULT_HEIGHT = 500;

    private int insetsVertical;
    private int insetsHorizontal;
    private GUIWorld world;
    private DisplayPane displayPane;
    private JLabel time;
    private final JLayeredPane layers;
    private final TaskDistributionPane tasksPane;

    public Display(GUIWorld w) {
        this.world = w;
        JPanel root = new JPanel(new BorderLayout());
        this.setContentPane(root);

        Dimension d = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        layers = new JLayeredPane();
        root.add(layers, BorderLayout.CENTER);
        layers.setPreferredSize(d);

        displayPane = new DisplayPane(world);
        displayPane.setPreferredSize(d);
        displayPane.setBounds(new Rectangle(d));
        displayPane.setOpaque(false);
        layers.add(displayPane);

        tasksPane = new TaskDistributionPane(w.getFactory().getConfiguration().problemDefinition);
        tasksPane.setPreferredSize(d);
        tasksPane.setBackground(Color.WHITE);
        tasksPane.setBounds(new Rectangle(d));
        tasksPane.setOpaque(true);
        layers.add(tasksPane);

        //viewer = new TaskDistributionViewer(w.getFactory().getConfiguration().problemDefinition);

        JPanel top = new JPanel(new FlowLayout());

        JToggleButton b = new JToggleButton("Tasks");
        b.addActionListener(new AbstractAction("Tasks") {

            @Override
            public void actionPerformed(ActionEvent ae) {
                tasksPane.toggle();
            }

        });
        top.add(b);

        JLabel l = new JLabel("Speed: ");
        top.add(l);
        JSlider s = new JSlider(1, 50, 25);

        s.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent ce) {
                JSlider source = (JSlider)ce.getSource();
                if (!source.getValueIsAdjusting()) {
                    int speed = (int)source.getValue();
                    speed = (int)Math.pow(10f, speed/10f);
                    System.err.println("New speed: " + speed);
                    world.setSpeed(speed);
                }
            }

        });

        top.add(s);
        time = new JLabel("Time: ");
        top.add(time);
        root.add(top, BorderLayout.NORTH);

        this.pack();
        this.setVisible(true);

        System.err.println("Insets: " + this.getInsets());
        Insets insets = this.getInsets();
        insetsVertical = insets.bottom + insets.top;
        insetsHorizontal = insets.left + insets.right;
        System.err.println(this.getSize());

        // Observador que s'encarrega de finalitzar el programa en
        // tancar la finestra principal.
        this.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            System.exit(0);
          }
        });

        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent ce) {

                // Bloquegem l'aspect ratio
                Dimension d  = Display.this.getSize();
                int width = d.width;
                int height = d.height;
                double ratio = (width-insetsHorizontal)/(double)(height-insetsVertical);
                ratio = Math.round(ratio*100f) / 100f;
                System.err.println("Ratio: " + ratio);
                if (ratio > 1) {
                    Display.this.setSize((int) (width / ratio), height);
                } else if (ratio < 1) {
                    Display.this.setSize(width, (int) (height * ratio));
                } else {
                    super.componentResized(ce);
                    Dimension dim = Display.this.getContentPane().getSize();
                    displayPane.setBounds(new Rectangle(d));
                    tasksPane.changeSize(width, height);
                    tasksPane.setBounds(new Rectangle(d));
                    System.err.println(dim);
                }
            }

        });

    }

    public Dimension getDisplayDimension() {
        return layers.getSize();
    }

    @Override
    public void paint(Graphics grphcs) {
        time.setText("Time: " + world.getTime());
        super.paint(grphcs);
    }
}