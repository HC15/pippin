package pippin;

import java.util.Map;
import java.util.TreeMap;
import java.util.Observable;

public class MachineModel extends Observable {

	public class Registers {
		private int accumulator;
		private int programCounter;
	}

	public final Map<Integer, Instruction> INSTRUCTION_MAP = new TreeMap();
	private Registers cpu = new Registers();
	private Memory memory = new Memory();
	private boolean withGUI = false;
	private Code code;
	private boolean running = false;

	public MachineModel(boolean withGUI){
		this.withGUI = withGUI;

		//INSTRUCTION_MAP entry for "NOP"
		INSTRUCTION_MAP.put(0x0, (arg,level) -> {
			if(level != 0) {
				throw new IllegalArgumentException(
						"Illegal indirection level in NOP instruction");
			}
			if (level == 0) {
				cpu.programCounter ++;
			}
		});

		//INSTRUCTION_MAP entry for "LOD"
		INSTRUCTION_MAP.put(0x1, (arg,level) -> {
			if(level < 0 || level > 2) {
				throw new IllegalArgumentException(
						"Illegal indirection level in LOD instruction");
			}
			if (level > 0) {
				INSTRUCTION_MAP.get(0x1).execute(memory.getData(arg), level-1);
			}
			else {
				cpu.accumulator = arg;
				cpu.programCounter ++;
			}			
		});


		//INSTRUCTION_MAP entry for "STO"
		INSTRUCTION_MAP.put(0x2, (arg,level) -> {
			if(level < 1 || level > 2) {
				throw new IllegalArgumentException(
						"Illegal indirection level in STO instruction");
			}
			if (level == 1) {
				memory.setData(arg, cpu.accumulator);
				cpu.programCounter ++;
			}
			else {
				INSTRUCTION_MAP.get(0x2).execute(memory.getData(arg), level-1);
			}

		});

		//INSTRUCTION_MAP entry for "ADD"
		INSTRUCTION_MAP.put(0x3, (arg,level) -> {
			if(level < 0 || level > 2) {
				throw new IllegalArgumentException(
						"Illegal indirection level in ADD instruction");
			}
			if (level > 0) {
				INSTRUCTION_MAP.get(0x3).execute(memory.getData(arg), level-1);
			} else {
				cpu.accumulator += arg;
				cpu.programCounter ++;
			}
		});

		//INSTRUCTION_MAP entry for "SUB"
		INSTRUCTION_MAP.put(0x4, (arg,level) -> {
			if(level < 0 || level > 2) {
				throw new IllegalArgumentException(
						"Illegal indirection level in SUB instruction");
			}
			if (level > 0) {
				INSTRUCTION_MAP.get(0x4).execute(memory.getData(arg), level-1);
			} else {
				cpu.accumulator -= arg;
				cpu.programCounter ++;
			}
		});

		//INSTRUCTION_MAP entry for "MUL"
		INSTRUCTION_MAP.put(0x5, (arg,level) -> {
			if(level < 0 || level > 2) {
				throw new IllegalArgumentException(
						"Illegal indirection level in MUL instruction");
			}
			if (level > 0) {
				INSTRUCTION_MAP.get(0x5).execute(memory.getData(arg), level-1);
			} else {
				cpu.accumulator *= arg;
				cpu.programCounter ++;
			}
		});

		//INSTRUCTION_MAP entry for "DIV"
		INSTRUCTION_MAP.put(0x6, (arg,level) -> {
			if(level < 0 || level > 2) {
				throw new IllegalArgumentException(
						"Illegal indirection level in DIV instruction");
			}
			if (level > 0) {
				INSTRUCTION_MAP.get(0x6).execute(memory.getData(arg), level-1);
			} else {
				if (arg == 0) {
					throw new DivideByZeroException("Division by Zero");
				}
				cpu.accumulator /= arg;
				cpu.programCounter ++;
			}
		});

		//INSTRUCTION_MAP entry for "AND"
		INSTRUCTION_MAP.put(0x7, (arg,level) -> {
			if(level < 0 || level > 1) {
				throw new IllegalArgumentException(
						"Illegal indirection level in AND instruction");
			}
			if (level > 0) {
				INSTRUCTION_MAP.get(0x7).execute(memory.getData(arg), level-1);
			} else {
				if (arg != 0 && cpu.accumulator != 0) {
					cpu.accumulator = 1;
				}
				else {
					cpu.accumulator = 0;
				}
				cpu.programCounter ++;
			}
		});

		//INSTRUCTION_MAP entry for "NOT"
		INSTRUCTION_MAP.put(0x8, (arg,level) -> {
			if(level != 0) {
				throw new IllegalArgumentException(
						"Illegal indirection level in NOT instruction");
			}
			if(cpu.accumulator == 0){
				cpu.accumulator = 1;
			}
			else {
				cpu.accumulator = 0;
			}
			cpu.programCounter ++;
		});

		//INSTRUCTION_MAP entry for "CMPZ"
		INSTRUCTION_MAP.put(0x9, (arg,level) -> {
			if(level != 1) {
				throw new IllegalArgumentException(
						"Illegal indirection level in CMPZ instruction");
			}
			if(memory.getData(arg) == 0){
				cpu.accumulator = 1;
			}
			else{
				cpu.accumulator = 0;
			}
			cpu.programCounter ++;
		});

		//INSTRUCTION_MAP entry for "CMPL"
		INSTRUCTION_MAP.put(0xA, (arg,level) -> {
			if(level != 1) {
				throw new IllegalArgumentException(
						"Illegal indirection level in CMPL instruction");
			}
			if(memory.getData(arg) < 0){
				cpu.accumulator = 1;
			}
			else{
				cpu.accumulator = 0;
			}
			cpu.programCounter ++;
		});

		//INSTRUCTION_MAP entry for "JUMP"
		INSTRUCTION_MAP.put(0xB, (arg,level) -> {
			if(level < 0 || level > 1) {
				throw new IllegalArgumentException(
						"Illegal indirection level in JUMP instruction");
			}
			if(level != 0){
				INSTRUCTION_MAP.get(0xB).execute(memory.getData(arg), level-1);
			}
			else{
				cpu.programCounter = arg;
			}
		});

		//INSTRUCTION_MAP entry for "JMPZ"
		INSTRUCTION_MAP.put(0xC, (arg,level) -> {
			if(level < 0 || level > 1) {
				throw new IllegalArgumentException(
						"Illegal indirection level in JMPZ instruction");
			}
			if(level != 0){
				INSTRUCTION_MAP.get(0xC).execute(memory.getData(arg), level-1);
			}
			else {
				if(cpu.accumulator == 0){
					cpu.programCounter = arg; 
				}
				else {
					cpu.programCounter ++;
				}
			}
		});

		//INSTRUCTION_MAP entry for "HALT"
		INSTRUCTION_MAP.put(0xF, (arg,level) -> {
			halt();
		});


		//INSTRUCTION_MAP entry for "ROT"
		/*
		 * This instruction rotates data inside memory. Instruction uses arg to get a data value
		 * to use as its starting value, then the arg + 1 to get the length of how many values are
		 * being rotated, and arg + 2 to get amount of moves. When move is greater than zero, the
		 * data values from start to its length are moved to the next value, with the starting value
		 * replaced by the last value. When amount of moves are less then zero, the values are rotated
		 * in the opposite direction, with the last value being the original starting value. Rotates the
		 * number of times specified from the value gotten from arg + 2.
		 */
		INSTRUCTION_MAP.put(0x14, (arg,level) -> {
			if(level != 0) {
				throw new IllegalArgumentException(
						"Illegal indirection level in ROT instruction");
			}
			else {
				int start = memory.getData(arg);
				int length = memory.getData(arg + 1);
				int move = memory.getData(arg + 2);
				if (start < 0 || length < 0 || 
						start + length - 1 >= Memory.DATA_SIZE ||
						(start <= arg + 2 && start + length - 1 >= arg)) {
					throw new IllegalArgumentException();
				}
				if(move > 0) {
					for(int i = 0; i < move; i++){
						int tempVal = memory.getData(start);    
						cpu.accumulator = memory.getData(start + length - 1);
						memory.setData(start, cpu.accumulator);
						for (int j = 1; j < length; j++) {
							cpu.accumulator = tempVal;
							tempVal = memory.getData(start + j);
							memory.setData(start + j, cpu.accumulator);
						}
					}
				}
				else if(move < 0){
					move = move + length;
					for(int i = 0; i < move; i++){
						int tempVal = memory.getData(start);    
						cpu.accumulator = memory.getData(start + length - 1);
						memory.setData(start, cpu.accumulator);
						for(int j = 0; j < length; j++){
							cpu.accumulator = tempVal;
							tempVal = memory.getData(start + j + 1);
							memory.setData(start + j + 1, cpu.accumulator);
						}
					}
				}
				cpu.programCounter++;
			}
		});
	}

