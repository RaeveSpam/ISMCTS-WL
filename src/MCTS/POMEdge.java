package MCTS;

import conquest.game.move.Move;

public class POMEdge extends Edge {

	public POMEdge(String player, Move move, Node previousNode) {
		super(player, new POMMove(player), previousNode);
		pom = true;
		// TODO Auto-generated constructor stub
	}

}
