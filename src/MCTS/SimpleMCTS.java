package MCTS;

import java.util.LinkedList;

import conquest.bot.BotState;
import conquest.game.GameMap;
import conquest.game.RegionData;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.Move;

public class SimpleMCTS {

	Node root;
	Node current;
	
	GameMap visibleMap;
	BotState state;
	
	public SimpleMCTS(BotState state, LinkedList<AttackTransferMove> plannedMoves){
		this.state = state;
	}
	
	public AttackTransferMove getMove(LinkedList<AttackTransferMove> potentialMoves, int iterations){
		
		
		for(int i = 0; i < iterations; i++){
			// run iteration
			
			// determination
			// selection
			// expansion
			// simulation
			// backpropogate
		}
		
		// result = root.getbest
		return null;
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
	
	private void determinization(){
		// determine unknown
		
		// determine map - Simple see all
		
		// determine planned moves
		
	}
	
	private Edge selection(){
		// get available edges
		
		// UCB
		
		return null;
	}
	private void expansion(){
		
	}
	
	private void simulation(){
		
	}
	
	private void backPropagation(){
		
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
