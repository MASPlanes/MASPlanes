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

import es.csic.iiia.planes.*;
import es.csic.iiia.planes.definition.DProblem;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.ImageCapabilities;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.VolatileImage;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class GUIWorld extends AbstractWorld {

    private static final int BUFFER_DIMENSION = 10;

    private Display display;
    private AffineTransform transform;
    public BlockingQueue<VolatileImage> graphicsQueue = new ArrayBlockingQueue<VolatileImage>(BUFFER_DIMENSION);
    private double displayEvery = 100;
    public final Object displayEveryLock;
    private int steps = 0;

    public GUIWorld(Factory factory) {
        super(factory);
        this.displayEveryLock = new Object();
    }

    @Override
    public void run() {
        super.run();
        displayStep();
    }

    @Override
    public void computeStep() {
        super.computeStep();
    }

    @Override public void init(DProblem d) {
        super.init(d);
    }

    @Override
    protected void displayStep() {

        synchronized(displayEveryLock) {
            while(displayEvery == 0) {
                try {
                    displayEveryLock.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(GUIWorld.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (displayEvery < 1) {
                try {
                    displayEveryLock.wait((int)(100 - 100*displayEvery));
                } catch (InterruptedException ex) {}
            } else if (++steps >= displayEvery) {
                steps = 0;
            } else {
                return;
            }
        }

        // Fetch the display dimensions
        Dimension dd = display.getDisplayDimension();
        if (dd.height <= 0 || dd.width <= 0) {
            return;
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gs.getDefaultConfiguration();
        VolatileImage buf;
        try {
            buf = gc.createCompatibleVolatileImage(dd.width, dd.height, new ImageCapabilities(false), Transparency.TRANSLUCENT);
        } catch (AWTException ex) {
            return;
        }

        Graphics2D surface = buf.createGraphics();
        surface.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        surface.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        surface.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        surface.setBackground(new Color(255, 255, 255, 0));
        surface.clearRect(0, 0, dd.width, dd.height);

        dd = new Dimension(dd);
        Dimension wd = getSpace().getDimension();
        transform = new AffineTransform();
        transform.scale(dd.width/(wd.getWidth()+400), dd.height/(wd.getHeight()+400));
        transform.translate(200, 200);
        surface.setTransform(transform);

        // Draw recharge stations only if planes don't use an infinite battery
        if (!getFactory().getConfiguration().getBatteryClass().equals(InfiniteBattery.class)) {
            for (Station s : getStations()) {
                s.draw(surface);
            }
        }

        for (Operator o : getOperators()) {
            o.draw(surface);
        }
        for (Plane p : getPlanes()) {
            p.getDrawer().draw(surface);
        }
        for (Task t : getTasks()) {
            t.draw(surface);
        }

        surface.dispose();
        for (boolean ok = false; !ok;) {
            try {
                ok = graphicsQueue.offer(buf, 1, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {}
        }

        display.repaint();
    }

    public Plane getPlaneAt(Location l) {
        List<Plane> ps = getPlanes();
        for (int i=ps.size()-1; i>=0; i--) {
            final Plane p = ps.get(i);
            Location l2 = p.getLocation();
            double dx = l.getDistance(l2);
            if (dx < 200) {
                return p;
            }
        }
        return null;
    }

    public Task getTaskAt(Location l) {
        List<Task> ts = getTasks();
        for (int i=ts.size()-1; i>=0; i--) {
            final Task t = ts.get(i);
            Location l2 = t.getLocation();
            double dx = l.getDistance(l2);
            if (dx < 600) {
                return t;
            }
        }
        return null;
    }

    public Location screenToWorld(Point2D point) {
        try {
            AffineTransform t = transform.createInverse();
            return new Location(t.transform(point, point));
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(GUIWorld.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void setDisplay(Display d) {
        display = d;
    }

    private Plane selectedPlane;
    public void togglePlaneSelection(Plane p) {
        selectedPlane = p;
    }
    public Plane getSelectedPlane() {
        return selectedPlane;
    }

    void setDisplayEvery(double speed) {
        synchronized (displayEveryLock) {
            displayEvery = speed;
            displayEveryLock.notify();
        }
    }
}