package chordNode;

import java.rmi.Remote;

interface NodeAbstract extends Remote{
	
	//Node methods
	public void create();
	void join(int remoteNode);
	int findSuccessor(int node);	//node and nodeId interchangeably used
}
