import javax.swing.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;

public class MainClass extends JFrame implements KeyListener {

	private static final long serialVersionUID = 1L;

	private JTextArea codeArea;
	private JTextArea outputArea;
	private JScrollPane scrollCodeArea;
	private JScrollPane scrollOutputArea;
	private JButton runButton;
	private ActionListener actionLnr;
	private Font consoleLikeFont;
	private JLabel inProgressInfoLabel, charInputInfoLabel, finishInfoLabel;

	ParseMechanism parser;
	Thread updateGUIthread;
	Thread parserThr;

	String startCodeStr;

	public MainClass() {

		// EVENT HANDLING
		actionLnr = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == runButton) {
					outputArea.setText("");

					parse(codeArea.getText());

					updateGUIthread = new Thread() {
						@Override
						public void run() {
							while (parser.getState().current != Flag.FINISH) {
								try {
									Thread.sleep(1);
									int flag = parser.getState().current;
									switch (flag) {
									case Flag.GET_CHAR:
										readCharByOutputArea();
										break;
									case Flag.TO_PRINT:
										appendCharToOutputArea(parser.getCharToPrint());
										break;
									case Flag.LOOP_ERROR:
										outputArea.setText("Number of [ and ] is not equal");
										break;
									case Flag.OUT_OF_MEM_BOUNDS:
										outputArea.setText("Out od memory bounds");
										break;
									}
									showInfo(parser.getState());
								} catch (Exception e) {
								}
							}
						}
					};
					updateGUIthread.start();
				}

			}
		};
		// Hello world program written to codeArea
		startCodeStr = "++++++++++\r\n" + "[\r\n" + ">+++++++>++++++++++>+++>+<<<<-\r\n" + "]\r\n"
				+ ">++.               drukuje 'H'\r\n" + ">+.                drukuje 'e'\r\n"
				+ "+++++++.           drukuje 'l'\r\n" + ".                  drukuje 'l'\r\n"
				+ "+++.               drukuje 'o'\r\n" + ">++.               spacja\r\n"
				+ "<<+++++++++++++++. drukuje 'W'\r\n" + ">.                 drukuje 'o'\r\n"
				+ "+++.               drukuje 'r'\r\n" + "------.            drukuje 'l'\r\n"
				+ "--------.          drukuje 'd'\r\n" + ">+.                drukuje '!'\r\n"
				+ ">.                 nowa linia";

		// WINDOW
		setSize(540, 400);
		setTitle("BrainFuck Interpreter by G.Romanczyk");
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);

		// Elements inits
		consoleLikeFont = new Font("Consolas", Font.PLAIN, 14);

		codeArea = new JTextArea();
		codeArea.setFont(consoleLikeFont);
		codeArea.setLineWrap(true);
		codeArea.setWrapStyleWord(true);
		codeArea.setTabSize(2); // tab size of 2 spaces
		codeArea.setText(startCodeStr);
		codeArea.setBackground(new Color(236, 242, 249));

		scrollCodeArea = new JScrollPane(codeArea); // assign JScrollPane to the text area
		scrollCodeArea.setBounds(5, 5, 440, 245);
		scrollCodeArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		outputArea = new JTextArea();
		outputArea.setFont(consoleLikeFont);
		outputArea.setLineWrap(true);
		outputArea.setWrapStyleWord(true);
		outputArea.setEditable(false);
		outputArea.setText("Output");
		outputArea.setBackground(new Color(236, 242, 249));
		outputArea.addKeyListener(this);

		scrollOutputArea = new JScrollPane(outputArea);
		scrollOutputArea.setBounds(5, 255, 440, 100);
		scrollOutputArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		runButton = new JButton("Run");
		runButton.setBounds(450, 5, 70, 30);
		runButton.addActionListener(actionLnr);

		// labels with information about state of parsing and executing
		inProgressInfoLabel = new JLabel("in progress");
		inProgressInfoLabel.setBounds(450, 325, 90, 40);
		inProgressInfoLabel.setOpaque(true);
		inProgressInfoLabel.setVisible(false);

		charInputInfoLabel = new JLabel("input char");
		charInputInfoLabel.setBounds(455, 325, 90, 40);
		charInputInfoLabel.setOpaque(true);
		charInputInfoLabel.setVisible(false);

		finishInfoLabel = new JLabel("Finish");
		finishInfoLabel.setBounds(465, 325, 90, 40);
		finishInfoLabel.setOpaque(true);
		finishInfoLabel.setVisible(false);

		// Adding elements to the window
		add(scrollCodeArea);
		add(scrollOutputArea);
		add(runButton);
		add(inProgressInfoLabel);
		add(charInputInfoLabel);
		add(finishInfoLabel);

	}

	private void showInfo(Flag info) {
		switch (info.current) {
		case Flag.FINISH:
			finishInfoLabel.setVisible(true);
			inProgressInfoLabel.setVisible(false);
			charInputInfoLabel.setVisible(false);
			break;
		case Flag.IN_PROGRESS:
			inProgressInfoLabel.setVisible(true);
			finishInfoLabel.setVisible(false);
			charInputInfoLabel.setVisible(false);
			break;
		case Flag.GET_CHAR:
			finishInfoLabel.setVisible(false);
			inProgressInfoLabel.setVisible(false);
			charInputInfoLabel.setVisible(true);
			break;
		}
	}

	public void appendCharToOutputArea(char ch) {
		outputArea.setText(outputArea.getText() + Character.toString(ch));
	}

	public synchronized void readCharByOutputArea() {
		outputArea.setEditable(true);
		outputArea.requestFocus();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (outputArea.hasFocus() && outputArea.isEditable()) {
			parser.setChar(e.getKeyChar());
			outputArea.setEditable(false);
		}
	}

	public void parse(String BFcode) {
		outputArea.setEditable(true);
		parser = new ParseMechanism();
		parser.deliverCode(BFcode);
		parserThr = new Thread(parser);
		parserThr.start();
	}

	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MainClass();
			}
		});
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
