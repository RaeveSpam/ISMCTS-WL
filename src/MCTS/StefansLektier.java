package MCTS;

public class StefansLektier {
	public static void main(String args[]) {
		System.out.println(isPowerOf(2, 1024	));

		System.out.println(isPowerOf(3, 16));
		
	}
	
	public static boolean isPowerOf(int x, int y) {
		if(x < 2 || y < 1) {
			return false;
		}
		if(y == 1) {
			return true;
		}
		boolean result = y % x == 0;
		if(!result) {
			return false;
		}
		return isPowerOf(x, y/x) && true;
	}
}
