import javax.swing.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;

public class MainClass extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JTextArea codeArea;
	private JTextArea outputArea;
	private JScrollPane scrollCodeArea;
	private JButton runButton;
	private JButton abortButton;
	private ActionListener actionLnr;
	private Font consoleLikeFont;

	public MainClass() {
		actionLnr = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// if(e.getSource() == sth) ...
			}
		};
		// WINDOW
		setSize(540, 400);
		setTitle("BrainFuck Interpreter by G.Romanczyk");
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);

		//Elements inits
		consoleLikeFont = new Font("Consolas", Font.PLAIN, 14);

		codeArea = new JTextArea();
		codeArea.setFont(consoleLikeFont);
		codeArea.setLineWrap(true);
		codeArea.setWrapStyleWord(true);
		codeArea.setTabSize(2); // tab size of 2 spaces
		codeArea.setText("Code here");
		codeArea.setBackground(new Color(236, 242, 249));

		scrollCodeArea = new JScrollPane(codeArea); // assign JScrollPane to the text area
		scrollCodeArea.setBounds(5, 5, 440, 245);
		scrollCodeArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		outputArea = new JTextArea();
		outputArea.setFont(consoleLikeFont);
		outputArea.setLineWrap(true);
		outputArea.setBounds(5, 255, 440, 100);
		outputArea.setWrapStyleWord(true);
		outputArea.setEditable(false);
		outputArea.setText("Output");
		outputArea.setBackground(new Color(236, 242, 249));

		runButton = new JButton("Run");
		runButton.setBounds(450, 5, 70, 30);
		runButton.addActionListener(actionLnr);

		abortButton = new JButton("Abort");
		abortButton.setBounds(450, 40, 70, 30);
		abortButton.addActionListener(actionLnr);

		// Adding elements to the window
		add(scrollCodeArea);
		add(outputArea);
		add(runButton);
		add(abortButton);

	}

	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MainClass();
			}
		});
	}

}
