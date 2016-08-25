package es.csic.iiia.planes.operator_behavior;

import es.csic.iiia.planes.Location;
import es.csic.iiia.planes.Operator;
import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.World;
import java.util.List;

/**
 * Created by Guillermo on 12/12/2015.
 */
public class SendAll implements OperatorStrategy {

    @Override
    public boolean submitTask(World w, Operator o, Task t) {
        final List<Plane> planes = w.getPlanes();

        for (Plane p : planes) {
            p.addSearchTask(t);
        }
        return true;
    }

}
