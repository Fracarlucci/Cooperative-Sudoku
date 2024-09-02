package pcd.ass02.part1.simtrafficexamples;

import pcd.ass02.part1.simengineseq.AbstractSimulation;
import pcd.ass02.part1.simtrafficbase.*;

import java.util.List;

/**
 *
 * Traffic Simulation about 2 cars moving on a single road, with one semaphore
 *
 */
public class TrafficSimulationSingleRoadWithTrafficLightTwoCars extends AbstractSimulation {

    private final ThreadManager threadManager;
    private final RoadsEnv env;

	public TrafficSimulationSingleRoadWithTrafficLightTwoCars(int nThreads) {
		super();
        this.env = new RoadsEnv();
        this.threadManager = new ThreadManager(nThreads, 1, this, env);
	}

	public void setup() {

		int dt = 1;

		final int nCyclesPerSec = 25;

		this.setupTimings(0, dt);

		this.setupEnvironment(env);

		Road r = env.createRoad(new P2d(0,300), new P2d(1500,300));

		TrafficLight tl = env.createTrafficLight(new P2d(740,300), TrafficLight.TrafficLightState.GREEN, 75, 25, 100, threadManager, this, dt);
		r.addTrafficLight(tl, 740);

		threadManager.generateTrafficLight(List.of(tl));


		CarAgent car1 = new CarAgentExtended("car-1", env, r, 0, 0.1, 0.3, 6, threadManager.getActBarrier(), threadManager.getStepBarrier(), this);
		this.addAgent(car1);
		CarAgent car2 = new CarAgentExtended("car-2", env, r, 100, 0.1, 0.3, 5, threadManager.getActBarrier(), threadManager.getStepBarrier(), this);
		this.addAgent(car2);

		threadManager.generateCars(List.of(car1, car2));
		threadManager.setnCyclesPerSec(nCyclesPerSec);
		this.syncWithTime(nCyclesPerSec);
	}

	@Override
	public void run(int nSteps) {
		this.threadManager.setSteps(nSteps);
		this.threadManager.startThreads(this.getDt());
	}
}
