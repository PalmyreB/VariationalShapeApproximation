import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import Jcg.geometry.*;
import Jcg.polyhedron.Face;
import Jcg.polyhedron.Halfedge;
import Jcg.polyhedron.Polyhedron_3;
import Jcg.polyhedron.Vertex;

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
	public void approximate() {
		// First k-partitioning
		// k vertices are randomly selected
		
		// TODO: choose k!
		this.k_partitioning(4);
		
		// Knowing a current set of proxies P , we wish to update the partition
		// R while trying to minimize the distortion error E(R, P ) in the
		// process.
		//geometry partitioning
		//this.geometry_partitioning();
		
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
	private void k_partitioning(int k) {
		
		// Pick k random triangles
		final Random rnd = new Random();
		final List<Face<Point_3>> randomFaces = new ArrayList<Face<Point_3>>(k);
		
		proxies = new Proxy[k];
	    for (int i = 0; i < k; i++) {
	        int v = 0;
	        Face<Point_3> f;
	        do {
	            v = rnd.nextInt(this.polyhedron3D.sizeOfFacets());
	            f = this.polyhedron3D.facets.get(v);
	        } while (randomFaces.contains(f));
	        randomFaces.add(f);
	        this.proxies[i] = new Proxy(this.getBarycenterOfFace(f));
	        f.tag = i;
	    }
		
	    // Partition surface into k regions
	    this.partition = new ArrayList<List<Face<Point_3>>>(k);
	    for(int i = 0; i < k; i++) {
	    	this.partition.add(new ArrayList<Face<Point_3>>());
	    }
	    System.out.println(partition.size());
	    for(Face<Point_3> face : this.polyhedron3D.facets) {
	    	if(this.partition.size() > face.tag)
	    		this.partition.get(face.tag).remove(face);
	    	double minDistance = this.barycenter_metric(face, randomFaces.get(0));
	    	for(Face<Point_3> randomFace : randomFaces) {
	    		double distance = this.barycenter_metric(face, randomFace);
	    		if(distance < minDistance) {
	    			minDistance = distance;
	    			face.tag = randomFace.tag;
	    		}
	    	}
	    	this.partition.get(face.tag).add(face);
	    }
	    
	}
	
	private void geometry_partitioning() {
		
		
		// Initial seeding
		Face<Point_3> nearestFace;
		float minDistortionError;
		float currentError;
		for(List<Face<Point_3>> region : partition) {
			minDistortionError = -1;
			for(Face<Point_3> triangle : region) {
				currentError = L21_metric(triangle, this.proxies[triangle.tag]);
				if(currentError < minDistortionError || minDistortionError == -1) {
					minDistortionError = currentError;
					nearestFace = triangle;
				}
			}
			
			for(Face<Point_3> triangle : region) {
				triangle.tag = -1;
			}
			// TODO nearestFace.tag = proxy
		}
		throw new RuntimeException("Not yet implemented");
	}
	
	private void proxy_fitting() {
		throw new RuntimeException("Not yet implemented");
	}
	
	/**
	 * Distance between the barycenters of two surfaces.
	 * Used ini first approximation.
	 */
	private double barycenter_metric(Face<Point_3> surface1, Face<Point_3> surface2) {
		Point_3 barycenter1 = this.getBarycenterOfFace(surface1);
		Point_3 barycenter2 = this.getBarycenterOfFace(surface2);
		return (double)barycenter1.distanceFrom(barycenter2);
	}
	
	private float L2_metric(Face<Point_3> surface, Proxy proxy) {
		throw new RuntimeException("Not yet implemented");
	}
	
	private float L21_metric(Face<Point_3> surface, Proxy proxy) {
		throw new RuntimeException("Not yet implemented");
	}
	
	private Point_3 getBarycenterOfFace(Face<Point_3> surface) {
		int[] vertexIndices = surface.getVertexIndices(this.polyhedron3D);
		Point_3 barycenter = new Point_3();
		Point_3[] points = new Point_3[vertexIndices.length];
		for(int i=0; i < vertexIndices.length; i++)
			points[i] = this.polyhedron3D.vertices.get(vertexIndices[i]).getPoint();
		barycenter.barycenter(points);
		return barycenter;
	}

}
