package pcd.ass03.part1.simtrafficbase;

import pcd.ass03.part1.simengineseq.Action;

/**
 * Car agent move forward action
 */
public record MoveForward(double distance) implements Action {}
