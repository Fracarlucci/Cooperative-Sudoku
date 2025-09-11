package pcd.ass03.part1.simtrafficbase.messages;

import akka.actor.typed.ActorRef;

public record TrafficLightState(String id, TrafficLightState state, ActorRef<Message> sender) implements Message{}
