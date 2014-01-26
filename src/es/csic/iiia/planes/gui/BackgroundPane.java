/*
 * Copyright (c) 2014, Marc Pujol <mpujol@iiia.csic.es>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package es.csic.iiia.planes.gui;

import es.csic.iiia.planes.definition.DOperator;
import es.csic.iiia.planes.definition.DProblem;
import es.csic.iiia.planes.definition.DTask;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class BackgroundPane extends JPanel {
    private static final Logger LOG = Logger.getLogger(BackgroundPane.class.getName());

    private static final int DEFAULT_WIDTH  = 800;
    private static final int DEFAULT_HEIGHT = 800;
    private final static String BACKGROUND = "background.png";

    BufferedImage image;
    BufferedImage buffer;

    public BackgroundPane() {
        this.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        try {
            image = ImageIO.read(getClass().getClassLoader().getResource(BACKGROUND));
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Unable to load background image.", ex);
        }
    }

    @Override
    public void setBounds(int i, int i1, int i2, int i3) {
        super.setBounds(i, i1, i2, i3);
        changeSize(i2, i3);
    }

    @Override
    public void setBounds(Rectangle rctngl) {
        super.setBounds(rctngl);
        changeSize(rctngl.width, rctngl.height);
    }

    public void changeSize(int width, int height) {
        if (width < 0 || height < 0) {
            return;
        }

        double xscale = ((double)width)  / DEFAULT_WIDTH;
        double yscale = ((double)height) / DEFAULT_HEIGHT;

        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D)buffer.getGraphics();
        AlphaComposite ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);
        g.setComposite(ac);

        g.setTransform(AffineTransform.getScaleInstance(xscale, yscale));
        g.drawImage(image, 0, 0, null);
        this.repaint();
    }

    @Override
    public void paint(Graphics grphcs) {
        super.paint(grphcs);
        grphcs.drawImage(buffer, 0, 0, null);
    }

}
