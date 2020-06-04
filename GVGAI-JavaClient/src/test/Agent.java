package test;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Agent extends AbstractPlayer {

    protected Random randomGenerator;
    protected ArrayList<Types.ACTIONS> actions;
    private int npcPos = 0;
    private boolean npcMovingInTime = false;

    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        randomGenerator = new Random();
        actions = so.getAvailableActions();
    }


    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        // Al inicio registramos la primer posicion de los PNJ
        if (stateObs.getGameTick() == 0) {
            this.movingNPC(stateObs);
        }

        // Ejecutamos el codigo cada 5 ticks
        if (stateObs.getGameTick() % 5 == 0) {
            if (this.deterministicOrStochastic(stateObs)) {
                System.out.println("Deterministic");
            } else {
                System.out.println("Stochastic");
            }
        }

        // Cuando llegamos al 200 se detiene la ejecucion
        if (stateObs.getGameTick() >= 200) {
            System.out.println("Ending");
            System.exit(1);
        }

        // Se ejecuta una accion aleatoria
        int index = randomGenerator.nextInt(actions.size());
        return actions.get(index);
    }

    // Detecta si existe algun Personaje No Jugable moviendose actualmente mediante el estado del juego
    private void movingNPC (StateObservation so) {
        StateObservation state = so.copy();
        if (state.getNPCPositions() != null && state.getNPCPositions().length > 0) {
            if (npcPos == 0) {
                npcPos = this.hashObs(state.getNPCPositions());
            } else {
                if (npcPos != this.hashObs(state.getNPCPositions())) {
                    this.npcMovingInTime = true;
                } else {
                    this.npcMovingInTime = false;
                }
                npcPos = this.hashObs(state.getNPCPositions());
            }
        }
    }

    //Determina si es estocastico o determinista. Regresa verdadero si esdeterminista y falso si es estocastico
    private boolean deterministicOrStochastic (StateObservation so) {
        boolean existsnpc = false;
        boolean deterministic = true;
        int runs = 2;
        int steps = 20;

        //  Check NPCs
        StateObservation[] obs = new StateObservation[runs];

        for(int i = 0; i < runs; i++) {
            obs[i] = so.copy();
            for(int j = 0; j < steps; j++) {
                obs[i].advance(Types.ACTIONS.ACTION_NIL);
            }

            if(obs[i].getNPCPositions() != null && obs[i].getNPCPositions().length > 0) {
                existsnpc = true;
            }
        }

        // Si hay npcs revisa si se mueven
        if (existsnpc) {
            System.out.print("NPC ");
            this.movingNPC(so);

            if (!this.npcMovingInTime) {
                System.out.print("static ");
                // Check if there are others objects that move
                if (this.movableObjs(so)) {
                    System.out.print("movable objs ");
                    deterministic = false;
                }
            } else {
                System.out.print("moving ");
                deterministic = false;
            }
        } else {
            // Revisa si existen otros objetos que se mueven
            if (this.movableObjs(so)) {
                System.out.print("movable objs ");
                deterministic = false;
            }
        }

        return deterministic;
    }

    // Detecta si existen otro tipo de objetos moviendose
    private boolean movableObjs (StateObservation so) {
        boolean movObjs = false;
        int runs = 2;
        int steps = 15;

        List<Observation>[] movables = so.getMovablePositions();

        if (movables != null && movables.length > 0) {
            StateObservation state = so.copy();
            for(int i = 0; i < runs; i++) {
                int hashbefore = this.hashObs(state.getMovablePositions());

                for(int j = 0; j < steps; j++) {
                    state.advance(Types.ACTIONS.ACTION_NIL);
                }

                int hashafter = this.hashObs(state.getMovablePositions());

                if (hashbefore != hashafter) {
                    movObjs = true;
                    break;
                }
            }
        }

        return movObjs;
    }

    // Genera un hash unico de cada estado a partir de las posiciones de los objetos recibidos
    private int hashObs(java.util.List<Observation>[] observations) {
        if (observations == null) {
            System.out.println("Hash failed");
            return 0;
        }

        int sum = 0;
        for (List<Observation> observationsOfSameType : observations) {
            for (Observation anObservation : observationsOfSameType) {
                sum += (int) (anObservation.obsID * (anObservation.position.x + anObservation.position.y));
            }
        }
        return sum;
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
    }


}
