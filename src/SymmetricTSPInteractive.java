import java.util.*;

public class SymmetricTSPInteractive {

    static class Route {
        List<Integer> cities;
        int[][] distanceMatrix;
        int distance;

        public Route(List<Integer> cities, int[][] distanceMatrix) {
            this.cities = new ArrayList<>(cities);
            this.distanceMatrix = distanceMatrix;
            calculateDistance();
        }

        void calculateDistance() {
            distance = 0;
            for (int i = 0; i < cities.size() - 1; i++) {
                distance += distanceMatrix[cities.get(i)][cities.get(i + 1)];
            }
            distance += distanceMatrix[cities.get(cities.size() - 1)][cities.get(0)];
        }

        void mutate(double mutationProbability) {
            Random random = new Random();
            if (random.nextDouble() < mutationProbability) {
                int idx1 = random.nextInt(cities.size());
                int idx2 = random.nextInt(cities.size());
                Collections.swap(cities, idx1, idx2);
                calculateDistance();
            }
        }

        void localImprove() {
            for (int i = 0; i < cities.size() - 1; i++) {
                for (int j = i + 1; j < cities.size(); j++) {
                    Collections.swap(cities, i, j);
                    int newDistance = 0;
                    for (int k = 0; k < cities.size() - 1; k++) {
                        newDistance += distanceMatrix[cities.get(k)][cities.get(k + 1)];
                    }
                    newDistance += distanceMatrix[cities.get(cities.size() - 1)][cities.get(0)];
                    if (newDistance < distance) {
                        distance = newDistance;
                    } else {
                        Collections.swap(cities, i, j); // Відкат змін
                    }
                }
            }
        }
    }

    static class Population {
        List<Route> routes;
        int[][] distanceMatrix;

        public Population(int size, int[][] distanceMatrix) {
            this.distanceMatrix = distanceMatrix;
            routes = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                List<Integer> cities = new ArrayList<>();
                for (int j = 0; j < distanceMatrix.length; j++) {
                    cities.add(j);
                }
                Collections.shuffle(cities);
                routes.add(new Route(cities, distanceMatrix));
            }
        }

        Route selectParent() {
            return routes.get(new Random().nextInt(routes.size()));
        }

        Route crossover(Route parent1, Route parent2) {
            Random random = new Random();
            int start = random.nextInt(parent1.cities.size());
            int end = random.nextInt(parent1.cities.size());
            if (start > end) {
                int temp = start;
                start = end;
                end = temp;
            }

            List<Integer> childCities = new ArrayList<>(Collections.nCopies(parent1.cities.size(), -1));
            for (int i = start; i <= end; i++) {
                childCities.set(i, parent1.cities.get(i));
            }

            int currentIndex = 0;
            for (int city : parent2.cities) {
                if (!childCities.contains(city)) {
                    while (childCities.get(currentIndex) != -1) {
                        currentIndex++;
                    }
                    childCities.set(currentIndex, city);
                }
            }
            return new Route(childCities, parent1.distanceMatrix);
        }

        void evolve(double mutationProbability) {
            List<Route> newRoutes = new ArrayList<>();
            for (int i = 0; i < routes.size(); i++) {
                Route parent1 = selectParent();
                Route parent2 = selectParent();
                Route child = crossover(parent1, parent2);
                child.mutate(mutationProbability);
                child.localImprove();
                newRoutes.add(child);
            }
            routes = newRoutes;
        }

        Route getBestRoute() {
            return routes.stream().min(Comparator.comparingInt(r -> r.distance)).orElse(null);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Інтерактивний ввід
        System.out.println("Enter the number of cities: ");
        int numCities = scanner.nextInt();

        System.out.println("Enter the minimum distance between cities: ");
        int minDistance = scanner.nextInt();

        System.out.println("Enter the maximum distance between cities: ");
        int maxDistance = scanner.nextInt();

        System.out.println("Enter the population size: ");
        int populationSize = scanner.nextInt();

        System.out.println("Enter the number of iterations: ");
        int iterations = scanner.nextInt();

        // Фіксоване значення ймовірності мутації
        double mutationProbability = 0.1;

        // Генерація матриці ваг
        int[][] distanceMatrix = generateSymmetricMatrix(numCities, minDistance, maxDistance);

        System.out.println("Generated Distance Matrix:");
        printMatrix(distanceMatrix);

        Population population = new Population(populationSize, distanceMatrix);

        System.out.println("\nInitial Population:");
        for (Route route : population.routes) {
            System.out.println("Distance: " + route.distance);
        }

        for (int i = 0; i < iterations; i++) {
            population.evolve(mutationProbability);
        }

        Route bestRoute = population.getBestRoute();

        System.out.println("\nFinal Population:");
        for (Route route : population.routes) {
            System.out.println("Distance: " + route.distance);
        }

        System.out.println("\nBest Route:");
        System.out.println("Distance: " + bestRoute.distance);
        System.out.println("Route: " + bestRoute.cities);
    }

    private static int[][] generateSymmetricMatrix(int numCities, int minDistance, int maxDistance) {
        Random random = new Random();
        int[][] matrix = new int[numCities][numCities];
        for (int i = 0; i < numCities; i++) {
            for (int j = i; j < numCities; j++) {
                if (i == j) {
                    matrix[i][j] = 0;
                } else {
                    matrix[i][j] = matrix[j][i] = random.nextInt(maxDistance - minDistance + 1) + minDistance;
                }
            }
        }
        return matrix;
    }

    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }
}
