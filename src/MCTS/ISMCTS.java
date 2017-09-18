package MCTS;


import java.util.LinkedList;

import MCTS.Determinization.Phase;
import conquest.bot.BotState;
import conquest.game.GameMap;
import conquest.game.RegionData;

import conquest.game.move.Move;
import conquest.game.move.PlaceArmiesMove;

public class ISMCTS {

	Node root;
	Node current;
	boolean POM = false;
	boolean order;
	
	GameMap visibleMap;
	BotState state;
	
	private final LinkedList<Move> plannedMoves;
	
	public ISMCTS(BotState state, LinkedList<Move> plannedMoves, boolean POM, boolean order){
		this.state = state;
		this.plannedMoves = plannedMoves;
		this.POM = POM;
		this.order = order;
	}
	
	public Move getMove(int iterations, Phase phase){
		root = new Node(state.getMyPlayerName(), state.getMap().getMapCopy(), plannedMoves);
		current = root;		
		for(int i = 0; i < iterations; i++){
			//System.out.println("Iteration " + (i+1));
			// run iteration
			current = root;
			LinkedList<Edge> visitedEdges = new LinkedList<Edge>();
			// determinization
			LinkedList<Move> previousMoves = new LinkedList<Move>();
			for(Move m : plannedMoves){
				previousMoves.add(m);
			}
			Determinization determ = null;
			// determinization
			if(order) {
				determ = new OrderDeterminization(state.getMap(), previousMoves, state.getMyPlayerName(), state.getOpponentPlayerName(), state.getRoundNumber(), POM);
			} else {
				determ = new Determinization(state.getMap(), previousMoves, state.getMyPlayerName(), state.getOpponentPlayerName(), state.getRoundNumber(), POM);
			}
			determ.determinize(phase);
			boolean hasExpanded = false;
			int depth = 0;
			do{
				//System.out.println(determ.roundNumber);
				depth++;
			//	System.out.println(depth);
				/*
				// selection / expansion
				current.setDeterminization(determ);
				Edge currentEdge = current.getEdge();
				//System.out.println("Got Edge");
				visitedEdges.add(currentEdge);
				*/
				
				// her
				Edge currentEdge = null;
				current.setDeterminization(determ);
				current.determinizeAvailableMoves();
				//System.out.println("Node player " + current.player);
				if(hasExpanded) {
					// simulation
					currentEdge = current.getSimulationEdge();
					
				} else {
					if(current.isFullyExpanded()) {
						// selection
						currentEdge = current.getSelectionEdge();
						visitedEdges.add(currentEdge);
						current.updateAvailability();
						//System.out.println("Selection");
					} else {
						// expansion
						currentEdge = current.getExpansionEdge();
						visitedEdges.add(currentEdge);
						current.updateAvailability();
						hasExpanded = true;
						//System.out.println("Expansion");
					}
				}
				//System.out.println(currentEdge.player + " " + currentEdge.move.getClass());
				//System.out.println(currentEdge.player + " " + currentEdge.getClass());
				// update determined map
				if(currentEdge.pass){
					//System.out.println(currentEdge.player + " pass" + determ.currentPhase);
					determ.passMove(currentEdge.player);
				} else {
					//System.out.println(currentEdge.player + " " + currentEdge.move.getClass());
					determ.playOutMove(currentEdge.move);
				}
				current = currentEdge.getNode(order);
				if(currentEdge.pass){
					current.player = determ.getOtherPlayer(currentEdge.player);
				} 
				/* else if(currentEdge.move.getClass() == PlaceArmiesMove.class 
						&& currentEdge.player.equals(determ.player2.name)
						&& determ.getCurrentPhase() == Phase.AttackTransfer) {
					current.player = determ.player1.name;
				}*/
				
			} while(!determ.isTerminal());
			
			// backpropogate
			String winner = determ.getWinner();
			double reward = determ.getWinner2();
			//System.out.println(winner + " " + reward);
			for(Edge e : visitedEdges){
				e.backPropogate(reward, winner);
			}
			determ = null;
		}
		//System.out.println("possible moves " + root.edges.size());
		Move best = root.getBestEdge(state.getMyPlayerName()).move;
		//Move best = root.getMostVisitedEdge().move;
	//	Move best = root.getBestAverageEdge().move;
		//System.out.println(best);
		
		//deconstruct();
		//System.out.println(best.wins);
		//System.out.println(best);
		return best;
	}
	
	private void deconstruct(){
		root = null;
		current = null;
		visibleMap = null;
		state = null;
	}
	
	private GameMap formatMap(GameMap map){
		GameMap result = map.getMapCopy();
		for(RegionData r : result.regions){
			if(r.getArmies() == 0){
				r.setPlayerName("neutral");
				r.setArmies(2);
			}
		}
		return result;
	}
	
	
	private GameMap applyFogOfWar(BotState state){
		GameMap result = state.getFullMap();
		for(RegionData myRegion : state.getMap().getRegions()){
			if(myRegion.ownedByPlayer(state.getMyPlayerName())){
				String owner = myRegion.getPlayerName();
				int armies = myRegion.getArmies();
				int id = myRegion.getId();
				
				result.getRegion(id).setPlayerName(owner);
				result.getRegion(id).setArmies(armies);
				// add neighbours
				for(RegionData neighbour : myRegion.getNeighbors()){
					String nOwner = neighbour.getPlayerName();
					int nArmies = neighbour.getArmies();
					int nId = neighbour.getId();
					
					result.getRegion(nId).setPlayerName(nOwner);
					result.getRegion(nId).setArmies(nArmies);	
				}			
			}
		}
		return result;
	}

	
	/**
	 * @param first
	 * @param second
	 * @return
	 */
	private boolean compareMaps(GameMap first, GameMap second){
		for(int i = 1; i < first.getRegions().size()+1; i++){
			RegionData f = first.getRegion(i);
			RegionData s = second.getRegion(i);
			if(f.getPlayerName() != s.getPlayerName()){
				return false;
			}
			if(f.getArmies() != s.getArmies()){
				return false;
			}
		}
		return true;
	}
	

}
