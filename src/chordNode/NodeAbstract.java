package chordNode;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public interface NodeAbstract extends Remote{
	
	//Node methods
	int getExpectedSuccessor() throws RemoteException;
	int getSuccessor() throws RemoteException;
	int getPredecessor() throws RemoteException;
	
	void simulate(int virtSuccessor, int virtPredecessor) throws RemoteException;
	
	void create(int nodeIp, DistributedCount distCount, Semaphore countLock, Semaphore dataLock, Semaphore mutex) throws Exception;
	void join(int remoteNode) throws RemoteException;
	int findSuccessor(int target) throws RemoteException;
	//Chord software will invoke findSuccessor to insert key:value in a node
	
	void notify(int predNode) throws RemoteException;
	void stabilize() throws RemoteException;
	void updateFingers() throws RemoteException;
	void storeData(int key, int value) throws RemoteException;
	int getData(int key) throws RemoteException;
	HashMap<Integer, Integer> getHashMapWithKey(int key) throws RemoteException;
}
