import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import Jcg.geometry.*;
import Jcg.polyhedron.*;

public class ShapeApproximation {

	public Polyhedron_3<Point_3> polyhedron3D;
	public ArrayList<List<Face<Point_3>>> partition;
	public Proxy[] proxies;
	public int k;

	public ShapeApproximation(Polyhedron_3<Point_3> polyhedron3D, int k) {
		this.polyhedron3D = polyhedron3D;
		this.k = k;
	}

	/**
	 * The main method performing the approximation process To be implemented
	 */
	public void approximate() {
		this.random_bootstrap();
		this.geometry_partitioning();
		this.proxy_fitting();
		// TODO: Meshing
	}

	/**
	 * In order to bootstrap the algorithm, the very first geometry partitioning
	 * picks k triangles at random on the object, and each of these triangles are
	 * assigned a proxy defined as the triangle’s barycenter and its normal.
	 */
	public void random_bootstrap() {
		for (Face<Point_3> face : this.polyhedron3D.facets)
			face.tag = -1;

		this.partition = new ArrayList<List<Face<Point_3>>>(k);
		for (int i = 0; i < k; i++) {
			this.partition.add(new ArrayList<Face<Point_3>>());
		}

		// Pick k random triangles
		final Random rnd = new Random();
		List<Face<Point_3>> randomFaces = new ArrayList<Face<Point_3>>(k);
		this.proxies = new Proxy[k];

		for (int i = 0; i < k; i++) {
			int v = 0;
			Face<Point_3> f;
			do {
				v = rnd.nextInt(this.polyhedron3D.sizeOfFacets());
				f = this.polyhedron3D.facets.get(v);
			} while (randomFaces.contains(f));
			randomFaces.add(f);
			this.proxies[i] = new Proxy(this.getBarycenterOfFace(f), this.getNormalOfFace(f));
			f.tag = i;
			this.partition.get(i).add(f);
		}

	}

	/**
	 * Geometry partitioning Knowing a current set of proxies P , we wish to update
	 * the partition R while trying to minimize the distortion error E(R, P ) in the
	 * process.
	 */
	public void geometry_partitioning() {

		DistortionErrorComparator comp = new DistortionErrorComparator(this);
		PriorityQueue<TempFace> firstSeedTriangles = new PriorityQueue<TempFace>(3 * this.polyhedron3D.sizeOfFacets(),
				(Comparator<? super Face<Point_3>>) comp);
		PriorityQueue<TempFace> seedTriangles = new PriorityQueue<TempFace>(3 * this.polyhedron3D.sizeOfFacets(),
				(Comparator<? super Face<Point_3>>) comp);

		// Initial seeding
		for (int i = 0; i < k; i++) {
			List<Face<Point_3>> region = this.partition.get(i);
			if (region.size() > 0) {
				double minDistortionError = -1;
				TempFace nearestFace = new TempFace(region.get(0));
				for (Face<Point_3> triangle : region) {
					double currentError = L21_metric(triangle, this.proxies[i]);
					triangle.tag = -1;
					if (minDistortionError == -1 || currentError < minDistortionError) {
						minDistortionError = currentError;
						nearestFace = new TempFace(triangle);
					}
				}
				nearestFace.tag = i;
				firstSeedTriangles.add(nearestFace);
			}
		}

		// Distortion minimizing flooding
		for (List<Face<Point_3>> region : this.partition)
			region.clear();

		seedTriangles.addAll(firstSeedTriangles);
		for (TempFace seedTriangle : firstSeedTriangles) {
			Halfedge<Point_3> h = seedTriangle.copiedFace.getEdge();
			Halfedge<Point_3> e = h.getNext();
			do {
				TempFace f = new TempFace(e.getOpposite().face);
				f.tag = seedTriangle.tag;
				seedTriangles.add(f);
				e = e.getNext();
			} while (!e.equals(h));
		}

		while (!seedTriangles.isEmpty()) {
			TempFace tempF = seedTriangles.poll();
			Face<Point_3> f = tempF.copiedFace;
			if (f.tag == -1) {
				f.tag = tempF.tag;
				this.partition.get(f.tag).add(f);
				Halfedge<Point_3> h = f.getEdge();
				Halfedge<Point_3> e = h.getNext();
				do {
					if (e.getOpposite().face.tag == -1) {
						TempFace g = new TempFace(e.getOpposite().face);
						g.tag = f.tag;
						seedTriangles.add(g);
					}
					e = e.getNext();
				} while (!e.equals(h));
			}

		}

	}

