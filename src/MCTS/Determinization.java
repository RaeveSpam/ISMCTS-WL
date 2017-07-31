package MCTS;

import java.util.LinkedList;
import java.util.Random;

import conquest.bot.BotState;
import conquest.game.ContinentData;
import conquest.game.GameMap;
import conquest.game.RegionData;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.Move;
import conquest.game.move.PlaceArmiesMove;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Determinization {
	final GameMap baseMap;
	
	GameMap determinedMap;
	
	LinkedList<Move> previousMoves;
	Random random;
	int PLACEARMIESMOVES = 5;
	String player1;
	String player2;
	LinkedList<Integer> player1deployments;
	LinkedList<Integer> player2deployments;
	
	public Determinization(GameMap map, LinkedList<Move> plannedMoves, String myName, String opponentName){
		this.previousMoves = plannedMoves;
		baseMap = map.getMapCopy();
		determinedMap = map.getMapCopy();
		random = new Random();
		player1 = myName;
		player2 = opponentName;
	}
	
	/**
	 * Determinization step
	 */
	public void determinize(){
		// roll out previous moves
		// 
		// determinedMap = baseMap.getMapCopy();
		// get available enemy moves
		// while previous.notEmpty
		// 		Choose order 
		//		p1 move  | p2 move
		//		p2 move  | p1 move
		//		update determined map
	}
	
	
	
	public void PlayOutMove(Move move){
		if(move.getClass() == PlaceArmiesMove.class){
			// add armies to region
			int region = ((PlaceArmiesMove)move).getRegion().getId();
			int armies = determinedMap.getRegion(region).getArmies() + getDeployment(((PlaceArmiesMove)move).getPlayerName());
			determinedMap.getRegion(region).setArmies(armies);
		} else if(move.getClass() == AttackTransferMove.class){
			playOutChanceMove((AttackTransferMove)move);
		}
	}
	
	
	private int getArmiesPerTurn(String player){
		int result = 5;
		for(ContinentData c : determinedMap.getContinents()){
			if(c.ownedByPlayer() == player){
				result += c.getArmiesReward();
			}
		}
		return result;
	}
	
	/**
	 * Simulates an attack transfer move on the given map. Used instead of chance nodes in search tree.
	 * @param move
	 * @param map
	 * @return Resulting map from the simulation.
	 */
	public void playOutChanceMove(AttackTransferMove move){
		// random roll determine result
		RegionData fromRegion = determinedMap.getRegion(move.getFromRegion().getId());
		RegionData toRegion = determinedMap.getRegion(move.getToRegion().getId());
		int armies = Math.min((fromRegion.getArmies()-1), move.getArmies());
		GameMap result = determinedMap.getMapCopy();
		
		if(fromRegion.getArmies() == 0 || toRegion.getArmies() == 0 || armies == 0){
			// unknown region illegal move
			return;
		}
		if(fromRegion.getPlayerName() == toRegion.getPlayerName()){
			// transfer move
			result.getRegion(toRegion.getId()).setArmies(armies);
			result.getRegion(fromRegion.getId()).setArmies(fromRegion.getArmies()-armies);
		} else {
			// deploy attackers
			result.getRegion(fromRegion.getId()).setArmies(fromRegion.getArmies()-armies);
			
			// apply outcome of combat
			int[] combatResult = simulateAttack(toRegion.getArmies(), armies);	
			if(combatResult[0] > 0){
				// defenders survive, return surviving armies
				// return defenders
				result.getRegion(toRegion.getId()).setArmies(combatResult[0]);
				// return attackers
				int fromArmies = result.getRegion(fromRegion.getId()).getArmies();
				result.getRegion(fromRegion.getId()).setArmies(fromArmies+combatResult[1]);
			} 
			else if (combatResult[0] == 0){
				// defenders dead, transfer surviving attackers and change ownership of conquered region
				result.getRegion(toRegion.getId()).setArmies(combatResult[1]);
				result.getRegion(toRegion.getId()).setPlayerName(fromRegion.getPlayerName());
			}
		}
		determinedMap = result;
	}
	
	public LinkedList<Move> getavailableAttackTransferMoves(String player){
		LinkedList<Move> result = new LinkedList<Move>();
		for(RegionData fromRegion : baseMap.getRegions()){
			if(fromRegion.ownedByPlayer(player) && fromRegion.getArmies() > 1){
				for(RegionData toRegion : fromRegion.getNeighbors()){
					result.add(new AttackTransferMove(player, fromRegion, toRegion, fromRegion.getArmies()-1));
				}
			}
		}
		return result;
	}
	
	public int getDeployment(String player){
		if(player1deployments.size() < 1 || player2deployments.size() < 1){
			updateDeployments();
		}
		if(player == player1){
			return player1deployments.pollFirst();
		} else if(player == player2){
			return player1deployments.pollFirst();
		}
		throw new NullPointerException("Wrong motherfucking player");
	}
	
	private void updateDeployments(){
		updatePlayerDeployment(player1);
		updatePlayerDeployment(player2);
	}
	
	private void updatePlayerDeployment(String player){
		int armies = getArmiesPerTurn(player);
		int[] deployments = new int[] {0, 0, 0, 0, 0};
		int i = 0;
		while(armies > 0){
			i %=5;
			deployments[i]++;
			i++;
		}
		if(player == player1){
			player1deployments = new LinkedList<Integer>();
			for(Integer d : deployments){
				player1deployments.add(d);
			}
		}
		if(player == player2){
			player2deployments = new LinkedList<Integer>();
			for(Integer d : deployments){
				player2deployments.add(d);
			}
		}
		
	}
	
	public LinkedList<PlaceArmiesMove> getPossiblePlaceArmiesMoves(String player){
		LinkedList<PlaceArmiesMove> result = new LinkedList<PlaceArmiesMove>();
		for(RegionData r : determinedMap.getRegions()){
			if(r.ownedByPlayer(player)){
				result.add(new PlaceArmiesMove(player, r, 0));
			}
		}
		return result;
	}
	
	public LinkedList<Edge> getAvailableEdges(String player){
		// identify whether it is an attackTransfer or place move
		
		return null; //getavailableAttackTransferMoves(player);
	}
	
	// FIgure out how this works pl0x 
	public boolean isTerminal(Node node){
			if(node.roundNumber > 100){
				return true;
			}
			boolean playerLives = false;
			boolean opponentLives = false;
			for(RegionData r : node.map.getRegions()){
				if(playerLives && opponentLives){
					return false;
				}
				if(r.ownedByPlayer(node.player)){
					playerLives = true;
				} else if(r.ownedByPlayer(node.opponent)){
					opponentLives = true;
				}
			}
			return true;
		}
	
	private int[] simulateAttack(int defenders, int attackers){
		int[] result = new int[] {defenders, attackers};
		Random random = new Random();
		// defenders fight
		for(int i = 0; i < defenders; i++){ 
			if(random.nextInt(10) > 2){ // 0.7
				result[1]--;
			}
		}
		if(result[1] < 0){
			// ensure no negatives
			result[1] = 0;
		}
		// attackers fight
		for(int i = 0; i < attackers; i++){
			if(random.nextInt(10) > 3){ // 0.6
				result[0]--;
			}
		}
		if(result[0] < 1){
			// ensure no negatives
			result[0] = 0;
			if(result[1] == 0){
				// resurrect 1 defender if all defenders and attackers are dead
				result[0] = 1;
			}
		}
		return result;
	}
}
