import Jcg.geometry.Point_3;
import Jcg.geometry.Vector_3;

public class Proxy {
	Point_3 X; // Center
	Vector_3 N; // Normal

	public Proxy(Point_3 X, Vector_3 N) {
		this.X = X;
		this.N = N;
	}
}
