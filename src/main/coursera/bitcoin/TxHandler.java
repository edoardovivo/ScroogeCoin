package main.coursera.bitcoin;

import java.security.PublicKey;
import java.util.ArrayList;

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
   		if (isSubset) {
   			return true;
   		}
   		else {
   			return false;
   		}
   		
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
    
    
    
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
    	// all outputs claimed by {@code tx} are in the current UTXO pool
    	
    	return true;
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
