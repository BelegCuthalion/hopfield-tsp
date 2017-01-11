package aut.mahmoudian;

import TSPLIB4J.src.org.moeaframework.problem.tsplib.TSPExample;
import TSPLIB4J.src.org.moeaframework.problem.tsplib.TSPInstance;
import TSPLIB4J.src.org.moeaframework.problem.tsplib.TSPPanel;
import TSPLIB4J.src.org.moeaframework.problem.tsplib.Tour;
import com.sun.org.apache.bcel.internal.generic.FLOAD;
import org.moeaframework.problem.misc.Lis;
import org.neuroph.nnet.Hopfield;
import org.neuroph.nnet.learning.HopfieldLearning;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;


public class Main {

    private static final String DATA_PATH = "./TSPLIB4J/data/tsp/";
    private static final String FILE = "burma14";
//    static double[][] d = {{0   ,   1   ,   10   ,  10},
//                           {1   ,   0   ,   1  ,   10},
//                           {10   ,   1   ,   0 ,   1},
//                           {10  ,   10  ,   1  ,   0}};

//    static double[][] d = {{0,1},
//                           {1,0}};
    public static void main(String[] args) {
        int iteration = 1827;
        try {
            TSPInstance tspInstance = new TSPInstance(new File(DATA_PATH + FILE + ".tsp"));
            double[][] d = new double[tspInstance.getDimension()][tspInstance.getDimension()];
            double max = 0.0;
            for(int i=0; i<tspInstance.getDimension(); i++)
                for(int j=0; j<tspInstance.getDimension(); j++)
                    if(tspInstance.getDistanceTable().getDistanceBetween(i+1, j+1) > max)
                        max = tspInstance.getDistanceTable().getDistanceBetween(i+1, j+1);

            for(int i=0; i<tspInstance.getDimension(); i++) {
                for (int j = 0; j < tspInstance.getDimension(); j++) {
                    d[i][j] = tspInstance.getDistanceTable().getDistanceBetween(i + 1, j + 1) / max;
                    System.out.print(d[i][j] + "\t");
                }
                System.out.println();
            }




            tspInstance.addTour(new File(DATA_PATH + FILE + ".opt.tour"));

            MatrixTsp matrixTsp;
            Tour matrixTspTour = new Tour();
            int veryMuchToleratedSoFar = 0;
            for(int A=100000; true; A+=100000) {
                veryMuchToleratedSoFar = 0;
                for (int C = A/500; C<A/20; C += 100) {
                    int toleratedSoFar = 0;
                    for (int D = C / 2500; D<C/25; D++) {
                        matrixTsp = new MatrixTsp(A, A, C, D, d, tspInstance.getDimension());
                        int feasibles = 0;
                        double alpha = 0;
                        java.util.List<String> rawOutput = new ArrayList<String>();
                        for (int r = 0; r < 10; r++) {
                            matrixTspTour.fromArray(Main.shiftOrder(matrixTsp.run(false)));
                            if (matrixTspTour.isHamiltonianCycle(tspInstance)) {
                                feasibles++;
                                alpha += tspInstance.getTours().get(0).distance(tspInstance) / matrixTspTour.distance(tspInstance);
                            }
                        }
                        alpha /= feasibles;
                        rawOutput.add(iteration + "\t" + feasibles + "\t" + A + "\t" + C + "\t" + D + "\t" + alpha);
                        System.out.println(rawOutput.toString());
                        if(feasibles > 5 && D > 0) {
                            Path rawFile = Paths.get("results/results.raw");
                            Files.write(rawFile, rawOutput, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
                        }
                        iteration++;
                        if(feasibles < 5)
                            toleratedSoFar++;
                        else
                            toleratedSoFar = 0;
                        if(toleratedSoFar > 5)
                            break;
                    }
                    if(toleratedSoFar > 5)
                        veryMuchToleratedSoFar++;
                    else
                        veryMuchToleratedSoFar = 0;
                    if(veryMuchToleratedSoFar > 5)
                        break;
                }
            }

//            MatrixTsp matrixTsp = new MatrixTsp(10000000, 10000000, 50000, 5000000, d, tspInstance.getDimension());
//
//            Tour matrixTspTour = new Tour();
//            matrixTspTour.fromArray(Main.shiftOrder(matrixTsp.run(false)));
//            System.out.println(matrixTspTour.isHamiltonianCycle(tspInstance) ? "**FEASIBLE" : "INFEASIBLE");
//            tspInstance.addTour(matrixTspTour);

//            System.out.println("COST: " + matrixTspTour.distance(tspInstance));
//            System.out.println("OPTIMAL COST: " + tspInstance.getTours().get(0).distance(tspInstance));

//            TSPPanel panel = new TSPPanel(tspInstance);
//            panel.displayTour(tspInstance.getTours().get(0), Color.RED);
//            panel.displayTour(tspInstance.getTours().get(1), Color.BLUE);

//            JFrame frame = new JFrame(tspInstance.getName());
//            frame.getContentPane().setLayout(new BorderLayout());
//            frame.getContentPane().add(panel, BorderLayout.CENTER);
//            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//            frame.setSize(500, 400);
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int[] shiftOrder(int[] array) {
        for(int i=0; i<array.length; i++)
            array[i]++;
        return array;
    }
}
