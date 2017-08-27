package MCTS;

import conquest.game.GameMap;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.Move;
import conquest.game.move.PlaceArmiesMove;

public class ComparatorHelper {
	public static boolean compareMove(Move first, Move second){
		if(first.getClass() == second.getClass()){
			if(first.getPlayerName() != second.getPlayerName()){
				return false;
			}
			if(first.getClass() == PlaceArmiesMove.class){ //PlaceArmiesMove
				return ((PlaceArmiesMove)first).getRegion().getId() == ((PlaceArmiesMove)second).getRegion().getId();
			} else if(first.getClass() == POMMove.class && second.getClass() == POMMove.class) {
				return true;
			} else { // AttackTransferMoves
				return ((AttackTransferMove)first).getToRegion().getId() == ((AttackTransferMove)second).getToRegion().getId() 
						&& ((AttackTransferMove)first).getFromRegion().getId() == ((AttackTransferMove)second).getFromRegion().getId();
			}
			
		} 
		return false;
	}
	

}
