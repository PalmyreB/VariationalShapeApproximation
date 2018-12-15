import Jcg.geometry.Point_3;
import Jcg.polyhedron.Face;

public class TempFace extends Face<Point_3> implements Cloneable {

	Face<Point_3> copiedFace;

	public TempFace(Face<Point_3> f) {
		super();
		this.tag = f.tag;
		this.copiedFace = f;
	}

	@Override
	public TempFace clone() throws CloneNotSupportedException {
		return (TempFace) super.clone();
	}
}
