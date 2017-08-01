package MCTS;

import conquest.game.move.Move;

public class Edge {
	private Double C = 1.41;
	protected int wins;
	protected int visits;
	protected int available;
	public String player;
	private Node nextNode;
	public Move move;
	public boolean pass;
	private Node previousNode;
	
	public Edge(String player, Move move, Node previousNode){
		this.player = player;
		this.move = move;
		this.previousNode = previousNode;
		pass = false;
		
	
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
	
	public Node getNode(){
		if(nextNode == null){
			nextNode = new Node(previousNode, this);
		}
		return nextNode;
	}
	
	public double getUCBScore(){
		if(visits == 0 || available == 0){
			return Double.MIN_VALUE;
		}
		double w = wins;
		double n = visits;
		double a = available;
		
		return w/n + C * Math.sqrt(Math.log(a)/n);
	}
}