package pcd.ass03.part1.simtrafficbase;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pcd.ass03.part1.simtrafficbase.messages.Message;
import pcd.ass03.part1.simtrafficbase.messages.Step;
import pcd.ass03.part1.simtrafficbase.messages.TrafficLightState;

public class TrafficLightActor extends AbstractBehavior<Message>{

    private final TrafficLight trafficLight;

    public static Behavior<Message> create(TrafficLight trafficLight) {
        return Behaviors.setup(context -> new TrafficLightActor(context, trafficLight));
    }

    private TrafficLightActor(ActorContext<Message> context, TrafficLight trafficLight) {
        super(context);
        this.trafficLight = trafficLight;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
            .onMessage(Step.class, msg -> {
                this.trafficLight.semaphoreStep();
                msg.sender().tell(new TrafficLightReady(getContext().getSelf().narrow()));
                return this;
            })        
            .build();
    }
}
