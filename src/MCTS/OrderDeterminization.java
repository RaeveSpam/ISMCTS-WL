package MCTS;

import java.util.LinkedList;
import java.util.Random;

import MCTS.Determinization.Phase;
import conquest.game.GameMap;
import conquest.game.RegionData;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.Move;
import conquest.game.move.PlaceArmiesMove;

public class OrderDeterminization extends Determinization {

	
	
	public OrderDeterminization(GameMap map, LinkedList<Move> plannedMoves, String myName, String opponentName,
			int roundNumber, boolean POM) {
		super(map, plannedMoves, myName, opponentName, roundNumber, POM);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void playOutMove(Move move) {
		//System.out.println(currentPhase);
		if(move.getClass() == POMMove.class){
			// play out a random available move
			LinkedList<Move> moves = new LinkedList<Move>();
			if(currentPhase == Phase.PlaceArmies){
				moves = getPlayerData(move.getPlayerName()).placeArmiesMoves;
				//System.out.println("place army moves " + moves.size());
			} else if(currentPhase == Phase.AttackTransfer){
				moves = getPlayerData(move.getPlayerName()).attackTransferMoves;
				//System.out.println("attack transfer moves " + moves.size());
			}
			
			if(moves.size() > 0){
				// pick random move
				//System.out.println("pick move");
				Random random = new Random();
				move = moves.get(random.nextInt(moves.size()));	
			} else {
				passMove(move.getPlayerName());
			}
		}
	//	System.out.println(roundNumber);
		// Commit attack transfer move and update available attack transfer moves 
		if(move.getClass() == AttackTransferMove.class) {
			getPlayerData(move.getPlayerName()).committedMoves.add(move);
			LinkedList<Move> updateMoves = new LinkedList<Move>();
			for(Move m : getPlayerData(move.getPlayerName()).attackTransferMoves) {
				if(((AttackTransferMove)m).getFromRegion().getId() != ((AttackTransferMove)move).getFromRegion().getId()) {
					updateMoves.add(m);
				}
			}
			getPlayerData(move.getPlayerName()).attackTransferMoves = updateMoves;
		}
		
		// Perform placement move
		if(move.getClass() == PlaceArmiesMove.class){
			// add armies to region
		//	System.out.println(getPlayerData(move.getPlayerName()).deployments.size());
			getPlayerData(move.getPlayerName()).deployCount++;
			int region = ((PlaceArmiesMove)move).getRegion().getId();
			int armies = determinedMap.getRegion(region).getArmies() + getDeployment(((PlaceArmiesMove)move).getPlayerName());
			determinedMap.getRegion(region).setArmies(armies);
		}
	}
	
	@Override
	public void passMove(String player) {
		//System.out.println("pass " + player);
		if(currentPhase == Phase.AttackTransfer) {
			getPlayerData(player).hasPassed = true;
		}
		
		if(player == player2.name) {
			int movePairs = Math.min(player1.committedMoves.size(), player2.committedMoves.size());
			for(int i = 0; i < movePairs; i++) {
				if(random.nextBoolean()) {
					playOutChanceMove((AttackTransferMove)player1.committedMoves.get(i));
					playOutChanceMove((AttackTransferMove)player2.committedMoves.get(i));
				} else {
					playOutChanceMove((AttackTransferMove)player2.committedMoves.get(i));
					playOutChanceMove((AttackTransferMove)player1.committedMoves.get(i));
				}
			}
			
			for(int i = movePairs; i < player1.committedMoves.size(); i++) {
				playOutChanceMove((AttackTransferMove)player1.committedMoves.get(i));
			}
			for(int i = movePairs; i < player2.committedMoves.size(); i++) {
				playOutChanceMove((AttackTransferMove)player2.committedMoves.get(i));
			}
			//TODO play rest
			player1.hasPassed = true;
			player2.hasPassed = true;
			player1.deployCount = 0;
			player2.deployCount = 0;
			//getCurrentPhase();		
		}
	}
	
	@Override
	public LinkedList<Edge> getAvailableEdges(Node node){
		getCurrentPhase();
		if(currentPhase == Phase.PlaceArmies && getPlayerData(node.player).deployCount > 4) {
			LinkedList<Edge> result = new LinkedList<Edge>();
			result.add(new PassEdge(node.player, null, node));
			return result;
		} else {
			return super.getAvailableEdges(node);
		}
	}
}
