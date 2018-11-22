package src.util;

import java.io.Serializable;

public class Token implements Serializable{
		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int[] TN;
	private String[] TS;
	
	public Token(int n){
		this.TN = new int[n];
		this.TS = new String[n];
		for (int i=0; i<n; i++){
			this.TN[i] = 0;
			if (i==0) this.TS[i] = "H";
			else this.TS[i] = "O";
		}
		this.TS = new String[n];
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
