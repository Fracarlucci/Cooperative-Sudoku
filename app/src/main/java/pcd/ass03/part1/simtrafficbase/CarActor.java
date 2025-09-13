package pcd.ass03.part1.simtrafficbase;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pcd.ass03.part1.simtrafficbase.messages.Message;
import pcd.ass03.part1.simtrafficbase.messages.Step;
import pcd.ass03.part1.simtrafficbase.messages.CarAction;

public class CarActor extends AbstractBehavior<Message> {
    
    private final CarAgent carAgent;

    public static Behavior<Message> create(CarAgent carAgent) {
        return Behaviors.setup(context -> new CarActor(context, carAgent));
    }

    private CarActor(ActorContext<Message> context, CarAgent carAgent) {
        super(context);
        this.carAgent = carAgent;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
            .onMessage(Step.class, msg -> {
                this.carAgent.step();
                msg.sender().tell(new ActionReady(getContext().getSelf().narrow()));
                return this;
            })
            .onMessage(DoAction.class, msg -> {
                this.carAgent.act(); 
                msg.sender().tell(new ActionDone(getContext().getSelf().narrow())); 
                return this;
            })
            .build();
    }
}
