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
    	System.out.println(allInputsSignaturesValid(tx));
    	return isValidTx;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // Check that each transaction is valid
    	ArrayList<Transaction> refinedPossibleTxs = new ArrayList<Transaction>();
    	for (Transaction tx : possibleTxs) {
    		if (isValidTx(tx)) {
    			refinedPossibleTxs.add(tx);
    		}
    	}
    	// Check that there are no double spends, i.e among all the 
    	// remaining transactions, there shouldn't be any two who share the same input
    	Transaction tx1, tx2;
    	ArrayList<Transaction> excludedTxs = new ArrayList<Transaction>();
    	ArrayList<Input> inputs1 = new ArrayList<Input>();
    	ArrayList<Input> inputs2 = new ArrayList<Input>();
    	for (int i=0; i < refinedPossibleTxs.size(); i++) {
    		tx1 = refinedPossibleTxs.get(i);
    		inputs1 = tx1.getInputs();
    		for (int j=i+1; j<refinedPossibleTxs.size(); j++) {
    			tx2 = refinedPossibleTxs.get(i);
    			inputs2 = tx2.getInputs();
    			//Check if any input in input1 is also in input2
    			inputs2.retainAll(inputs1);
    			// If so, exclude the second transaction
    			if (inputs2.size() > 0) {
    				excludedTxs.add(tx2);
    			}
    		}
    	}
    	
    	//Exclude the transactions
    	refinedPossibleTxs.removeAll(excludedTxs);
    	
    	return refinedPossibleTxs.toArray(new Transaction[refinedPossibleTxs.size()]);
    }

}
