public class Flag {
	static final int GET_CHAR = 1;
	static final int FINISH = 2;
	static final int IN_PROGRESS = 3;
	static final int LOOP_ERROR = 4;
	static final int OUT_OF_MEM_BOUNDS = 5;
	static final int DOING_NOTHING = 6;
	static final int PARSED = 7;
	static final int TO_PRINT = 8;
	
	int current = DOING_NOTHING;
	
	public Flag(int state) {
		current = state;
	}
}
