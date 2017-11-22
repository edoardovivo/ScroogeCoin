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
    	//Outputs for the transaction to be validated
    	ArrayList<Output> outputTx = tx.getOutputs();
    	//All UTXOs in the pool
    	ArrayList<UTXO> allUTXO = txHandlerPool.getAllUTXO();
    	//Build a list of all outputs in the pool
    	ArrayList<Output> allUTXOOutput = new ArrayList<Output>();
   		for (UTXO utxo : allUTXO ) {
   				allUTXOOutput.add(txHandlerPool.getTxOutput(utxo));
    	}
   		// If the list of outputs of the transaction is a subset 
   		//of the list of all outputs in the pool, then returns true
   		boolean isSubset = allUTXOOutput.containsAll(outputTx);
   		return isSubset;
   		
    }
    
    public boolean allInputsSignaturesValid(Transaction tx) {
    	//Inputs for the transaction to be validated
    	ArrayList<Input> inputTx = tx.getInputs();
    	//Outputs for the transaction to be validated
    	ArrayList<Output> outputTx = tx.getOutputs();
    	
    	byte[] rawData;
    	PublicKey publicKey;
    	for (int i=0; i < tx.numInputs();i++) {
    		rawData = tx.getRawDataToSign(i);
    		publicKey = outputTx.get(i).address;
    		if (!Crypto.verifySignature(publicKey, rawData, inputTx.get(i).signature)) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public boolean noUTXOMultipleTimes(Transaction tx) {
    	//Inputs for the transaction to be validated
    	ArrayList<Input> inputTx = tx.getInputs();
    	
    	//Simply check if there are duplicated inputs
    	Set<Input> setInputTx = new HashSet<Input>(inputTx);

    	if(setInputTx.size() < inputTx.size()){
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
    	
    	//Hash all outputs in the pool
    	HashMap<UTXO, Output> txHandlerPoolH = txHandlerPool.getH();
    	HashMap<UTXO, byte[]> poolValuesAndHash = new HashMap<UTXO, byte[]>();

    	for(Map.Entry<UTXO, Output> entry : txHandlerPoolH.entrySet()) {
    	    byte[] outputHash = entry.getKey().getTxHash();
    		poolValuesAndHash.put(entry.getKey(), outputHash);
    	}

    	//Get the hashes of the previous transaction for each input
    	ArrayList<byte[]> allInputsprevHash = new ArrayList<byte[]>();
    	for (Input input : inputTx) {
    		allInputsprevHash.add(input.prevTxHash);
    	}
    	
    	//Find which transactions in the pool are used as inputs
    	HashMap<UTXO, byte[]> poolValuesAndHashUsedInTx = new HashMap<UTXO, byte[]>();
    	for(Map.Entry<UTXO, byte[]> entry : poolValuesAndHash.entrySet()) {
    		for (byte[] inputHash : allInputsprevHash) {
    			if (inputHash == entry.getValue()) {
    				poolValuesAndHashUsedInTx.put(entry.getKey(), 
    						entry.getValue());
    			}
    		}
    	}
    	//Sum the outputs of those transactions, which are Inputs to the current tx
    	double sumInputs = 0.0;
    	for(Map.Entry<UTXO, byte[]> entry : poolValuesAndHashUsedInTx.entrySet()) {
    		UTXO utxo = entry.getKey();
    		Output output = txHandlerPool.getTxOutput(utxo);
    		sumInputs += output.value;
    	} 
    	
    	//Sum the output of tx
    	double sumOutputs = 0.0;
    	for (Output output : outputTx) {
    		sumOutputs += output.value;
    	}
    	
    	//Compare the two
    	if (sumInputs < sumOutputs) {
    		return false;
    	}
    	else {
    		return true;
    	}
    	
    }
    
    
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
    	// all outputs claimed by {@code tx} are in the current UTXO pool
    	boolean isValidTx = allOutputsCurrentUTXOpool(tx) && allInputsSignaturesValid(tx) &&
    			noUTXOMultipleTimes(tx) && nonNegativeOutputs(tx) && sumOfInputsGreaterThanSumOfOutputs(tx);
    	return isValidTx;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
    	return possibleTxs;
    }

}
