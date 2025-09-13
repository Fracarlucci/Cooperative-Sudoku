package pcd.ass03.part1.simtrafficexamples;

import pcd.ass03.part1.simengineseq.AbstractSimulation;
import pcd.ass03.part1.simtrafficbase.*;
import pcd.ass03.part1.simtrafficbase.messages.*;

import java.util.LinkedList;
import java.util.List;
import akka.actor.typed.ActorSystem;

public class TrafficSimulationSingleRoadMassiveNumberOfCars extends AbstractSimulation {

	private final int numCars;
    private final RoadsEnv env;
	private ActorSystem<Message> system;

	public TrafficSimulationSingleRoadMassiveNumberOfCars(int numCars) {
		super();
		this.numCars = numCars;
        this.env = new RoadsEnv();
    }

	public void setup() {

        List<CarAgent> cars = new LinkedList<>();

		this.setupTimings(0, 1);

		this.setupEnvironment(env);
		
		Road road = env.createRoad(new P2d(0,300), new P2d(15000,300));
		
		for (int i = 0; i < numCars; i++) {
			
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
				cars.add(car);
				
				/* no sync with wall-time */
		}
		system = ActorSystem.create(ActorManager.create(this, env), "Environment");
    }
	
    @Override
    public void run(int nSteps) {
		system.tell(new Start());
    }
}
