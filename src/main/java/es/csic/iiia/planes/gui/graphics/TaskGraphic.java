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
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * Graphic for tasks.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class TaskGraphic {

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

    private void paintShapeNode_0_0_0(Graphics2D g) {
        GeneralPath shape0 = new GeneralPath();
        shape0.moveTo(22.280272, 396.8336);
        shape0.lineTo(207.13226, 28.031586);
        shape0.curveTo(207.13226, 28.031586, 233.54128, -3.4724152, 259.94727, 28.031586);
        shape0.curveTo(286.35526, 59.537586, 440.13928, 398.68558, 440.13928, 398.68558);
        shape0.curveTo(440.13928, 398.68558, 443.24426, 417.21957, 412.17828, 417.21957);
        shape0.curveTo(381.11227, 417.21957, 50.240273, 417.21957, 50.240273, 417.21957);
        shape0.curveTo(50.240273, 417.21957, 26.940271, 417.2176, 22.280272, 396.8336);
        shape0.closePath();
        g.setPaint(new Color(0, 0, 0, 255));
        g.fill(shape0);
    }

    private void paintShapeNode_0_0_1(Graphics2D g) {
        GeneralPath shape1 = new GeneralPath();
        shape1.moveTo(8.551, 390.497);
        shape1.lineTo(193.403, 21.695);
        shape1.curveTo(193.403, 21.695, 219.812, -9.809, 246.218, 21.695);
        shape1.curveTo(272.626, 53.201, 426.41, 392.349, 426.41, 392.349);
        shape1.curveTo(426.41, 392.349, 429.515, 410.883, 398.449, 410.883);
        shape1.curveTo(367.383, 410.883, 36.511, 410.883, 36.511, 410.883);
        shape1.curveTo(36.511, 410.883, 13.211, 410.881, 8.551, 390.497);
        shape1.closePath();
        g.setPaint(new RadialGradientPaint(new Point2D.Double(216.7041015625, 393.7948913574219), 296.6968f, new Point2D.Double(216.7041015625, 393.7948913574219), new float[]{0.0f, 1.0f}, new Color[]{new Color(244, 215, 8, 255), new Color(252, 180, 0, 255)}, MultipleGradientPaint.CycleMethod.NO_CYCLE, MultipleGradientPaint.ColorSpaceType.SRGB, new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f)));
        g.fill(shape1);
    }

    private void paintShapeNode_0_0_2(Graphics2D g) {
        GeneralPath shape2 = new GeneralPath();
        shape2.moveTo(8.551, 390.497);
        shape2.lineTo(193.403, 21.695);
        shape2.curveTo(193.403, 21.695, 219.812, -9.809, 246.218, 21.695);
        shape2.curveTo(272.626, 53.201, 426.41, 392.349, 426.41, 392.349);
        shape2.curveTo(426.41, 392.349, 429.515, 410.883, 398.449, 410.883);
        shape2.curveTo(367.383, 410.883, 36.511, 410.883, 36.511, 410.883);
        shape2.curveTo(36.511, 410.883, 13.211, 410.881, 8.551, 390.497);
        shape2.closePath();
        g.setPaint(new Color(226, 167, 19, 255));
        g.setStroke(new BasicStroke(5.0f, 0, 0, 4.0f, null, 0.0f));
        g.draw(shape2);
    }

    private void paintShapeNode_0_0_3_0(Graphics2D g) {
        GeneralPath shape3 = new GeneralPath();
        shape3.moveTo(-392.0625, 28.9375);
        shape3.curveTo(-392.17993, 29.141064, -392.37985, 29.501896, -392.65625, 30.0);
        shape3.curveTo(-392.1424, 29.330217, -392.11414, 29.073711, -392.0625, 28.9375);
        shape3.closePath();
        shape3.moveTo(-386.40625, 29.6875);
        shape3.curveTo(-386.37363, 29.743301, -385.9259, 30.119497, -385.84375, 30.21875);
        shape3.curveTo(-385.89215, 30.140516, -386.05777, 29.868866, -386.09375, 29.8125);
        shape3.curveTo(-386.23685, 29.58822, -386.56287, 29.419708, -386.40625, 29.6875);
        shape3.closePath();
        shape3.moveTo(-388.15625, 30.625);
        shape3.curveTo(-389.88336, 30.50541, -392.10272, 31.245201, -393.71875, 31.96875);
        shape3.curveTo(-394.51224, 33.430683, -395.32602, 34.914635, -396.53125, 37.1875);
        shape3.curveTo(-399.7489, 43.25551, -404.27594, 51.944267, -409.65625, 62.3125);
        shape3.curveTo(-420.41684, 83.048965, -434.56396, 110.57422, -448.65625, 138.125);
        shape3.curveTo(-458.86295, 158.07935, -467.8102, 175.70268, -476.625, 193.09375);
        shape3.curveTo(-467.6627, 189.74818, -458.52142, 187.00513, -449.25, 185.0);
        shape3.curveTo(-429.2333, 180.67099, -403.0504, 180.62221, -383.21875, 185.0625);
        shape3.curveTo(-367.70816, 188.53531, -354.96066, 193.26141, -335.03125, 202.625);
        shape3.curveTo(-326.28098, 206.7362, -314.87515, 211.6718, -313.0, 212.375);
        shape3.curveTo(-305.386, 215.23038, -297.9891, 216.97464, -292.8125, 217.5625);
        shape3.curveTo(-292.4478, 217.60391, -292.1862, 217.54636, -291.84375, 217.5625);
        shape3.lineTo(-291.84375, 217.53125);
        shape3.lineTo(-337.53125, 125.34375);
        shape3.curveTo(-355.61072, 88.861855, -367.73746, 64.60052, -375.5625, 49.375);
        shape3.curveTo(-379.475, 41.76224, -382.33862, 36.402084, -384.1875, 33.09375);
        shape3.curveTo(-384.5048, 32.52602, -384.5884, 32.410324, -384.84375, 31.96875);
        shape3.curveTo(-385.53793, 31.538422, -386.34225, 31.022312, -386.5625, 30.9375);
        shape3.curveTo(-386.87384, 30.817612, -387.43365, 30.675034, -388.15625, 30.625);
        shape3.closePath();
        g.setPaint(new RadialGradientPaint(new Point2D.Double(-409.7658386230469, 87.34991455078125), 92.390625f, new Point2D.Double(-409.7658386230469, 87.34991455078125), new float[]{0.0f, 0.6785714f, 1.0f}, new Color[]{new Color(255, 234, 149, 255), new Color(255, 234, 149, 127), new Color(255, 234, 149, 0)}, MultipleGradientPaint.CycleMethod.NO_CYCLE, MultipleGradientPaint.ColorSpaceType.SRGB, new AffineTransform(0.7807053923606873f, 1.8151880502700806f, -1.0895527601242065f, 0.4686119854450226f, 5.312927722930908f, 789.4688110351562f)));
        g.fill(shape3);
    }

    private void paintCompositeGraphicsNode_0_0_3(Graphics2D g) {
        // _0_0_3_0
        AffineTransform trans_0_0_3_0 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, -6.336585521697998f, -7.392683029174805f));
        paintShapeNode_0_0_3_0(g);
        g.setTransform(trans_0_0_3_0);
    }

    private void paintShapeNode_0_0_4(Graphics2D g) {
        GeneralPath shape4 = new GeneralPath();
        shape4.moveTo(212.496, 292.632);
        shape4.curveTo(199.328, 212.663, 192.746, 169.51099, 192.746, 163.17899);
        shape4.curveTo(192.746, 155.47598, 195.297, 149.25299, 200.406, 144.50299);
        shape4.curveTo(205.511, 139.75699, 211.27701, 137.38199, 217.699, 137.38199);
        shape4.curveTo(224.64801, 137.38199, 230.51901, 139.91699, 235.308, 144.98);
        shape4.curveTo(240.09698, 150.043, 242.496, 156.00299, 242.496, 162.86299);
        shape4.curveTo(242.496, 169.40599, 235.828, 212.66399, 222.496, 292.633);
        shape4.lineTo(212.496, 292.633);
        shape4.closePath();
        shape4.moveTo(239.496, 330.804);
        shape4.curveTo(239.496, 336.90198, 237.34, 342.10498, 233.02701, 346.417);
        shape4.curveTo(228.714, 350.72598, 223.56601, 352.882, 217.574, 352.882);
        shape4.curveTo(211.476, 352.882, 206.27301, 350.72598, 201.961, 346.417);
        shape4.curveTo(197.648, 342.104, 195.496, 336.901, 195.496, 330.804);
        shape4.curveTo(195.496, 324.81198, 197.648, 319.663, 201.961, 315.35098);
        shape4.curveTo(206.274, 311.03897, 211.477, 308.882, 217.574, 308.882);
        shape4.curveTo(223.56601, 308.882, 228.71501, 311.038, 233.02701, 315.35098);
        shape4.curveTo(237.339, 319.66397, 239.496, 324.812, 239.496, 330.804);
        shape4.closePath();
        g.setPaint(new Color(0, 0, 0, 255));
        g.fill(shape4);
    }

    private void paintShapeNode_0_0_5(Graphics2D g) {
        GeneralPath shape5 = new GeneralPath();
        shape5.moveTo(-245.46875, 358.15625);
        shape5.curveTo(-254.81715, 358.17392, -264.5083, 358.80008, -275.15625, 359.96875);
        shape5.curveTo(-301.26434, 362.83423, -325.63385, 369.9838, -345.9375, 380.5625);
        shape5.curveTo(-347.52344, 381.38882, -347.961, 381.72794, -349.0625, 382.34375);
        shape5.curveTo(-348.98578, 382.34744, -348.95215, 382.34003, -348.875, 382.34375);
        shape5.curveTo(-339.56598, 382.79163, -327.12985, 383.34802, -314.125, 383.90625);
        shape5.curveTo(-288.11533, 385.02274, -259.67313, 386.1292, -249.5625, 386.375);
        shape5.curveTo(-232.07434, 386.8001, -171.62209, 386.4206, -122.21875, 385.875);
        shape5.curveTo(-130.86015, 383.4167, -140.48416, 380.44162, -152.15625, 376.5625);
        shape5.curveTo(-191.94469, 363.33914, -217.40334, 358.10324, -245.46875, 358.15625);
        shape5.closePath();
        shape5.moveTo(5.1875, 368.6875);
        shape5.curveTo(0.44490537, 374.04584, -5.6702204, 378.52005, -12.375, 382.28125);
        shape5.curveTo(-1.6479121, 381.62225, 5.4013, 381.14386, 9.46875, 380.8125);
        shape5.curveTo(8.491813, 376.26648, 6.9457517, 372.0656, 5.1875, 368.6875);
        shape5.closePath();
        g.setPaint(new Color(255, 234, 150, 255));
        g.fill(shape5);
    }

    private void paintCanvasGraphicsNode_0_0(Graphics2D g) {
        // _0_0_0
        g.setComposite(AlphaComposite.getInstance(3, 0.29f * origAlpha));
        AffineTransform trans_0_0_0 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
        paintShapeNode_0_0_0(g);
        g.setTransform(trans_0_0_0);
        // _0_0_1
        g.setComposite(AlphaComposite.getInstance(3, 1.0f * origAlpha));
        AffineTransform trans_0_0_1 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
        paintShapeNode_0_0_1(g);
        g.setTransform(trans_0_0_1);
        // _0_0_2
        AffineTransform trans_0_0_2 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
        paintShapeNode_0_0_2(g);
        g.setTransform(trans_0_0_2);
        // _0_0_3
        AffineTransform trans_0_0_3 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 611.7048950195312f, 2.1121950149536133f));
        paintCompositeGraphicsNode_0_0_3(g);
        g.setTransform(trans_0_0_3);
        // _0_0_4
        AffineTransform trans_0_0_4 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
        paintShapeNode_0_0_4(g);
        g.setTransform(trans_0_0_4);
        // _0_0_5
        AffineTransform trans_0_0_5 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 389.70001220703125f, 10.560977935791016f));
        paintShapeNode_0_0_5(g);
        g.setTransform(trans_0_0_5);
    }

    private void paintRootGraphicsNode_0(Graphics2D g) {
        // _0_0
        g.setComposite(AlphaComposite.getInstance(3, 1.0f * origAlpha));
        AffineTransform trans_0_0 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, -0.0f, -0.0f));
        paintCanvasGraphicsNode_0_0(g);
        g.setTransform(trans_0_0);
    }

    /**
     * Returns the X of the bounding box of the original SVG image.
     *
     * @return The X of the bounding box of the original SVG image.
     */
    public int getOrigX() {
        return 0;
    }

    /**
     * Returns the Y of the bounding box of the original SVG image.
     *
     * @return The Y of the bounding box of the original SVG image.
     */
    public int getOrigY() {
        return 0;
    }

    /**
     * Returns the width of the bounding box of the original SVG image.
     *
     * @return The width of the bounding box of the original SVG image.
     */
    public int getOrigWidth() {
        return 448;
    }

    /**
     * Returns the height of the bounding box of the original SVG image.
     *
     * @return The height of the bounding box of the original SVG image.
     */
    public int getOrigHeight() {
        return 433;
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
    public TaskGraphic() {
        this.width = getOrigWidth();
        this.height = getOrigHeight();
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
    public void paint(Graphics g, int x, int y) {
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
