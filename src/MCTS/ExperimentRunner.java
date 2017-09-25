package MCTS;

import java.io.IOException;
import java.io.PrintWriter;

import conquest.bot.ISMCTSBot;
import conquest.bot.ISMCTS_POMBot;
import conquest.engine.RunGame;
import conquest.engine.RunGame.Config;
import conquest.engine.RunGame.GameResult;

public class ExperimentRunner extends Thread {
	public String filename;
	public ISMCTSBot bot1;
	public ISMCTSBot bot2;
	public Config config;
	
	
	public ExperimentRunner(ISMCTSBot bot1, ISMCTSBot bot2, String filename) {
		this.bot1 = bot1;
		this.bot2 = bot2;
		this.filename = filename;
		
		config = new Config();
		config.bot1Init = "internal:conquest.bot.ISMCTSBot";
		config.bot2Init = "internal:conquest.bot.ISMCTS_POMBot";
		config.engine.botCommandTimeoutMillis = 24*60*60*1000;
		config.engine.maxGameRounds = 100;
		config.visualize = false;
		// if false, not all human controls would be accessible (when hijacking bots via 'H' or 'J')
		config.forceHumanVisualization = true;   
		
	}
	
	public void run() {
		// kør et eksperiment
	try {
		PrintWriter writer = new PrintWriter(filename + ".csv");
		writer.println("totalRounds; bot1 regions; bot2 regions; bot1 armies; bot2armies; #bot1wins; #bot2wins; #ties; bot1time; bot2time");
		RunGame run = new RunGame(config);
		GameResult result = run.go(bot1, bot2);
		int totalBot1Regions = 0;
		int totalBot1Wins = 0;
		int totalBot2Wins = 0;
		int totalTies = 0;
		int totalBot2Regions = 0;
		int totalBot1Armies = 0;
		int totalBot2Armies = 0;
		int totalBot1Time = 0;
		int totalBot2Time = 0;
		int totalRounds = 0;
		if(result.player1Regions == 0) {
			totalBot2Wins++;
		} else if (result.player2Regions == 0) {
			totalBot1Wins++;
		} else {
			totalTies++;
		}
		
		totalRounds += result.round;
		totalBot1Regions += result.player1Regions;
		totalBot2Regions += result.player2Regions;
		totalBot1Armies += result.player1Armies;
		totalBot2Armies += result.player2Armies;
		totalBot1Time += bot1.timeSpent/1000;
		totalBot2Time += bot2.timeSpent/1000;
		writer.println();
		writer.print(totalRounds + "; " + totalBot1Regions + "; "+ totalBot2Regions+ "; " + totalBot1Armies + "; "+ totalBot2Armies+ "; " + totalBot1Wins + "; " + totalBot2Wins + "; "+ totalTies + "; " + totalBot1Time + "; " + totalBot2Time);
		writer.close();
	} catch (IOException e) {
		System.out.println("IOException | " + e.getMessage());
	}
	}
	
	public static void main(String args[]) {
		int matches = 5;
		for(int i = 0; i < matches; i++) {
			ISMCTSBot b1 = new ISMCTSBot();
			b1.ITERATIONS = 50;
			b1.order = true;
			ISMCTSBot b2 = new ISMCTS_POMBot();
			b2.ITERATIONS = 50;
			b2.order = true;
			String file = "50iterations" + i;
			new ExperimentRunner(b1, b2, file).start();
		}
	}
}
