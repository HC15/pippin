package pippin;

public interface Instruction {
	void execute(int arg, int indirectionLevel);
}