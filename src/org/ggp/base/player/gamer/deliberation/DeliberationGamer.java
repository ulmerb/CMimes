package org.ggp.base.player.gamer.deliberation;

import java.util.ArrayList;
import java.util.Arrays;
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

public class DeliberationGamer extends StateMachineGamer{
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

	public Move bestMove(Role role, MachineState state, List<Move> actions) {
		Move action = actions.get(0);
		int score = 0;
		try {
			for(int i = 0; i < actions.size(); i++) {
				int result;
				result = maxScore(role, getStateMachine().getNextState(state, new ArrayList<Move>(Arrays.asList(action))));

				if(result == 100)
					return actions.get(i);
				if(result > score) {
					score = result;
					action = actions.get(i);
				}
			}
				//return action;
			}
		catch (TransitionDefinitionException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return action;
	}

	public int maxScore(Role role, MachineState state) {
		int score = 0;
		try {
			if (getStateMachine().isTerminal(state))
				return getStateMachine().getGoal(state, role);

			List<Move> actions = getStateMachine().getLegalMoves(getCurrentState(), getRole());
			for (int i=0; i<actions.size(); i++) {
				int result = maxScore(role, getStateMachine().getNextState(state, new ArrayList<Move>(Arrays.asList(actions.get(i)))));
				if (result > score)
					score = result;
			}

		} catch(TransitionDefinitionException e) {
			e.printStackTrace();
		}
		return score;
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
