/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2013 Marc Pujol <mpujol@iiia.csic.es>.
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

import es.csic.iiia.planes.definition.DOperator;
import es.csic.iiia.planes.definition.DProblem;
import es.csic.iiia.planes.definition.DTask;
import es.csic.iiia.planes.generator.Generator;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class TaskDistributionPane extends JPanel {

    BufferedImage buffer;
    final DProblem problem;

    private Color[] taskColorList = new Color[]{
        Color.GREEN, Color.CYAN, Color.RED, Color.MAGENTA, Color.GRAY
    };

    public TaskDistributionPane(DProblem p) {
        problem = p;
        generateColors();
        buildBuffer(600, 600);
    }

    public void changeSize(int width, int height) {
        buildBuffer(width, height);
        this.repaint();
    }

    private void generateColors() {
        float ratio = 0.618033988749895f;
        float h = 0.534f;
        for (int i=0; i<taskColorList.length; i++) {
            Color c = Color.getHSBColor(h, 0.9f, 0.9f);
            taskColorList[i] = new Color(c.getRed(), c.getGreen(), c.getBlue(), 20);
            h += ratio;
            h %= 1;
        }
    }

    private void buildBuffer(int width, int height) {
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        double xscale = ((double)width) / problem.getWidth();
        double yscale = ((double)height) / problem.getHeight();
        Graphics2D g = (Graphics2D) buffer.getGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        for (DOperator o : problem.getOperators()) {
            for (DTask t : o.getTasks()) {
                g.setColor(taskColorList[t.getnCrisis()]);
                g.fillOval((int) (t.getX() * xscale), (int) (t.getY() * yscale), 10, 10);
            }
        }

    }

    @Override
    public void paint(Graphics grphcs) {
        super.paint(grphcs);
        grphcs.drawImage(buffer, 0, 0, null);
    }

}
