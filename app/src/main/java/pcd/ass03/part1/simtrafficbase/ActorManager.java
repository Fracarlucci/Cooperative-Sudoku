package pcd.ass03.part1.simtrafficbase;

import java.util.ArrayList;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pcd.ass03.part1.simengineseq.AbstractSimulation;
import pcd.ass03.part1.simtrafficbase.messages.Message;
import pcd.ass03.part1.simtrafficbase.messages.Start;
import pcd.ass03.part1.simtrafficbase.messages.Stop;

public class ActorManager extends AbstractBehavior<Message>{
  private final ArrayList<ActorRef<Message>> cars;
  private final ArrayList<ActorRef<Message>> trafficLights;
  private final AbstractSimulation simulation;
  private final RoadsEnv env;

  public static Behavior<Message> create(AbstractSimulation simulation, RoadsEnv env) {
      // TODO create car actors
      // TODO create traffic light actors  
      return Behaviors.setup(context -> new ActorManager(context, simulation, env));
    }

    private ActorManager(ActorContext<Message> context, AbstractSimulation simulation, RoadsEnv env) {
        super(context);
        this.cars = new ArrayList<>();
        this.trafficLights = new ArrayList<>();
        this.simulation = simulation;
        this.env = env;
    }

    @Override
    public Receive<Message> createReceive() {
      // TODO handle messages
      return newReceiveBuilder()
        .onMessage(Start.class, msg -> { return this; })
        .onMessage(Stop.class, msg -> Behaviors.stopped())
        .build();
    }
}
