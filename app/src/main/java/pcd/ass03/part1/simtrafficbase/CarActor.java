package pcd.ass03.part1.simtrafficbase;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pcd.ass03.part1.simtrafficbase.messages.CarAction;
import pcd.ass03.part1.simtrafficbase.messages.Message;
import pcd.ass03.part1.simtrafficbase.messages.Step;

public class CarActor extends AbstractBehavior<Message> {
    
    private final CarAgent carAgent;
    private final int dt;

    public static Behavior<Message> create(CarAgent carAgent, int dt) {
        return Behaviors.setup(context -> new CarActor(context, carAgent, dt));
    }

    private CarActor(ActorContext<Message> context, CarAgent carAgent, int dt) {
        super(context);
        this.carAgent = carAgent;
        this.dt = dt;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
            .onMessage(Step.class, msg -> { this.carAgent.step(); return Behaviors.same(); })
            .onMessage(CarAction.class, msg -> { this.carAgent.act(); return Behaviors.same(); })
            .build();
    }
}
