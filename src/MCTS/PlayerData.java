package MCTS;

import java.util.LinkedList;

import conquest.game.move.Move;

public class PlayerData {
	String name;
	LinkedList<Move> placeArmiesMoves;
	LinkedList<Move> attackTransferMoves;
	boolean hasPassed;
	LinkedList<Integer> deployments;
	
	public PlayerData(String player){
		name = player;
		placeArmiesMoves = new LinkedList<Move>();
		attackTransferMoves = new LinkedList<Move>();
		hasPassed = false;
		deployments = new LinkedList<Integer>();
	}
	
	@Override
	public String toString(){
		return name;
	}
}
