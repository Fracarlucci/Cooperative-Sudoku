package pcd.ass03.part1.simtrafficbase.messages;

import akka.actor.typed.ActorRef;

public record ActionReady(ActorRef<Message> sender) implements Message{}
