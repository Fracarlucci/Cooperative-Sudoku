package pcd.ass03.part1.simtrafficexamples;

import pcd.ass03.part1.simengineseq.AbstractSimulation;
import pcd.ass03.part1.simtrafficbase.*;
import pcd.ass03.part1.simtrafficbase.messages.*;

import akka.actor.typed.ActorSystem;

/**
 *
 * Traffic Simulation about 2 cars moving on a single road, no traffic lights
 *
 */
public class TrafficSimulationSingleRoadTwoCars extends AbstractSimulation {

	private final RoadsEnv env;
	private ActorSystem<Message> system;

	public TrafficSimulationSingleRoadTwoCars() {
		super();
		this.env = new RoadsEnv();
    }

	public void setup() {

		int nCyclesPerSec = 25;

		this.setupTimings(0, 1);

		this.setupEnvironment(env);
		Road r = env.createRoad(new P2d(0,300), new P2d(1500,300));
		CarAgent car1 = new CarAgentBasic("car-1", env, r,0, 0.1, 0.2, 8);
		this.addAgent(car1);
		car1.init(env, nCyclesPerSec);
		CarAgent car2 = new CarAgentBasic("car-2", env, r,100, 0.1, 0.1, 7);
		this.addAgent(car2);
		car2.init(env, nCyclesPerSec);

		/* sync with wall-time: 25 steps per sec */
		this.syncWithTime(nCyclesPerSec);
		system = ActorSystem.create(ActorManager.create(this, env), "Environment");
	}

	@Override
	public void run(int nSteps) {
		System.out.println("Running simulation for " + nSteps + " steps");
		super.run(nSteps);
		system.tell(new Start());
	}

	@Override
	public void stop() {
		system.tell(new Stop());
	}

}
