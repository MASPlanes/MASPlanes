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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class Display extends Frame {
    
    private static int DEFAULT_WIDTH = 500;
    private static int DEFAULT_HEIGHT = 500;
    
    /**
     * Offscreen buffer.
     */
    private BufferedImage buffer;
    private Graphics2D bufferGraphics;
    
    
    private boolean rebuildBuffer = false;
    
    public Display(World world) {
        Dimension d = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setPreferredSize(d);
        this.pack();
        this.setVisible(true);
        

        // Observador que s'encarrega de finalitzar el programa en
        // tancar la finestra principal.
        this.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            System.exit(0);
          }
        });
        
        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent ce) {
        
                // Bloquegem l'aspect ratio
                Dimension d  = Display.this.getSize();
                double ratio = d.getWidth() / d.getHeight();
                System.err.println("Ratio: " + ratio);
                if (ratio > 1) {
                  Display.this.setSize((int)(d.getWidth()/ratio), d.height);
                } else if (ratio < 1) {
                  Display.this.setSize(d.width, (int)(d.getHeight()*ratio));
                }
                
                //Display.this.buildBuffer();
                //rebuildBuffer = true;
                
                super.componentResized(ce);
            }
            
        });
        
        buildBuffer();
    }
//
//    @Override
//    public void paint(Graphics grphcs) {
//        super.paint(grphcs);
//        System.err.println("Repainted!");
//    }
//    
    
    
    private void buildBuffer() {
        createBufferStrategy(2);
        rebuildBuffer = false;
    }

}
