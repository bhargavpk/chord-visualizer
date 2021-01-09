package chordNode;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeAbstract extends Remote{
	
	//Node methods
	int getExpectedSuccessor() throws RemoteException;
	int getSuccessor() throws RemoteException;
	int getPredecessor() throws RemoteException;
	int[] getFingerTable() throws RemoteException;
	
	void simulate(int virtSuccessor, int virtPredecessor) throws RemoteException;
	
	int create(int nodeIp) throws Exception;
	void join(int remoteNode) throws RemoteException;
	int findSuccessor(int target) throws RemoteException;
	//Chord software will invoke findSuccessor to insert key:value in a node
	
	void notify(int predNode) throws RemoteException;
	void stabilize() throws RemoteException;
	void updateFingers() throws RemoteException;
}
