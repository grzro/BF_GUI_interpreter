import javax.swing.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.*;

public class MainClass extends JFrame implements KeyListener {

	private static final long serialVersionUID = 1L;

	private JTextArea codeArea;
	private JTextArea outputArea;
	private JScrollPane scrollCodeArea;
	private JScrollPane scrollOutputArea;
	private JButton runButton;
	private JButton terminateButton;
	private ActionListener actionLnr;
	private Font consoleLikeFont;
	private JLabel infoLabel;

	private ParseMechanism parser;
	private Thread updateGUIthread;
	private Thread parserThr;

	private String startCodeStr;

	public MainClass() {

		// EVENT HANDLING
		initActionListener();
		
		// code that is in codeArea at the program start
		setStartCode();

		// WINDOW
		setWindowProp();

		// Elements inits
		configureFont();

		configureGUIelements();

		// Adding elements to the window
		addElemToWindow();

	}

	private void setStartCode() {
		// Hello world program written to codeArea
		startCodeStr = "++++++++++\r\n" + "[\r\n" + ">+++++++>++++++++++>+++>+<<<<-\r\n" + "]\r\n"
				+ ">++.               drukuje 'H'\r\n" + ">+.                drukuje 'e'\r\n"
				+ "+++++++.           drukuje 'l'\r\n" + ".                  drukuje 'l'\r\n"
				+ "+++.               drukuje 'o'\r\n" + ">++.               spacja\r\n"
				+ "<<+++++++++++++++. drukuje 'W'\r\n" + ">.                 drukuje 'o'\r\n"
				+ "+++.               drukuje 'r'\r\n" + "------.            drukuje 'l'\r\n"
				+ "--------.          drukuje 'd'\r\n" + ">+.                drukuje '!'\r\n"
				+ ">.                 nowa linia";
	}
	
	private void setWindowProp() {
		setSize(540, 400);
		setTitle("BrainFuck Interpreter by G.Romanczyk");
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);
	}
	
	private void addElemToWindow() {
		add(scrollCodeArea);
		add(scrollOutputArea);
		add(runButton);
		add(terminateButton);
		add(infoLabel);
	}
	
	private void configureFont() {
		consoleLikeFont = new Font("Consolas", Font.PLAIN, 14);
	}
	
	private void configureGUIelements() {
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
		runButton.setMargin(new Insets(0, 0, 0, 0));
		runButton.addActionListener(actionLnr);

		terminateButton = new JButton("Terminate");
		terminateButton.setBounds(450, 40, 70, 30);
		terminateButton.addActionListener(actionLnr);
		terminateButton.setMargin(new Insets(0, 0, 0, 0));

		// labels with information about state of parsing and executing
		infoLabel = new JLabel("Ready");
		infoLabel.setBounds(450, 325, 90, 40);
		infoLabel.setOpaque(true);
		infoLabel.setVisible(true);
	}
	
	private void initActionListener() {
		actionLnr = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == runButton) {
					outputArea.setText("");
					parse(codeArea.getText());
					configAndRunThrGUIandParser();
				}
				if (e.getSource() == terminateButton) {
					parser.stopThr();
				}
			}
		};
	}

	private void configAndRunThrGUIandParser() {
		updateGUIthread = new Thread() {
			@Override
			public void run() {
				while (parser.getState().current != Flag.FINISH) {
					try {
						Thread.sleep(2);
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
							parser.stopThr();
							break;
						case Flag.OUT_OF_MEM_BOUNDS:
							outputArea.setText("Out od memory bounds");
							parser.stopThr();
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

	private void showInfo(Flag info) {
		switch (info.current) {
		case Flag.FINISH:
			infoLabel.setText("Finished");
			break;
		case Flag.IN_PROGRESS:
			infoLabel.setText("In progress...");
			break;
		case Flag.GET_CHAR:
			infoLabel.setText("Get char");
			break;
		case Flag.TERMINATED:
			infoLabel.setText("Terminated");
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
	public void keyTyped(KeyEvent e) { // keys without shift, ctrl etc
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
	public void keyPressed(KeyEvent e) { // any key, no need
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
