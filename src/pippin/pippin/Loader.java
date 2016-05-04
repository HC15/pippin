package pippin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class Loader {
	public static void load(MachineModel model, Code code, File file) throws FileNotFoundException {
		if(model == null || code == null || file == null) return;
		try(Scanner input = new Scanner(file)) {
			boolean incode = true;
			while(input.hasNextLine()) {
				String line = input.nextLine();
				Scanner parser = new Scanner(line);
				int first = parser.nextInt(16);
				if(incode && first == -1) {
					incode = false;
				}
				else if(incode) {
					int arg = parser.nextInt(16);
					int level = parser.nextInt(16);
					code.setCode(first,arg,level);
				}
				else {
					int value = parser.nextInt(16);
					model.setData(first,value);
				}
				parser.close();
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			JOptionPane.showMessageDialog(null,
					e.getMessage(),
					"Failure loading data", JOptionPane.WARNING_MESSAGE);
		} catch (NoSuchElementException e) {
			JOptionPane.showMessageDialog(null,
					"NoSuchElementException",
					"Failure loading data", JOptionPane.WARNING_MESSAGE);
		}
	}
}
