package chordNode;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ChordSoft {

	public static void main(String[] args)
	{
		NodeImpl node1 = new NodeImpl();
		NodeImpl node2 = new NodeImpl();
		try 
		{
			int nodeId1 = node1.create();
			int nodeId2 = node2.create();
			String str1 = String.valueOf(nodeId1);
			String str2 = String.valueOf(nodeId2);
			Registry register = LocateRegistry.getRegistry();
			
			NodeAbstract secondNode = (NodeAbstract)register.lookup(str2);
			NodeAbstract firstNode = (NodeAbstract)register.lookup(str1);
			secondNode.join(nodeId1);
			
			NodeAbstract skeleton = (NodeAbstract)register.lookup(str1);
			System.out.println("Successor of node " + nodeId1 + ": " + skeleton.getSuccessor());
			skeleton = (NodeAbstract)register.lookup(str2);
			System.out.println("Successor of node " + nodeId2 + ": " + skeleton.getSuccessor());
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
