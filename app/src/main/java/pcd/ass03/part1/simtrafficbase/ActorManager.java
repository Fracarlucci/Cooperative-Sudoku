package pcd.ass03.part1.simtrafficbase;

import java.util.ArrayList;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pcd.ass03.part1.simengineseq.AbstractSimulation;
import pcd.ass03.part1.simtrafficbase.messages.ActionDone;
import pcd.ass03.part1.simtrafficbase.messages.ActionReady;
import pcd.ass03.part1.simtrafficbase.messages.DoAction;
import pcd.ass03.part1.simtrafficbase.messages.Message;
import pcd.ass03.part1.simtrafficbase.messages.Start;
import pcd.ass03.part1.simtrafficbase.messages.Step;
import pcd.ass03.part1.simtrafficbase.messages.Stop;
import pcd.ass03.part1.simtrafficbase.messages.TrafficLightReady;

public class ActorManager extends AbstractBehavior<Message>{
  private final ArrayList<ActorRef<Message>> cars;
  private final ArrayList<ActorRef<Message>> trafficLights;
  private final AbstractSimulation simulation;
  private final RoadsEnv env;

  private int nTrafficLightsReadyToAct;
  private int nCarsReadyToAct;
  private int nCarsDoneAction;
  private int nStepsDone;
  private long currentWallTime;
  
  int actualSteps = 0;
  long startWallTime = System.currentTimeMillis();
  int t = 0;
  long timePerStep = 0;
  long startStepTime = 0;
  int nCyclesPerSec = 25;
  int dt = 0;

  public static Behavior<Message> create(AbstractSimulation simulation, RoadsEnv env) {
      return Behaviors.setup(context -> new ActorManager(context, simulation, env));
    }

    private ActorManager(ActorContext<Message> context, AbstractSimulation simulation, RoadsEnv env) {
        super(context);
        this.cars = new ArrayList<>();
        this.trafficLights = new ArrayList<>();
        this.simulation = simulation;
        this.env = env;
        this.nCarsReadyToAct = 0;
        this.nTrafficLightsReadyToAct = 0;
        this.nCarsDoneAction = 0;
        this.nStepsDone = 0;
        this.dt = simulation.getDt();
    }

    @Override
    public Receive<Message> createReceive() {
      return newReceiveBuilder()
        .onMessage(Start.class, msg -> {
          System.out.println("Starting simulation...");
          createCarActors();
          createTrafficLightActors();
          currentWallTime = System.currentTimeMillis();
          if (this.trafficLights.isEmpty()) {
              this.cars.forEach(act -> act.tell(new Step(getContext().getSelf())));
              System.out.println("No traffic lights present.");
          } else {
              System.out.println("Traffic lights present: " + this.trafficLights.size());
              this.trafficLights.forEach(act -> act.tell(new Step(getContext().getSelf())));
          }
          return this; 
        })
        .onMessage(TrafficLightReady.class, msg -> {
          nTrafficLightsReadyToAct++;
          if (nTrafficLightsReadyToAct == this.trafficLights.size()) {
            nTrafficLightsReadyToAct = 0;
            this.cars.forEach(act -> act.tell(new Step(getContext().getSelf())));
          }
          return this;
        })
        .onMessage(ActionReady.class, msg -> {
          System.out.println("Car ready to act: ");
          nCarsReadyToAct++;
          if (nCarsReadyToAct == this.cars.size()) {
            nCarsReadyToAct = 0;
            this.cars.forEach(car -> car.tell(new DoAction(getContext().getSelf())));
          }
          return this;
        })
        .onMessage(ActionDone.class, msg -> {
          System.out.println("Car done action: ");
          nCarsDoneAction++;
          if (nCarsDoneAction == this.cars.size()) {
            nCarsDoneAction = 0;

            if (startStepTime != 0) {
                timePerStep += System.currentTimeMillis() - startStepTime;
            }
            t += dt;
            currentWallTime = System.currentTimeMillis();

            if (nCyclesPerSec > 0) {
                simulation.syncWithWallTime(currentWallTime);
            }
            startStepTime = System.currentTimeMillis();
            nStepsDone++;
            simulation.notifyNewStep(t, env);
            System.out.println("STEPS: " + nStepsDone);

            // endcycleandwait
            if (nStepsDone >= env.getnSteps()) {
              this.simulation.stop();
              getContext().getSelf().tell(new Stop());
            } else {
              currentWallTime = System.currentTimeMillis();
              if (this.trafficLights.isEmpty()) {
                  this.cars.forEach(act -> act.tell(new Step(getContext().getSelf())));
              } else {
                  this.trafficLights.forEach(act -> act.tell(new Step(getContext().getSelf())));
              }
            }
          }
          return this;
        })
        .onMessage(Stop.class, msg -> Behaviors.stopped())
        .build();
    }

    private void createCarActors() {
      for (CarAgentInfo car : env.getAgentInfo()) {
        ActorRef<Message> carActor = getContext().spawn(CarActor.create(car.getCar()), "car-" + car.getCar().getAgentId());
        cars.add(carActor);
      }
    }

    private void createTrafficLightActors() {
      int counter = 0;
      for (TrafficLight tl : env.getTrafficLights()) {
        ActorRef<Message> tlActor = getContext().spawn(TrafficLightActor.create(tl), "trafficLight-" + counter++);
        trafficLights.add(tlActor);
      }
    }

    public void setnCyclesPerSec(int nCyclesPerSec) {
        this.nCyclesPerSec = nCyclesPerSec;
    }
}
