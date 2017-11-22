package main.coursera.bitcoin;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import main.coursera.bitcoin.Transaction.Input;
import main.coursera.bitcoin.Transaction.Output;
import main.coursera.bitcoin.Crypto;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
	UTXOPool txHandlerPool;
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
    	txHandlerPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean allOutputsCurrentUTXOpool(Transaction tx) {

    	//Inputs for the transaction to be validated
    	ArrayList<Input> inputTx = tx.getInputs();

    	UTXO utxo;
    	for (Input input: inputTx) {
    		utxo = new UTXO(input.prevTxHash, input.outputIndex);
	    	if (!txHandlerPool.contains(utxo)) return false;
    	}

    	return true;
   		
    }
    
    public boolean allInputsSignaturesValid(Transaction tx) {
    	//Inputs for the transaction to be validated
    	ArrayList<Input> inputTx = tx.getInputs();
    	//Outputs for the transaction to be validated
    	ArrayList<Output> outputTx = tx.getOutputs();
    	
    	
    	byte[] rawData;
    	PublicKey publicKey;
    	UTXO utxo;
    	Output output;
    	for (int i=0; i < tx.numInputs();i++) {
    		rawData = tx.getRawDataToSign(i);
    		utxo = new UTXO(inputTx.get(i).prevTxHash, inputTx.get(i).outputIndex);
    		output = txHandlerPool.getTxOutput(utxo);
    		publicKey = output.address;
    		if (!Crypto.verifySignature(publicKey, rawData, inputTx.get(i).signature)) {
    			return false;
    		}
    	}
    	
    	return true;
    }
    
    public boolean noUTXOMultipleTimes(Transaction tx) {
    	//Inputs for the transaction to be validated
    	ArrayList<Input> inputTx = tx.getInputs();
    	
    	UTXO utxo;
    	Output output;
    	ArrayList<Output> utxoOutputsClaimed = new ArrayList<Output>();
    	for (Input input: inputTx) {
    		utxo = new UTXO(input.prevTxHash, input.outputIndex);
    		output = txHandlerPool.getTxOutput(utxo);
    		utxoOutputsClaimed.add(output);
    	}
    	
    	
    	//Simply check if there are duplicated inputs
    	Set<Output> setUtxoOutputsClaimed = new HashSet<Output>(utxoOutputsClaimed);
    	
    	//System.out.println("Set size: " + setUtxoOutputsClaimed.size());
    	//System.out.println("List size: " + utxoOutputsClaimed.size());
    	
    	if(setUtxoOutputsClaimed.size() < utxoOutputsClaimed.size()){
    	    // There are duplicates
    		return false;
    	}
    	else {
    		return true;
    	}
    	
    }
    
    public boolean nonNegativeOutputs(Transaction tx) {
    	//Outputs for the transaction to be validated
    	ArrayList<Output> outputTx = tx.getOutputs();
    	for (Output output : outputTx) {
    		if (output.value < 0) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public boolean sumOfInputsGreaterThanSumOfOutputs(Transaction tx) {
    	//Inputs for the transaction to be validated
    	ArrayList<Input> inputTx = tx.getInputs();
    	//Outputs for the transaction to be validated
    	ArrayList<Output> outputTx = tx.getOutputs();
    	
    	//Sum of inputs
    	UTXO utxo;
    	Output output;
    	double sumInputs = 0.0;
    	for (Input input: inputTx) {
    		utxo = new UTXO(input.prevTxHash, input.outputIndex);
    		output = txHandlerPool.getTxOutput(utxo);
    		sumInputs += output.value;
    	}
    	
    	//Sum of outputs
    	double sumOutputs = 0;
    	for (Output op : outputTx) {
    		sumOutputs += op.value;
    	}
    	
    	//System.out.println("SumInputs: " + sumInputs);
    	//System.out.println("SumOutputs: " + sumOutputs);
    	
    	return (sumInputs >= sumOutputs);

    }
    
    
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
    	// all outputs claimed by {@code tx} are in the current UTXO pool
    	boolean isValidTx = allOutputsCurrentUTXOpool(tx) && allInputsSignaturesValid(tx) &&
    			noUTXOMultipleTimes(tx) && nonNegativeOutputs(tx) && sumOfInputsGreaterThanSumOfOutputs(tx);
    	//System.out.println(allOutputsCurrentUTXOpool(tx));
    	return isValidTx;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // Check that each transaction is valid
    	ArrayList<Transaction> refinedPossibleTxs = new ArrayList<>();
    	HashMap<UTXO, Output> utxoOutputsClaimed = new HashMap<UTXO, Output>();
    	for (Transaction tx : possibleTxs) {
    		if (isValidTx(tx)) {
    			//For each valid transaction, updates pool
    			refinedPossibleTxs.add(tx);
    			for (Transaction.Input in : tx.getInputs()) {
                    UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                    txHandlerPool.removeUTXO(utxo);
                }
                for (int i = 0; i < tx.numOutputs(); i++) {
                    Transaction.Output out = tx.getOutput(i);
                    UTXO utxo = new UTXO(tx.getHash(), i);
                    txHandlerPool.addUTXO(utxo, out);
                }
    		}
    	}
    	   	

    	return eturn refinedPossibleTxs.toArray(new Transaction[refinedPossibleTxs.size()]);
    }

}
