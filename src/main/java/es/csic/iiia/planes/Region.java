package es.csic.iiia.planes;

/**
 * Class that describes a region, its location and state in space,
 * its identifier (for faster lookup), and positioning
 * relative to other regions.
 * Created by Guillermo Bautista on 12/12/2015.
 */
public class Region {

    private Location corner;

    private Location center;

    public enum regionState {
        UNASSIGNED, ASSIGNED, EXPLORED, RE_EXPLORED
    }

    private regionState state;

    private int id;

    private int xLoc, yLoc;

    private int tasksFound;

    public Region(Location location, int id, int xLoc, int yLoc, int blockSize) {
        corner = location;
        center = new Location(corner.getX()+1.5*blockSize, corner.getY()+1.5*blockSize);

        state = regionState.UNASSIGNED;
        this.id = id;
        this.xLoc = xLoc;
        this.yLoc = yLoc;
        tasksFound = 0;
    }


    public Location getCorner() { return corner; }

    public Location getCenter() { return center; }

    public void taskFound() { tasksFound++; }

    public int getTasksFound() { return tasksFound; }

    public int getID()  { return id; }

    public int getyLoc() {
        return yLoc;
    }

    public int getxLoc() {
        return xLoc;
    }

    public regionState getState() { return state; }

    public void setState(regionState state) { this.state = state; }
}
