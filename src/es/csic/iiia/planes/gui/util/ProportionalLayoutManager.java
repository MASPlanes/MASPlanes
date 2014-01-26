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
package es.csic.iiia.planes.gui.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.Arrays;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class ProportionalLayoutManager implements LayoutManager {

    private int width;
    private int height;

    public ProportionalLayoutManager(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        System.err.println("Adding component " + comp + " name: " + name);
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        System.err.println("Removing component " + comp);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return new Dimension(width, height);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(100, 100);
    }

    @Override
    public void layoutContainer(Container target) {
        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();
            int top = insets.top;
            int bottom = target.getHeight() - insets.bottom;
            int left = insets.left;
            int right = target.getWidth() - insets.right;
            int lWidth = right-left;
            int lHeight = bottom-top;

            double scaleW = lWidth / (double)width;
            double scaleH = lHeight / (double)height;

            int fWidth;
            int fHeight;
            if (scaleW > scaleH) {
                fHeight = lHeight;
                fWidth = (int) Math.floor(width * scaleH);
            } else {
                fWidth = lWidth;
                fHeight = (int) Math.floor(height * scaleW);
            }
            int wMargin = (lWidth - fWidth)/2;
            int hMargin = (lHeight - fHeight)/2;

            for (Component c : target.getComponents()) {
                c.setSize(fWidth, fHeight);
                c.setBounds(left+wMargin, top+hMargin, fWidth, fHeight);
            }
        }
    }

}
