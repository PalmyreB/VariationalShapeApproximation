import Jcg.geometry.*;
import Jcg.polyhedron.Face;
import Jcg.polyhedron.Halfedge;
import Jcg.polyhedron.Polyhedron_3;
import Jcg.polyhedron.Vertex;

/**
 * Abstract class defining methods for mesh subdivision
 *  
 * @author Luca Castelli Aleardi (INF555, 2012)
 *
 */
public abstract class MeshSubdivision {

	public Polyhedron_3<Point_3> polyhedron3D;
	
	public MeshSubdivision(Polyhedron_3<Point_3> polyhedron3D) {
		this.polyhedron3D=polyhedron3D;
	}

	/**
	 * The main method performing the subdivision process
	 * To be implemented
	 */
	public abstract void subdivide();

}