	private void proxy_fitting() {
		for (int i = 0; i < k; i++) {
			List<Face<Point_3>> region = this.partition.get(i);
			int n = region.size();
			Vector_3 N = new Vector_3(0, 0, 0);
			Point_3[] barycenters = new Point_3[n];
			for (int j = 0; j < n; j++) {
				Face<Point_3> f = region.get(j);
				barycenters[j] = this.getBarycenterOfFace(f);
				N = N.sum(this.getNormalOfFace(f).multiplyByScalar(this.areaOfTriangle(f)));
			}
			this.proxies[i].X.barycenter(barycenters);
			if ((double) N.squaredLength() != 0)
				this.proxies[i].N = N.divisionByScalar(Math.sqrt((double) N.squaredLength()));
		}
	}

	public double L2_metric(Face<Point_3> surface, Proxy proxy) {
		throw new RuntimeException("Not implemented yet");
	}

	public double L21_metric(Face<Point_3> surface, Proxy proxy) {
		// Distance between the normals
		Point_3 x = this.getBarycenterOfFace(surface);
		Vector_3 n = this.getNormalOfFace(surface);
		Vector_3 prod = n.crossProduct(proxy.N);
		double norm = Math.sqrt((double) prod.squaredLength());
		if (norm == 0.) {
			double numerator = (double) x.distanceFrom(proxy.X);
			double denominator = Math.sqrt((double) n.squaredLength() + 1);
			return numerator / denominator;
		}
		prod = prod.divisionByScalar(norm);
		Vector_3 diff = (Vector_3) x.minus(proxy.X);
		double distance = Math.abs((double) prod.innerProduct(diff));

		double A = this.areaOfTriangle(surface);

		return A * Math.pow(distance, 2);
	}

	/**
	 * Area calculated thanks to Heron's formula
	 * 
	 * @param triangle
	 * @return
	 */
	public double areaOfTriangle(Face<Point_3> triangle) {
		double S; // semiperimeter
		double A; // area
		double a, b, c; // side lengths of triangle

		if (triangle instanceof TempFace)
			triangle = ((TempFace) triangle).copiedFace;

		int[] vertexIndices = triangle.getVertexIndices(this.polyhedron3D);
		Point_3[] points = new Point_3[3];
		for (int i = 0; i < 3; i++)
			points[i] = this.polyhedron3D.vertices.get(vertexIndices[i]).getPoint();
		a = (double) points[0].distanceFrom(points[1]);
		b = (double) points[1].distanceFrom(points[2]);
		c = (double) points[0].distanceFrom(points[2]);
		S = 0.5 * (a + b + c);
		A = Math.sqrt(S * (S - a) * (S - b) * (S - c));

		return A;
	}

	private Point_3 getBarycenterOfFace(Face<Point_3> surface) {
		if (surface instanceof TempFace)
			surface = ((TempFace) surface).copiedFace;
		int[] vertexIndices = surface.getVertexIndices(this.polyhedron3D);
		Point_3 barycenter = new Point_3();
		Point_3[] points = new Point_3[vertexIndices.length];
		for (int i = 0; i < vertexIndices.length; i++)
			points[i] = this.polyhedron3D.vertices.get(vertexIndices[i]).getPoint();
		barycenter.barycenter(points);
		return barycenter;
	}

	private Vector_3 getNormalOfFace(Face<Point_3> surface) {
		if (surface instanceof TempFace)
			surface = ((TempFace) surface).copiedFace;
		int[] vertexIndices = surface.getVertexIndices(this.polyhedron3D);
		if (vertexIndices.length < 3)
			throw new RuntimeException("Not a valid surface");
		Point_3[] points = new Point_3[3];
		for (int i = 0; i < 3; i++)
			points[i] = this.polyhedron3D.vertices.get(vertexIndices[i]).getPoint();
		Vector_3 V = new Vector_3(points[0], points[1]);
		Vector_3 W = new Vector_3(points[0], points[2]);
		Vector_3 N = V.crossProduct(W);
		N = N.divisionByScalar(Math.sqrt((double) N.squaredLength()));
		return N;
	}

}
