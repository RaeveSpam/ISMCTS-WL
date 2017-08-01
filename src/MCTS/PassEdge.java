package MCTS;

import conquest.game.move.Move;

public class PassEdge extends Edge {
		
	public PassEdge(String player, Move move, Node previousNode) {
		super(player, move, previousNode);
		pass = true;
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public boolean equals(Object o){
		if(o.getClass().equals(PassEdge.class)){
			return player == ((PassEdge)o).player;
		}
		return false;
	}

}
