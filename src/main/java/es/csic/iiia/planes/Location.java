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

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Represents a point in the world's space.
 *
 * @see World
 * @see Space
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class Location extends Point2D {

    private double x;
    private double y;

    public Location(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Location(Point2D point) {
        this.x = point.getX();
        this.y = point.getY();
    }

    /**
     * Moves this location towards the given destination, at speed meters per
     * millisecond.
     *
     * param destination
     * param speed
     * @return true if the destination has been reached, false otherwise.
     *
    public boolean move(Location destination, double speed) {
        if (destination == null) {
            return true;
        }

        double dx = destination.x - x;
        double dy = destination.y - y;
        double alpha = Math.atan2(dy, dx);
        double incx = speed * Math.cos(alpha);
        double incy = speed * Math.sin(alpha);

        if (Math.abs(incx) >= Math.abs(dx) &&
            Math.abs(incy) >= Math.abs(dy)){
            x = destination.x;
            y = destination.y;
            return true;
        }

        if (Math.abs(incx) >= Math.abs(dx)) {
            x = destination.x;
        } else {
            x += incx;
        }
        if (Math.abs(incy) >= Math.abs(dy)) {
            y = destination.y;
        } else {
            y += incy;
        }

        return false;
    }*/

    /**
     * Characterizes the blocks that the simulation space is broken into by calculating
     * the corners of all blocks given the height, width and block side-length.
     *
     * @param blockSize side length of blocks
     * @param widthRegions width of scenario space in regions
     * @param heightRegions height of scenario space in regions
     * @return a 2-D array of Locations that identify the locations of each block's corners.
     *
     */
    public static Location[][] buildBlocks(int blockSize, int widthRegions, int heightRegions){
        double locX, locY;
        locX = 0;
        locY = 0;

        int blocksWidth, blocksHeight, numBlocks;
        blocksWidth = widthRegions*3;
        blocksHeight = heightRegions*3;
        //numBlocks = blocksHeight*blocksWidth;

        //Location[][] corners = new Location[2][numBlocks];
        Location[][] blocks = new Location[blocksWidth][blocksHeight];

        //int index = 0;
        for (int i = 0; i < blocksWidth; i++) {
            for (int j = 0; j < blocksHeight; j++) {
                blocks[i][j] = new Location(locX+blockSize, locY+blockSize);
                //corners[0][index] = new Location(locX, locY);
                //corners[1][index] = new Location(locX+blockSize, locY+blockSize);
                locY += blockSize;
                //index++;
            }
            locX += blockSize;
            locY = 0;
        }

        return blocks;
    }

    /**
     * Characterizes the regions that the simulation space is broken into by calculating
     * the corners of all regions given the height, width and block side-length.
     *
     * @param blockSize side length of blocks
     * @param widthRegions width of scenario space in regions
     * @param heightRegions height of scenario space in regions
     * @return an array of Locations that identify the locations of each region's corners.
     *
     */
    public static ArrayList<Region> buildRegions(int blockSize, int widthRegions, int heightRegions){
        double locX, locY;
        locX = 0;
        locY = 0;

        ArrayList<Region> regions = new ArrayList<Region>();

        int regionID = 0;
        for (int i = 0; i < widthRegions; i++) {
            for (int j = 0; j < heightRegions; j++) {
                Region r = new Region(new Location(locX, locY), regionID, i, j, blockSize);
                regions.add(r);
                locY += 3*blockSize;
                regionID++;
            }
            locX += 3*blockSize;
            locY = 0;
        }

        return regions;
    }

    public static Block[][] buildBlocks(ArrayList<Region> regions, int blockSize) {
        Block[][] blocks = new Block[regions.size()][9];

        int index = 0;
        for (Region r: regions) {
            double locX = r.getCorner().getX();
            double locY = r.getCorner().getY();

            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    int xLoc = r.getyLoc()*3 + j;
                    int yLoc = r.getxLoc()*3 + k;
                    Location blockLoc = new Location(locX+blockSize,locY+blockSize);
                    blocks[r.getID()][index] = new Block(blockSize, blockLoc, xLoc, yLoc, r.getID());
                    locY += blockSize;
                    index++;
                }
                locX += blockSize;
                locY = r.getCorner().getY();
            }
            index = 0;
        }

        return blocks;
    }

    /**
     * Continue advancing along a MoveStep plan built with 
     * {@link #buildMoveStep(Location, double)}.
     * 
     * @param step movestep plan to follow.
     * @return True if the destination has been reached, or false otherwise.
     */
    public boolean move(MoveStep step) {
        return step.move();
    }

    /**
     * Returns the movestep plan to reach the desired destination.
     * <p/>
     * Because it is usually needed to advance during multiple steps (tenths of
     * second) to reach the desired destination, this function returns a
     * MoveStep object. Then, the subsequent steps can be made using
     * {@link #move(es.csic.iiia.planes.Location.MoveStep)}, which is more
     * efficient.
     *
     * @see MoveStep
     * @param destination
     * @param speed
     * @return MoveStep to reach the given destination.
     */
    public MoveStep buildMoveStep(Location destination, double speed) {
        if (destination == null) {
            return null;
        }

        return new MoveStep(destination, speed);
    }

    @Override
    public double getX() {
        return x;
    }
    public int getXInt() {
        return (int)x;
    }

    @Override
    public double getY() {
        return y;
    }
    public int getYInt() {
        return (int)y;
    }

    public double getDistance(Location l) {
        double dx = x - l.x;
        double dy = y - l.y;
        return Math.sqrt(dx*dx + dy*dy);
    }

    public boolean within(double range, Location l) {
        double dx = x - l.x;
        if (dx > range) {
            return false;
        }

        double dy = y - l.y;
        if (dy > range) {
            return false;
        }

        return Math.sqrt(dx*dx + dy*dy) >= range;
    }

    @Override public String toString() {
        return "(" + getXInt() + "," + getYInt() + ")";
    }

    public double getAngle(Location destination) {
        double dx = x - destination.x;
        double dy = y - destination.y;
        return Math.atan2(dy, dx);
    }

    @Override
    public void setLocation(double d, double d1) {
        x = d;
        y = d1;
    }

    public class MoveStep {
        public final double dx;
        public final double dy;
        public final double alpha;
        public final double incx;
        public final double incy;
        public final Location destination;
        public int steps;

        public MoveStep(Location destination, double speed) {
            this.destination = destination;
            dx = destination.x - x;
            dy = destination.y - y;
            alpha = Math.atan2(dy, dx);
            incx = speed * Math.cos(alpha);
            incy = speed * Math.sin(alpha);

            final double distance = getDistance(destination);
            steps = (int)Math.ceil(distance/speed);
        }

        protected boolean move() {
            steps--;
            if (steps <= 0) {
                x = destination.x;
                y = destination.y;
                return true;
            }

            x += incx;
            y += incy;
            return false;
        }
    }
}