	public MachineModel(){
		this(false);
	}

	public int getData(int index) {
		return memory.getData(index);
	}

	int[] getData() {
		return memory.getData();
	}

	public Instruction get(Integer key) {
		return INSTRUCTION_MAP.get(key);
	}

	public int getProgramCounter() {
		return cpu.programCounter;
	}

	public int getAccumulator() {
		return cpu.accumulator;
	}

	public int getChangedIndex() {
		return memory.getChangedIndex();
	}

	public Code getCode() {
		return code;
	}

	public void setData(int index, int value){
		memory.setData(index, value);
	}

	public void setAccumulator(int i) {
		cpu.accumulator = i;
	}

	public void setProgramCounter(int i) {
		cpu.programCounter = i;
	}

	public void setCode(Code code){
		this.code = code;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isRunning() {
		return running;
	}

	public void halt() {
		if(withGUI) {
			running = false;
		} else {
			System.exit(0);
		}
	}

	public void clearMemory() {
		memory.clear();
	}

	public void step() {
		try {
			int pc = cpu.programCounter;
			int opcode = code.getOp(pc);
			int arg = code.getArg(pc);
			int indirectionLevel = code.getIndirectionLevel(pc);
			get(opcode).execute(arg, indirectionLevel);
		} catch (Exception e) {
			halt();
			throw e;
		}
	}

	public void clear() {
		memory.clear();
		if (code != null) {
			code.clear();
		}
		cpu.accumulator = 0;
		cpu.programCounter = 0;
	}
}