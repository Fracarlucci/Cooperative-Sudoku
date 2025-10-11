package pcd.ass03.part2A;

import pcd.ass03.part2A.controller.SudokuController;
import pcd.ass03.part2A.controller.SudokuControllerImpl;
import pcd.ass03.part2A.model.Player;
import pcd.ass03.part2A.view.SudokuGUIimpl;

public class Main {
    public static void main(String[] args) {
        try {      
            Player p1 = new Player("p1", "player-1");
            Player p2 = new Player("p2", "player-2");
            Player p3 = new Player("p3", "player-3");
            
            SudokuController controller1 = new SudokuControllerImpl(p1);
            SudokuController controller2 = new SudokuControllerImpl(p2);
            SudokuController controller3 = new SudokuControllerImpl(p3);

            SudokuGUIimpl gui1 = new SudokuGUIimpl(controller1);
            SudokuGUIimpl gui2 = new SudokuGUIimpl(controller2);
            SudokuGUIimpl gui3 = new SudokuGUIimpl(controller3);

            p1.setController(controller1);
            p2.setController(controller2);
            p3.setController(controller3);

            controller1.setView(gui1);
            controller2.setView(gui2);
            controller3.setView(gui3);

            gui1.setVisible(true);
            gui2.setVisible(true);
            gui3.setVisible(true);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}