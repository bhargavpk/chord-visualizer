package chordNode;

import java.util.Arrays;

class FingerTable {
	
	int size;
	int fingerTable[];
	
	FingerTable()
	{
		this.size = 7;	//default size
		this.fingerTable = new int[7];
		Arrays.fill(this.fingerTable, -1);
	}
	FingerTable(int tableSize)
	{
		this.size = tableSize;
		this.fingerTable = new int[tableSize];
		Arrays.fill(this.fingerTable, -1);	//Later filled by stabilization
	}
	int get(int index)
	{
		if((index >= 0)&&(index < this.size))
			return this.fingerTable[index];
		return -1;
	}
	void set(int index,int value)
	{
		this.fingerTable[index] = value;
	}

}
