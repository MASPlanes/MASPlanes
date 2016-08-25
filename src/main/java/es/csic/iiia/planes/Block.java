package es.csic.iiia.planes;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Guillermo on 12/12/2015.
 */
public class Block {

    private Location corner;

    private Location center;

    /**
     * Generator of unique identifiers.
     */
    private final static AtomicInteger idGenerator = new AtomicInteger();

    /**
     * Identifier of this task.
     */
    private final int id = idGenerator.incrementAndGet();

    public int getId() { return id; }

    private int xLoc, yLoc;

    public int getxLoc() {
        return xLoc;
    }

    public int getyLoc() {
        return yLoc;
    }

    public enum blockState {
        UNASSIGNED, ASSIGNED, EXPLORED, RE_EXPLORED
    }

    private blockState state;

    private int region;

    private Task survivor = null;

    public Block(int width, Location location, int xLoc, int yLoc, int regionID) {
        corner = location;
        center = new Location(corner.getX()-width/2.0, corner.getY()-width/2.0);
        this.xLoc = xLoc;
        this.yLoc = yLoc;
        this.region = regionID;
        state = blockState.UNASSIGNED;
    }

    public Location getCenter() { return center; }

    public Task getSurvivor() { return survivor; }

    public int getRegion() { return region; }

    public void setSurvivor(Task t) { this.survivor = t; }

    public boolean hasSurvivor() { return survivor != null; }

    public blockState getState() { return state; }

    public void setState(blockState state) { this.state = state; }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
//        buf.append("Task[").append(id).append("](").append(getLocation().getX())
//                .append(",").append(getLocation().getY()).append(")");
        buf.append("Block[").append(id).append("]");
        return buf.toString();
    }


}
