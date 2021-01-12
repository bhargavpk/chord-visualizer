package chordSoftware;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import chordNode.*;

public class ChordTest {

	public static void main(String[] args)
	{
//		NodeImpl node1 = new NodeImpl();
//		NodeImpl node2 = new NodeImpl();
//		NodeImpl node3 = new NodeImpl();
//		try 
//		{
//			int nodeId1 = node1.create(5);
//			int nodeId2 = node2.create(15);
//			int nodeId3 = node3.create(20);
//			
//			String str1 = String.valueOf(nodeId1);
//			String str2 = String.valueOf(nodeId2);
//			String str3 = String.valueOf(nodeId3);
//			
//			Registry register = LocateRegistry.getRegistry();
//			
//			NodeAbstract secondNode = (NodeAbstract)register.lookup(str2);
//			NodeAbstract firstNode = (NodeAbstract)register.lookup(str1);
//			NodeAbstract thirdNode = (NodeAbstract)register.lookup(str3);
//			
//			secondNode.join(nodeId1);
//			thirdNode.join(nodeId2);
//												
//			firstNode.stabilize();
//			secondNode.stabilize();
//			thirdNode.stabilize();
//			
////			Thread.sleep(1000);
//			
////			while((firstNode.getSuccessor() != nodeId3)||(secondNode.getSuccessor() != nodeId1)||(thirdNode.getSuccessor() != nodeId2))
////			;
////			
////			firstNode.updateFingers();
////			secondNode.updateFingers();
////			thirdNode.updateFingers();
//			
//			for(int i = 1;i <= 5;i++)
//			{
//				Thread.sleep(1000);
//	//			NodeAbstract skeleton = (NodeAbstract)register.lookup(str1);
//				System.out.println("Successor of node " + nodeId1 + ": " + firstNode.getSuccessor());
//				System.out.println("Expected successor " + firstNode.getExpectedSuccessor());
//				System.out.println("Predecessor of node " + nodeId1 + ": " + firstNode.getPredecessor());
//	//			skeleton = (NodeAbstract)register.lookup(str2);
//				System.out.println("Successor of node " + nodeId2 + ": " + secondNode.getSuccessor());
//				System.out.println("Expected successor: " + secondNode.getExpectedSuccessor());
//				System.out.println("Predecessor of node " + nodeId2 + ": " + secondNode.getPredecessor());
//	//			skeleton = (NodeAbstract)register.lookup(str3);
//				System.out.println("Successor of node " + nodeId3 + ": " + thirdNode.getSuccessor());
//				System.out.println("Expected successor " + thirdNode.getExpectedSuccessor());
//				System.out.println("Predecessor of node " + nodeId3 + ": " + thirdNode.getPredecessor());
//			}
//			
//			NodeImpl node4 = new NodeImpl();
//			int nodeId4 = node4.create(1);
//			NodeAbstract fourthNode = (NodeAbstract)register.lookup(String.valueOf(nodeId4));
//			fourthNode.join(nodeId3);
//			fourthNode.stabilize();
//			
////			while(fourthNode.getSuccessor() != nodeId3)	;
//			
////			fourthNode.updateFingers();
//			
//			Thread.sleep(1000);
//			
//			System.out.println("Successor of node " + nodeId1 + ": " + firstNode.getSuccessor());
//			System.out.println("Expected successor " + firstNode.getExpectedSuccessor());
//			System.out.println("Predecessor of node " + nodeId1 + ": " + firstNode.getPredecessor());
////			skeleton = (NodeAbstract)register.lookup(str2);
//			System.out.println("Successor of node " + nodeId2 + ": " + secondNode.getSuccessor());
//			System.out.println("Expected successor: " + secondNode.getExpectedSuccessor());
//			System.out.println("Predecessor of node " + nodeId2 + ": " + secondNode.getPredecessor());
////			skeleton = (NodeAbstract)register.lookup(str3);
//			System.out.println("Successor of node " + nodeId3 + ": " + thirdNode.getSuccessor());
//			System.out.println("Expected successor " + thirdNode.getExpectedSuccessor());
//			System.out.println("Predecessor of node " + nodeId3 + ": " + thirdNode.getPredecessor());
//			System.out.println("Successor of node " + nodeId4 + ": " + fourthNode.getSuccessor());
//			System.out.println("Expected successor: " + fourthNode.getExpectedSuccessor());
//			System.out.println("Predecessor of node " + nodeId4 + ": " + fourthNode.getPredecessor());
//			
//			
//			
//		}catch(Exception e)
//		{
//			e.printStackTrace();
//		}
		
	}
}
