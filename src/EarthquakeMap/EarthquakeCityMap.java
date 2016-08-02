package EarthquakeMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;
import processing.core.PFont;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {
	
	// We will use member variables, instead of local variables, to store the data
	// that the setUp and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.
	
	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = false;
	
	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	

	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	private String stationsList = "nuclear-power-station.json";
	
	
	private boolean citySelected= true;
	// The map
	private static UnfoldingMap map;
	
	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;
	
	private List<Marker> nuclearStations = new ArrayList();	
	
	// NEW IN MODULE 5
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 750, 700, new MBTilesMapProvider(mbTilesString));
		    // earthquakesURL = "2.5_week.atom;  ";
		    // The same feed, but saved August 7, 2015
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
		    //earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// FOR TESTING: Set earthquakesURL to be one of the testing files by uncommenting
		// one of the lines below.  This will work whether you are online or offline
		//earthquakesURL = "test1.atom";
		//earthquakesURL = "test2.atom";
		
		// Uncomment this line to take the quiz
		//earthquakesURL = "quiz2.atom";
		
		
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    
	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  }
		  // OceanQuakes
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }
	    
	    List<Feature> nuclearPowerStations = GeoJSONReader.loadData(this, stationsList) ;

	    for(Feature station: nuclearPowerStations)
	    {
	    	nuclearStations.add(new NuclearMarker(station));
	    }
	    for(Marker marker : nuclearStations)
	    {
	    	marker.setHidden(true);
	    }
	    // could be used for debugging
	    //printQuakes();
	    sortAndPrint(100);
	 		
	    // (3) Add markers to map
	    //     NOTE: Country markers are not added to the map.  They are used
	    //           for their geometric properties
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    map.addMarkers(nuclearStations);
	    //System.out.print(nuclearStations);
	    
	}  // End setup
	
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
		drawButton();		
	}
	
	
	
	 //TODO: Add the method:
	// and then call that method from setUp
	
	private void sortAndPrint(int numToPrint){
		   ArrayList<EarthquakeMarker> sorted = new ArrayList<EarthquakeMarker>();
		   for(int i=0; i<quakeMarkers.size(); i++)
		   {
			   sorted.add((EarthquakeMarker) quakeMarkers.get(i));
		   }
		   Collections.sort(sorted);
		   int value;
		   if(numToPrint>=sorted.size()) value=sorted.size();
		   else value = numToPrint;
		   for(int i=0; i<value; i++)
		   {
			   System.out.println(sorted.get(sorted.size()-i-1).getMagnitude()+" impacted "+sorted.get(sorted.size()-i-1).getStringProperty("age") );
		   }
	   }
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()
	{
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		
		}
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
		selectMarkerIfHover(nuclearStations);
		//loop();
	}
	
	// If there is a marker selected 
	private void selectMarkerIfHover(List<Marker> markers)
	{
		// Abort if there's already a marker selected
		if (lastSelected != null) {
			return;
		}
		
		for (Marker m : markers) 
		{
			CommonMarker marker = (CommonMarker)m;
			if (marker.isInside(map,  mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}
	}
	
	/** The event handler for mouse clicks
	 * It will display an earthquake and its threat circle of cities
	 * Or if a city is clicked, it will display all the earthquakes 
	 * where the city is in the threat circle
	 */
	@Override
	public void mouseClicked()
	{
		System.out.println(map.getZoom());
		if(mouseX>=125 && mouseX<=175 && mouseY>=50 && mouseY<=100)
		{
			setCitySelected(!isCitySelected());
		
		if(citySelected)
		{
			for(Marker marker: nuclearStations)
			{
				marker.setHidden(true);
			}
			
			for(Marker marker: cityMarkers)
			{
				marker.setHidden(false);
			}
			
		}
		
		if(!citySelected)
		{
			for(Marker marker: cityMarkers)
			{
				marker.setHidden(true);
			}
			
			for(Marker marker: nuclearStations)
			{
				marker.setHidden(false);
			}
		}
		}		
		
		if (lastClicked != null) {
			unhideMarkers();
			lastClicked = null;
		}
		else if (lastClicked == null) 
		{
			checkEarthquakesForClick();
			if (lastClicked == null) 
			{
				checkCitiesForClick();
			}
			if(lastClicked==null)
			{
				checkNuclearStationsForClick();
			}
		}
		
		
	}
	
	// Helper method that will check if a city marker was clicked on
	// and respond appropriately
	private void checkCitiesForClick()
	{
		if (lastClicked != null) return;
		// Loop over the earthquake markers to see if one of them is selected
		for (Marker marker : cityMarkers) {
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker)marker;
				// Hide all the other earthquakes and hide
				for (Marker mhide : cityMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				for (Marker mhide : quakeMarkers) {
					EarthquakeMarker quakeMarker = (EarthquakeMarker)mhide;
					if (quakeMarker.getDistanceTo(marker.getLocation()) 
							> quakeMarker.threatCircle()) {
						quakeMarker.setHidden(true);
					}
				}
				return;
			}
		}		
	}
	
	// Helper method that will check if an earthquake marker was clicked on
	// and respond appropriately
	private void checkEarthquakesForClick()
	{
		if (lastClicked != null) return;
		// Loop over the earthquake markers to see if one of them is selected
		for (Marker m : quakeMarkers) {
			EarthquakeMarker marker = (EarthquakeMarker)m;
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = marker;
				// Hide all the other earthquakes and hide
				for (Marker mhide : quakeMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				for (Marker mhide : cityMarkers) {
					if (mhide.getDistanceTo(marker.getLocation()) 
							> marker.threatCircle()) {
						mhide.setHidden(true);
					}
				}
				for (Marker mhide : nuclearStations) {
					if (mhide.getDistanceTo(marker.getLocation()) 
							> marker.threatCircle()) {
						mhide.setHidden(true);
					}
				}
				return;
			}
		}
	}
	
	
	private void checkNuclearStationsForClick()
	{
		if(lastClicked !=null) return;
		
		for(Marker m : nuclearStations)
		{
			NuclearMarker marker = (NuclearMarker) m;
			if(!marker.isHidden()&& marker.isInside(map, mouseX, mouseY))
			{
				lastClicked = marker;
				for(Marker mhide : nuclearStations)
				{
					if(mhide != lastClicked)
					{
						mhide.setHidden(true);
					}
				}
				
				for(Marker mhide: quakeMarkers)
				{
					if(mhide.getDistanceTo(lastClicked.getLocation())>((EarthquakeMarker)mhide).threatCircle())
					{
						mhide.setHidden(true);
					}
				}
				return;
			}
			
		}
	}
	
	// loop over and unhide all markers
	private void unhideMarkers() {
		for(Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}
		if(citySelected)
			{
				for(Marker marker : cityMarkers) 
				{
					marker.setHidden(false);
				}
			}
		if(!citySelected)
		{
			for(Marker marker : nuclearStations)
			{
				marker.setHidden(false);
			}
		}
	}
	
	// helper method to draw key in GUI
	private void addKey() {	
		// Remember you can use Processing's graphics methods here
		fill(255, 250, 240);
		
		int xbase = 25;
		int ybase = 130;
		
		rect(xbase, ybase, 150, 250);
		
		int x = xbase+35;
		int y = ybase+50;
		float radius = 15;
		float p1 = (float) 1.04;
		float p2 = p1*2;
		float p3 = p1*3;
		float p4 = p1*4;
		float p5 = p1*5;
		float p6 = p1*6;
		pushStyle();			
		fill(255,225,0);
		arc(x, y, radius, radius, 0, p1);
		arc(x, y, radius, radius, p2, p3);
		arc(x, y, radius, radius, p4, p5);
		
		fill(0,0,0);
		arc(x, y, radius, radius, p1, p2);
		arc(x, y, radius, radius, p3, p4);
		arc(x, y, radius, radius, p5, p6);
		stroke(255,225,0);
		strokeWeight(2);
		ellipse(x,y, 5, 5);
		noStroke();
		popStyle();
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", xbase+25, ybase+10);
		
		fill(150, 30, 30);
		int tri_xbase = xbase + 35;
		int tri_ybase = ybase + 30;
		triangle(tri_xbase, tri_ybase-CityMarker.TRI_SIZE, tri_xbase-CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE, tri_xbase+CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE);

		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		text("City Marker", tri_xbase + 15, tri_ybase);
		text("Nuclear Marker",tri_xbase + 15, ybase+50);
		text("Land Quake", xbase+50, ybase+70);
		text("Ocean Quake", xbase+50, ybase+90);
		text("Size ~ Magnitude", xbase+25, ybase+110);
		
		fill(255, 255, 255);
		ellipse(xbase+35, 
				ybase+70, 
				10, 
				10);
		rect(xbase+35-5, ybase+90-5, 10, 10);
		
		fill(15, 195, 40);
		ellipse(xbase+35, ybase+140, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase+35, ybase+160, 12, 12);
		fill(color(255, 0, 0));
		ellipse(xbase+35, ybase+180, 12, 12);
		
		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("Shallow", xbase+50, ybase+140);
		text("Intermediate", xbase+50, ybase+160);
		text("Deep", xbase+50, ybase+180);

		text("Past hour", xbase+50, ybase+200);
		
		fill(255, 255, 255);
		int centerx = xbase+35;
		int centery = ybase+200;
		ellipse(centerx, centery, 12, 12);

		strokeWeight(2);
		line(centerx-8, centery-8, centerx+8, centery+8);
		line(centerx-8, centery+8, centerx+8, centery-8);
		
		fill(255,255,255);
		textSize(13);
		// The font must be located in the sketch's 
		// "data" directory to load successfully
		text("Click the button", 10, 55);
		
		fill(255, 250, 240);
		rect(125, 50, 50, 50);
		
		
	}

	
	
	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.
	private boolean isLand(PointFeature earthquake) {
		
		// IMPLEMENT THIS: loop over all countries to check if location is in any of them
		// If it is, add 1 to the entry in countryQuakes corresponding to this country.
		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}
		
		// not inside any country
		return false;
	}
	
	// prints countries with number of earthquakes
	// You will want to loop through the country markers or country features
	// (either will work) and then for each country, loop through
	// the quakes to count how many occurred in that country.
	// Recall that the country markers have a "name" property, 
	// And LandQuakeMarkers have a "country" property set.
	private void printQuakes() {
		int totalWaterQuakes = quakeMarkers.size();
		for (Marker country : countryMarkers) {
			String countryName = country.getStringProperty("name");
			int numQuakes = 0;
			for (Marker marker : quakeMarkers)
			{
				EarthquakeMarker eqMarker = (EarthquakeMarker)marker;
				if (eqMarker.isOnLand()) {
					if (countryName.equals(eqMarker.getStringProperty("country"))) {
						numQuakes++;
					}
				}
			}
			if (numQuakes > 0) {
				totalWaterQuakes -= numQuakes;
				System.out.println(countryName + ": " + numQuakes);
			}
		}
		System.out.println("OCEAN QUAKES: " + totalWaterQuakes);
	}
	
	
	
	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake feature if 
	// it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
				
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
					
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
						
					// return if is inside one
					return true;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			
			return true;
		}
		return false;
	}
		
	
	private void drawButton()
	{
		if(!citySelected)
		{
			pushStyle();
			fill(150, 30, 30);
			triangle(150, 65, 140, 85, 160, 85);
			fill(255,255,255);
			textSize(13);
			text("to show cities", 21,75);
			stroke(0,0,0);
			popStyle();
		}
		if(citySelected)
		{

			int x = 150;
			int y = 75;
			float radius = 25;
			float p1 = (float) 1.04;
			float p2 = p1*2;
			float p3 = p1*3;
			float p4 = p1*4;
			float p5 = p1*5;
			float p6 = p1*6;
			pushStyle();			
			fill(255,225,0);
			arc(x, y, radius, radius, 0, p1);
			arc(x, y, radius, radius, p2, p3);
			arc(x, y, radius, radius, p4, p5);
			
			fill(0,0,0);
			arc(x, y, radius, radius, p1, p2);
			arc(x, y, radius, radius, p3, p4);
			arc(x, y, radius, radius, p5, p6);
			stroke(255,225,0);
			strokeWeight(2);
			ellipse(x,y, 5, 5);
			noStroke();
			
			fill(255, 255, 255);
			text("to show nuclear", 14,75);
			text("stations", 65,95);
			stroke(0,0,0);
			popStyle();
		}
		
	}
	
	public static float getZoomLevel()
	{
		return map.getZoom();
	}
	
	public void setCitySelected(boolean or)
	{
		this.citySelected=or;
	}
	
	public boolean isCitySelected()
	{
		return this.citySelected;
	}

}
