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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class TimeHistogramPane extends JPanel {

    public static final int DEFAULT_HEIGHT = 25;
    public static final int DEFAULT_WIDTH = 1000;

    private BufferedImage buffer;
    private final Display display;
    private final DProblem problem;

    public TimeHistogramPane(Display display, DProblem p) {
        this.display = display;
        this.problem = p;
        buildBuffer(p);
    }

    private void buildBuffer(DProblem p) {
        buffer = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_RGB);
        double[][] bins = new double[p.getnCrisis()][DEFAULT_WIDTH];
        double[] sums = new double[DEFAULT_WIDTH];

        // Compute the histogram, along with the maximum number of elements in a bin
        double max = 0;
        for (DOperator o : p.getOperators()) {
            for (DTask t : o.getTasks()) {
                final int bin = Math.min((int) (t.getTime() * DEFAULT_WIDTH / p.getDuration()),
                        DEFAULT_WIDTH-1);
                final int n = t.getnCrisis();
                bins[n][bin]++;
                sums[bin]++;
                if (sums[bin] > max) {
                    max = sums[bin];
                }
            }
        }

        // Normalize the bins
        for (int n = 0; n < p.getnCrisis(); n++) {
            for (int x = 0; x < DEFAULT_WIDTH; x++) {
                bins[n][x] = bins[n][x] * DEFAULT_HEIGHT / max;
            }
        }

        // Fetch the colors, without alpha
        Color[] colors = new Color[p.getnCrisis()];
        for (int n = 0; n < p.getnCrisis(); n++) {
            Color c = display.getColor(n);
            colors[n] = new Color(c.getRed(), c.getGreen(), c.getBlue(), 150);
        }

        // Draw it
        Graphics2D g = (Graphics2D) buffer.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setColor(new Color(0,0,0));
        g.fillRect(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
//        g.setColor(Color.BLACK);
//        g.draw(new Line2D.Double(0, 0, DEFAULT_WIDTH, 0));
        for (int x = 0; x < DEFAULT_WIDTH; x++) {
            double y = DEFAULT_HEIGHT;
            for (int n = 0; n < p.getnCrisis(); n++) {
                g.setColor(colors[n]);
                double yend = y - bins[n][x];
                g.draw(new Line2D.Double(x, y, x, yend));
                y = yend;
            }
        }
    }

    @Override
    public void paint(Graphics grphcs) {
        super.paint(grphcs);

        Graphics2D g = (Graphics2D)grphcs;
        Dimension d = getSize();
        double xscale = d.width  / (double)DEFAULT_WIDTH;
        double yscale = d.height / (double)DEFAULT_HEIGHT;
        AffineTransform t = AffineTransform.getScaleInstance(xscale, yscale);
        g.drawImage(buffer, t, null);

        // Time
        int time = (int)(display.getTime() * d.width / problem.getDuration());
        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(2f));
        g.draw(new Line2D.Double(time, 0, time, d.height));
    }


}
