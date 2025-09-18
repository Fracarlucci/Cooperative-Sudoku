package pcd.ass03.part1.simtrafficexamples;

import pcd.ass03.part1.simengineseq.AbstractSimulation;
import pcd.ass03.part1.simtrafficbase.*;
import pcd.ass03.part1.simtrafficbase.messages.*;

import java.util.LinkedList;
import java.util.List;
import akka.actor.typed.ActorSystem;

/**
 *
 * Traffic Simulation about a number of cars
 * moving on a single road, no traffic lights
 *
 */
public class TrafficSimulationSingleRoadSeveralCars extends AbstractSimulation {

	private final RoadsEnv env;
	private ActorSystem<Message> system;

	public TrafficSimulationSingleRoadSeveralCars() {
		super();
		this.env = new RoadsEnv();
	}

	public void setup() {

		List<CarAgent> cars = new LinkedList<>();

		final int nCyclesPerSec = 25;

		this.setupTimings(0, 1);

		this.setupEnvironment(env);

		Road road = env.createRoad(new P2d(0,300), new P2d(1500,300));

		int nCars = 30;

		for (int i = 0; i < nCars; i++) {

			String carId = "car-" + i;
			double initialPos = i*10;
			double carAcceleration = 1; //  + gen.nextDouble()/2;
			double carDeceleration = 0.3; //  + gen.nextDouble()/2;
			double carMaxSpeed = 7; // 4 + gen.nextDouble();

			CarAgent car = new CarAgentBasic(
					carId,
					env,
					road,
					initialPos,
					carAcceleration,
					carDeceleration,
					carMaxSpeed);
			this.addAgent(car);
			car.init(env, this.getDt());
			cars.add(car);
		}
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
		system.tell(new Pause());
	}

	@Override
	public void start() {
		system.tell(new Resume());
	}
}
