package pippin;
import java.util.Map;
import java.util.TreeMap;

public class InstructionMap {
	public static Map<String, Integer> opcode = new TreeMap<>();
	public static Map<Integer, String> mnemonics = new TreeMap<>();

	static {
		opcode.put("NOP", 0x0);
		opcode.put("LOD", 0x1);
		opcode.put("STO", 0x2);
		opcode.put("ADD", 0x3);
		opcode.put("SUB", 0x4);
		opcode.put("MUL", 0x5);
		opcode.put("DIV", 0x6);
		opcode.put("AND", 0x7);
		opcode.put("NOT", 0x8);
		opcode.put("CMPZ", 0x9);
		opcode.put("CMPL", 0xA);
		opcode.put("JUMP", 0xB);
		opcode.put("JMPZ", 0xC);
		opcode.put("HALT", 0xF);
		opcode.put("ROT", 0x14);
		
		for(String str : opcode.keySet()) {
			mnemonics.put(opcode.get(str), str);
		}
	}
}