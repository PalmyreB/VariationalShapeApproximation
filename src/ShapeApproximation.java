import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import Jcg.geometry.*;
import Jcg.polyhedron.*;

/**
 * 
 *
 */
public class ShapeApproximation {

	public Polyhedron_3<Point_3> polyhedron3D;
	
	// TODO: create a class region?
	// extension of List<Face<Point_3>> with proxy
	public ArrayList<List<Face<Point_3>>> partition;
	
	// TODO: create class for proxies
	// extension of face with normal and barycenter?
	public Proxy[] proxies;
	
	public ShapeApproximation(Polyhedron_3<Point_3> polyhedron3D) {
		this.polyhedron3D=polyhedron3D;
	}

	/**
	 * The main method performing the approximation process
	 * To be implemented
	 */
	public void approximate(int k) {
		// First k-partitioning
		// k vertices are randomly selected
		
		// TODO: choose k!
		this.k_partitioning(k);
		
		// Knowing a current set of proxies P , we wish to update the partition
		// R while trying to minimize the distortion error E(R, P ) in the
		// process.
		//geometry partitioning
		this.geometry_partitioning(k);
		
		// proxy fitting
		//this.proxy_fitting();
		
		
		// meshing
	}
	
	/**
	 * In order to bootstrap the algorithm, the very first geometry
	 * partitioning picks k triangles at random on the object, and
	 * each of these triangles are assigned a proxy defined as the
	 * triangle’s barycenter and its normal.
	 */
	public void k_partitioning(int k) {
		
		for(Face<Point_3> face : this.polyhedron3D.facets)
			face.tag  = -1;
		// Pick k random triangles
		final Random rnd = new Random();
		final List<Face<Point_3>> randomFaces = new ArrayList<Face<Point_3>>(k);
		
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
	    }
		
