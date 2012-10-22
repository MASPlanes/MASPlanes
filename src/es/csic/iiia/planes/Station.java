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
package es.csic.iiia.planes;

import es.csic.iiia.planes.graphics.PlaneGraphic;
import es.csic.iiia.planes.graphics.StationGraphic;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class Station extends AbstractDrawable {
    
    final private static AtomicInteger idGenerator = new AtomicInteger();
    final int id = idGenerator.incrementAndGet();
    
    public Station(Location l) {
        super(l);
    }
    
    @Override
    public void draw(Graphics2D g) {
        int x = location.getXInt();
        int y = location.getYInt();
        Color previousColor = g.getColor();
        Stroke previousStroke = g.getStroke();
        AffineTransform previousTransform = g.getTransform();
        
        // The background circle
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new BasicStroke(40f));
        g.fillOval(x-250,y-250,500,500);
        
        // The inner circle
        g.setColor(new Color(255,210,0));
        g.drawOval(x-190, y-190, 380, 380);
        
        // The power graphic
        AffineTransform t = new AffineTransform(previousTransform);
        t.translate(x-250, y-250);
        t.scale(500, 500);
        g.setTransform(t);
        g.fill(StationGraphic.getImage());
        
        g.setTransform(previousTransform);
        g.setColor(previousColor);
        g.setStroke(previousStroke);
    }
    
}
