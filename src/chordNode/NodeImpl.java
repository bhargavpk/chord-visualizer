package chordNode;

import registry.*;
import java.util.Random;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class NodeImpl implements NodeAbstract{
	
	int nodeId = -1; 	//To be modified when joined
						//Models hashed IP Address of server
	int successor = -1;	//To be modified to successor list
	int predecessor = -1;
	int fingerTableSize = 7;
	FingerTable fingerTable = new FingerTable(fingerTableSize);
	
	BigInteger encryptString (String str) throws NoSuchAlgorithmException 
	{
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] messageDigest = md.digest(str.getBytes());
		BigInteger ans = new BigInteger(1,messageDigest);
		return ans;
	}
	
	public void create()
	{
		Random random = new Random();
		int randNum = random.nextInt();
		if(randNum < 0)
			randNum *= -1;
		try {
			
			BigInteger bigNodeId = encryptString(String.valueOf(randNum)).mod(new BigInteger("128"));
			this.nodeId = bigNodeId.intValue();
			
		}catch(NoSuchAlgorithmException e) {
			
			//Handle error
			System.out.println("SHA Algo Exception:");
			e.printStackTrace();
		}
	}
	
	public void join(int remoteNode)	//Invoked by chord software
	{
		try {
			
			Registry register = LocateRegistry.getRegistry(null);
			NodeAbstract skeleton = (NodeAbstract)register.lookup(String.valueOf(remoteNode));
			this.successor = skeleton.findSuccessor(this.nodeId);
			
		}catch(Exception e) {
			//Handle error
			//Error possibly due to failure of a node
			System.out.println("Registry error:");
			e.printStackTrace();
		}
	}

}
