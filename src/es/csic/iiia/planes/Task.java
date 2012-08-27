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
package es.csic.iiia.planes;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class Task extends AbstractDrawable {
    
    private final static AtomicInteger idGenerator = new AtomicInteger();
    private final int id = idGenerator.incrementAndGet();
    
    private long submissionTime;
    
    public Task(Location location) {
        super(location);
    }
    
    @Override
    public void initialize() {
        submissionTime = getWorld().getTime();
    }
    
    public long getSubmissionTime() {
        return submissionTime;
    }
    
    public int getId() {
        return id;
    }

    @Override
    public void draw(Graphics2D g) {
        int x = location.getXInt();
        int y = location.getYInt();
        
        Color previous = g.getColor();
        g.setColor(Color.BLUE);
        g.fillOval(x-10, y-10, 20, 20);
        
        
        Font f = new Font(Font.SANS_SERIF, Font.BOLD, 8);
        String sid = String.valueOf(id);
        g.setFont(f);
        FontMetrics m = g.getFontMetrics(f);
        int w = m.stringWidth(sid);
        int h = m.getHeight()-2;
        g.setColor(Color.WHITE);
        g.drawString(sid, x-(w/2), y+(h/2));
        g.setColor(previous);
    }
    
}
