package pcd.ass03.part1.simtrafficexamples;

import pcd.ass03.part1.simengineseq.AbstractSimulation;
import pcd.ass03.part1.simtrafficbase.*;
import pcd.ass03.part1.simtrafficbase.messages.*;

import akka.actor.typed.ActorSystem;

/**
 *
 * Traffic Simulation about 2 cars moving on a single road, with one semaphore
 *
 */
public class TrafficSimulationSingleRoadWithTrafficLightTwoCars extends AbstractSimulation {

    private final RoadsEnv env;
	private ActorSystem<Message> system;

	public TrafficSimulationSingleRoadWithTrafficLightTwoCars() {
		super();
        this.env = new RoadsEnv();
	}

	public void setup() {

		int dt = 1;

		final int nCyclesPerSec = 25;

		this.setupTimings(0, dt);

		this.setupEnvironment(env);

		Road r = env.createRoad(new P2d(0,300), new P2d(1500,300));

		TrafficLight tl = env.createTrafficLight(new P2d(740,300), TrafficLight.TrafficLightState.GREEN, 75, 25, 100, this, dt);
		r.addTrafficLight(tl, 740);

		CarAgent car1 = new CarAgentExtended("car-1", env, r, 0, 0.1, 0.3, 6);
		this.addAgent(car1);
		CarAgent car2 = new CarAgentExtended("car-2", env, r, 100, 0.1, 0.3, 5);
		this.addAgent(car2);

		this.syncWithTime(nCyclesPerSec);
		system = ActorSystem.create(ActorManager.create(this, env), "Environment");
	}

	@Override
	public void run(int nSteps) {
		system.tell(new Start());
	}

	@Override
	public void stop() {
		system.tell(new Stop());
	}
}
