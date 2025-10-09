package pcd.ass03.part2A.model;

public record PlayerInfo(String playerId, String playerName, boolean isActive, 
                      int currentGridId, int selectedRow, int selectedCol) {}
