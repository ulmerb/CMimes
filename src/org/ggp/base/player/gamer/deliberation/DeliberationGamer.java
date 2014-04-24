package org.ggp.base.player.gamer.deliberation;

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

public class DeliberationGamer extends StateMachineGamer{
	List<Move> GamePath = new ArrayList<Move>();
	int counter = 0;

	@Override
	public String getName() {
		return "SinglePlayerGame";
	}

	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		System.out.println("Reached move");
		long start = System.currentTimeMillis();
		List<Move> moves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		Move selection = GamePath.get(counter++);
		long stop = System.currentTimeMillis();
		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}


	@Override
	public StateMachine getInitialStateMachine() {
		return new CachedStateMachine(new ProverStateMachine());
	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException {
		GamePath = new ArrayList<Move>();
		findWinningPath(getRole(), getCurrentState());
	}

	public boolean findWinningPath(Role role, MachineState state){
			try {
				if (getStateMachine().isTerminal(state) && getStateMachine().getGoal(state, role) == 100) return true;
				if (getStateMachine().isTerminal(state)) return false;
				List<Move> actions = getStateMachine().getLegalMoves(state, role);
				for (int i = 0; i < actions.size(); i++){
					List<Move> moves = new ArrayList<Move>();
					moves.add(actions.get(i));
					if (findWinningPath(role, getStateMachine().getNextState(state, moves))){
						GamePath.add(0, actions.get(i));
						System.out.println(GamePath.size());
						return true;
					}
				}
				return false;
			} catch (GoalDefinitionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MoveDefinitionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransitionDefinitionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		return false;

	}



	@Override
	public DetailPanel getDetailPanel() {
		return new SimpleDetailPanel();
	}


	@Override
	public void stateMachineStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stateMachineAbort() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		// TODO Auto-generated method stub

	}
}