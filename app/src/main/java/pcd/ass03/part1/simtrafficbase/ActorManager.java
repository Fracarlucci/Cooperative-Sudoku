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
import pcd.ass03.part1.simtrafficbase.messages.Pause;
import pcd.ass03.part1.simtrafficbase.messages.Resume;
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
  private boolean paused = false;
  
  int actualSteps = 0;
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
          createCarActors();
          createTrafficLightActors();
          simulation.setCurrentWallTime();
          if (this.trafficLights.isEmpty()) {
              this.cars.forEach(act -> act.tell(new Step(getContext().getSelf())));
          } else {
              this.trafficLights.forEach(act -> act.tell(new Step(getContext().getSelf())));
          }
          return this; 
        })
        // If there are traffic lights, wait until all are ready
        // before letting the cars decide their next move
        .onMessage(TrafficLightReady.class, msg -> {
          nTrafficLightsReadyToAct++;
          if (nTrafficLightsReadyToAct == this.trafficLights.size()) {
            nTrafficLightsReadyToAct = 0;
            this.cars.forEach(act -> act.tell(new Step(getContext().getSelf())));
          }
          return this;
        })
        // Message received from cars when they have decided their next move
        .onMessage(ActionReady.class, msg -> {
          nCarsReadyToAct++;
          if (nCarsReadyToAct == this.cars.size()) {
            nCarsReadyToAct = 0;
            this.cars.forEach(car -> car.tell(new DoAction(getContext().getSelf())));
          }
          return this;
        })
        // Message received from cars when they have performed their move
        .onMessage(ActionDone.class, msg -> {
          nCarsDoneAction++;
          if (nCarsDoneAction >= this.cars.size() && !this.paused) {
            nCarsDoneAction = 0;
            nStepsDone++;
            simulation.stepCycle();
            System.out.println("STEPS: " + nStepsDone);
            
            // If all steps have been completed, the simulation ends
            // otherwise, start a new step
            if (nStepsDone >= env.getnSteps()) {
              this.simulation.stop();
              getContext().getSelf().tell(new Stop());
            } else {
              simulation.setCurrentWallTime();
              if (this.trafficLights.isEmpty()) {
                  this.cars.forEach(act -> act.tell(new Step(getContext().getSelf())));
              } else {
                  this.trafficLights.forEach(act -> act.tell(new Step(getContext().getSelf())));
              }
            }
          }
          return this;
        })
        .onMessage(Pause.class, msg -> { 
          this.paused = true;
          return this;
        })
        // When receiving a resume message
        // it sends a message to itself to restart the simulation
        .onMessage(Resume.class, msg -> {
          this.paused = false;
          getContext().getSelf().tell(new ActionDone(getContext().getSelf()));
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
}
