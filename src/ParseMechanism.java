import java.util.Stack;

public class ParseMechanism implements Runnable {
	private Flag flag; //current state of parse Mechanism

	final int MAX_MEM_SIZE = 30000; // number of memory cells
	private byte memory[] = new byte[MAX_MEM_SIZE]; // memory cells
	private int ptr = 0; // pointer to memory cell

	private int instrCounter; // count instructions of code
	private int totalInstrCounter;

	private char code[] = null; // to store parsed code

	private boolean killThread;
	private boolean threadSuspended;

	public ParseMechanism() {
		init();
	}

	private void init() {
		for(byte b : memory)
			b = 0;
		
		flag = new Flag(Flag.DOING_NOTHING);

		instrCounter = -1; // -1 because of parseCode implementation & structure
		totalInstrCounter = 0;
		ptr = 0;

		threadSuspended = false;
		killThread = false;
	}

	private int countAllOccurrences(String str, char ch) {
		int lastIndex = str.indexOf(ch);
		int count = 0;
		while (lastIndex >= 0) {
			lastIndex = str.indexOf(ch, lastIndex + 1);
			count++;
		}

		return count;
	}

	public Flag deliverCode(String code) {
		init();

		code.replaceAll("\\w", ""); // erase all characters that are not special chars

		// check if number of [ and ] is equal
		int loopB = countAllOccurrences(code, '[');
		int loopE = countAllOccurrences(code, ']');
		if (loopB != loopE) {
			flag.current = Flag.LOOP_ERROR;
		} else
			flag.current = Flag.PARSED;

		totalInstrCounter = code.length(); // count all instructions
		this.code = new char[totalInstrCounter];
		this.code = code.trim().toCharArray();

		return flag;
	}

	private void parseCode() {
		
		while(!threadSuspended) {
	
			++instrCounter;
			flag.current = Flag.IN_PROGRESS;
	
			if (instrCounter == totalInstrCounter) { // there are no further instructions
				flag.current = Flag.FINISH;
				return;
			}
	
			switch (code[instrCounter]) {
			case '.': // print mem cell
				flag.current = Flag.TO_PRINT; // there is something to print
				pauseThread();
				return;
	
			case ',': // get char and write its value to the mem cell
				flag.current = Flag.GET_CHAR;
				pauseThread();
				return;
	
			case '>':
				++ptr;
				if (ptr >= MAX_MEM_SIZE) {
					ptr = 0;
				}
				break;
	
			case '<':
				--ptr;
				if (ptr < 0) {
					ptr = MAX_MEM_SIZE - 1;
				}
				break;
	
			case '+':
				memory[ptr] = (memory[ptr] == Byte.MAX_VALUE) ? 0 : ++memory[ptr]; // validation of byte limit
				break;
	
			case '-':
				memory[ptr] = (memory[ptr] > 0) ? --memory[ptr] : 0; // check if cell value > 0, decrements
				break;
	
			case '[':
				if(memory[ptr] == 0) //if the condition of exiting loop is met
					moveToMatchingCloseBracket(instrCounter); // skip to the matching end for the loop
				//else run code in loop (go to next instr.)
				break;
	
			case ']':		
				if(memory[ptr] != 0) { // if condition of breaking loop is not met
					moveToMatchingOpenBracket(instrCounter); // repeat loop
				}
				//else move to the next instr.
				break;
			}
		}
	}
	
	private void moveToMatchingCloseBracket(int fromPoint) { // search matching ] for [ standing at fromPoint position
		int bracketsNum = 1;
		int endBracketPos = fromPoint;
		while(bracketsNum > 0) {
			if(endBracketPos > totalInstrCounter) // It shouldn't be true because of validation in deliverCode() but... :)
				return; //exit function
			
			endBracketPos++;
			if(code[endBracketPos] == '[')
				bracketsNum++;
			if(code[endBracketPos] == ']')
				bracketsNum--;
		}
		
		instrCounter = endBracketPos;
	}
	
	private void moveToMatchingOpenBracket(int fromPoint) { // search matching [ for ] standing at fromPoint position
		int bracketsNum = 1;
		int begBracketPos = fromPoint;
		while(bracketsNum > 0) {
			if(begBracketPos < 0) //same as in moveToMatchingCloseBracket()
				return;
			
			begBracketPos--;
			if(code[begBracketPos] == '[')
				bracketsNum--;
			if(code[begBracketPos] == ']')
				bracketsNum++;
		}
		
		instrCounter = begBracketPos;
	}
	
	private synchronized void continueThread() {
		flag.current = Flag.IN_PROGRESS;
		threadSuspended = false;
		notify();
	}
	
	private void pauseThread() {
		threadSuspended = true;
	}
	
	public synchronized char getCharToPrint() {
		continueThread();
		notify();
		return (char) memory[ptr];
	}

	public synchronized void setChar(char ch) {
		if (flag.current == Flag.GET_CHAR) {
			memory[ptr] = (byte) ch;
			if((int)ch == 27) memory[ptr] = 0; //escape
			continueThread();
		}
	}
	
	public Flag getState() {
		return flag;
	}

	@Override
	public void run() {
		while (!killThread) {
			try {
				Thread.sleep(1);
				
				if(flag.current == Flag.FINISH) {
					stopThr(); // kill thread
					flag.current = Flag.FINISH; //reassigned because stopThr() sets flag TERMINATED
				}
				
				else if(flag.current == Flag.IN_PROGRESS || flag.current == Flag.PARSED){
					parseCode();
				}
				
				if (threadSuspended) { //waiting for char input or printing
					synchronized (this) {
						while (threadSuspended) {
							wait();
						}
					}
				}
			} catch (InterruptedException e) {
			}
		}
	}

	public synchronized void stopThr() {
		flag.current = Flag.TERMINATED;
		killThread = true;
		notify();
	}
}
