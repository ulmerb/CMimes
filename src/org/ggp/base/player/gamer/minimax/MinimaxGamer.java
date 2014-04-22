package org.ggp.base.player.gamer.minimax;

import java.util.ArrayList;
import java.util.List;

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

public final class MinimaxGamer extends StateMachineGamer{
	@Override
	public String getName() {
		return "Random";
	}

	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		long start = System.currentTimeMillis();

		List<Move> moves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		Move selection = bestMove(getRole(), getCurrentState(), moves);

		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}

	public Move bestMove(Role role, MachineState state, List<Move> moves) {
		Move action = moves.get(0);
		int score = 0;
		for(int i = 0; i < moves.size(); i++) {
			int result = minscore(role, moves.get(i), state);
			if(result == 100) return moves.get(i);
			if(result > score){
				score = result;
				action = moves.get(i);
			}
			return action;
		}
	}

	public int minscore(Role role, Move action, MachineState state) {

		try {
			Role opponent = getStateMachine().getRoles().get(0);
			if(opponent == role) opponent = getStateMachine().getRoles().get(1);
			List<Move> opponentActions;
			opponentActions = getStateMachine().getLegalMoves(getCurrentState(), opponent);
			int score = 100;
			for(int i = 0; i < opponentActions.size(); i++) {
				Move move= opponentActions.get(i);
				List<Move> jointMove = new ArrayList<Move>();
				if(getStateMachine().getRoles().get(0) == role) {
					jointMove.add(0, action);
					jointMove.add(1, move);
				} else {
					jointMove.add(0, move);
					jointMove.add(1, action);
				}

				MachineState newState = getStateMachine().getNextState(state, jointMove);
				int result = maxscore(role, newState);
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
		}

		return 0;
	}

	public int maxscore(Role role, MachineState state) {
		return 0;
	}

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
