package conquest.bot;

import java.util.ArrayList;
import java.util.LinkedList;

import MCTS.SimpleMCTS;
import MCTS.Determinization.Phase;
import conquest.game.RegionData;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.Move;
import conquest.game.move.PlaceArmiesMove;
import conquest.view.GUI;

public class SimpleMCTSBot implements Bot {
	/**
	 * A method used at the start of the game to decide which player start with what Regions. 6 Regions are required to be returned.
	 * This example randomly picks 6 regions from the pickable starting Regions given by the engine.
	 * @return : a list of m (m=6) Regions starting with the most preferred Region and ending with the least preferred Region to start with 
	 */
	@Override
	public ArrayList<RegionData> getPreferredStartingRegions(BotState state, Long timeOut)
	{
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
		int armies = 2;
		int armiesLeft = state.getStartingArmies();
		LinkedList<RegionData> visibleRegions = state.getMap().getRegions();
		LinkedList<RegionData> allRegions = state.getFullMap().getRegions();
		System.out.println("Visible regions " + visibleRegions.size());
		System.out.println("lålålå");
		//System.out.println(("unknown" == allRegions.get(0).getPlayerName()));
		while(armiesLeft > 0)
		{
			double rand = Math.random();
			int r = (int) (rand*visibleRegions.size());
			RegionData region = visibleRegions.get(r);
			
			if(region.ownedByPlayer(myName))
			{
				placeArmiesMoves.add(new PlaceArmiesMove(myName, region, Math.min(armiesLeft, armies)));
				armiesLeft -= armies;
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
		
		
		/*plannedMoves.add(move);
		AttackTransferMove result = (AttackTransferMove)move;
		result.setArmies(result.getFromRegion().getArmies()-1);
		attackTransferMoves.add(result);*/
		Move move = null;
		do {
			SimpleMCTS mcts = new SimpleMCTS(state, plannedMoves);
			move = mcts.getMove(5, Phase.AttackTransfer);
			if(move != null){
				plannedMoves.add(move);
				AttackTransferMove result = (AttackTransferMove)move;
				result.setArmies(result.getFromRegion().getArmies()-1);
				attackTransferMoves.add(result);
			}
			} while(move != null);
	
		/*
		String myName = state.getMyPlayerName();
		int armies = 5;
		
		for(RegionData fromRegion : state.getMap().getRegions())
		{
			if(fromRegion.ownedByPlayer(myName)) //do an attack
			{
				ArrayList<RegionData> possibleToRegions = new ArrayList<RegionData>();
				possibleToRegions.addAll(fromRegion.getNeighbors());
				
				while(!possibleToRegions.isEmpty())
				{
					double rand = Math.random();
					int r = (int) (rand*possibleToRegions.size());
					RegionData toRegion = possibleToRegions.get(r);
					
					if(!toRegion.getPlayerName().equals(myName) && fromRegion.getArmies() > 6) //do an attack
					{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
						break;
					}
					else if(toRegion.getPlayerName().equals(myName) && fromRegion.getArmies() > 1) //do a transfer
					{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
						break;
					}
					else
						possibleToRegions.remove(toRegion);
				}
			}
		}
		*/
		return attackTransferMoves;
	}

	public LinkedList<RegionData> getBoard(BotState state, LinkedList<AttackTransferMove> plannedMoves){
		
		return null;
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
