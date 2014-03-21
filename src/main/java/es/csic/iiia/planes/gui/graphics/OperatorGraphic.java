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
package es.csic.iiia.planes.gui.graphics;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

/**
 * Graphic that represents an operator.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class OperatorGraphic {

    private float origAlpha = 1.0f;

    /**
     * Paints the transcoded SVG image on the specified graphics context. You
     * can install a custom transformation on the graphics context to scale the
     * image.
     *
     * @param g Graphics context.
     */
    public void paint(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        origAlpha = 1.0f;
        Composite origComposite = g.getComposite();
        if (origComposite instanceof AlphaComposite) {
            AlphaComposite origAlphaComposite =
                    (AlphaComposite) origComposite;
            if (origAlphaComposite.getRule() == AlphaComposite.SRC_OVER) {
                origAlpha = origAlphaComposite.getAlpha();
            }
        }

        // _0
        AffineTransform trans_0 = g.getTransform();
        paintRootGraphicsNode_0(g);
        g.setTransform(trans_0);

    }

    private void paintShapeNode_0_0_0_0(Graphics2D g) {
        Ellipse2D.Double shape0 = new Ellipse2D.Double(3.3689918518066406, 2.8689918518066406, 122.26201629638672, 122.26201629638672);
        g.setPaint(new Color(191, 191, 191, 255));
        g.fill(shape0);
        g.setPaint(new Color(51, 51, 51, 255));
        g.setStroke(new BasicStroke(5.0f, 0, 0, 4.0f, null, 0.0f));
        g.draw(shape0);
    }

    private void paintShapeNode_0_0_0_1_0(Graphics2D g) {
        GeneralPath shape1 = new GeneralPath();
        shape1.moveTo(100.645744, 61.346016);
        shape1.lineTo(100.645744, 61.346016);
        shape1.closePath();
        shape1.moveTo(100.645744, 61.346016);
        shape1.lineTo(66.50978, 31.825264);
        shape1.lineTo(32.354134, 61.358803);
        shape1.lineTo(32.354134, 103.56057);
        shape1.curveTo(32.354134, 104.65048, 33.256016, 105.52478, 34.372147, 105.52478);
        shape1.lineTo(55.718624, 105.52478);
        shape1.lineTo(55.718624, 87.02152);
        shape1.curveTo(55.718624, 85.93162, 56.613964, 85.05094, 57.730083, 85.05094);
        shape1.lineTo(75.26983, 85.05094);
        shape1.curveTo(76.385925, 85.05094, 77.28129, 85.93162, 77.28129, 87.02152);
        shape1.lineTo(77.28129, 105.52478);
        shape1.lineTo(98.6343, 105.52478);
        shape1.curveTo(99.75042, 105.52478, 100.645744, 104.65048, 100.645744, 103.56057);
        shape1.lineTo(100.645744, 61.346016);
        shape1.closePath();
        shape1.moveTo(32.35412, 61.358803);
        shape1.lineTo(32.35412, 61.358803);
        shape1.closePath();
        g.fill(shape1);
    }

    private void paintShapeNode_0_0_0_1_1(Graphics2D g) {
        GeneralPath shape2 = new GeneralPath();
        shape2.moveTo(66.31168, 15.669662);
        shape2.lineTo(17.869019, 57.555855);
        shape2.lineTo(22.971907, 63.1774);
        shape2.lineTo(66.50987, 25.52921);
        shape2.lineTo(110.03784, 63.1774);
        shape2.lineTo(115.13081, 57.555855);
        shape2.lineTo(66.69811, 15.669662);
        shape2.lineTo(66.50987, 15.882563);
        shape2.lineTo(66.31168, 15.669662);
        shape2.closePath();
        g.fill(shape2);
    }

    private void paintShapeNode_0_0_0_1_2(Graphics2D g) {
        GeneralPath shape3 = new GeneralPath();
        shape3.moveTo(32.35412, 21.69902);
        shape3.lineTo(44.634468, 21.69902);
        shape3.lineTo(44.527477, 28.801655);
        shape3.lineTo(32.35412, 39.540554);
        shape3.lineTo(32.35412, 21.69902);
        shape3.closePath();
        g.fill(shape3);
    }

    private void paintCompositeGraphicsNode_0_0_0_1(Graphics2D g) {
        // _0_0_0_1_0
        AffineTransform trans_0_0_0_1_0 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
        paintShapeNode_0_0_0_1_0(g);
        g.setTransform(trans_0_0_0_1_0);
        // _0_0_0_1_1
        AffineTransform trans_0_0_0_1_1 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
        paintShapeNode_0_0_0_1_1(g);
        g.setTransform(trans_0_0_0_1_1);
        // _0_0_0_1_2
        AffineTransform trans_0_0_0_1_2 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
        paintShapeNode_0_0_0_1_2(g);
        g.setTransform(trans_0_0_0_1_2);
    }

    private void paintCompositeGraphicsNode_0_0_0(Graphics2D g) {
        // _0_0_0_0
        AffineTransform trans_0_0_0_0 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
        paintShapeNode_0_0_0_0(g);
        g.setTransform(trans_0_0_0_0);
        // _0_0_0_1
        AffineTransform trans_0_0_0_1 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
        paintCompositeGraphicsNode_0_0_0_1(g);
        g.setTransform(trans_0_0_0_1);
    }

    private void paintCanvasGraphicsNode_0_0(Graphics2D g) {
        // _0_0_0
        AffineTransform trans_0_0_0 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
        paintCompositeGraphicsNode_0_0_0(g);
        g.setTransform(trans_0_0_0);
    }

    private void paintRootGraphicsNode_0(Graphics2D g) {
        // _0_0
        g.setComposite(AlphaComposite.getInstance(3, 1.0f * origAlpha));
        AffineTransform trans_0_0 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
        paintCanvasGraphicsNode_0_0(g);
        g.setTransform(trans_0_0);
    }

    /**
     * Returns the X of the bounding box of the original SVG image.
     *
     * @return The X of the bounding box of the original SVG image.
     */
    public int getOrigX() {
        return 1;
    }

    /**
     * Returns the Y of the bounding box of the original SVG image.
     *
     * @return The Y of the bounding box of the original SVG image.
     */
    public int getOrigY() {
        return 1;
    }

    /**
     * Returns the width of the bounding box of the original SVG image.
     *
     * @return The width of the bounding box of the original SVG image.
     */
    public int getOrigWidth() {
        return 128;
    }

    /**
     * Returns the height of the bounding box of the original SVG image.
     *
     * @return The height of the bounding box of the original SVG image.
     */
    public int getOrigHeight() {
        return 128;
    }
    /**
     * The current width of this resizable icon.
     */
    int width;
    /**
     * The current height of this resizable icon.
     */
    int height;

    /**
     * Creates a new transcoded SVG image.
     */
    public OperatorGraphic() {
        this.width = getOrigWidth();
        this.height = getOrigHeight();
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.Icon#getIconHeight()
     */
    public int getIconHeight() {
        return height;
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.Icon#getIconWidth()
     */
    public int getIconWidth() {
        return width;
    }

    /*
     * Set the dimension of the icon.
     */
    public void setDimension(Dimension newDimension) {
        this.width = newDimension.width;
        this.height = newDimension.height;
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
     */
    public void paint(Graphics2D g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(x, y);

        double coef1 = (double) this.width / (double) getOrigWidth();
        double coef2 = (double) this.height / (double) getOrigHeight();
        double coef = Math.min(coef1, coef2);
        g2d.scale(coef, coef);
        paint(g2d);
        g2d.dispose();
    }
}
