package chordNode;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeAbstract extends Remote{
	
	//Node methods
	int getSuccessor() throws RemoteException;
	int getPredecessor() throws RemoteException;
	int create() throws Exception;
	void join(int remoteNode) throws RemoteException;
	int findSuccessor(int target) throws RemoteException;
	//Chord software will invoke findSuccessor to insert key:value in a node
	
	void notify(int predNode) throws RemoteException;
	void stabilize() throws RemoteException;
	void updateFingers() throws RemoteException;
}
