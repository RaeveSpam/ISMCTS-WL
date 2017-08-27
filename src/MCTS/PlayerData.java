package MCTS;

import java.util.LinkedList;

import conquest.game.move.Move;

public class PlayerData {
	String name;
	LinkedList<Move> placeArmiesMoves;
	LinkedList<Move> attackTransferMoves;
	boolean hasPassed;
	LinkedList<Integer> deployments;
	LinkedList<Move> committedMoves;
	int deployCount;
	
	public PlayerData(String player){
		name = player;
		placeArmiesMoves = new LinkedList<Move>();
		attackTransferMoves = new LinkedList<Move>();
		hasPassed = false;
		deployments = new LinkedList<Integer>();
		committedMoves = new LinkedList<Move>();
		deployCount = 0;
	}
	
	@Override
	public String toString(){
		return name;
	}
}
