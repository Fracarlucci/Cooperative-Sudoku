package pcd.ass03.part1.simtrafficbase.messages;

import akka.actor.typed.ActorRef;
import pcd.ass03.part1.simengineseq.Action;

public record CarAction(String id, Action action, ActorRef<Message> sender) implements Message{}
