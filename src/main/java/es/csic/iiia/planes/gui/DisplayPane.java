/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2012 Marc Pujol <mpujol@iiia.csic.es>.
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

import es.csic.iiia.planes.Location;
import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.Task;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
class DisplayPane extends JComponent {

    private GUIWorld world;
    private Image lastImage;

    public DisplayPane(GUIWorld w) {
        world = w;

        this.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseReleased(final MouseEvent me) {

                // Select or deselect a Plane
                Location l = world.screenToWorld(new Point2D.Double(me.getX(), me.getY()));
                Plane p = world.getPlaneAt(l);
                world.togglePlaneSelection(p);

                // If no plane is selected, try to find a task to give its information
                if (p == null) {
                    Task t = world.getTaskAt(l);
                    if (t != null) {
                        System.err.println(t);
                    }
                }

            } // End of 'mouseReleased(MouseEvent)' method
        });

    }

    @Override
    public void paintComponent(Graphics grphcs) {
        Image img = world.graphicsQueue.poll();
        if (img != null) {
            lastImage = img;
        }

        if (lastImage != null) {
            grphcs.drawImage(lastImage, 0, 0, null);
        }
    }

    @Override
    public void setSize(int width, int height) {
        world.graphicsQueue.clear();
        super.setSize(width, height);
    }

}