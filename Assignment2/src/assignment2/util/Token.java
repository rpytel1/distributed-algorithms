package assignment2.util;

import java.io.Serializable;

public class Token implements Serializable{
		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int[] TN; // the token array with the number of requests of each process
	private String[] TS; // the token array with the state of each process
	
	public Token(int n){
		this.TN = new int[n];
		this.TS = new String[n];
		for (int i=0; i<n; i++){
			this.TN[i] = 0;
			this.TS[i] = "O";
		}
	}
	
	public void setTN(int i, int c){
		this.TN[i] = c;
	}
	
	public void setTS(int i, String s){
		this.TS[i] = s;
	}
	
	public int getTN(int i){
		return this.TN[i];
	}
	
	public String getTS(int i){
		return this.TS[i];
	}
		
}
