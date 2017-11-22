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
    	int sumInputs = 0;
    	for (Input input: inputTx) {
    		utxo = new UTXO(input.prevTxHash, input.outputIndex);
    		output = txHandlerPool.getTxOutput(utxo);
    		sumInputs += output.value;
    	}
    	
    	//Sum of outputs
    	int sumOutputs = 0;
    	for (Output op : outputTx) {
    		sumOutputs += op.value;
    	}
    	
    	return (sumInputs >= sumOutputs);

    }
    
    
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
    	// all outputs claimed by {@code tx} are in the current UTXO pool
    	boolean isValidTx = allOutputsCurrentUTXOpool(tx) && allInputsSignaturesValid(tx) &&
    			noUTXOMultipleTimes(tx) && nonNegativeOutputs(tx) && sumOfInputsGreaterThanSumOfOutputs(tx);
    	System.out.println(noUTXOMultipleTimes(tx));
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
