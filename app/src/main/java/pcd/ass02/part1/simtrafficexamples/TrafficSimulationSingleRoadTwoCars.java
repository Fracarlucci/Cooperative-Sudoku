package pcd.ass02.part1.simtrafficexamples;

import pcd.ass02.part1.simengineseq.AbstractSimulation;
import pcd.ass02.part1.simtrafficbase.*;

import java.util.List;

/**
 *
 * Traffic Simulation about 2 cars moving on a single road, no traffic lights
 *
 */
public class TrafficSimulationSingleRoadTwoCars extends AbstractSimulation {

	private final ThreadManager threadManager;
	private final RoadsEnv env;

	public TrafficSimulationSingleRoadTwoCars(int nThreads) {
		super();
		this.env = new RoadsEnv();
        this.threadManager = new ThreadManager(nThreads, 0, this, env);
    }

	public void setup() {

		int nCyclesPerSec = 25;

		this.setupTimings(0, 1);

		this.setupEnvironment(env);
		Road r = env.createRoad(new P2d(0,300), new P2d(1500,300));
		CarAgent car1 = new CarAgentBasic("car-1", env, r,0, 0.1, 0.2, 8, threadManager.getActBarrier(), threadManager.getStepBarrier(), this);
		this.addAgent(car1);
		CarAgent car2 = new CarAgentBasic("car-2", env, r,100, 0.1, 0.1, 7, threadManager.getActBarrier(), threadManager.getStepBarrier(), this);
		this.addAgent(car2);

		/* sync with wall-time: 25 steps per sec */
		this.syncWithTime(nCyclesPerSec);
		threadManager.setnCyclesPerSec(nCyclesPerSec);
		threadManager.generateCars(List.of(car1,car2));
	}

	@Override
	public void run(int nSteps) {
		this.threadManager.setSteps(nSteps);
		this.threadManager.startThreads(this.getDt());
	}

}
