import java.util.Arrays;
import java.util.Stack;

public class ParseMechanism implements Runnable {
	private Flag flag;

	final int MAX_MEM_SIZE = 3000; // number of memory cells
	private int memory[] = new int[MAX_MEM_SIZE]; // memory cells
	private int ptr = 0; // pointer to memory cell

	private Stack<Integer> loopPoints = new Stack<Integer>(); // for '[' points, stack because LIFO

	private int instrCounter = 0; // count instructions of code
	private int totalInstrCounter = 0;

	private char code[] = null; // to store parsed code

	private boolean killThread;
	private boolean threadSuspended;

	public ParseMechanism() {
		init();
	}

	private void init() {		
		Arrays.fill(memory, 0); // fill every memory cell with 0

		flag = new Flag(Flag.DOING_NOTHING);

		instrCounter = -1;
		totalInstrCounter = 0;

		threadSuspended = false;
		killThread = false;
	}

	public static int countAllOccurrences(String str, char ch) {
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

	private Flag parseCodeChar() {
		
		if(threadSuspended)
			return flag;

		++instrCounter;

		if (instrCounter == totalInstrCounter) { // there are no further instructions
			flag.current = Flag.FINISH;
			return flag;
		}
		
		flag.current = Flag.IN_PROGRESS;

		switch (code[instrCounter]) {
		case '.': // print mem cell
			flag.current = Flag.TO_PRINT; // there is something to print
			pauseThread();
			return flag;

		case ',': // get char and write its value to the mem cell
			flag.current = Flag.GET_CHAR;
			pauseThread();
			return flag;

		case '>':
			if (++ptr > MAX_MEM_SIZE) {
				flag.current = Flag.OUT_OF_MEM_BOUNDS;
				return flag;
			}
			break;

		case '<':
			if (--ptr < 0) {
				flag.current = Flag.OUT_OF_MEM_BOUNDS;
				return flag;
			}
			break;

		case '+':
			memory[ptr] = (memory[ptr] > Integer.MAX_VALUE) ? Integer.MAX_VALUE : ++memory[ptr]; // validation of int limit
			break;

		case '-':
			memory[ptr] = (memory[ptr] > 0) ? --memory[ptr] : 0; // check if cell value > 0, decrements
			break;

		case '[':
			loopPoints.push(instrCounter); // add begin-loop-point on the stack
			break;

		case ']':
			if (memory[ptr] != 0) { // if it is not the end of the loop
				instrCounter = loopPoints.peek(); // move to the instruction position next of last '[' (loop begin)
			} else if (!loopPoints.empty())
				loopPoints.pop(); // remove last begin-loop-point from stack
			break;
		}
		return new Flag(Flag.IN_PROGRESS);
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
			memory[ptr] = (int) ch;
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
					flag = parseCodeChar();
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
