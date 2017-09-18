package MCTS;

import conquest.game.move.Move;

public class Edge {
	private Double C = 0.7; //1.41;
	protected double wins;
	protected int visits;
	protected int available;
	public String player;
	public Node nextNode;
	public Move move;
	public boolean pass;
	private Node previousNode;
	public boolean pom = false;

	
	public Edge(String player, Move move, Node previousNode){
		this.player = player;
		this.move = move;
		this.previousNode = previousNode;
		wins = 0;
		visits = 0;
		available = 0;
		pass = false;
		
	
	}
	
	@Override
	public boolean equals(Object o){
		//System.out.println("*** Edge equals ***");
		
		if(o.getClass().equals(Edge.class)){
			boolean result = ComparatorHelper.compareMove(move, ((Edge)o).move);
			//System.out.println(result);
			return result;
		} 
		
		return false;
	}
	
	public void backPropogate(String winner){
		visits++;
		if(player.equals(winner)){
			wins++;
		}
	}
	
	public void backPropogate(double reward, String winner){
		visits++;
		if(player.equals(winner)){
			wins+=reward;
			//System.out.println("winner edge" + winner);
		} else {
		//	System.out.println("loss edge " + winner);
			
			wins+=(1-reward);
		}
	}
	
	public Node getNode(boolean order){
		if(nextNode == null){
			nextNode = new Node(previousNode, this);
			if(order) {
				nextNode.player = player;
				if(pass) {
					// nextNode.player = opponent;
				}
			}
		}
		return nextNode;
	}
	
	public double getUCBScore(){
		if(visits == 0 || available == 0){
			return Double.NEGATIVE_INFINITY;
		}
		double w = wins;
		double n = visits;
		double a = available;
		return w/n + C * Math.sqrt(2*Math.log(a)/n);
	}
}