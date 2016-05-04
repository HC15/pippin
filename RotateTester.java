package pippin;

import static org.junit.Assert.*;

import org.junit.Test;

public class RotateTester {

	@Test (expected=IllegalArgumentException.class) 
	// Test when there is an exception
	public void testROTexception() {
		MachineModel machine = new MachineModel();
		
		// creating a memory where argument will cause it to be out of bound
		for (int i = 0; i < Memory.DATA_SIZE; i++) {
			machine.setData(i, i + 500);
		}
		
		Instruction instr = machine.get(0X14);
		instr.execute(0, 0);
	}
	
	@Test (expected=IllegalArgumentException.class) 
	// Test when there is an exception with level
	public void testROTexceptionLevel() {
		MachineModel machine = new MachineModel();
		
		// creating a memory where argument will cause it to be out of bound
		for (int i = 0; i < Memory.DATA_SIZE; i++) {
			machine.setData(i, i + 500);
		}
		
		Instruction instr = machine.get(0X14);
		instr.execute(0, 1);
	}
	
	@Test
	// Test when working correctly
	public void testROT() {
		MachineModel machine = new MachineModel();
		
		for (int i = 0; i < Memory.DATA_SIZE; i++) {
			machine.setData(i, i - 3);
		}
		for (int i = 0; i < 3; i++) {
			machine.setData(i, 12 / (i + 2));
		}
		
		MachineModel machineCopy = machine;
		int[] rotNum = {4,5,6,3};
		for(int i = 6; i < 10; i++) {
			machine.setData(i, rotNum[i - 6]);
		}
		
		Instruction instr = machine.get(0X14);
		instr.execute(0, 0);
		
		assertEquals(machineCopy,machine);
	}
}
