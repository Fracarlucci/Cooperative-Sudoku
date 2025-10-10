package pcd.ass03.part2A;

import pcd.ass03.part2A.controller.SudokuController;
import pcd.ass03.part2A.controller.SudokuControllerImpl;
import pcd.ass03.part2A.model.Player;
import pcd.ass03.part2A.view.SudokuGUI;

public class Main {
  public static void main(String[] args) {
    try {      
        Player p1 = new Player("p1", "player-1");
        Player p2 = new Player("p2", "player-2");
        
        Thread.sleep(200);

        SudokuController controller1 = new SudokuControllerImpl(p1);
        SudokuController controller2 = new SudokuControllerImpl(p2);
        p1.setController(controller1);
        p2.setController(controller2);

        SudokuGUI gui = new SudokuGUI(controller1);
        SudokuGUI gui2 = new SudokuGUI(controller2);
        ((SudokuControllerImpl)controller1).setView(gui);
        ((SudokuControllerImpl)controller2).setView(gui2);
        gui.setVisible(true);
        gui2.setVisible(true);
        
        // ((SudokuControllerImpl)controller1).setView(gui);
        // ((SudokuControllerImpl)controller2).setView(new SudokuGUI(controller2));

        // SwingUtilities.invokeLater(() -> {
        //     try {
        //         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        //     SudokuGUI gui = new SudokuGUI();
        //     gui.setVisible(true);
        // });
        // SwingUtilities.invokeLater(() -> {
        //     try {
        //         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        //     SudokuGUI gui = new SudokuGUI(new SudokuControllerImpl(p2));
        //     gui.setVisible(true);
        // });

        // SudokuGUI gui2 = new SudokuGUI(new SudokuControllerImpl(p2));
        // gui2.setVisible(true);
                
    } catch (Exception e) {
        e.printStackTrace();
    }
}
}