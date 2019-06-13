package ist.meic.pa.FunctionalProfiler;

public class MyCountRegister {

	private int readNumber;
	private int writeNumber;

	public MyCountRegister() {
		this.readNumber=0;
		this.writeNumber=0;
	}

	public void incrRead() {
		readNumber++;
	}

	public void incrWrite() {
		writeNumber++;
	}

	public int getReads() {
		return readNumber;
	}

	public int getWrites() {
		return writeNumber;
	}
}
