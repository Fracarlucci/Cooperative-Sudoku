package pcd.ass03.part2B.model;

public record PlayerInfo(String playerId, String playerName, boolean isActive, 
                        String currentGridId, int selectedRow, int selectedCol) {}
