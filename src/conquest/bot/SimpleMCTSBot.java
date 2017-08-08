package conquest.bot;

import java.util.ArrayList;
import java.util.LinkedList;

import MCTS.ISMCTS;
import MCTS.Determinization.Phase;
import conquest.game.RegionData;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.Move;
import conquest.game.move.PlaceArmiesMove;
import conquest.view.GUI;



public class SimpleMCTSBot implements Bot {
	private ISMCTS mcts = null;
	private boolean POM = false;
	
	private int ITERATIONS = 20;
	
	/**
	 * A method used at the start of the game to decide which player start with what Regions. 6 Regions are required to be returned.
	 * This example randomly picks 6 regions from the pickable starting Regions given by the engine.
	 * @return : a list of m (m=6) Regions starting with the most preferred Region and ending with the least preferred Region to start with 
	 */
	@Override
	public ArrayList<RegionData> getPreferredStartingRegions(BotState state, Long timeOut)
	{
		//Runtime runtime = Runtime.getRuntime();
		//System.out.println(runtime.maxMemory());
		int m = 6;
		ArrayList<RegionData> preferredStartingRegions = new ArrayList<RegionData>();
		for(int i=0; i<m; i++)
		{
			double rand = Math.random();
			int r = (int) (rand*state.getPickableStartingRegions().size());
			//System.out.println("Starting regions");
			//System.out.println(state.getPickableStartingRegions().toString());
			int regionId = state.getPickableStartingRegions().get(r).getId();
			RegionData region = state.getFullMap().getRegion(regionId);

			if(!preferredStartingRegions.contains(region))
				preferredStartingRegions.add(region);
			else
				i--;
			
			
		}
		
		return preferredStartingRegions;
	}
	
	/**
	 * This method is called for at first part of each round. This example puts two armies on random regions
	 * until he has no more armies left to place.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	@Override
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) 
	{		
		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();
		LinkedList<Integer> armies = new LinkedList<Integer>();
		int armiesLeft = state.getStartingArmies();
		int moves = 5;
		int[] deployments = new int[] {0, 0, 0, 0, 0};
		int index = 0;
		while(armiesLeft > 0){
			index %=moves;
			deployments[index]++;
			index++;
			armiesLeft--;
		}
		
		for(Integer d : deployments){
			armies.add(d);
		}		
		LinkedList<Move> plannedMoves = new LinkedList<Move>();
		for(int i = 0; i < moves; i++){
			System.out.println("Get place move");
			mcts = new ISMCTS(state, plannedMoves, POM);
			Move move = mcts.getMove(ITERATIONS, Phase.PlaceArmies);
			mcts = null;
			if(move != null){
				plannedMoves.add(move);
				PlaceArmiesMove result = (PlaceArmiesMove)move;
				result.setArmies(armies.pollFirst());
				placeArmiesMoves.add(result);
			}
		}		
		return placeArmiesMoves;
	}
	
	/**
	 * This method is called for at the second part of each round. This example attacks if a region has
	 * more than 6 armies on it, and transfers if it has less than 6 and a neighboring owned region.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	@Override
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) 
	{
		ArrayList<AttackTransferMove> attackTransferMoves = new ArrayList<AttackTransferMove>();
		LinkedList<Move> plannedMoves = new LinkedList<Move>();
		Move move = null;
		int i = 3;
		do {
			
			mcts = new ISMCTS(state, plannedMoves, POM);
			move = mcts.getMove(ITERATIONS, Phase.AttackTransfer);
			mcts = null;
			if(move != null){
				plannedMoves.add(move);
				AttackTransferMove result = (AttackTransferMove)move;
				result.setArmies(result.getFromRegion().getArmies()-1);
				attackTransferMoves.add(result);
			}
			i--;
		} while(move != null && i > 0);
		return attackTransferMoves;
	}
	
	
	
	
	public LinkedList<AttackTransferMove> getPotentialMoves(BotState state){
		String myName = state.getMyPlayerName();
		LinkedList<AttackTransferMove> result = new LinkedList<AttackTransferMove>();
		for(RegionData fromRegion : state.getMap().getRegions()){
			if(fromRegion.ownedByPlayer(myName) && fromRegion.getArmies() > 1){
				for(RegionData toRegion : fromRegion.getNeighbors()){
					result.add(new AttackTransferMove(myName, fromRegion, toRegion, fromRegion.getArmies()-1));
				}
			}
		}
		
		return result;
		
		
	}
	
	
	
	@Override
	public void setGUI(GUI gui) {
	}
	
	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new SimpleMCTSBot());
		//parser.setLogFile(new File("./BotStarter.log"));
		parser.run();
	}
	
	

}
