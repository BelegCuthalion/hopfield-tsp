package aut.mahmoudian;

import org.neuroph.core.Neuron;
import org.neuroph.nnet.Hopfield;
import org.neuroph.util.NeuronProperties;

/**
 * Created by beleg on 12/19/16.
 */
public class HopfieldTspWrapper extends Hopfield {

    private int tspTourSize;

    /**
     * Constructor, makes a matrix of Hopfield neurons.
     * @param tspTourSize the dimension of neurons matrix.
     * @param neuronProperties basic properties for neurons, like activation function, etc.
     */
    public HopfieldTspWrapper(int tspTourSize, NeuronProperties neuronProperties) {
        super(tspTourSize*tspTourSize, neuronProperties);
        this.tspTourSize = tspTourSize;
    }

    /**
     * Map from basic 1-layer topology to a matrix of neurons used to solve TSP.
     * @param city idx from 0
     * @param step idx from 0
     * @return Neuron at the [city, step] position.
     */
    public Neuron getMatrixNeuronAt(int city, int step) {
        return this.getLayerAt(0).getNeuronAt((city * this.tspTourSize) + step);
    }

    public double[] getMatrixColumn(int step) {
        double[] result = new double[this.tspTourSize];
        for(int i=0; i<this.tspTourSize; i++)
            result[i] = this.getLayerAt(0).getNeuronAt((i * this.tspTourSize) + step).getOutput();
        return result;
    }
}