	    // Partition surface into k regions
	    this.partition = new ArrayList<List<Face<Point_3>>>(k);
	    for(int i = 0; i < k; i++) {
	    	this.partition.add(new ArrayList<Face<Point_3>>());
	    }
	    for(Face<Point_3> face : this.polyhedron3D.facets) {
	    	if(face.tag != -1 && this.partition.size() > face.tag)
	    		this.partition.get(face.tag).remove(face);
	    	double minDistance = -1;
	    	for(Face<Point_3> randomFace : randomFaces) {
	    		double distance = this.barycenter_metric(face, randomFace);
	    		if(minDistance == -1 || distance < minDistance) {
	    			minDistance = distance;
	    			face.tag = randomFace.tag;
	    		}
	    	}
	    	this.partition.get(face.tag).add(face);
	    }
	    
	}
	
	private void geometry_partitioning(int k) {
		
		// Initial seeding
		Face<Point_3> nearestFace;
		List<Face<Point_3>> seedTriangles = new ArrayList<Face<Point_3>>(k); 
		double minDistortionError;
		double currentError;
		List<Face<Point_3>> region;
		
		for(int i = 0; i < this.partition.size(); i++) {
			region = this.partition.get(i);
			minDistortionError = -1;
			nearestFace = region.get(0);
			for(Face<Point_3> triangle : region) {
				currentError = L21_metric(triangle, this.proxies[i]);
				triangle.tag = -1;
				if(currentError < minDistortionError || minDistortionError == -1) {
					minDistortionError = currentError;
					nearestFace = triangle;
				}
			}
			nearestFace.tag = i;
			seedTriangles.add(nearestFace);
		}
		
		// Distortion minimizing flooding
		this.partition = new ArrayList<List<Face<Point_3>>>(k);
		for(int i = 0; i < k; i++) {
	    	this.partition.add(new ArrayList<Face<Point_3>>());
	    }
		DistortionErrorComparator comp = new DistortionErrorComparator(this);
		PriorityQueue<TempFace> neighborFaces = new PriorityQueue<TempFace>(3*this.polyhedron3D.sizeOfFacets(), (Comparator<? super Face<Point_3>>) comp);
		for(Face<Point_3> seedTriangle: seedTriangles) {
			Halfedge<Point_3> h = seedTriangle.getEdge();
			Halfedge<Point_3> e = h.getNext();
			do {
				TempFace f = new TempFace(e.getOpposite().face);
				f.tag = seedTriangle.tag;
				neighborFaces.add(f);
				e = e.getNext();
			}while(!e.equals(h));
		}
		
		while(!neighborFaces.isEmpty()) {
			TempFace tempF = neighborFaces.poll();
			Face<Point_3> f = tempF.copiedFace;
			if(f.tag != -1) {
				f.tag = tempF.tag;
				this.partition.get(tempF.tag).add(f);
				Halfedge<Point_3> h = f.getEdge();
				Halfedge<Point_3> e = h.getNext();
				do {
					if(e.getOpposite().face.tag == -1) {
						TempFace g = new TempFace(e.getOpposite().face);
						g.tag = f.tag;
						neighborFaces.add(g);
					}
					e = e.getNext();
				}while(!e.equals(h));
			}
				
		}
	}
	
	private void proxy_fitting() {
		throw new RuntimeException("Not implemented yet");
	}
	
	/**
	 * Distance between the barycenters of two surfaces.
	 * Used in first approximation.
	 */
	private double barycenter_metric(Face<Point_3> surface1, Face<Point_3> surface2) {
		Point_3 barycenter1 = this.getBarycenterOfFace(surface1);
		Point_3 barycenter2 = this.getBarycenterOfFace(surface2);
		return (double)barycenter1.distanceFrom(barycenter2);
	}
	
	public double barycenter_proxy_metric(Face<Point_3> surface, Proxy proxy) {
		Point_3 barycenter1 = this.getBarycenterOfFace(surface);
		return (double)barycenter1.distanceFrom(proxy.X);
	}
	
	public double L2_metric(Face<Point_3> surface, Proxy proxy) {
		throw new RuntimeException("Not implemented yet");
	}
	
	public double L21_metric(Face<Point_3> surface, Proxy proxy) {
		// DIstance between the normals
		Point_3 x = this.getBarycenterOfFace(surface);
		Vector_3 n = this.getNormalOfFace(surface);
		Vector_3 prod = n.crossProduct(proxy.N);
		double norm = Math.sqrt((double) prod.squaredLength());
		if(norm == 0.) {
			double numerator = (double) x.distanceFrom(proxy.X);
			double denominator = Math.sqrt((double) n.squaredLength() + 1);
			return numerator / denominator;
		}
		prod = prod.divisionByScalar(norm);
		Vector_3 diff = (Vector_3) x.minus(proxy.X);
		double distance = Math.abs((double)prod.innerProduct(diff));

		// Area calculated thanks to Heron's formula
		double S; // semiperimeter
		double A; // area
		double a, b, c; // side lengths of triangle

		if(surface instanceof TempFace)
			surface = ((TempFace)surface).copiedFace;

		int[] vertexIndices = surface.getVertexIndices(this.polyhedron3D);
		Point_3[] points = new Point_3[3];
		for(int i=0; i < 3; i++)
			points[i] = this.polyhedron3D.vertices.get(vertexIndices[i]).getPoint();
		a = (double) points[0].distanceFrom(points[1]);
		b = (double) points[1].distanceFrom(points[2]);
		c = (double) points[0].distanceFrom(points[2]);
		S = 0.5 * (a+b+c);
		A = Math.sqrt(S*(S-a)*(S-b)*(S-c));

		return A*Math.pow(distance, 2);
	}

	private double region_L21_metric(List<Face<Point_3>> region, Proxy proxy) {
		double result = 0;
		for(Face<Point_3> triangle : region)
			result += this.L21_metric(triangle, proxy);
		return result;
	}
	
	private Point_3 getBarycenterOfFace(Face<Point_3> surface) {
		if(surface instanceof TempFace)
			surface = ((TempFace)surface).copiedFace;
		int[] vertexIndices = surface.getVertexIndices(this.polyhedron3D);
		Point_3 barycenter = new Point_3();
		Point_3[] points = new Point_3[vertexIndices.length];
		for(int i=0; i < vertexIndices.length; i++)
			points[i] = this.polyhedron3D.vertices.get(vertexIndices[i]).getPoint();
		barycenter.barycenter(points);
		return barycenter;
	}
	
	private Vector_3 getNormalOfFace(Face<Point_3> surface) {
		if(surface instanceof TempFace)
			surface = ((TempFace)surface).copiedFace;
		int[] vertexIndices = surface.getVertexIndices(this.polyhedron3D);
		if(vertexIndices.length < 3)
			throw new RuntimeException("Not a valid surface");
		Point_3[] points = new Point_3[3];
		for(int i=0; i < 3; i++)
			points[i] = this.polyhedron3D.vertices.get(vertexIndices[i]).getPoint();
		Vector_3 V = new Vector_3(points[0], points[1]);
		Vector_3 W = new Vector_3(points[0], points[2]);
		Vector_3 N = V.crossProduct(W);
		N = N.divisionByScalar(Math.sqrt((double)N.squaredLength()));
		return N;
	}

}
