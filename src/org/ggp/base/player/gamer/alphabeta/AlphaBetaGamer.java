package org.ggp.base.player.gamer.alphabeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ggp.base.apps.player.detail.DetailPanel;
import org.ggp.base.apps.player.detail.SimpleDetailPanel;
import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

public final class AlphaBetaGamer extends StateMachineGamer{
	@Override
	public String getName() {
		return "AlphaBetaPlayer";
	}

	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		long start = System.currentTimeMillis();
		//List<List<Move>> moves = getStateMachine().getLegalJointMoves(getCurrentState());
		List<Move> legalMoves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		Move selection = bestMove(getRole(), getCurrentState());
		long stop = System.currentTimeMillis();
		notifyObservers(new GamerSelectedMoveEvent(legalMoves, selection, stop - start));
		return selection;
	}


	public Move bestMove(Role role, MachineState state){
		List<Move> actions;
		try {
			actions = getStateMachine().getLegalMoves(state, role);
			Move action = actions.get(0);
			int score = 0;
			for (int i = 0; i < actions.size(); i++){
				int result = minscore(role, actions.get(i), state, 0, 100);
				if (result == 100) return actions.get(i);
				if (result > score){
					score = result;
					action = actions.get(i);
				}
			}
			return action;
		} catch (MoveDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public int minscore(Role role, Move move, MachineState state, int alpha, int beta){
		try {
			Map<Role, Integer> roles = getStateMachine().getRoleIndices();
			int ourRole = roles.get(role);
			Role opponent = null;
			if (ourRole == 1){
				opponent = getStateMachine().getRoles().get(0);
			} else {
				opponent = getStateMachine().getRoles().get(1);
			}
			List<Move> moves = getStateMachine().getLegalMoves(state, opponent);
			for (int i = 0; i < moves.size(); i++){
				List<Move> jointMove = new ArrayList<Move>();
				if (ourRole == 0){
					jointMove.add(move);
					jointMove.add(1, moves.get(i));
				} else {
					jointMove.add(moves.get(i));
					jointMove.add(1, move);
				}
				MachineState nextState = getStateMachine().getNextState(state, jointMove);
				int result = maxscore(role, nextState, alpha, beta);
				if (beta > result) beta = result;
				if (beta <= alpha) return alpha;
			}
			return beta;
		} catch (MoveDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransitionDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public int maxscore(Role role, MachineState state, int alpha, int beta){
			try {
				if (getStateMachine().isTerminal(state)) return getStateMachine().getGoal(state, role);
				List<Move> actions = getStateMachine().getLegalMoves(state, role);
				//int score = 0;
				for (int i = 0; i < actions.size(); i++){
					int result = minscore(role, actions.get(i), state, alpha, beta);
					if (result > alpha) alpha = result;
					if (alpha >= beta) return beta;
				}
				return alpha;
			} catch (GoalDefinitionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MoveDefinitionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return 0;
	}

/*	public Move bestMove(Role role, MachineState state, List<List<Move>> moves){
		Map<Role, Integer> roles = getStateMachine().getRoleIndices();
		int ourRole = roles.get(role);
		int score = 0;
		Move bestMove = null;
		for (int i = 0; i < moves.size(); i++){
			int result = minscore(role, state, moves.get(i));
			if (result > score){
				score = result;
				bestMove = moves.get(i).get(ourRole);
			}
		}
		return bestMove;
	}

	public int minscore(Role role, MachineState state, List<Move> moves){
		try {
			MachineState newState = getStateMachine().getNextState(state, moves);

		} catch (TransitionDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}*/

/*
	public Move bestMove(Role role, MachineState state, List<List<Move>> moves, long start, long timeout) {
		Map<Role, Integer> roles = getStateMachine().getRoleIndices();
		int ourRole = roles.get(role);
		List<Move> action = moves.get(0);
		int score = 0;
		for(int i = 0; i < moves.size(); i++) {
			int result = minscore(role, moves.get(i), state, start, timeout);
			if(result == 100) return moves.get(i).get(ourRole);
			if(result > score){
				score = result;
				action = moves.get(i);
			}
		}
		return action.get(ourRole);
	}

	public int minscore(Role role, List<Move> moveMade, MachineState state, long start, long timeout) {
		try {
			if (getStateMachine().isTerminal(state)) return getStateMachine().getGoal(state, role);
			int score = 100;
			MachineState nextState = getStateMachine().getNextState(state, moveMade);
			List<List<Move>> jointMoves = getStateMachine().getLegalJointMoves(nextState);
			for(int i = 0; i < jointMoves.size(); i++) {
				List<Move> moves= jointMoves.get(i);
				MachineState newState = getStateMachine().getNextState(state, moves);
				int result = maxscore(role, newState, start, timeout);
				if(result < score)
					score = result;
			}
			return score;
		} catch (MoveDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransitionDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GoalDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	public int maxscore(Role role, MachineState state, long start, long timeout) {
			try {
				if (getStateMachine().isTerminal(state)) return getStateMachine().getGoal(state, role);
				List<List<Move>> actions = getStateMachine().getLegalJointMoves(state);
				int score = 0;
				for (int i = 0; i < actions.size(); i++){
					int result = minscore(role, actions.get(i), state, start, timeout);
					if (result == 100) return 100;
					if (result >score) score = result;
				}
				return score;
			} catch (GoalDefinitionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MoveDefinitionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return 0;
	}
*/
	@Override
	public StateMachine getInitialStateMachine() {
		return new CachedStateMachine(new ProverStateMachine());
	}

	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		// Random gamer does no game previewing.
	}

	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		// Random gamer does no metagaming at the beginning of the match.
	}

	@Override
	public void stateMachineStop() {
		// Random gamer does no special cleanup when the match ends normally.
	}

	@Override
	public void stateMachineAbort() {
		// Random gamer does no special cleanup when the match ends abruptly.
	}

	@Override
	public DetailPanel getDetailPanel() {
		return new SimpleDetailPanel();
	}
}