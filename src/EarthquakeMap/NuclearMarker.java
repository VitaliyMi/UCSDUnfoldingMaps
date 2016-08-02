package EarthquakeMap;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PGraphics;

public class NuclearMarker extends CommonMarker{

	public NuclearMarker(Location location) {
		super(location);
		// TODO Auto-generated constructor stub
	}
	
	public NuclearMarker(Feature feature) {
		super(((PointFeature)feature).getLocation(), feature.getProperties());
		// TODO Auto-generated constructor stub
	}
	

	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		
		pg.pushStyle();
		float radius = 17;
		float p1 = (float) 1.04;
		float p2 = p1*2;
		float p3 = p1*3;
		float p4 = p1*4;
		float p5 = p1*5;
		float p6 = p1*6;
		
		pg.fill(255,225,0);
		pg.arc(x, y, radius, radius, 0, p1);
		pg.arc(x, y, radius, radius, p2, p3);
		pg.arc(x, y, radius, radius, p4, p5);
		
		pg.fill(0,0,0);
		pg.arc(x, y, radius, radius, p1, p2);
		pg.arc(x, y, radius, radius, p3, p4);
		pg.arc(x, y, radius, radius, p5, p6);
		pg.stroke(255,225,0);
		pg.strokeWeight(2);
		pg.ellipse(x,y, 5, 5);
		pg.noStroke();
		pg.popStyle();
	}

	@Override
	public void showTitle(PGraphics pg, float x, float y) {
		pg.fill(255,255,255);
		pg.rect(x, y, 100, 25, 5);
		pg.fill(0,0,0);
		pg.text(this.getName(), x+2, y+15);
	}
	
	
	private String getName(){
		return getStringProperty("name");
	}

}
