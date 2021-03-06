package battle.controllers.diego.search;

import battle.SimpleBattle;
import battle.controllers.diego.ActionMap;
import battle.controllers.diego.strategy.ICrossover;
import battle.controllers.diego.strategy.IMutation;
import battle.controllers.diego.strategy.ISelection;
import battle.controllers.diego.strategy.OpponentGenerator;

import java.util.Random;

/**
 * PTSP-Competition
 * Random Search engine. Creates random paths and determines which one is the best to execute according to an heuristic.
 * It keeps looking during MACRO_ACTION_LENGTH time steps. After that point, the search is reset.
 * Created by Diego Perez, University of Essex.
 * Date: 17/10/12
 */
public class GASearch extends Search {

    /**
     * Current game state
     */
    public static SimpleBattle m_currentGameState;

    /**
     * Game state used to roll actions and evaluate  the current path.
     */
    public static SimpleBattle m_futureGameState;


    public static int TOURNAMENT_SIZE = 3;
    public final int ELITISM = 2;
    public final int NUM_INDIVIDUALS = 10;


    public GAIndividual[] m_individuals;
    ICrossover cross;
    IMutation mut;
    ISelection sel;
    public int m_numGenerations;

    OpponentGenerator opponentGen;

    /**
     * Constructor of the random search engine.
     */
    public GASearch(ICrossover ic, IMutation im, ISelection is, OpponentGenerator opponentGen, Random rnd)
    {
        super(rnd);
        cross = ic;
        mut = im;
        sel = is;
        this.opponentGen = opponentGen;
    }

    /**
     * Initializes the random search engine. This function is also called to reset it.
     */
    @Override
    public void init(SimpleBattle gameState, int playerId)
    {
        m_individuals = new GAIndividual[NUM_INDIVIDUALS];
        this.playerID = playerId;
        GAIndividual opponent = opponentGen.getOpponent(Search.NUM_ACTIONS_INDIVIDUAL);

        for(int i = 0; i < NUM_INDIVIDUALS; ++i)
        {
            m_individuals[i] = new GAIndividual(Search.NUM_ACTIONS_INDIVIDUAL, playerID);
            m_individuals[i].randomize(m_rnd, ActionMap.ActionMap.length );
            m_individuals[i].evaluate(gameState, opponent);
        }

        sortPopulationByFitness(m_individuals);

        //Resetting the random paths found and best fitness.
        m_bestRandomPath = new int[NUM_ACTIONS_INDIVIDUAL];
        m_currentRandomPath = new int[NUM_ACTIONS_INDIVIDUAL];
        m_bestFitnessFound = -1;
        m_numGenerations = 0;
    }



    /**
     * Runs the Random Search engine for one cycle.
     * @param a_gameState Game state where the macro-action to be decided must be executed from.
     * @return  the action decided to be executed.
     */
    @Override
    public int run(SimpleBattle a_gameState)
    {
        m_currentGameState = a_gameState;
        int numIters = 0;

        //check that we don't overspend
        while(numIters < 100)
        {
            GAIndividual[] nextPop = new GAIndividual[m_individuals.length];

            int i;
            for(i = 0; i < ELITISM; ++i)
            {
                nextPop[i] = m_individuals[i];
            }

            GAIndividual opponent = opponentGen.getOpponent(Search.NUM_ACTIONS_INDIVIDUAL);

            for(;i<m_individuals.length;++i)
            {
                //System.out.println("######### " + numIters + ":" + i + " #############");
                nextPop[i] = breed(); // m_individuals[i-ELITISM].copy();


                mut.mutate(nextPop[i]);

                //System.out.print("c-m: " + nextPop[i].toString());
                nextPop[i].evaluate(a_gameState, opponent);
                //System.out.println(", " + nextPop[i].m_fitness);
            }

            m_individuals = nextPop;
            sortPopulationByFitness(m_individuals);

            /*for(i = 0; i < m_individuals.length; ++i)
                System.out.format("individual i: " + i + ", fitness: %.3f, actions: %s \n", m_individuals[i].m_fitness, m_individuals[i].toString());     */


            m_numGenerations++;
            numIters++;
        }

        return m_individuals[0].m_genome[0];
    }


    private GAIndividual breed()
    {
        GAIndividual gai1 = sel.getParent(m_individuals, null);        //First parent.
        GAIndividual gai2 = sel.getParent(m_individuals, gai1);        //Second parent.
        return cross.uniformCross(gai1, gai2);
    }

    /**
     * Provides an heuristic score of the game state m_futureGameState.
     * @return the score.
     */
    @Override
    public double scoreGame(SimpleBattle game)
    {
        return game.score(playerID); //game.getPoints(playerID);
    }



}
