package pippin;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class InstructionTester {

	MachineModel machine = new MachineModel();
	int[] dataCopy = new int[Memory.DATA_SIZE];
	int accInit;
	int ipInit;

	@Before
	public void setup() {
		for (int i = 0; i < Memory.DATA_SIZE; i++) {
			dataCopy[i] = -5*Memory.DATA_SIZE + 10*i;
			machine.setData(i, -5*Memory.DATA_SIZE + 10*i);
			// Initially the machine will contain a known spread
			// of different numbers: 
			// -2560, -2550, -2540, ..., 0, 10, 20, ..., 2550 
			// This allows us to check that the instructions do 
			// not corrupt machine unexpectedly.
			// 0 is at index 256
		}
		accInit = 0;
		ipInit = 0;
	}


	@Test
	public void testNOP(){
		Instruction instr = machine.get(0x0);
		instr.execute(0,0);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData());
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit+1,
				machine.getProgramCounter());
		//Test accumulator untouched
		assertEquals("Accumulator unchanged", accInit,
				machine.getAccumulator());
	}

	@Test
	// Test whether load is correct with direct addressing
	public void testLOD(){
		Instruction instr = machine.get(0x1);
		machine.setAccumulator(27);
		int arg = 12;
		// should load -2560+120 into the accumulator
		instr.execute(arg, 1);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData());
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit+1,
				machine.getProgramCounter());
		//Test accumulator modified
		assertEquals("Accumulator changed", -2560+120,
				machine.getAccumulator());
	}

	@Test
	// Test whether load is correct with immediate addressing
	public void testLODimmediate(){
		Instruction instr = machine.get(0x1);
		machine.setAccumulator(27);
		int arg = 12;
		// should load 12 into the accumulator
		instr.execute(arg, 0);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData());
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit+1,
				machine.getProgramCounter());
		//Test accumulator modified
		assertEquals("Accumulator changed", 12,
				machine.getAccumulator());
	}

	@Test
	// Test whether load is correct with direct addressing
	public void testLODindirect() {
		Instruction instr = machine.get(0x1);
		machine.setAccumulator(-1);
		int arg = 260;
		// should load data[-2560+2600] = data[40] = -2560 + 400
		// into the accumulator
		instr.execute(arg, 2);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData());
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit+1,
				machine.getProgramCounter());
		//Test accumulator modified
		assertEquals("Accumulator changed", -2560+400,
				machine.getAccumulator());
	}	

	@Test
	// Test whether store is correct with direct addressing
	public void testSTOdirect() {
		Instruction instr = machine.get(0x2);
		int arg = 12;
		machine.setAccumulator(567);
		dataCopy[12] = 567;
		instr.execute(arg, 1);
		//Test machine is changed correctly
		assertArrayEquals(dataCopy, machine.getData());
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit+1,
				machine.getProgramCounter());
		//Test accumulator unchanged
		assertEquals("Accumulator unchanged", 567,
				machine.getAccumulator());
	}

	@Test
	// Test whether store is correct with indirect addressing
	public void testSTOindirect() {
		Instruction instr = machine.get(0x2);
		int arg = 260; // -2560+2600 = 40
		machine.setAccumulator(567);
		dataCopy[40] = 567;
		instr.execute(arg, 2);
		//Test machine is changed correctly
		assertArrayEquals(dataCopy, machine.getData());
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit+1,
				machine.getProgramCounter());
		//Test accumulator unchanged
		assertEquals("Accumulator unchanged", 567,
				machine.getAccumulator());
	}

	@Test (expected=IllegalArgumentException.class)
	// Test whether STO throws exception with immediate addressing
	public void testSTOimmediate() {
		Instruction instr = machine.get(0x2);
		instr.execute(0, 0);
	}

	@Test 
	// this test checks whether the add is done correctly, when
	// addressing is immediate
	public void testADDimmediate() {
		Instruction instr = machine.get(0x3);
		int arg = 12; 
		machine.setAccumulator(200);
		instr.execute(arg, 0); 
		// should have added 12 to accumulator
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getProgramCounter());
		assertEquals("Accumulator was changed", 200+12,
				machine.getAccumulator());
	}

	@Test 
	// this test checks whether the add is done correctly, when
	// addressing is direct
	public void testADD() {
		Instruction instr = machine.get(0x3);
		int arg = 12; // we know that machine value is -2560+120
		machine.setAccumulator(200);
		instr.execute(arg, 1); 
		// should have added -2560+120 to accumulator
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getProgramCounter());
		assertEquals("Accumulator was changed", 200-2560+120,
				machine.getAccumulator());
	}

	@Test 
	// this test checks whether the add is done correctly, when
	// addressing is indirect
	public void testADDindirect() {
		Instruction instr = machine.get(0x3);
		int arg = 260; // we know that address is -2560+2600 = 40
		// and the machine value is data[40] = -2560+400 = -2160 
		machine.setAccumulator(200);
		instr.execute(arg, 2); 
		// should have added -2560+400 to accumulator
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getProgramCounter());
		assertEquals("Accumulator was changed", 200-2560+400,
				machine.getAccumulator());
	}

	@Test 
	// this test checks whether the subtract is done correctly, when
	// addressing is immediate
	public void testSUBimmediate() {
		Instruction instr = machine.get(0x4);
		int arg = 12; 
		machine.setAccumulator(200);
		instr.execute(arg, 0); 
		// should have subtracted 12 from accumulator
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getProgramCounter());
		assertEquals("Accumulator was changed", 200-12,
				machine.getAccumulator());
	}

	@Test 
	// this test checks whether the subtract is done correctly, when
	// addressing is direct
	public void testSUB() {
		Instruction instr = machine.get(0x4);
		int arg = 12; // we know that machine value is -2560+120
		machine.setAccumulator(200);
		instr.execute(arg, 1); 
		// should have subtracted -2560+120 from accumulator
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getProgramCounter());
		assertEquals("Accumulator was changed", 200+2560-120,
				machine.getAccumulator());
	}

	@Test 
	// this test checks whether the subtract is done correctly, when
	// addressing is indirect
	public void testSUBindirect() {
		Instruction instr = machine.get(0x4);
		int arg = 260; // we know that address is -2560+2600 = 40
		// and the machine value is data[40] = -2560+400 = -2160 
		machine.setAccumulator(200);
		instr.execute(arg, 2); 
		// should have subtracted -2560+400 from accumulator
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getProgramCounter());
		assertEquals("Accumulator was changed", 200+2560-400,
				machine.getAccumulator());
	}

	@Test 
	// this test checks whether the multiplication is done correctly, when
	// addressing is immediate
	public void testMULimmediate() {
		Instruction instr = machine.get(0x5);
		int arg = 12; 
		machine.setAccumulator(200);
		instr.execute(arg, 0); 
		// should have multiplied accumulator by 12
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getProgramCounter());
		assertEquals("Accumulator was changed", 200*12,
				machine.getAccumulator());
	}

	@Test 
	// this test checks whether the multiplication is done correctly, when
	// addressing is direct
	public void testMUL() {
		Instruction instr = machine.get(0x5);
		int arg = 12; // we know that machine value is -2560+120
		machine.setAccumulator(200);
		instr.execute(arg, 1); 
		// should have multiplied accumulator by -2560+120 
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getProgramCounter());
		assertEquals("Accumulator was changed", 200*(-2560+120),
				machine.getAccumulator());
	}

	@Test 
	// this test checks whether the multiplication is done correctly, when
	// addressing is indirect
	public void testMULindirect() {
		Instruction instr = machine.get(0x5);
		int arg = 260; // we know that address is -2560+2600 = 40
		// and the machine value is data[40] = -2560+400 = -2160 
		machine.setAccumulator(200);
		instr.execute(arg, 2); 
		// should have multiplied to accumulator -2560+400
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getProgramCounter());
		assertEquals("Accumulator was changed", 200*(-2560+400),
				machine.getAccumulator());
	}


	@Test 
	// this test checks whether the division is done correctly, when
	// addressing is immediate
	public void testDIVimmediate() {
		Instruction instr = machine.get(0x6);
		int arg = 12; 
		machine.setAccumulator(200);
		instr.execute(arg, 0); 
		// should have divided accumulator by 12
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getProgramCounter());
		assertEquals("Accumulator was changed", 200/12,
				machine.getAccumulator());
	}

	@Test 
	// this test checks whether the division is done correctly, when
	// addressing is direct
	public void testDIV() {
		Instruction instr = machine.get(0x6);
		int arg = 12; // we know that machine value is -2560+120
		machine.setAccumulator(200);
		instr.execute(arg, 1); 
		// should have divided accumulator by -2560+120 
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getProgramCounter());
		assertEquals("Accumulator was changed", 200/(-2560+120),
				machine.getAccumulator());
	}

	@Test 
	// this test checks whether the division is done correctly, when
	// addressing is indirect
	public void testDIVindirect() {
		Instruction instr = machine.get(0x6);
		int arg = 260; // we know that address is -2560+2600 = 40
		// and the machine value is data[40] = -2560+400 = -2160 
		machine.setAccumulator(200);
		instr.execute(arg, 2); 
		// should have divided to accumulator -2560+400
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit + 1,
				machine.getProgramCounter());
		assertEquals("Accumulator was changed", 200/(-2560+400),
				machine.getAccumulator());
	}

	@Test (expected=DivideByZeroException.class) 
	// this test checks whether the DivideByZeroException is thrown 
	// for immediate division by 0
	public void testDIVZEROImmediate() {
		Instruction instr = machine.get(0x6);
		int arg = 0; 
		instr.execute(arg, 0);
	}

	@Test (expected=DivideByZeroException.class) 
	// this test checks whether the DivideByZeroException is thrown 
	// for division by 0 from machine
	public void testDIVZERODirect() {
		Instruction instr = machine.get(0x6);
		int arg = 256; 
		instr.execute(arg, 1);
	}

	@Test (expected=DivideByZeroException.class) 
	// this test checks whether the DivideByZeroException is thrown 
	// for division by 0 from machine
	public void testDIVZEROIndirect() {
		Instruction instr = machine.get(0x6);
		machine.setData(100, 256);
		int arg = 100; 
		instr.execute(arg, 2);
	}


	@Test 
	// this test checks whether the jump is done correctly, when
	// addressing is direct
	public void testJUMPimmediate() {
		Instruction instr = machine.get(0xB);
		int arg = 260;  
		machine.setAccumulator(200);
		instr.execute(arg, 0); 
		// should have set the program counter to 40
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was changed", 260,
				machine.getProgramCounter());
		assertEquals("Accumulator was not changed", 200,
				machine.getAccumulator());
	}


	@Test 
	// this test checks whether the jump is done correctly, when
	// addressing is indirect
	public void testJUMPdirect() {
		Instruction instr = machine.get(0xB);
		int arg = 260; // the machine value is data[260] = -2560+2600 = 40 
		machine.setAccumulator(200);
		instr.execute(arg, 1); 
		// should have set the program counter to 40
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was changed", 40,
				machine.getProgramCounter());
		assertEquals("Accumulator was not changed", 200,
				machine.getAccumulator());
	}

	@Test (expected=IllegalArgumentException.class)
	// Test whether JUMP throws exception with immediate addressing
	public void testJUMPindirect() {
		Instruction instr = machine.get(0xB);
		instr.execute(0, 2);
	}

	@Test 
	// this test checks whether the jump is done correctly, when
	// addressing is direct
	public void testJMPZimmediateAccumZero() {
		Instruction instr = machine.get(0xC);
		int arg = 260;  
		machine.setAccumulator(0);
		instr.execute(arg, 0); 
		// should have set the program counter to 40
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was changed", 260,
				machine.getProgramCounter());
		assertEquals("Accumulator was not changed", 0,
				machine.getAccumulator());
	}

	@Test 
	// this test checks whether the jump is done correctly, when
	// addressing is indirect
	public void testJMPZdirectAccumZero() {
		Instruction instr = machine.get(0xC);
		int arg = 260; // the machine value is data[260] = -2560+2600 = 40 
		machine.setAccumulator(0);
		instr.execute(arg, 1); 
		// should have set the program counter to 40
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was changed", 40,
				machine.getProgramCounter());
		assertEquals("Accumulator was not changed", 0,
				machine.getAccumulator());
	}

	@Test 
	// this test checks whether no jump is done if accumulator is zero, 
	// when addressing is direct
	public void testJMPZimmedAccumNonZero() {
		Instruction instr = machine.get(0xC);
		int arg = 260;  
		machine.setAccumulator(200);
		instr.execute(arg, 0); 
		// should have set the program counter incremented
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit+1,
				machine.getProgramCounter());
		assertEquals("Accumulator was not changed", 200,
				machine.getAccumulator());
	}

	@Test 
	// this test checks whether no jump is done if accumulator is zero, 
	// when addressing is indirect
	public void testJMPZdirectAccumNonZero() {
		Instruction instr = machine.get(0xC);
		int arg = 260; // the machine value is data[260] = -2560+2600 = 40 
		machine.setAccumulator(200);
		instr.execute(arg, 1); 
		// should have set the program counter incremented
		assertArrayEquals(dataCopy, machine.getData()); 
		assertEquals("Program counter was incremented", ipInit+1,
				machine.getProgramCounter());
		assertEquals("Accumulator was not changed", 200,
				machine.getAccumulator());
	}


	@Test (expected=IllegalArgumentException.class)
	// Test whether JMPZ throws exception with immediate addressing
	public void testJMPZindirect() {
		Instruction instr = machine.get(0xC);
		instr.execute(0, 2);
	}

	@Test
	// Check CMPL when comparing less than 0 gives true
	public void testCMPLmemLT0() {
		Instruction instr = machine.get(0xA);
		int arg = 100;
		instr.execute(arg, 1);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 1", 1,
				machine.getAccumulator());
	}

	@Test
	// Check CMPL when comparing equal to 0 gives false
	public void testCMPLmemEQ0() {
		Instruction instr = machine.get(0xA);
		int arg = 256;
		instr.execute(arg, 1);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 0", 0,
				machine.getAccumulator());
	}

	@Test
	// Check CMPL when comparing greater than 0 gives false
	public void testCMPLmemGT0() {
		Instruction instr = machine.get(0xA);
		int arg = 300;
		instr.execute(arg, 1);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 0", 0,
				machine.getAccumulator());
	}

	@Test (expected=IllegalArgumentException.class)
	// Test whether CMPL throws exception with indirect addressing
	public void testCMPLindirect() {
		Instruction instr = machine.get(0xA);
		instr.execute(0, 2);
	}

	@Test (expected=IllegalArgumentException.class)
	// Test whether CMPL throws exception with indirect addressing
	public void testCMPLimmediate() {
		Instruction instr = machine.get(0xA);
		instr.execute(0, 0);
	}

	@Test
	// Check CMPZ when comparing less than 0 gives false
	public void testCMPZmemLT0() {
		Instruction instr = machine.get(0x9);
		int arg = 100;
		instr.execute(arg, 1);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 0", 0,
				machine.getAccumulator());
	}

	@Test
	// Check CMPZ when comparing equal to 0 gives true
	public void testCMPZmemEQ0() {
		Instruction instr = machine.get(0x9);
		int arg = 256;
		instr.execute(arg, 1);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 1", 1,
				machine.getAccumulator());
	}

	@Test
	// Check CMPZ when comparing greater than 0 gives false
	public void testCMPZmemGT0() {
		Instruction instr = machine.get(0x9);
		int arg = 300;
		instr.execute(arg, 1);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 0", 0,
				machine.getAccumulator());
	}

	@Test (expected=IllegalArgumentException.class)
	// Test whether CMPZ throws exception with immediate addressing
	public void testCMPZimmediate() {
		Instruction instr = machine.get(0x9);
		instr.execute(0, 0);
	}

	@Test (expected=IllegalArgumentException.class)
	// Test whether CMPZ throws exception with indirect addressing
	public void testCMPZindirect() {
		Instruction instr = machine.get(0x9);
		instr.execute(0, 2);
	}

	@Test
	// Check AND when accum and mem equal to 0 gives false
	public void testANDaccEQ0memEQ0() {
		Instruction instr = machine.get(0x7);
		int arg = 256;
		machine.setAccumulator(0);
		instr.execute(arg, 1);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 0", 0,
				machine.getAccumulator());
	}

	@Test
	// Check AND when accum and mem pos gives true
	public void testANDaccGT0memGT0() {
		Instruction instr = machine.get(0x7);
		int arg = 300;
		machine.setAccumulator(10);
		instr.execute(arg, 1);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 1", 1,
				machine.getAccumulator());
	}

	@Test
	// Check AND when accum and mem neg gives true
	public void testANDaccLT0memLT0() {
		Instruction instr = machine.get(0x7);
		int arg = 200;
		machine.setAccumulator(-10);
		instr.execute(arg, 1);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 1", 1,
				machine.getAccumulator());
	}

	@Test
	// Check AND when accum neg and mem pos gives true
	public void testANDaccLT0memGT0() {
		Instruction instr = machine.get(0x7);
		int arg = 300;
		machine.setAccumulator(-10);
		instr.execute(arg, 1);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 1", 1,
				machine.getAccumulator());
	}

	@Test
	// Check AND when accum pos and mem neg gives true
	public void testANDaccGT0memLT0() {
		Instruction instr = machine.get(0x7);
		int arg = 200;
		machine.setAccumulator(10);
		instr.execute(arg, 1);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 1", 1,
				machine.getAccumulator());
	}

	@Test
	// Check AND when accum pos mem equal to zero gives false
	public void testANDaccGT0memEQ0() {
		Instruction instr = machine.get(0x7);
		int arg = 256;
		machine.setAccumulator(10);
		instr.execute(arg, 1);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 0", 0,
				machine.getAccumulator());
	}

	@Test
	// Check AND when accum neg mem equal to zero gives false
	public void testANDaccLT0memEQ0() {
		Instruction instr = machine.get(0x7);
		int arg = 256;
		machine.setAccumulator(-10);
		instr.execute(arg, 1);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 0", 0,
				machine.getAccumulator());
	}

	@Test
	// Check AND when accum equal to zero and mem pos gives false
	public void testANDaccEQ0memGT0() {
		Instruction instr = machine.get(0x7);
		int arg = 300;
		machine.setAccumulator(0);
		instr.execute(arg, 1);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 0", 0,
				machine.getAccumulator());
	}

	@Test
	// Check AND when accum equal to zero and mem neg gives false
	public void testANDaccEQ0memLT0() {
		Instruction instr = machine.get(0x7);
		int arg = 200;
		machine.setAccumulator(0);
		instr.execute(arg, 1);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 0", 0,
				machine.getAccumulator());
	}


	@Test (expected=IllegalArgumentException.class)
	// Test whether AND throws exception with indirect addressing
	public void testANDindirect() {
		Instruction instr = machine.get(0x7);
		instr.execute(0, 2);
	}

	@Test
	// Check NOT greater than 0 gives false
	public void testNOTaccGT0() {
		Instruction instr = machine.get(0X8);
		machine.setAccumulator(10);
		instr.execute(0, 0);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 0", 0,
				machine.getAccumulator());
	}

	@Test
	// Check NOT equal to 0 gives true
	public void testNOTaccEQ0() {
		Instruction instr = machine.get(0X8);
		machine.setAccumulator(0);
		instr.execute(0, 0);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 1", 1,
				machine.getAccumulator());
	}

	@Test
	// Check NOT less than 0 gives false
	public void testNOTaccLT0() {
		Instruction instr = machine.get(0X8);
		machine.setAccumulator(-10);
		instr.execute(0, 0);
		//Test machine is not changed
		assertArrayEquals(dataCopy, machine.getData()); 
		//Test program counter incremented
		assertEquals("Program counter incremented", ipInit + 1,
				machine.getProgramCounter());
		//Accumulator is 1
		assertEquals("Accumulator is 0", 0,
				machine.getAccumulator());
	}

	@Test (expected=IllegalArgumentException.class)
	// Test whether NOT throws exception with immediate addressing
	public void testNOTdirect() {
		Instruction instr = machine.get(0X8);
		instr.execute(0, 1);
	}

	@Test (expected=IllegalArgumentException.class)
	// Test whether NOT throws exception with indirect addressing
	public void testNOTindirect() {
		Instruction instr = machine.get(0X8);
		instr.execute(0, 2);
	}

}
