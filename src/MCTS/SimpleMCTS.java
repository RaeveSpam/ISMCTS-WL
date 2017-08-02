package MCTS;


import java.util.LinkedList;

import MCTS.Determinization.Phase;
import conquest.bot.BotState;
import conquest.game.GameMap;
import conquest.game.RegionData;

import conquest.game.move.Move;

public class SimpleMCTS {

	Node root;
	Node current;
	
	GameMap visibleMap;
	BotState state;
	
	private final LinkedList<Move> plannedMoves;
	
	public SimpleMCTS(BotState state, LinkedList<Move> plannedMoves){
		this.state = state;
		this.plannedMoves = plannedMoves;
	}
	
	public Move getMove(int iterations, Phase phase){
		root = new Node(state.getMyPlayerName(), state.getMap().getMapCopy(), plannedMoves);
		current = root;		
		for(int i = 0; i < iterations; i++){
			System.out.println("Iteration " + (i+1));
			// run iteration
			current = root;
			LinkedList<Edge> visitedEdges = new LinkedList<Edge>();
			// determinization
			LinkedList<Move> previousMoves = new LinkedList<Move>();
			for(Move m : plannedMoves){
				previousMoves.add(m);
			}
			Determinization determ = new Determinization(state.getMap(), previousMoves, state.getMyPlayerName(), state.getOpponentPlayerName(), state.getRoundNumber());
			determ.determinize(phase);
			do{
				
				//System.out.println(determ.roundNumber);
				// selection / expansion
				current.setDeterminization(determ);
				Edge currentEdge = current.getEdge();
				visitedEdges.add(currentEdge);
				
				// simulation
				if(currentEdge.pass){
					//System.out.println(currentEdge.player + " passes");
					determ.passMove(currentEdge.player);
				} else {
					
					determ.playOutMove(currentEdge.move);
				}
				
				current = currentEdge.getNode();
			} while(!determ.isTerminal());
			
			// backpropogate
			String winner = determ.getWinner();
			for(Edge e : visitedEdges){
				e.backPropogate(winner);
			}
		}
		
		Move best = root.getBestEdge(state.getMyPlayerName()).move;
		deconstruct();
		//System.out.println(best.wins);
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
