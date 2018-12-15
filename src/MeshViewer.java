import processing.core.*;

import Jcg.geometry.*;
import Jcg.polyhedron.*;

/**
 * A simple 3d viewer for visualizing surface meshes (based on Processing)
 * 
 * @author Luca Castelli Aleardi (INF555, 2012)
 *
 */
public class MeshViewer extends PApplet {
	SurfaceMesh mesh; // 3d surface mesh
	ShapeApproximation sa;
	int renderType = 1; // choice of type of rendering
	int renderModes = 3; // number of rendering modes
	int k = 50; // number of regions of partition

	// String filename="OFF/high_genus.off";
	// String filename="OFF/sphere.off";
	// String filename="OFF/cube.off";
	// String filename="OFF/torus_33.off";
	// String filename="OFF/tore.off";
	// String filename="OFF/tri_hedra.off";
	// String filename="OFF/letter_a.off";
	// String filename="OFF/star.off";
	String filename = "OFF/tri_triceratops.off";
	// String filename="OFF/twisted.off";
	// String filename="OFF/tri_gargoyle.off";

	public void setup() {
		size(800, 600, P3D);
		ArcBall arcball = new ArcBall(this);

		this.mesh = new SurfaceMesh(this, filename);
		this.sa = new ShapeApproximation(this.mesh.polyhedron3D, this.k);

//		  System.out.println(""+ms.polyhedron3D.facesToString());
//		  ms.subdivide();
//		  ms.polyhedron3D.isValid(false);
//		  ms.subdivide();
//		  ms.polyhedron3D.isValid(false);
//		  ms.subdivide();
//		  ms.polyhedron3D.isValid(false);
	}

	public void draw() {
		background(0);
		this.lights();
//		  directionalLight(101, 204, 255, -1, 0, 0);
//		  directionalLight(51, 102, 126, 0, -1, 0);
//		  directionalLight(51, 102, 126, 0, 0, -1);
//		  directionalLight(102, 50, 126, 1, 0, 0);
//		  directionalLight(51, 50, 102, 0, 1, 0);
//		  directionalLight(51, 50, 102, 0, 0, 1);

		translate(width / 2.f, height / 2.f, -1 * height / 2.f);
		this.strokeWeight(1);
		stroke(150, 150, 150);

		this.mesh.draw(renderType);
	}

	public void keyPressed() {
		switch (key) {
		case ('a'):
		case ('A'):
			this.approximate();
			break;
		case ('p'):
		case ('P'):
			this.partition();
			break;
		case ('r'):
			this.renderType = (this.renderType + 1) % this.renderModes;
			break;
		}
	}

	public void approximate() {
		this.sa.approximate();
		this.mesh.updateScaleFactor();
		this.mesh.polyhedron3D.isValid(false);
	}

	public void partition() {
		this.sa.k_partitioning();
		this.mesh.updateScaleFactor();
		this.mesh.polyhedron3D.isValid(false);
	}

	/**
	 * For running the PApplet as Java application
	 */
	public static void main(String args[]) {
		// PApplet pa=new MeshViewer();
		// pa.setSize(400, 400);
		PApplet.main(new String[] { "MeshViewer" });
	}

}
