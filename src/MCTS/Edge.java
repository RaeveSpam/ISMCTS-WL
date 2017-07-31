package MCTS;

import java.util.LinkedList;

import conquest.game.GameMap;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.Move;

public class Edge {
	protected int wins;
	protected int visits;
	protected int available;
	public String player;
	protected LinkedList<Node> nodes;
	public Move move;
	public boolean pass;
	
	public Edge(String player, Move move){
		this.player = player;
		this.move = move;
		pass = false;
		nodes = new LinkedList<Node>();
	}
	
	@Override
	public boolean equals(Object o){
		//System.out.println("*** Edge equals ***");
		if(o.getClass().equals(Edge.class)){
			boolean result = ComparatorHelper.compareMove(move, ((Edge)o).move);
			if(result){
				System.out.println("Existing Edge! Wooo~~");
			}
			return result;
		}
		return false;
	}
	
	public void backPropogate(String winner){
		visits++;
		if(player == winner){
			wins++;
		}
	}
	
	public double getUCBScore(){
		return 0.0;
	}
}