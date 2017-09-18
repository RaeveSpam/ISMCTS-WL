package MCTS;

import java.util.LinkedList;
import java.util.Random;

import conquest.game.GameMap;
import conquest.game.move.Move;

public class Node {
	public int wins = 0;
	public int visits = 0;
	
	public String player = "unknown";
	
	public GameMap map; // Base state
	
	LinkedList<Move> previousMoves; // Uncertainty defining the InformationSet
	
	LinkedList<Edge> edges = new LinkedList<Edge>();; // all possible edges originating from this InformationSet
	
	LinkedList<Edge> availableEdges;
	
	Determinization determ;
	
	public Node(Node previousNode, Edge previousEdge){
		map = previousNode.map;
		previousMoves = new LinkedList<Move>();
		previousMoves.addAll(previousNode.previousMoves);
		previousMoves.add(previousEdge.move);
		availableEdges = new LinkedList<Edge>();
		determ = null;
	}
	
	public Node(String player, GameMap knownMap, LinkedList<Move> previousMoves){
		this.player = player;
		map = knownMap;
		this.previousMoves = previousMoves;
		availableEdges = new LinkedList<Edge>();
		determ = null;
	}
	
	public void setDeterminization(Determinization d){
		determ = d;
	}
	
	public LinkedList<Move> getPreviousMoves(){
		return previousMoves;
	}
	
	/**
	 * MCTS step
	 * @return
	 */
	public Edge getEdge(){
		visits++;
		if(determ == null){
			throw new NullPointerException("No determinization available");
		}
		Edge result = null;
		availableEdges = determ.getAvailableEdges(this);
		if(edges.size() == 0 && visits == 1){
			// simulation
			return getSimulationEdge();
		}
		availableEdges = getRealEdges(availableEdges);
		
		
		if(isFullyExpanded()){
			// select
			result = getSelectionEdge();
		} else {
			// expansion
			result = getExpansionEdge();
		}
		
		if(result == null){
			throw new NullPointerException("What the damn hell? No Edge found");
		}
		// increment available edge's availability
		for(Edge e : availableEdges){
			e.available++;
		}
		return result;
	}
	
	public void determinizeAvailableMoves() {
		if(determ == null){
			throw new NullPointerException("No determinization available");
		}
		availableEdges = determ.getAvailableEdges(this);
		availableEdges = getRealEdges(availableEdges);
	}
	
	public void updateAvailability() {
		for(Edge e : availableEdges){
			e.available++;
		}
	}
	
	public Edge getBestEdge(String player){
		
		LinkedList<Edge> bestEdges = new LinkedList<Edge>();
		double score = Double.NEGATIVE_INFINITY;
		
		for(Edge e : edges){
			if(e.player == player){
				double escore = e.getUCBScore();
				if(escore > score){
					bestEdges = new LinkedList<Edge>();
					bestEdges.add(e);
					score = escore;
				} else if(escore == score){
					bestEdges.add(e);
				}
			}
		}
		Random random = new Random();
		
		return bestEdges.get(random.nextInt(bestEdges.size()));
	}
	
	public Edge getSimulationEdge(){
		Random random = new Random();
		return availableEdges.get(random.nextInt(availableEdges.size()));
	}
	
	public boolean isFullyExpanded(){
		// check that every available edge has been visited at least once
		if(availableEdges.size() == 0){
			return false;
		}
		for(Edge e : availableEdges){
			if(e.visits < 1){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Select
	 * @return
	 */
	public Edge getSelectionEdge(){
		double bestScore = Double.NEGATIVE_INFINITY;
		Edge result = null;
		for(Edge e : availableEdges){
			//System.out.println("UCB " + e.getUCBScore());
			double escore = e.getUCBScore();
			if(escore > bestScore){
				bestScore = escore;
				result = e;
			}
		}
		if(result == null){
			throw new NullPointerException("No Selection Edge found WTF " + availableEdges.size());
		}
		return result;
	}
	/**
	 * Expand
	 * @return
	 */
	public Edge getExpansionEdge(){
		int pathsLessTraveled = 0;
		for(Edge e : availableEdges){
			if(e.visits < 1){
				pathsLessTraveled++;
			}
		}
		if(pathsLessTraveled == 0){
			//just to be sure
			throw new NullPointerException("Node is fully expanded!");
		}
		Random random = new Random();
		pathsLessTraveled = random.nextInt(pathsLessTraveled);
		for(Edge e : availableEdges){
			if(e.visits < 1){
				if(pathsLessTraveled > 0){
					pathsLessTraveled--;
				} else {
					return e;
				}
			}
		}
		throw new NullPointerException("No selection edge found WTF");
	}
	
	private LinkedList<Edge> getRealEdges(LinkedList<Edge> newEdges){
		LinkedList<Edge> result = new LinkedList<Edge>();
		for(Edge e : newEdges){
			if(!edges.contains(e)){
				edges.add(e);
				result.add(e);
			} else {
				int index = edges.indexOf(e);
				result.add(edges.get(index));
			}
		}
		return result;
	}
	
	public Edge getMostVisitedEdge() {
		int max = -1;
		Edge result = null;
		for(Edge e : edges) {
			if(e.visits > max) {
				max = e.visits;
				result = e;
			}
		}
		return result;
	}
	
	public Edge getBestAverageEdge() {
		double max = Double.NEGATIVE_INFINITY;
		Edge result = null;
		for(Edge e : edges) {
			double eAverage = e.wins/e.visits;
			if(e.visits > max) {
				max = eAverage;
				result = e;
			}
		}
		return result;
	}
	
	public void backPropogation(String winner){
		if(winner == player){
			wins++;
		}
	}
}
