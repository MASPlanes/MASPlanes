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

import es.csic.iiia.planes.definition.DProblem;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
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

    private GUIWorld world;
    private DisplayPane displayPane;
    private JLabel time;
    private final JLayeredPane layers;
    private final TaskDistributionPane tasksPane;
    private Color[] colors;
    private final TimeHistogramPane histogramPane;
    private final DProblem problemDefinition;
    private final JPanel top;

    public Display(GUIWorld w) {
        this.world = w;
        problemDefinition = w.getFactory().getConfiguration().getProblemDefinition();
        generateColors();

        JPanel root = new JPanel(new BorderLayout());
        this.setContentPane(root);

        Dimension d = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        layers = new JLayeredPane();
        root.add(layers, BorderLayout.CENTER);
        layers.setPreferredSize(d);

        displayPane = new DisplayPane(world);
        //displayPane.setPreferredSize(d);
        displayPane.setBounds(new Rectangle(d));
        displayPane.setOpaque(false);
        layers.add(displayPane);

        tasksPane = new TaskDistributionPane(this, problemDefinition);
        //tasksPane.setPreferredSize(d);
        tasksPane.setBackground(Color.WHITE);
        tasksPane.setBounds(new Rectangle(d));
        tasksPane.setOpaque(true);
        layers.add(tasksPane);

        histogramPane = new TimeHistogramPane(this, problemDefinition);
        histogramPane.setPreferredSize(new Dimension(d.width, TimeHistogramPane.DEFAULT_HEIGHT));
        root.add(histogramPane, BorderLayout.SOUTH);

        //viewer = new TaskDistributionViewer(w.getFactory().getConfiguration().problemDefinition);

        top = new JPanel(new FlowLayout());
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
                JSlider source = (JSlider) ce.getSource();
                if (!source.getValueIsAdjusting()) {
                    int speed = (int) source.getValue();
                    speed = (int) Math.pow(10f, speed / 10f);
                    world.setSpeed(speed);
                }
            }
        });

        top.add(s);
        time = new JLabel("Time: ");
        top.add(time);

        root.add(top, BorderLayout.NORTH);

        this.pack();
        this.setMinimumSize(new Dimension(top.getSize().width, top.getSize().height + 200));
        this.setVisible(true);

        // Observador que s'encarrega de finalitzar el programa en
        // tancar la finestra principal.
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
              System.exit(0);
            }
        });

        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent ce) {
                Dimension innerD = Display.this.layers.getSize();
                int innerWidth  = innerD.width;
                int innerHeight = innerD.height;

                layers.setBounds(new Rectangle(innerD));
                displayPane.setBounds(new Rectangle(innerD));
                tasksPane.setBounds(new Rectangle(innerD));
                tasksPane.changeSize(innerWidth, innerHeight);
                super.componentResized(ce);
            }

        });

    }

    public Color getColor(int nCrisis) {
        return colors[nCrisis];
    }

    private void generateColors() {
        float ratio = 0.618033988749895f;
        float h = 0.534f;
        colors = new Color[problemDefinition.getnCrisis()];
        for (int i=0; i<problemDefinition.getnCrisis(); i++) {
            Color c = Color.getHSBColor(h, 0.9f, 0.9f);
            colors[i] = new Color(c.getRed(), c.getGreen(), c.getBlue(), 20);
            h += ratio;
            h %= 1;
        }
    }

    public Dimension getDisplayDimension() {
        return layers.getSize();
    }

    public long getTime() {
        return world.getTime();
    }

    @Override
    public void paint(Graphics grphcs) {
        time.setText("Time: " + world.getTime());
        super.paint(grphcs);
    }
}
