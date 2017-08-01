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
		map = previousNode.map.getMapCopy();
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
		if(determ == null){
			throw new NullPointerException("No determinization available");
		}
		Edge result = null;
		availableEdges = getRealEdges(determ.getAvailableEdges(this));
		
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
		// increment available edge's availibility
		for(Edge e : availableEdges){
			e.available++;
		}
		return result;
	}
	
	public void setAvailableMoves(LinkedList<Edge> moves){
		// To some extent expansion step
		availableEdges = moves;
	}
	
	private boolean isFullyExpanded(){
		// check that every available edge has been visited at least once
		for(Edge e : availableEdges){
			if(e.visits < 1){
				return false;
			}
		}
		return false;
	}
	
	/**
	 * Select
	 * @return
	 */
	public Edge getSelectionEdge(){
		double bestScore = 0.0;
		Edge result = null;
		for(Edge e : availableEdges){
			if(e.getUCBScore() > bestScore){
				result = e;
			}
		}
		if(result == null){
			throw new NullPointerException("No Selection Edge found WTF");
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
	
	public void backPropogation(String winner){
		if(winner == player){
			wins++;
		}
	}
}
