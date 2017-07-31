package conquest.bot;

import java.util.LinkedList;
import java.util.Random;

import conquest.game.RegionData;
import conquest.game.move.Move;

public class Simulator {
	final LinkedList<RegionData> regions; 
	
	public Simulator(LinkedList<RegionData> regions){
		this.regions = regions;
	}
	
	public LinkedList<RegionData> simulateMoves(LinkedList<Move> moves){
		LinkedList result = regions;
		for(Move m : moves){
			
		}
		return result;
	}
	
	/**
	 * Simulates uncertainty in battles
	 * @param defenders
	 * @param attackers
	 * @return
	 */
	private int[] simulateAttack(int defenders, int attackers){
		int[] result = new int[] {defenders, attackers};
		Random random = new Random();
		for(int i = 0; i < defenders; i++){ 
			if(random.nextInt(10) > 2){ // 0.7
				result[1]--;
			}
		}
		for(int i = 0; i < attackers; i++){
			if(random.nextInt(10) > 3){ // 0.7
				result[0]--;
			}
		}
		return result;
	}
}
