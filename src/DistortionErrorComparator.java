import java.util.Comparator;

import Jcg.geometry.*;
import Jcg.polyhedron.*;

public class DistortionErrorComparator implements Comparator<Face<Point_3>> {

	ShapeApproximation sa;
	public DistortionErrorComparator(ShapeApproximation sa) {
		this.sa = sa;
	}
    @Override
    public int compare(Face<Point_3> f, Face<Point_3> g)
    {
    	double distanceF = sa.L21_metric(f, sa.proxies[f.tag]);
    	double distanceG = sa.L21_metric(g, sa.proxies[g.tag]);
        if (distanceF < distanceG)
            return -1;
        if (distanceF > distanceG)
            return 1;
        return 0;
    }
}