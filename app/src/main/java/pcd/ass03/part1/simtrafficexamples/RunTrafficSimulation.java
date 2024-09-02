package pcd.ass03.part1.simtrafficexamples;

/**
 * 
 * Main class to create and run a simulation
 * 
 */
public class RunTrafficSimulation {

	public static void main(String[] args) {

//	 	var simulation = new TrafficSimulationSingleRoadTwoCars(2);
		var simulation = new TrafficSimulationSingleRoadSeveralCars(30);
//		var simulation = new TrafficSimulationSingleRoadWithTrafficLightTwoCars(2);
//		var simulation = new TrafficSimulationWithCrossRoads(4);
		simulation.setup();

		RoadSimStatistics stat = new RoadSimStatistics();
		RoadSimView view = new RoadSimView(simulation);
		view.display();
		
		simulation.addSimulationListener(stat);
		simulation.addSimulationListener(view);
	}
}
