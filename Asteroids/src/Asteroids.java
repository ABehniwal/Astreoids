import javax.swing.*;

class Asteroids extends JFrame{

	GamePanel game;
		
    public Asteroids() {
		super("Asteroids");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		game = new GamePanel();
		add(game);
		pack();  // set the size of my Frame exactly big enough to hold the contents
		setVisible(true);
		setResizable(false);
    }
}