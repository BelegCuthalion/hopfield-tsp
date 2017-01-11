package aut.mahmoudian;

import java.util.Random;

/**
 * Created by beleg on 12/23/16.
 */
public class MatrixTsp {
    int n;
    double A, B, C, D;
    double[][] d;
    private double[][] N;
    private double[][] newN;
    double energy = 0.0;
    public MatrixTsp(double A, double B, double C, double D, double[][] d, int n) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.D = D;
        this.d = d;
        this.n = n;
        this.N = new double[n][n];
    }

    /**
     *
     * @return optimal path as int[]
     */
    public int[] run(boolean sync) {
        //  initialize v(0)
        Random randomGenerator = new Random();
        double[][] randomInput = new double[n][n];
        double inputSum = 0;
        for(int i=0; i<n; i++)
            for(int j=0; j<n; j++) {
                randomInput[i][j] = randomGenerator.nextDouble();
                inputSum += randomInput[i][j];
            }
        for(int i=0; i<n; i++)
            for(int j=0; j<n; j++)
                N[i][j] = randomInput[i][j] * n / inputSum;
        //  FIXME sum(N[i][j])=n but N[i][j]s might not be in [0,1)

        double lastEnergy, nextEnergy;
        int[] path = new int[n];
        do {
            lastEnergy = calculateEnergy();
//            System.out.println("E = " + lastEnergy);
//            for(int x=0; x<n; x++) {
//                for (int i = 0; i < n; i++)
//                    System.out.print(N[x][i] + "\t");
//                System.out.println();
//            }
//            System.out.print("PATH:\t");
            double cost = 0.0;
            for(int i=0; i<n; i++) {
                path[i] = argMax(getMatrixColumn(i));
//                System.out.printf("%d\t", path[i]);
            }
//            for(int i=0; i<n; i++) {
//                cost += d[path[i]][path[(i+1) % n]];
//            }
//            System.out.println("\nCOST:\t" + cost);

            //  new iteration
            if(sync)
                syncRecursion();
            else
                asyncRecursion();

            nextEnergy = calculateEnergy();
        } while (lastEnergy - nextEnergy > Double.MIN_VALUE);

        return path;
    }

    /**
     * Calculates energy of the current system
     * @return energy of the current system.
     */
    public double calculateEnergy() {
        double term1 = 0.0;
        double term2 = 0.0;
        double term3 = 0.0;
        double term4 = 0.0;

        for(int x=0; x<n; x++)
            for(int i=0; i<n; i++)
                for(int j=0; j<n; j++)
                    if(i!=j)
                        term1 += N[x][i]*N[x][j];

        for(int i=0; i<n; i++)
            for(int x=0; x<n; x++)
                for(int y=0; y<n; y++)
                    if(x!=y)
                        term2 += N[x][i]*N[y][i];

        for(int x=0; x<n; x++)
            for(int i=0; i<n; i++)
                term3 += N[x][i];

        for(int x=0; x<n; x++)
            for(int y=0; y<n; y++)
                for(int i=0; i<n; i++)
                    term4 += d[x][y] * N[x][i] * (N[y][(((i - 1) % n) + n) % n] + N[y][(i + 1) % n]);

         return ((A/2) * term1) + ((B/2) * term2) + ((C/2) * (term3 - n) * (term3 - n)) + ((D/2) * term4);
    }

    private void asyncRecursion() {
        int[] xOrder, iOrder;
        xOrder = generateRandomOrder(n);
        iOrder = generateRandomOrder(n);

        for(int x : xOrder)
            for(int i : iOrder)
                N[x][i] = calculateActivation(calculateNetInput(x, i) + (C*n));
    }

    private void syncRecursion() {
        double[][] newN = new double[n][n];

        for(int x=0; x<n; x++)
            for(int i=0; i<n; i++)
                newN[x][i] = calculateActivation(calculateNetInput(x,i) + (C*n));

//        N = newN;
    }

    private int[] generateRandomOrder(int n) {
        Random random = new Random();
        int[] order = new int[n];
        for(int i=0; i<n; i++)
            order[i] = i;
        int nextToSwap, temp;
        for(int i=0; i<n; i++) {
            nextToSwap = i + random.nextInt(n - i);
            temp = order[i];
            order[i] = order[nextToSwap];
            order[nextToSwap] = temp;
        }
        return order;
    }

    private double calculateNetInput(int x, int i) {
        double term1 = 0.0;
        double term2 = 0.0;
        double term3 = 0.0;
        double term4 = 0.0;

        for(int j=0; j<n; j++)
            if(i!=j)
                term1 += N[x][j];

        for(int y=0; y<n; y++)
            if(x!=y)
                term2 += N[y][i];

        for(int y=0; y<n; y++)
            for(int j=0; j<n; j++)
                if(x!=y || i!=j)
                    term3 += N[y][j];

        for(int y=0; y<n; y++)
            term4 += d[x][y] * (N[y][(((i - 1) % n) + n) % n] + N[y][(i + 1) % n]);

        return (-A * term1) + (-B * term2) + (-C * (term3 - n)) + (-D * term4);
    }

    private double calculateActivation(double net) {
        return (Math.tanh(net) + 1) / 2;
    }

    private double[] getMatrixColumn(int step) {
        double[] result = new double[n];
        for(int i=0; i<n; i++)
            result[i] = N[i][step];
        return result;
    }

    private int argMax(double...params) {
        double max = 0;
        int maxIdx = 0;
        for(int i=0; i<params.length; i++)
            if(params[i] > max) {
                max = params[i];
                maxIdx = i;
            }
        return maxIdx;
    }
}
