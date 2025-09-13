package pcd.ass03.part1.simtrafficbase.messages;

import akka.actor.typed.ActorRef;

public record ActionDone(ActorRef<Message> sender) implements Message{}
