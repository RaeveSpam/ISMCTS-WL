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
	GameMap baseMap;
	
	GameMap determinedMap;
	
	LinkedList<Move> previousMoves;
	Random random;
	int PLACEARMIESMOVES = 5;
	
	PlayerData player1;
	PlayerData player2;	
	
	Node rootNode;
	
	Phase currentPhase;
	
	public boolean order = false;
	
	int roundNumber = 0;
	
	String currentPlayer = null;
	String nextPlayer = null;
	
	boolean POM = false;
	
	public enum Phase {
		PlaceArmies, AttackTransfer
	}
	
	public Determinization(GameMap map, LinkedList<Move> plannedMoves, String myName, String opponentName, int roundNumber, boolean POM){
		this.previousMoves = plannedMoves;
		baseMap = map.getMapCopy();
		determinedMap = map.getMapCopy();
		random = new Random();
		
		player1 = new PlayerData(myName);
		player2 = new PlayerData(opponentName);

		rootNode = new Node(myName, map, new LinkedList<>());
		this.roundNumber = roundNumber;
		this.POM = POM;
	}
	
	public String getOtherPlayer(String player) {
		if(!player.equals(player1.name)) {
			return player1.name;
		} else {
			return player2.name;
		}
	}
	
	/**
	 * Determinization step
	 */
	public void determinize(Phase phase){
		// roll out previous moves
		determinedMap = baseMap.getMapCopy();
		currentPhase = phase;
		if(currentPhase == Phase.AttackTransfer){
			player2.attackTransferMoves = getAvailableAttackTransferMoves(player2.name);
			player1.attackTransferMoves = getAvailableAttackTransferMoves(player1.name);
			// remove previous moves
			LinkedList<Move> availableMoves = new LinkedList<Move>();
			for(Move am : player1.attackTransferMoves){
				boolean available = true;
				for(Move m : previousMoves){
					if(((AttackTransferMove)m).getFromRegion().getId() == ((AttackTransferMove)am).getFromRegion().getId()){
						available = false;
						break;
					}
				}
				if(available){
					availableMoves.add(am);
				}
			}
			player1.attackTransferMoves = availableMoves;	
		} else if(currentPhase == Phase.PlaceArmies){
			updateDeployments();
			player1.placeArmiesMoves = getPossiblePlaceArmiesMoves(player1.name);
			player2.placeArmiesMoves = getPossiblePlaceArmiesMoves(player2.name);
		}
		// random previous turns
		while(!previousMoves.isEmpty()){
			String[] order = getOrder();
			for(int i = 0; i < order.length; i++){
				
				if(order[i] == player1.name){
					playOutMove(previousMoves.pollFirst());
				} else {
					// get random enemy move
					if(currentPhase == Phase.AttackTransfer){
						if(player2.attackTransferMoves.size() > 1){
							int index = random.nextInt(player2.attackTransferMoves.size());
							playOutMove(player2.attackTransferMoves.get(index));
						}					
					} else {
						if(player2.placeArmiesMoves.size() > 1){
							int index = random.nextInt(player2.placeArmiesMoves.size());
							playOutMove(player2.placeArmiesMoves.get(index));
						}
					}
				}
			}
		}
		if(getOrder()[0] == player2.name){
			// get random enemy move
			if(currentPhase == Phase.AttackTransfer){
				if(player2.attackTransferMoves.size() > 0){
					int index = random.nextInt(player2.attackTransferMoves.size());
					playOutMove(player2.attackTransferMoves.get(index));
				}					
			} else {
				if(player2.placeArmiesMoves.size() > 0){
					int index = random.nextInt(player2.placeArmiesMoves.size());
					playOutMove(player2.placeArmiesMoves.get(index));
				}
			}
		}
	}
	
	public String[] getOrder(){
		if(random.nextBoolean()){
			return new String[]{player1.name, player2.name};
		} else {
			return new String[]{player2.name, player1.name};
		}
	}
	
	protected PlayerData getPlayerData(String player){
		if(player.equals(player1.name)){
			return player1;
		} else if(player.equals(player2.name)){
			return player2;
		}
		throw new NullPointerException("Unknown player");
	}
	
	protected Phase getCurrentPhase(){
		if(currentPhase == Phase.PlaceArmies){
			//System.out.println(player1.deployments.size() + " " + player2.deployments.size());
			if(player1.deployments.isEmpty() && player2.deployments.isEmpty()){
				// new attack transfer phase
				currentPhase = Phase.AttackTransfer;
				player1.attackTransferMoves = getAvailableAttackTransferMoves(player1.name);
				player2.attackTransferMoves = getAvailableAttackTransferMoves(player2.name);
				player1.hasPassed = false;
				player1.hasPassed = false;
			}
		} else if(currentPhase == Phase.AttackTransfer){
			//System.out.println("Wooooooooo");
			if(player1.hasPassed && player2.hasPassed){
				// New Place armies phase & and new round
				roundNumber++;
				currentPhase = Phase.PlaceArmies;
				updateDeployments();
				player1.hasPassed = false;
				player1.hasPassed = false;
				player1.placeArmiesMoves = getPossiblePlaceArmiesMoves(player1.name);
				player2.placeArmiesMoves = getPossiblePlaceArmiesMoves(player2.name);
				baseMap = determinedMap.getMapCopy();
			}
		}
		//System.out.println(currentPhase);
		return currentPhase;
	}
	
	public void passMove(String player){
		getPlayerData(player).hasPassed = true;
	}
	
	/**
	 * Step determinization forward
	 * @param move
	 */
	public void playOutMove(Move move){
		//System.out.println(getPlayerData(move.getPlayerName()).deployments.size());
		//System.out.println(move.getPlayerName() + " move " + move.getClass());
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
		if(move.getClass() == PlaceArmiesMove.class){
			// add armies to region
			//System.out.println("Place move");
			int region = ((PlaceArmiesMove)move).getRegion().getId();
			int armies = determinedMap.getRegion(region).getArmies() + getDeployment(((PlaceArmiesMove)move).getPlayerName());
			determinedMap.getRegion(region).setArmies(armies);
		} else if(move.getClass() == AttackTransferMove.class){
			// attack transfer
			//System.out.println("attack/transfer move");
			playOutChanceMove((AttackTransferMove)move);
		}
	}
	
	
	protected int getArmiesPerTurn(String player){
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
	protected void playOutChanceMove(AttackTransferMove move){
		
	/* if(move.getPlayerName().equals(player1.name)) {
			System.out.println("PLAYER 1");
		}*/
		//System.out.println("	" + move.getPlayerName() + " attack/transfer");
		// remove moves from available moves
		LinkedList<Move> moves = getPlayerData(move.getPlayerName()).attackTransferMoves;
		
		LinkedList<Move> updated = new LinkedList<Move>();
		for(Move m : moves){
			
			if(((AttackTransferMove)m).getFromRegion().getId() != move.getFromRegion().getId()){
				updated.add(m);
			}
		}
		//System.out.println(moves.size() + "    " + updated.size());
		getPlayerData(move.getPlayerName()).attackTransferMoves = updated;
		// random roll determine result
		RegionData fromRegion = determinedMap.getRegion(move.getFromRegion().getId());
		RegionData toRegion = determinedMap.getRegion(move.getToRegion().getId());
		int armies = Math.min((fromRegion.getArmies()-1), move.getArmies());
		GameMap result = determinedMap.getMapCopy();
		if(!move.getPlayerName().equals(determinedMap.getRegion(move.getFromRegion().getId()).getPlayerName())){
			//System.out.println(move.getPlayerName() + " - " + determinedMap.getRegion(move.getFromRegion().getId()).getPlayerName());
			
			return;
		}
		if(fromRegion.getArmies() < 2 || armies == 0){
		
			//System.out.println("	2. failure " + fromRegion.getArmies() + " " + toRegion.getArmies()  + " " + armies);
			return;
		}
		
		if(fromRegion.getPlayerName().equals(toRegion.getPlayerName())){
			// transfer move
			//System.out.println("transfer move " + result.getRegion(fromRegion.getId()).getArmies() + " -> " + result.getRegion(toRegion.getId()).getArmies() +  " (" + armies + ")");

			//System.out.println("move " + fromRegion.getPlayerName() + " to " + toRegion.getPlayerName());
			if(!(result.getRegion(fromRegion.getId()).getArmies() > armies)){
				armies = result.getRegion(fromRegion.getId()).getArmies()-1;
			}
			if(armies < 1) {
				return;
			}
			result.getRegion(fromRegion.getId()).setArmies(fromRegion.getArmies()-armies);
			armies += result.getRegion(toRegion.getId()).getArmies();
			result.getRegion(toRegion.getId()).setArmies(armies);
			//System.out.println("	" + result.getRegion(fromRegion.getId()).getArmies() + " - " + result.getRegion(toRegion.getId()).getArmies() +  " (" + armies + ")");
			
			
			
			
			
			
			
		} else {
			// deploy attackers
			//System.out.println("attack " + fromRegion.getPlayerName() + " vs. " + toRegion.getPlayerName());
			//System.out.println("attack move " + result.getRegion(fromRegion.getId()).getArmies() + " -> " + result.getRegion(toRegion.getId()).getArmies() +  " (" + armies + ")");
			
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
			//System.out.println("	" + result.getRegion(fromRegion.getId()).getArmies() + " - " + result.getRegion(toRegion.getId()).getArmies() +  " (" + armies + ")");
			
		}
		
		//System.out.println("Update determined map");
		determinedMap = result;
	}
	
	public LinkedList<Move> getAvailableAttackTransferMoves(String player){
	//	System.out.println(determinedMap.getMapString());
		LinkedList<Move> result = new LinkedList<Move>();
		for(RegionData fromRegion : baseMap.getRegions()){
			if(fromRegion.ownedByPlayer(player) && fromRegion.getArmies() > 1){
			//	System.out.println(fromRegion.getPlayerName() + " " + fromRegion.getArmies() + " armies");
				for(RegionData toRegion : fromRegion.getNeighbors()){
					//System.out.println("new attack transfer " + (fromRegion.getArmies()-1));
					result.add(new AttackTransferMove(player, fromRegion, toRegion, fromRegion.getArmies()-1));
				}
			}
		}
		//System.out.println(player + " " + result.size());
		return result;
	}
	
	public int getDeployment(String player){
		if(getPlayerData(player).deployments.size() < 1){
			//updateDeployments();
			return 0;
		}
		return getPlayerData(player).deployments.pollFirst();
	}
	
	protected void updateDeployments(){
		updatePlayerDeployment(player1);
		updatePlayerDeployment(player2);
	}
	
	protected void updatePlayerDeployment(PlayerData player){
		int armies = getArmiesPerTurn(player.name);
		int[] deployments = new int[] {0, 0, 0, 0, 0};
		int i = 0;
		while(armies > 0){
			i %=5;
			deployments[i]++;
			i++;
			armies--;
		}
		player.deployCount = 0;
		player.deployments = new LinkedList<Integer>();
		for(Integer d : deployments){
			player.deployments.add(d);
		}		
	}
	
	public LinkedList<Move> getPossiblePlaceArmiesMoves(String player){
		LinkedList<Move> result = new LinkedList<Move>();
		for(RegionData r : determinedMap.getRegions()){
			if(r.ownedByPlayer(player)){
				result.add(new PlaceArmiesMove(player, r, 0));
			}
		}
		return result;
	}
	
	
	public LinkedList<Edge> getAvailableEdges(Node node){
		//System.out.println(node.player + " " + currentPhase);
		if(!order) {
			Phase phase = getCurrentPhase();
		}
		//System.out.println(phase);
		if(node.player.equals("unknown")){
			if(currentPlayer == null){
				String[] ord = getOrder();
				currentPlayer = ord[0];
				nextPlayer = ord[1];
			}
		} else {
			currentPlayer = node.player;
			nextPlayer = null;
		}
		LinkedList<Edge> result = new LinkedList<Edge>();
		//System.out.println(currentPlayer);
		if(POM && currentPlayer.equals(player2.name)){
			if(currentPhase == Phase.AttackTransfer && getPlayerData(currentPlayer).attackTransferMoves.size() < 1) {
				result.add(new PassEdge(currentPlayer, null, node));
				return result;
			}
			// Partial Observable Move
			result.add(new POMEdge(currentPlayer, null, node));
			currentPlayer = nextPlayer;
			nextPlayer = null;
			return result;
		}
		LinkedList<Move> moves = new LinkedList<Move>();
		
		if(currentPhase == Phase.PlaceArmies){
			moves = getPlayerData(currentPlayer).placeArmiesMoves; 		//getPossiblePlaceArmiesMoves(currentPlayer);
		} else if(currentPhase == Phase.AttackTransfer){
			moves = getPlayerData(currentPlayer).attackTransferMoves; 	//getAvailableAttackTransferMoves(currentPlayer);
		}
		//System.out.println("availableEdges " + moves.size());
		for(Move m : moves){
			result.add(new Edge(currentPlayer, m, node));
		}
		if(result.size() == 0){
			result.add(new PassEdge(currentPlayer, null, node));
		}
		currentPlayer = nextPlayer;
		nextPlayer = null;
		
		return result;
	}
	
	public boolean isTerminal(){
		if(!order) {
			getCurrentPhase();
		}
		if(roundNumber > 100){
			return true;
		}
		boolean player1Lives = false;
		boolean player2Lives = false;
		for(RegionData r : determinedMap.getRegions()){
			if(r.ownedByPlayer(player1.name)){
				player1Lives = true;
			} else if(r.ownedByPlayer(player2.name)){
				player2Lives = true;
			}
			if(player1Lives && player2Lives){
				return false;
			}
		}
		return true;
	}
	
	public String getWinner(){
		int player1regions = 0;
		int player2regions = 0;
		
		for(RegionData r : determinedMap.getRegions()){
			if(r.ownedByPlayer(player1.name)){
				player1regions++;
			} else if(r.ownedByPlayer(player2.name)){
				player2regions++;
			}
		}
		//System.out.println(determinedMap.getMapString());
		//System.out.println(player1regions + " - " + player2regions);
		if(player1regions == 0){
			return player2.name;
		}
		
		if(player2regions == 0){
			return player1.name;
		}
		
		if(player1regions >= player2regions*2) {
			return player1.name;
		}
		
		if(player2regions >= player1regions*2) {
			return player2.name;
		}
		
		return "none";
		/*if(player1regions == player2regions){
			return "none";
		}
		if(player1regions > player2regions){
			return player1.name;
		} else {
			return player2.name;
		}*/
	}
	
	public double getWinner2() {
		int player1regions = 0;
		int player2regions = 0;
		
		for(RegionData r : determinedMap.getRegions()){
			if(r.ownedByPlayer(player1.name)){
				player1regions++;
			} else if(r.ownedByPlayer(player2.name)){
				player2regions++;
			}
		}
		if(player1regions == 0){
			return 1;
		}
		
		if(player2regions == 0){
			return 1;
		}
		
		if(player1regions >= player2regions*2) {
			return 0.75;
		}
		
		if(player2regions >= player1regions*2) {
			return 0.75;
		}
		return 0.5;
	}
	
	
	protected int[] simulateAttack(int defenders, int attackers){
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
		//System.out.println("Battle " + attackers + " v " + defenders + " | " + result[1] + " - " + result[0]);
		return result;
	}
}
