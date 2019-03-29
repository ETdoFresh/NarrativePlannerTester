package qa;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import sabre.*;
import sabre.graph.PlanGraph;
import sabre.graph.PlanGraphActionNode;
import sabre.graph.PlanGraphEventNode;
import sabre.graph.PlanGraphLiteralNode;
import sabre.graph.PlanGraphNode;
import sabre.io.DefaultParser;
import sabre.io.Parser;
import sabre.logic.ConjunctiveClause;
import sabre.logic.DNFExpression;
import sabre.logic.Expression;
import sabre.logic.Literal;
import sabre.search.Result;
import sabre.search.Search;
import sabre.space.RootNode;
import sabre.space.SearchSpace;
import sabre.state.ArrayState;
import sabre.util.ImmutableArray;

public class Main {

	private static final String VERSION = "0.01.1";
	private static final String CREDITS = "Rachel Farrell and Edward Garcia";
	private static final String TITLE = "Planning Domain Automated Tester v" + VERSION + ", by " + CREDITS
			+ "\n using the Sabre Narrative Planner v0.32 by Stephen G. Ware";
	private static final String USAGE = "... Sit back, relax, and enjoy the demo ...\n";
	private static final String FILE = "RRH.txt";

	private static final String PASS = "[" + TextColor.GREEN + "Pass" + TextColor.RESET + "] ";
	private static final String FAIL = "[" + TextColor.RED + "Fail" + TextColor.RESET + "] ";
	@SuppressWarnings("unused")
	private static final String WARN = "[" + TextColor.YELLOW + "Warn" + TextColor.RESET + "] ";
	private static final String INFO = "[" + TextColor.BLUE + "Info" + TextColor.RESET + "] ";
	private static final String BLANK = "       ";

	private static final String SYNTAX = "File should be syntactically correct";
	private static final String GOAL = "Goal should be specified";
	private static final String INITIAL = "Goal should not be true in initial state";
	private static final String ACTIONS = "All action schemas should be usable";
	private static final String SOLUTION = "Goal should be achievable";

	static long lastModified = 0;
	static boolean firstRun = true;
	static Result result = null;
	static Search search = null;
	static ArrayList<Plan> plans = new ArrayList<Plan>();
	static File file;

