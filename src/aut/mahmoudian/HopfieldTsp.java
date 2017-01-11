package aut.mahmoudian;

import ch.qos.logback.core.helpers.ThrowableToStringArray;
import org.neuroph.core.Connection;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.input.InputFunction;
import org.neuroph.nnet.comp.neuron.InputOutputNeuron;
import org.neuroph.util.NeuronProperties;
import org.neuroph.util.TransferFunctionType;

import java.io.IOException;
import java.util.Random;

/**
 * Created by beleg on 12/18/16.
 */
public class HopfieldTsp {
    private static HopfieldTsp myInstance = new HopfieldTsp();
    double A, B, C, D;
    double[][] d;
    int n;
    HopfieldTspWrapper network;

    public static HopfieldTsp instance() {
        return myInstance;
    }

    private HopfieldTsp() {}

    public void run(double A, double B, double C, double D, double[][] d, int n) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.D = D;
        this.n = n;
        this.d = d;
        NeuronProperties neuronProperties = new NeuronProperties();
        neuronProperties.setProperty("neuronType", InputOutputNeuron.class);
        neuronProperties.setProperty("bias", new Double(C*n));
        neuronProperties.setProperty("transferFunction", TransferFunctionType.SIGMOID);
        neuronProperties.setProperty("transferFunction.yHigh", new Double(1));
        neuronProperties.setProperty("transferFunction.yLow", new Double(0));
        this.network = new HopfieldTspWrapper(n, neuronProperties);
        TspInput tspInput = new TspInput(network, n, d, A, B, C, D);
        for(Neuron neuron : network.getLayerAt(0).getNeurons())
            neuron.setInputFunction(tspInput);
        for(int y=0; y<n; y++)
            for(int j=0; j<n; j++)
                for(int x=0; x<n; x++)
                    for(int i=0; i<n; i++)
                        if(x != y || i != j)
                            network.getMatrixNeuronAt(y, j)
                                    .getConnectionFrom(network.getMatrixNeuronAt(x, i))
                                    .getWeight().setValue(
                                    -A * delta(x, y) * (1 - delta(i, j)) - B * delta(i, j) * (1 - delta(x, y)) - C - D * d[x][y] * (delta(j, i - 1) + delta(j, i + 1))
                            );
        Random randomGenerator = new Random();
        double[][] randomInput = new double[n][n];
        double inputSum = 0;
        for(int i=0; i<n; i++)
            for(int j=0; j<n; j++) {
                randomInput[i][j] = randomGenerator.nextDouble();
                inputSum += randomInput[i][j];
            }
        for(int i=0; i<n; i++)
            for(int j=0; j<n; j++) {
                network.getMatrixNeuronAt(i, j).setInput(randomInput[i][j] * n / inputSum);
//                network.getMatrixNeuronAt(i, j).setInput(randomInput[i][j]);
            }
        int iteration = 0;
        while(true) {
            network.calculate();
            System.out.printf("%d:\t", iteration++);
            for(int i=0; i<n*n; i++)
                System.out.printf("%.2f\t" + ((i%n==n-1)? ",\t" : ""), network.getLayerAt(0).getNeuronAt(i).getOutput());
//            for(int i=0; i<n; i++)
//                System.out.printf("%d\t", argMax(network.getMatrixColumn(i)));
            System.out.print("\t" + energy());
            System.out.print("\r");
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class TspInput extends InputFunction {
        HopfieldTspWrapper network;
        int n;
        double[][] d;
        double A, B, C, D;
        public TspInput(HopfieldTspWrapper network, int n, double[][] d, double A, double B, double C, double D) {
            this.network = network;
            this.n = n;
            this.d = d;
            this.A = A;
            this.B = B;
            this.C = C;
            this.D = D;
        }
        @Override
        public double getOutput(Connection[] connections) {
            double du = 0;
            int toIdx = network.getLayerAt(0).indexOf(connections[0].getToNeuron());
            int x = (int)Math.ceil(toIdx/n);
            int i = toIdx % n;
            for(Connection connection : connections) {
                int fromIdx = network.getLayerAt(0).indexOf(connection.getFromNeuron());
                int y = (int)Math.ceil(fromIdx/n);
                int j = fromIdx % n;
                if(y==x && j!=i)
                    du += -A*connection.getFromNeuron().getOutput();
                if(y!=x && j==i)
                    du += -B*connection.getFromNeuron().getOutput();
                if(y!=x || j!=i)
                    du += -C*(connection.getFromNeuron().getOutput() - n);
                if(y!=x && (j==i-1 || j==i+1))
                    du += -D*d[x][y]*connection.getFromNeuron().getOutput();
            }
            return connections[0].getToNeuron().getNetInput() + du;
        }
    }

    private double energy() {
        double energy = 0;
        for(int y=0; y<n; y++)
            for(int j=0; j<n; j++) {
                for (int x = 0; x < n; x++)
                    for (int i = 0; i < n; i++) {
                        if (x == y && i != j)
                            energy += A * network.getMatrixNeuronAt(x, i).getOutput() * network.getMatrixNeuronAt(y, j).getOutput() / 2;
                        if (x != y && i == j)
                            energy += B * network.getMatrixNeuronAt(x, i).getOutput() * network.getMatrixNeuronAt(y, j).getOutput() / 2;
                        if (y != x && (j == i - 1 || j == i + 1))
                            energy += D * d[x][y] * network.getMatrixNeuronAt(x, i).getOutput() * network.getMatrixNeuronAt(y, j).getOutput() / 2;
                    }
                energy += C * (network.getMatrixNeuronAt(y, j).getOutput() - n) * (network.getMatrixNeuronAt(y, j).getOutput() - n) / 2;
            }
        return energy;
    }

    public static int argMax(double...params) {
        double max = 0;
        int maxIdx = 0;
        for(int i=0; i<params.length; i++)
            if(params[i] > max) {
                max = params[i];
                maxIdx = i;
            }
        return maxIdx;
    }

    private static int delta(int x, int y) {
        return (x==y)? 1 : 0;
    }
}