	public static void main(String[] args) {

		// Open text file on desktop
		file = new File(FILE);
		Desktop desktop = Desktop.getDesktop();
		try {
			file.createNewFile(); // does nothing if file exists
			desktop.open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Clear/Reset Screen
		System.out.flush();
		System.out.println(TITLE);
		System.out.println(USAGE);

		while (true) {
			if (lastModified == file.lastModified()) {
				if (search != null && result != null) {
					if (result.plan == null) {
						System.out.println(INFO + "Search has found all plans");
						search = null;
						result = null;
						continue;
					}

					int planIndex = plans.size();
					System.out.println(INFO + "---------------- Solution " + planIndex + " ----------------");
					System.out.println(BLANK + result);
					for (Action action : result.plan)
						System.out.println(BLANK + action);

					plans.add(result.plan);

					// Evaluate plan vs other plans (all plans except last plan)
					for (int i = 0; i < plans.size() - 1; i++) {
						float jaccardDistance = getActionJaccard(plans.get(i), result.plan);
						System.out.println(
								BLANK + "Solution " + i + " vs Solution " + planIndex + ": " + jaccardDistance);
					}

					if (plans.size() > 2) {
						System.out.println(BLANK + "Cutting off after 3 solutions");
						result = null;
					} else {
						System.out.println(BLANK + "Searching for next solution...");
						result = runInteruptably(() -> search.getNextSolution());
					}
				} else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				continue;
			}

			// Update last modified
			lastModified = file.lastModified();
			result = null;
			search = null;
			plans.clear();

			if (firstRun) {
				firstRun = false;
				System.out.println(INFO + "File Opened: " + FILE + " Last Modified: "
						+ new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(lastModified));
			} else {
				System.out.println("----------------------------------------------------------------");
				System.out.println();
				System.out.println(INFO + "File Modified: " + FILE + " Last Modified: "
						+ new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(lastModified));
			}

			// Parse Domain
			Parser parser = new DefaultParser();
			Domain domain;
			try {
				domain = parser.parse(file, Domain.class);
			} catch (Exception ex) {
				System.out.println(FAIL + SYNTAX);
				System.out.println(ex);
				continue;
			}
			System.out.println(PASS + SYNTAX);

			// Space
			SearchSpace space = Utilities.get(status -> new SearchSpace(domain, status));
			System.out.println(INFO + "Number of ground actions: " + space.actions.size());
			System.out.println(INFO + "Number of state variables: " + space.slots.size());

			// Check if goal is empty
			if (domain.goal.equals(Expression.TRUE)) {
				System.out.println(FAIL + GOAL);
				continue;
			} else
				System.out.println(PASS + GOAL);

			// Check if goal is true in initial state
			ArrayState initial = new ArrayState(space);
			try {
				if (domain.goal.test(initial)) {
					System.out.println(FAIL + INITIAL);
					continue;
				} else
					System.out.println(PASS + INITIAL);
			} catch (Exception ex) {
				System.out.println(FAIL + "Exception while testing the goal in the initial state: " + ex);
				continue;
			}

			// Plan Graph
			space.graph.initialize(initial);
			while (!space.graph.hasLeveledOff())
				space.graph.extend(); // Extend graph until all goals have appeared
			// System.out.println(INFO + "Layers in plan graph: " + space.graph.size()); //
			// <---- just commenting out for demo

			// Get all the Relaxed Plans from the PlanGraph
			ArrayList<RelaxedPlan> plans = new ArrayList<>();
			for (Iterable<Literal> goal : GetDNFLiterals(space.goal))
				plans.addAll(GetAllPossiblePlanGraphPlans(space.graph, goal));
			
			// TODO Comment this out later, just displaying all relaxed Solutions
			for(int i = 0; i < plans.size(); i++)
			{
				System.out.println(INFO + "Relaxed Solution #" + i);
				System.out.println(plans.get(i));
			}

			// Number of actions available from the initial state
			int firstSteps = 0;
			System.out.println(INFO + "Actions possible from initial state: ");
			for (Action action : space.actions)
				if (action.precondition.test(initial)) {
					System.out.println("\t - " + action);
					firstSteps++;
				}
			System.out.println("\t (" + firstSteps + " total)");

			// TODO: Actions *motivated* from initial state, i.e. possible and the
			// characters would consent

			// Check for any unusable action schemas
			HashSet<Action> unusedActions = new HashSet<Action>();
			for (Action action : space.domain.actions) {
				boolean actionFound = false;
				for (PlanGraphEventNode graphEvent : space.graph.events)
					if (action.name == graphEvent.event.name) {
						actionFound = true;
						continue;
					}
				if (!actionFound)
					unusedActions.add(action);
			}
			if (unusedActions.size() == 0)
				System.out.println(PASS + ACTIONS);
			else {
				System.out.println(WARN + ACTIONS);
				for (Action action : unusedActions)
					System.out.println(BLANK + "Unusable: " + action.toString());
				// continue;
			}

			// Check if a solution exists
			Planner planner = new Planner();
			planner.setSearchSpace(space);
			search = planner.getSearchFactory().makeSearch(domain.goal);
			RootNode root = new RootNode(initial);
			search.push(root);
			System.out.println(BLANK + "Searching for next solution...");
			try {
				result = runInteruptably(() -> search.getNextSolution());
			} catch (Exception ex) {
				System.out.println(FAIL + "Exception while searching for solution: " + ex);
				continue;
			}
			if (result != null && result.plan != null)
				System.out.println(PASS + SOLUTION);
			else {
				System.out.println(FAIL + SOLUTION);
				result = null;
				search = null;
				continue;
			}
		}
	}

	private static <E> float getJaccard(Set<E> a, Set<E> b) {
		HashSet<E> intersection = new HashSet<>();
		HashSet<E> union = new HashSet<>();
		union.addAll(a);
		union.addAll(b);
		for (E item : a)
			if (b.contains(item))
				intersection.add(item);
		return 1 - (float) intersection.size() / union.size();
	}

	private static float getActionJaccard(Plan a, Plan b) {
		HashSet<Action> set_a = new HashSet<>();
		HashSet<Action> set_b = new HashSet<>();

		for (Action action : a)
			set_a.add(action);

		for (Action action : b)
			set_b.add(action);

		return getJaccard(set_a, set_b);
	}

	public static <T> T runInteruptably(Callable<T> task) {
		T result = null;
		boolean loop = true;
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Future<T> future = executor.submit(task);
		while (loop) {
			try {
				result = future.get(1, TimeUnit.SECONDS);
				if (future.isDone()) {
					loop = false;
					result = future.get();
				}
			} catch (Exception ex) {
				if (lastModified != file.lastModified()) {
					result = null;
					break;
				}
			}
		}
		executor.shutdownNow();
		return result;
	}

	// Returns a list of list of literals for each disjunct (ie disjunct goals).
	private static Iterable<Iterable<Literal>> GetDNFLiterals(Expression expression) {
		ArrayList<Iterable<Literal>> disjuncts = new ArrayList<>();
		for (ConjunctiveClause disjunct : expression.toDNF().arguments)
			disjuncts.add(GetLiterals(disjunct));
		return disjuncts;
	}

	// Returns a list of literals from a conjunctiveClause or individual literal
	private static Iterable<Literal> GetLiterals(Expression expression) {
		if (expression instanceof Literal)
			return new ArrayList<>(Arrays.asList((Literal) expression));
		else if (expression instanceof ConjunctiveClause) {
			ArrayList<Literal> literals = new ArrayList<>();
			for (Literal argument : ((ConjunctiveClause) expression).arguments)
				literals.add((Literal) argument);
			return literals;
		} else
			System.out.println(FAIL + "GetLiterals(): Only processes Literals and Conjunctions");

		return new ArrayList<>();
	}

	// Returns a list of all possible PlanGraph Plans
	private static Collection<RelaxedPlan> GetAllPossiblePlanGraphPlans(PlanGraph graph, Iterable<Literal> goal) {
		ArrayList<PlanGraphLiteralNode> planGraphGoal = new ArrayList<>();
		for (Literal literal : goal)
			planGraphGoal.add(graph.getLiteral(literal));

		return GetAllPossiblePlanGraphPlans(new ArrayList<RelaxedPlan>(), new RelaxedPlan(), planGraphGoal, planGraphGoal);
	}

	private static Collection<RelaxedPlan> GetAllPossiblePlanGraphPlans(ArrayList<RelaxedPlan> plans, RelaxedPlan plan,
			ArrayList<PlanGraphLiteralNode> localGoalLiterals, ArrayList<PlanGraphLiteralNode> absoluteGoalLiterals) {
		
		// Determine which goals have been found already
		ArrayList<PlanGraphLiteralNode> foundGoalLiterals = new ArrayList<>(absoluteGoalLiterals);
		for(int i = foundGoalLiterals.size()-1; i >= 0; i--)
			if (localGoalLiterals.contains(foundGoalLiterals.get(i)))
				foundGoalLiterals.remove(i);
		
		// Remove Initial State Literals from GoalLiterals
		for (int i = localGoalLiterals.size() - 1; i >= 0; i--) {
			PlanGraphLiteralNode goalLiteral = localGoalLiterals.get(i);
			if (goalLiteral.getLevel() == 0)
				localGoalLiterals.remove(goalLiteral);
		}

		// If GoalLiterals Size is 0, we are done! Add that plan!
		if (localGoalLiterals.size() == 0) {
			return new ArrayList<RelaxedPlan>(Arrays.asList(plan));
		}

		// Foreach Goal Literal, follow its parents.
		for (PlanGraphLiteralNode goalLiteral : localGoalLiterals) {
			for (PlanGraphNode actionNode : goalLiteral.parents) {
				PlanGraphActionNode action = (PlanGraphActionNode) actionNode;
				int min = action.graph.size();
				for(PlanGraphActionNode node : plan)
					if (node.getLevel() < min)
						min = node.getLevel();
				
				// Due to relaxed nature, do not use same ground action twice
				// Do not grab actions beyond current level (min)
				if (action.getLevel() > min || plan.contains(action))
					continue;

				ArrayList<PlanGraphLiteralNode> newGoalLiterals = new ArrayList<>(localGoalLiterals);
				newGoalLiterals.remove(goalLiteral);

				ImmutableArray<? extends Literal> newLiterals = action.parents.get(0).clause.arguments;
				for (Literal newLiteral : newLiterals)
					newGoalLiterals.add(action.graph.getLiteral(newLiteral));
				
				// Skip if this finds the goal again/earlier
				for (PlanGraphLiteralNode foundGoalLiteral : foundGoalLiterals)
					if (newGoalLiterals.contains(foundGoalLiteral))
						continue;

				RelaxedPlan planWithNewAction = plan.clone();
				planWithNewAction.push(action);

				Collection<RelaxedPlan> newPlan = GetAllPossiblePlanGraphPlans(plans, planWithNewAction, newGoalLiterals, absoluteGoalLiterals);
				if (newPlan != plans)
					plans.addAll(newPlan);
			}
		}

		return plans;
	}
}